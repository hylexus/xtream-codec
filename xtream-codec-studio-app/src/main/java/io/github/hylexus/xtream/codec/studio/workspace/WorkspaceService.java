/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.codec.studio.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.github.hylexus.xtream.codec.studio.workspace.WorkspaceData.*;

/**
 * 工作区的目录规则、乐观保存与备份交换。存储实现只承担原子提交。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class WorkspaceService implements AutoCloseable {
    private final WorkspaceStore store;

    public WorkspaceService(WorkspaceStore store) throws IOException {
        this.store = Objects.requireNonNull(store);
        validate(store.read().entries());
    }

    public Snapshot read() throws IOException {
        return store.read();
    }

    public Entry get(String id) throws IOException {
        return require(read(), id);
    }

    public synchronized Entry create(Edit edit) throws IOException {
        final Snapshot before = read();
        final long now = System.currentTimeMillis();
        final Entry entry = new Entry(UUID.randomUUID().toString(), edit.parentId(), edit.kind(),
                edit.name(), 1, now, now, edit.document());
        final var entries = new ArrayList<>(before.entries());
        entries.add(entry);
        validate(entries);
        store.commit(before.revision(), entries);
        return entry;
    }

    public synchronized Entry update(String id, Edit edit) throws IOException {
        final Snapshot before = read();
        final Entry old = require(before, id);
        checkRevision(old, edit.expectedRevision());
        if (!old.kind().equals(edit.kind())) {
            throw new IllegalArgumentException("不能改变目录或文档类型");
        }
        final Entry entry = new Entry(id, edit.parentId(), edit.kind(), edit.name(), old.revision() + 1,
                old.createdAt(), System.currentTimeMillis(), edit.document());
        final List<Entry> entries = before.entries().stream().map(item -> item.id().equals(id) ? entry : item).toList();
        validate(entries);
        store.commit(before.revision(), entries);
        return entry;
    }

    public synchronized Snapshot delete(String id, long expectedRevision, long workspaceRevision) throws IOException {
        final Snapshot before = read();
        checkRevision(require(before, id), expectedRevision);
        if (before.revision() != workspaceRevision) {
            throw new WorkspaceException(409, "目录内容已变化，请刷新后重新确认删除");
        }
        final var removed = new HashSet<String>();
        removed.add(id);
        boolean changed;
        do {
            changed = false;
            for (final Entry entry : before.entries()) {
                if (removed.contains(entry.parentId()) && removed.add(entry.id())) {
                    changed = true;
                }
            }
        } while (changed);
        return store.commit(before.revision(), before.entries().stream().filter(entry -> !removed.contains(entry.id())).toList());
    }

    public Backup export() throws IOException {
        return new Backup("studio-workspace", 1, read().entries());
    }

    public synchronized Snapshot importBackup(Backup backup) throws IOException {
        if (!"studio-workspace".equals(backup.format()) || backup.formatVersion() != 1) {
            throw new IllegalArgumentException("不支持的工作区备份格式");
        }
        validate(backup.entries());
        final Snapshot before = read();
        final var entries = new ArrayList<>(before.entries());
        final String folderId = UUID.randomUUID().toString();
        final long now = System.currentTimeMillis();
        entries.add(new Entry(folderId, null, "folder", "导入-" + folderId.substring(0, 8), 1, now, now, null));
        final var ids = new HashMap<String, String>();
        backup.entries().forEach(entry -> ids.put(entry.id(), UUID.randomUUID().toString()));
        for (final Entry entry : backup.entries()) {
            entries.add(new Entry(ids.get(entry.id()), entry.parentId() == null ? folderId : ids.get(entry.parentId()),
                    entry.kind(), entry.name(), 1, now, now, entry.document()));
        }
        validate(entries);
        return store.commit(before.revision(), entries);
    }

    private static Entry require(Snapshot snapshot, String id) {
        return snapshot.entries().stream().filter(entry -> entry.id().equals(id)).findFirst()
                .orElseThrow(() -> new WorkspaceException(404, "文档或目录不存在"));
    }

    private static void checkRevision(Entry entry, long expected) {
        if (entry.revision() != expected) {
            throw new WorkspaceException(409, "文档已被修改，请重新打开或另存副本");
        }
    }

    static void validate(List<Entry> entries) {
        if (entries.size() > 10000) {
            throw new IllegalArgumentException("工作区条目超过 10000 个");
        }
        final Map<String, Entry> byId = new HashMap<>();
        final var names = new HashSet<List<String>>();
        for (final Entry entry : entries) {
            if (entry == null || entry.id() == null || !UUID.fromString(entry.id()).toString().equals(entry.id())) {
                throw new IllegalArgumentException("条目标识必须为规范的小写 UUID");
            }
            if (byId.put(entry.id(), entry) != null || entry.revision() < 1) {
                throw new IllegalArgumentException("无效或重复的条目标识和版本");
            }
            if (entry.name() == null || entry.name().isBlank() || entry.name().length() > 120
                    || !entry.name().equals(entry.name().trim()) || entry.name().chars().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("名称必须为 1–120 个字符，不能包含控制字符或首尾空白");
            }
            if (!names.add(List.of(entry.parentId() == null ? "" : entry.parentId(), entry.name()))) {
                throw new IllegalArgumentException("同一目录内名称不能重复");
            }
            if (!("folder".equals(entry.kind()) && entry.document() == null)
                    && !("document".equals(entry.kind()) && entry.document() != null)) {
                throw new IllegalArgumentException("无效的目录或文档内容");
            }
        }
        for (final Entry entry : entries) {
            final var visited = new HashSet<String>();
            visited.add(entry.id());
            String parent = entry.parentId();
            while (parent != null) {
                if (!visited.add(parent)) {
                    throw new IllegalArgumentException("目录不能移动到自身或后代");
                }
                final Entry ancestor = byId.get(parent);
                if (ancestor == null || !"folder".equals(ancestor.kind())) {
                    throw new IllegalArgumentException("父目录不存在或不是文件夹");
                }
                if (visited.size() > 65) {
                    throw new IllegalArgumentException("目录深度超过 64 层");
                }
                parent = ancestor.parentId();
            }
        }
    }

    @Override
    public void close() throws IOException {
        store.close();
    }
}

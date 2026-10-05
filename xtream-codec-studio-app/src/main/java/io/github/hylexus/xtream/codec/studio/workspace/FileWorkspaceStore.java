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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static io.github.hylexus.xtream.codec.studio.workspace.WorkspaceData.*;

/**
 * 独立条目 JSON 存储。先写版本文件，再原子发布 manifest；读者只能看到完整快照。
 * 进程持有目录锁，避免另一个文件存储实例破坏版本比较。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class FileWorkspaceStore implements WorkspaceStore {
    private final Path directory;
    private final Path records;
    private final ObjectMapper mapper = new ObjectMapper();
    private final FileChannel lockChannel;
    private final FileLock lock;

    public FileWorkspaceStore(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath();
        this.records = this.directory.resolve("records");
        Files.createDirectories(records);
        lockChannel = FileChannel.open(this.directory.resolve("workspace.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            lock = lockChannel.tryLock();
            if (lock == null) {
                throw new IOException("工作区目录已被其他进程使用");
            }
        } catch (IOException | RuntimeException exception) {
            lockChannel.close();
            throw exception;
        }
        try {
            // 确认 manifest 指向的完整内容可读取后，才清理未引用记录。
            read();
            cleanup(manifest());
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    @Override
    public synchronized Snapshot read() throws IOException {
        final Manifest manifest = manifest();
        final var entries = new ArrayList<Entry>();
        for (final String name : manifest.records()) {
            if (!name.matches("[0-9a-f-]{36}\\.[0-9]+\\.json")) {
                throw new IOException("无效的工作区记录路径");
            }
            entries.add(mapper.readValue(records.resolve(name).toFile(), Entry.class));
        }
        return new Snapshot(manifest.revision(), entries);
    }

    @Override
    public synchronized Snapshot commit(long expectedRevision, List<Entry> entries) throws IOException {
        final Manifest before = manifest();
        if (before.revision() != expectedRevision) {
            throw new WorkspaceException(409, "工作区已变化，请刷新后重试");
        }
        final var names = new ArrayList<String>();
        final var existing = new HashSet<>(before.records());
        for (final Entry entry : entries) {
            final String name = entry.id() + "." + entry.revision() + ".json";
            if (!existing.contains(name)) {
                atomicWrite(records.resolve(name), mapper.writeValueAsBytes(entry));
            }
            names.add(name);
        }
        final Manifest next = new Manifest(1, expectedRevision + 1, names);
        atomicWrite(directory.resolve("manifest.json"), mapper.writeValueAsBytes(next));
        // 提交已发布；清理失败不应向调用方报告保存失败，下次启动仍会回收旧记录。
        try {
            cleanup(next);
        } catch (IOException ignored) {
            // 不影响 manifest 指向的有效记录。
        }
        return new Snapshot(next.revision(), entries);
    }

    private Manifest manifest() throws IOException {
        final Path path = directory.resolve("manifest.json");
        if (!Files.exists(path)) {
            return new Manifest(1, 0, List.of());
        }
        final Manifest manifest = mapper.readValue(path.toFile(), Manifest.class);
        if (manifest.formatVersion() != 1 || manifest.records().size() > 10000) {
            throw new IOException("不支持或损坏的文件工作区");
        }
        return manifest;
    }

    private static void atomicWrite(Path path, byte[] bytes) throws IOException {
        final Path temporary = Files.createTempFile(path.getParent(), "record-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                final ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void cleanup(Manifest manifest) throws IOException {
        final var retained = new HashSet<>(manifest.records());
        try (var files = Files.list(records)) {
            for (final Path path : files.toList()) {
                final String name = path.getFileName().toString();
                if (!retained.contains(name) && (name.matches("[0-9a-f-]{36}\\.[0-9]+\\.json")
                        || name.startsWith("record-") && name.endsWith(".tmp"))) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            lockChannel.close();
        }
    }

    private record Manifest(int formatVersion, long revision, List<String> records) {
    }
}

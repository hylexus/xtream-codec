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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * 与 HTTP 和存储介质无关的工作区数据合同。
 * 文档保留编辑原文，执行时才验证 DSL 和数值；运行快照只保存最近一次结果。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class WorkspaceData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WorkspaceData() {
    }

    public record Document(int formatVersion, String mode, Map<String, Object> draft,
                           String schemaText, String schemaTab, @Nullable Map<String, Object> run) {
        public Document {
            if (formatVersion != 1 || !List.of("dynamic", "entity", "field").contains(mode)
                    || !List.of("visual", "json").contains(schemaTab)) {
                throw new IllegalArgumentException("不支持的文档格式或模式");
            }
            Objects.requireNonNull(schemaText, "缺少结构编辑原文");
            draft = Map.copyOf(draft);
            for (final String key : List.of("values", "hex", "version", "entityId")) {
                if (!(draft.get(key) instanceof String)) {
                    throw new IllegalArgumentException("无效的文档输入: " + key);
                }
            }
            if (!(draft.get("schema") instanceof Map<?, ?> schema)
                    || !Integer.valueOf(1).equals(schema.get("schemaVersion"))
                    || !(schema.get("fields") instanceof List<?>)) {
                throw new IllegalArgumentException("无效的文档结构");
            }
            // 校验已经应用的结构格式；schemaText 和 values 是允许未完成的编辑原文。
            try {
                DynamicSchemaJson.read(MAPPER.writeValueAsString(schema));
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("无效的文档结构", exception);
            }
            if (run != null) {
                if (!(run.get("direction") instanceof String direction) || !List.of("encode", "decode").contains(direction)
                        || !(run.get("signature") instanceof String) || !(run.get("executedAt") instanceof Number)
                        || !(run.get("result") instanceof Map<?, ?> result) || !(result.get("success") instanceof Boolean)
                        || !(result.get("hex") instanceof String) || !(result.get("remainingHex") instanceof String)
                        || !(result.get("consumedBits") instanceof Number) || !(result.get("consumedBytes") instanceof Number)
                        || !(result.get("checksums") instanceof List<?>) || !(result.get("trace") instanceof Map<?, ?> trace)
                        || !(trace.get("nodes") instanceof List<?>) || !(trace.get("root") instanceof Map<?, ?>)
                        || !(trace.get("nodeIdsByByteOffset") instanceof Map<?, ?>)) {
                    throw new IllegalArgumentException("无效的运行快照");
                }
                validateTrace(trace);
                run = Map.copyOf(run);
            }
        }
    }

    private static void validateTrace(Map<?, ?> trace) {
        final var nodes = new ArrayList<Object>();
        nodes.add(trace.get("root"));
        nodes.addAll((List<?>) trace.get("nodes"));
        // 备份的跟踪树也要验证，避免格式损坏的快照在页面打开时中断渲染。
        for (int index = 0; index < nodes.size(); index++) {
            if (index > 100000 || !(nodes.get(index) instanceof Map<?, ?> node)
                    || !(node.get("id") instanceof String) || !(node.get("name") instanceof String)
                    || !(node.get("status") instanceof String) || !(node.get("children") instanceof List<?> children)
                    || !(node.get("diagnostics") instanceof List<?>) || !(node.get("attributes") instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("无效的字段跟踪快照");
            }
            nodes.addAll(children);
        }
    }

    public record Entry(String id, @Nullable String parentId, String kind, String name,
                        long revision, long updatedAt, @Nullable Document document) {
    }

    public record Snapshot(long revision, List<Entry> entries) {
        public Snapshot {
            entries = List.copyOf(entries);
        }
    }

    public record Edit(@Nullable String parentId, String kind, String name,
                       long expectedRevision, @Nullable Document document) {
    }

    public record Backup(String format, int formatVersion, List<Entry> entries) {
        public Backup {
            entries = List.copyOf(entries);
        }
    }
}

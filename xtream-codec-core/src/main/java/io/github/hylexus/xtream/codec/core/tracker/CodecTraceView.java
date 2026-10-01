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

package io.github.hylexus.xtream.codec.core.tracker;

import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 面向 Web 调试页面的编解码跟踪视图。
 * 节点保留字节范围以兼容已有消费者，同时可选携带精确位范围；
 * 字节索引用于从原始 HEX 定位覆盖该字节的所有字段。
 *
 * @author hylexus
 * @author Codex (AI)
 * @since 0.9.0
 */
public record CodecTraceView(
        CodecTraceDirection direction,
        @Nullable String entityClass,
        @Nullable String payloadHex,
        Node root,
        List<Node> nodes,
        Map<Integer, List<String>> nodeIdsByByteOffset,
        List<CodecTraceDiagnostic> diagnostics
) {
    public static CodecTraceView from(CodecTrace trace) {
        final List<Node> nodes = new ArrayList<>();
        final Map<Integer, List<String>> nodeIdsByByteOffset = new LinkedHashMap<>();
        final Node root = toViewNode(trace.getRoot(), trace.getPayloadHex(), nodes, nodeIdsByByteOffset);
        return new CodecTraceView(
                trace.getDirection(),
                trace.getEntityClass(),
                trace.getPayloadHex(),
                root,
                List.copyOf(nodes),
                copyIndex(nodeIdsByByteOffset),
                List.copyOf(trace.getDiagnostics())
        );
    }

    private static Node toViewNode(CodecTraceNode source, @Nullable String payloadHex, List<Node> nodes, Map<Integer, List<String>> nodeIdsByByteOffset) {
        // 同时生成树形视图和扁平索引，页面无需再次遍历整棵树查找节点。
        final List<Node> children = new ArrayList<>();
        final Node node = new Node(
                source.getId(),
                source.getParentId(),
                source.getKind(),
                source.getName(),
                source.getPath(),
                source.getJavaType(),
                source.getProcessorType(),
                CodecTraceValueRenderer.toJsonValue(source.getValue()),
                source.getValueSummary(),
                source.getByteStart(),
                source.getByteEnd(),
                resolveHex(source, payloadHex),
                source.getStatus(),
                Map.copyOf(source.getAttributes()),
                List.copyOf(source.getDiagnostics()),
                children,
                source.getBitStart(),
                source.getBitEnd()
        );
        nodes.add(node);
        indexByteRange(source, nodeIdsByByteOffset);
        for (final CodecTraceNode child : source.getChildren()) {
            children.add(toViewNode(child, payloadHex, nodes, nodeIdsByByteOffset));
        }
        return node;
    }

    private static @Nullable String resolveHex(CodecTraceNode source, @Nullable String payloadHex) {
        // 节点未显式保存 HEX 时，按其字节范围从完整报文切片。
        if (source.getHex() != null || payloadHex == null) {
            return source.getHex();
        }
        final Integer byteStart = source.getByteStart();
        final Integer byteEnd = source.getByteEnd();
        final int start = byteStart == null ? -1 : byteStart * 2;
        final int end = byteEnd == null ? -1 : byteEnd * 2;
        if (start < 0 || end < start || end > payloadHex.length()) {
            return null;
        }
        return payloadHex.substring(start, end);
    }

    private static void indexByteRange(CodecTraceNode source, Map<Integer, List<String>> nodeIdsByByteOffset) {
        // 共享字节可以关联多个位字段，因此索引值是节点 ID 列表。
        final Integer byteStart = source.getByteStart();
        final Integer byteEnd = source.getByteEnd();
        if (byteStart == null || byteEnd == null || byteStart < 0 || byteEnd <= byteStart) {
            return;
        }
        for (int i = byteStart; i < byteEnd; i++) {
            nodeIdsByByteOffset.computeIfAbsent(i, ignored -> new ArrayList<>()).add(source.getId());
        }
    }

    private static Map<Integer, List<String>> copyIndex(Map<Integer, List<String>> source) {
        final Map<Integer, List<String>> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, List.copyOf(value)));
        return result;
    }

    public record Node(
            String id,
            @Nullable String parentId,
            CodecTraceNodeKind kind,
            String name,
            @Nullable String path,
            @Nullable String javaType,
            @Nullable String processorType,
            @Nullable Object value,
            @Nullable String valueSummary,
            @Nullable Integer byteStart,
            @Nullable Integer byteEnd,
            @Nullable String hex,
            CodecTraceStatus status,
            Map<String, Object> attributes,
            List<CodecTraceDiagnostic> diagnostics,
            List<Node> children,
            @Nullable Long bitStart,
            @Nullable Long bitEnd
    ) {
        public Node(String id, @Nullable String parentId, CodecTraceNodeKind kind, String name,
                    @Nullable String path, @Nullable String javaType, @Nullable String processorType,
                    @Nullable Object value, @Nullable String valueSummary, @Nullable Integer byteStart,
                    @Nullable Integer byteEnd, @Nullable String hex, CodecTraceStatus status,
                    Map<String, Object> attributes, List<CodecTraceDiagnostic> diagnostics, List<Node> children) {
            this(id, parentId, kind, name, path, javaType, processorType, value, valueSummary,
                    byteStart, byteEnd, hex, status, attributes, diagnostics, children, null, null);
        }
    }
}

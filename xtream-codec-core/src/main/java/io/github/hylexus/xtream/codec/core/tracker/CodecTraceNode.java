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
 * 结构化编解码跟踪节点。
 *
 * @author hylexus
 * @author Codex (AI)
 * @since 0.9.0
 */
public class CodecTraceNode {
    private final String id;
    private final @Nullable String parentId;
    private final CodecTraceNodeKind kind;
    private final String name;
    private final @Nullable String path;
    private @Nullable String javaType;
    private @Nullable String processorType;
    private @Nullable Object value;
    private @Nullable String valueSummary;
    private @Nullable Integer byteStart;
    private @Nullable Integer byteEnd;
    // 位范围可选，旧的实体编解码路径仍只提供字节范围。
    private @Nullable Long bitStart;
    private @Nullable Long bitEnd;
    private @Nullable String hex;
    private CodecTraceStatus status = CodecTraceStatus.STARTED;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final List<CodecTraceDiagnostic> diagnostics = new ArrayList<>();
    private final List<CodecTraceNode> children = new ArrayList<>();

    public CodecTraceNode(CodecTraceNodeKind kind, String name, @Nullable CodecTraceNode parent) {
        this.id = UUID.randomUUID().toString();
        this.parentId = parent == null ? null : parent.getId();
        this.kind = kind;
        this.name = name;
        this.path = resolvePath(parent, kind, name);
    }

    public String getId() {
        return id;
    }

    public @Nullable String getParentId() {
        return parentId;
    }

    public CodecTraceNodeKind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getPath() {
        return path;
    }

    public @Nullable String getJavaType() {
        return javaType;
    }

    public CodecTraceNode setJavaType(@Nullable String javaType) {
        this.javaType = javaType;
        return this;
    }

    public @Nullable String getProcessorType() {
        return processorType;
    }

    public CodecTraceNode setProcessorType(@Nullable String processorType) {
        this.processorType = processorType;
        return this;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public CodecTraceNode setValue(@Nullable Object value) {
        this.value = value;
        this.valueSummary = CodecTraceValueRenderer.toSummary(value);
        return this;
    }

    public @Nullable String getValueSummary() {
        return valueSummary;
    }

    public @Nullable Integer getByteStart() {
        return byteStart;
    }

    public @Nullable Integer getByteEnd() {
        return byteEnd;
    }

    public CodecTraceNode setByteRange(@Nullable Integer byteStart, @Nullable Integer byteEnd) {
        this.byteStart = byteStart;
        this.byteEnd = byteEnd;
        return this;
    }

    public @Nullable String getHex() {
        return hex;
    }

    /**
     * 返回相对于本次编解码起点的位范围起点。
     *
     * @since 0.11.0
     */
    public @Nullable Long getBitStart() {
        return bitStart;
    }

    public @Nullable Long getBitEnd() {
        return bitEnd;
    }

    public CodecTraceNode setBitRange(@Nullable Long bitStart, @Nullable Long bitEnd) {
        // 范围采用左闭右开约定，与字节范围一致，便于高亮重叠字段。
        this.bitStart = bitStart;
        this.bitEnd = bitEnd;
        return this;
    }

    public CodecTraceNode setHex(@Nullable String hex) {
        this.hex = hex;
        return this;
    }

    public CodecTraceStatus getStatus() {
        return status;
    }

    public CodecTraceNode setStatus(CodecTraceStatus status) {
        this.status = status;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public CodecTraceNode putAttribute(String key, @Nullable Object value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
        return this;
    }

    public List<CodecTraceDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public CodecTraceNode addDiagnostic(CodecTraceDiagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
        return this;
    }

    public List<CodecTraceNode> getChildren() {
        return children;
    }

    public CodecTraceNode addChild(CodecTraceNode child) {
        this.children.add(child);
        return this;
    }

    private static @Nullable String resolvePath(@Nullable CodecTraceNode parent, CodecTraceNodeKind kind, String name) {
        if (parent == null || kind == CodecTraceNodeKind.ROOT) {
            return "";
        }
        final String parentPath = parent.getPath();
        if (parentPath == null || parentPath.isEmpty()) {
            return name;
        }
        if (kind == CodecTraceNodeKind.COLLECTION_ITEM || kind == CodecTraceNodeKind.MAP_ENTRY || kind == CodecTraceNodeKind.MAP_ENTRY_ITEM) {
            return parentPath + name;
        }
        return parentPath + "." + name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CodecTraceNode.class.getSimpleName() + "[", "]")
                .add("kind=" + kind)
                .add("name='" + name + "'")
                .add("path='" + path + "'")
                .add("javaType='" + javaType + "'")
                .add("processorType='" + processorType + "'")
                .add("value=" + valueSummary)
                .add("byteRange=" + byteStart + "-" + byteEnd)
                .add("hex='" + hex + "'")
                .add("status=" + status)
                .toString();
    }
}

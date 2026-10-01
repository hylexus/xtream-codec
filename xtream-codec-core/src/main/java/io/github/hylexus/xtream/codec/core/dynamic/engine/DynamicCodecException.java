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

package io.github.hylexus.xtream.codec.core.dynamic.engine;


import org.jspecify.annotations.Nullable;

/**
 * 带定义路径和执行位置的动态编解码错误。路径从 {@code $} 开始，
 * 便于编辑器定位到具体字段；位偏移相对于本次调用处理的报文起点。
 * 编译期尚无报文位置，因此使用负数位偏移。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class DynamicCodecException extends IllegalArgumentException {
    private final String fieldPath;
    private final long bitOffset;

    public DynamicCodecException(String fieldPath, long bitOffset, String reason) {
        this(fieldPath, bitOffset, reason, null);
    }

    public DynamicCodecException(
            String fieldPath, long bitOffset, String reason,
            @Nullable Throwable cause) {
        super(fieldPath + (bitOffset < 0 ? "" : " @ bit " + bitOffset) + ": " + reason, cause);
        this.fieldPath = fieldPath;
        this.bitOffset = bitOffset;
    }

    public String fieldPath() {
        return fieldPath;
    }

    public long bitOffset() {
        return bitOffset;
    }
}

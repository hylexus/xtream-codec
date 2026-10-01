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

/**
 * 编解码跟踪诊断信息。原有 byteOffset 保留给现有消费者；
 * 新增的 bitOffset 可精确定位共享字节中的失败位置，旧调用方可以保持为空。
 *
 * @author hylexus
 * @author Codex (AI)
 * @since 0.9.0
 */
public record CodecTraceDiagnostic(
        String level,
        String message,
        @Nullable String nodeId,
        @Nullable Integer byteOffset,
        @Nullable String exceptionType,
        @Nullable Long bitOffset
) {
    public CodecTraceDiagnostic(String level, String message, @Nullable String nodeId,
                                @Nullable Integer byteOffset, @Nullable String exceptionType) {
        this(level, message, nodeId, byteOffset, exceptionType, null);
    }
}

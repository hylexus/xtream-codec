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

package io.github.hylexus.xtream.codec.core.debugger;

import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicChecksumResult;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceView;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 编解码结果与失败后的部分 Trace，HEX 始终为原始数据。
 * 即使执行失败，仍返回已消费位置、已读取字节和已完成的跟踪节点，
 * 便于页面定位字段边界；动态模式错误额外提供字段路径与 bit 偏移。
 *
 * @param success       本次操作是否完成
 * @param values        完整字段映射；嵌套对象和集合使用 Map/List，失败时可能为空
 * @param hex           原始输入或输出的十六进制报文
 * @param consumedBits  实际处理的位数
 * @param consumedBytes 向上取整后的处理字节数
 * @param remainingHex  解码后未消费的原始字节
 * @param trace         完整或部分执行跟踪
 * @param error         失败时的错误摘要
 * @param checksums     动态解码的校验和核对结果，不匹配不影响默认模式的 success
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DebugCodecResponse(
        boolean success,
        @Nullable Map<String, Object> values,
        String hex,
        long consumedBits,
        int consumedBytes,
        String remainingHex,
        CodecTraceView trace,
        @Nullable Error error,
        List<DynamicChecksumResult> checksums) {

    public DebugCodecResponse {
        checksums = List.copyOf(checksums);
    }

    /**
     * 兼容未提供校验结果的原有响应构造入口。
     */
    public DebugCodecResponse(
            boolean success, @Nullable Map<String, Object> values, String hex,
            long consumedBits, int consumedBytes, String remainingHex,
            CodecTraceView trace, @Nullable Error error) {
        this(success, values, hex, consumedBits, consumedBytes, remainingHex, trace, error, List.of());
    }

    /**
     * 页面可直接展示的错误摘要；动态编解码错误附带结构字段路径和位偏移。
     *
     * @param message   可读错误说明
     * @param fieldPath 动态字段路径，其他错误可能为空
     * @param bitOffset 相对于本次报文起点的位偏移
     */
    public record Error(String message, @Nullable String fieldPath, @Nullable Long bitOffset) {
    }
}

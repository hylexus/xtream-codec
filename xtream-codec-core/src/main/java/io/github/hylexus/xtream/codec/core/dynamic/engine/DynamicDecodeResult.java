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

import java.util.List;

/**
 * 解码结果及消费量。{@code consumedBits} 表示结构实际处理的位数，
 * {@code consumedBytes} 将不足一字节的尾部向上取整；调用方缓冲区的 readerIndex
 * 只前进已消费的字节，{@code remainingBytes} 表示其后仍可读取的字节数。
 *
 * @param values         解码得到的字段值
 * @param consumedBits   结构实际消费的位数
 * @param consumedBytes  向上取整后的消费字节数
 * @param remainingBytes 调用方缓冲区仍未消费的字节数
 * @param checksums      已执行的校验和核对结果，默认模式允许包含不匹配结果
 * @author Codex (AI)
 * @since 0.11.0
 */
public record DynamicDecodeResult(
        DynamicValues values,
        long consumedBits,
        int consumedBytes,
        int remainingBytes,
        List<DynamicChecksumResult> checksums) {
    public DynamicDecodeResult {
        checksums = List.copyOf(checksums);
    }

    /**
     * 兼容未提供校验结果的原有构造入口。
     */
    public DynamicDecodeResult(DynamicValues values, long consumedBits, int consumedBytes, int remainingBytes) {
        this(values, consumedBits, consumedBytes, remainingBytes, List.of());
    }
}

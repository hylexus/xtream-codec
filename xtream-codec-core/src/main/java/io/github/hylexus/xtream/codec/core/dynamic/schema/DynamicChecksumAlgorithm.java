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

package io.github.hylexus.xtream.codec.core.dynamic.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 内置字节区间校验算法。CRC 使用 JDK 的标准 CRC-32 与 CRC-32C 参数。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DynamicChecksumAlgorithm {
    XOR8("xor8", 8),
    SUM8("sum8", 8),
    CRC32("crc32", 32),
    CRC32C("crc32c", 32),
    ;

    private final String value;
    private final int bits;

    DynamicChecksumAlgorithm(String value, int bits) {
        this.value = value;
        this.bits = bits;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public int bits() {
        return bits;
    }

    @JsonCreator
    public static DynamicChecksumAlgorithm from(String value) {
        for (final DynamicChecksumAlgorithm algorithm : values()) {
            if (algorithm.value.equals(value)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("未知的校验算法: " + value);
    }
}


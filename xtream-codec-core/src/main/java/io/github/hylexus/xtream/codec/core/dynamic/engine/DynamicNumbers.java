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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 动态值的精确整数转换与 JSON 表示规则。
 * 字段读写、派生值和表达式共用此规则，避免各自处理大整数时产生不同结果。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicNumbers {
    private static final BigInteger MAX_SAFE_INTEGER = BigInteger.valueOf(9007199254740991L);

    private DynamicNumbers() {
    }

    /**
     * JSON 安全整数范围内返回 long，其余返回十进制字符串，保证前端不丢失精度。
     */
    static Object toJsonValue(BigInteger number) {
        return number.abs().compareTo(MAX_SAFE_INTEGER) > 0 ? number.toString() : number.longValue();
    }

    static BigInteger requireInteger(@Nullable Object value, String path, long bitOffset) {
        // JSON 大整数以十进制字符串传递；浮点数不参与整数转换以免丢精度。
        try {
            return switch (value) {
                case BigInteger number -> number;
                case BigDecimal number -> number.toBigIntegerExact();
                case Byte number -> BigInteger.valueOf(number.longValue());
                case Short number -> BigInteger.valueOf(number.longValue());
                case Integer number -> BigInteger.valueOf(number.longValue());
                case Long number -> BigInteger.valueOf(number);
                case String text -> new BigInteger(text);
                case null, default -> throw new DynamicCodecException(path, bitOffset, "整数必须为整型数值或十进制字符串");
            };
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new DynamicCodecException(path, bitOffset, "无效整数", exception);
        }
    }
}

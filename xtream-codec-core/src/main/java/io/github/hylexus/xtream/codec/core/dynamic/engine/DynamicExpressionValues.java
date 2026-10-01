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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpressionOperator;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.*;

import static io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicNumbers.requireInteger;

/**
 * 表达式的精确数值、只读绑定快照及结果检查。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicExpressionValues {
    private DynamicExpressionValues() {
    }

    static @Nullable Object freeze(@Nullable Object value, String path, long offset) {
        return freeze(value, path, offset, 0);
    }

    private static @Nullable Object freeze(@Nullable Object value, String path, long offset, int depth) {
        if (depth > 64) {
            throw new DynamicCodecException(path, offset, "表达式结果嵌套过深");
        }
        return switch (value) {
            case null -> null;
            case String text -> text;
            case Boolean bool -> bool;
            case BigInteger number -> {
                if (number.bitLength() > 4096) {
                    throw new DynamicCodecException(path, offset, "表达式整数超过资源上限");
                }
                yield number;
            }
            case Byte number -> number;
            case Short number -> number;
            case Integer number -> number;
            case Long number -> number;
            case BigDecimal number -> number;
            case Double number -> {
                if (!Double.isFinite(number)) {
                    throw new DynamicCodecException(path, offset, "表达式结果必须为有限数值");
                }
                yield number;
            }
            case Float number -> {
                if (!Float.isFinite(number)) {
                    throw new DynamicCodecException(path, offset, "表达式结果必须为有限数值");
                }
                yield number;
            }
            case List<?> list -> {
                final List<Object> copy = new ArrayList<>(list.size());
                for (final Object item : list) {
                    final Object frozenItem = freeze(item, path, offset, depth + 1);
                    copy.add(frozenItem);
                }
                yield Collections.unmodifiableList(copy);
            }
            case Map<?, ?> map -> {
                final Map<String, Object> copy = new LinkedHashMap<>();
                for (final var entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String key)) {
                        throw new DynamicCodecException(path, offset, "表达式对象键必须为字符串");
                    }
                    final Object frozenValue = freeze(entry.getValue(), path, offset, depth + 1);
                    copy.put(key, frozenValue);
                }
                yield Collections.unmodifiableMap(copy);
            }
            default -> throw new DynamicCodecException(path, offset, "表达式返回了不支持的类型: " + value.getClass().getName());
        };
    }

    /**
     * 获取受资源上限约束的精确整数；bitLength 不计无限符号扩展位，负数仍可参与位运算。
     */
    private static BigInteger boundedInteger(@Nullable Object value, String path, long offset) {
        if (value instanceof String text && text.length() > 1235) {
            throw new DynamicCodecException(path, offset, "表达式整数超过资源上限");
        }
        final BigInteger number = requireInteger(value, path, offset);
        if (number.bitLength() > 4096) {
            throw new DynamicCodecException(path, offset, "表达式整数超过资源上限");
        }
        return number;
    }

    static @Nullable Object json(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put((String) key, json(item)));
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof List<?> list) {
            final List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(json(item)));
            return Collections.unmodifiableList(result);
        }
        return DynamicRuntime.json(value);
    }

    static Object binary(
            DynamicExpressionOperator operator, @Nullable Object leftValue,
            @Nullable Object rightValue, String path, long bitOffset) {
        if (operator == DynamicExpressionOperator.EQ || operator == DynamicExpressionOperator.NE) {
            final boolean equal = String.valueOf(leftValue).equals(String.valueOf(rightValue));
            return (operator == DynamicExpressionOperator.EQ) == equal;
        }
        final BigInteger left = boundedInteger(leftValue, path, bitOffset);
        final BigInteger right = boundedInteger(rightValue, path, bitOffset);
        if ((operator == DynamicExpressionOperator.SHL || operator == DynamicExpressionOperator.SHR)
                && (right.signum() < 0 || right.compareTo(BigInteger.valueOf(64)) > 0)) {
            throw new DynamicCodecException(path, bitOffset, "移位数量必须为 0..64");
        }

        final Object result = switch (operator) {
            case ADD -> left.add(right);
            case SUB -> left.subtract(right);
            case MUL -> left.multiply(right);
            case DIV -> left.divide(right);
            case MOD -> left.remainder(right);
            // BigInteger 位运算按无限符号扩展的补码解释负数，不截断到某个字段位宽。
            // AND 只保留两侧都为 1 的位，OR 保留任一侧为 1 的位，XOR 保留两侧不同的位。
            case BIT_AND -> left.and(right);
            case BIT_OR -> left.or(right);
            case BIT_XOR -> left.xor(right);
            // 左移在低位补 0，相当于乘以 2^n；使用 BigInteger，不受 long 移位距离取低 6 位的规则影响。
            case SHL -> left.shiftLeft(right.intValueExact());
            // 右移是算术右移，负数补符号位 1；例如 -3 右移 1 位得到 -2，不是无符号右移。
            case SHR -> left.shiftRight(right.intValueExact());
            case GT -> left.compareTo(right) > 0;
            case GTE -> left.compareTo(right) >= 0;
            case LT -> left.compareTo(right) < 0;
            case LTE -> left.compareTo(right) <= 0;
            case EQ, NE, AND, OR, NOT, BIT_NOT -> throw new IllegalStateException("该运算符应在对应的求值入口处理");
        };
        if (result instanceof BigInteger number && number.bitLength() > 4096) {
            throw new DynamicCodecException(path, bitOffset, "表达式整数超过资源上限");
        }
        return result;
    }

    /**
     * 按无限符号扩展的补码逐位取反，结果为 -value - 1，例如 ~0 = -1、~5 = -6。
     * 结果不自动限定为 8、32 或 64 位；需要无符号 n 位结果时，由表达式再 AND (2^n - 1)。
     */
    static BigInteger bitNot(@Nullable Object value, String path, long bitOffset) {
        return boundedInteger(value, path, bitOffset).not();
    }

    static boolean bool(@Nullable Object value, String path, long bitOffset) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new DynamicCodecException(path, bitOffset, "布尔运算需要布尔值");
    }
}

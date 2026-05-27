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

package io.github.hylexus.xtream.codec.common.utils;


import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 位操作工具类，最多支持 8 字节。
 * <p>
 * Copied from jt-framework.
 *
 * @author hylexus
 */
public interface XtreamBitOperator {

    static XtreamBitOperator immutable() {
        return immutable(0);
    }

    /**
     * 返回不可变对象(每个操作都 {@code new} 一个新实例)
     */
    static XtreamBitOperator immutable(long value) {
        return () -> value;
    }

    static XtreamBitOperator mutable() {
        return mutable(0);
    }

    /**
     * 返回可变对象(每个操作都返回当前实例)
     */
    static XtreamBitOperator mutable(long value) {
        return new MutableBitOperator(value);
    }

    long value();

    default XtreamBitOperator map(Function<Long, Long> mapper) {
        return () -> mapper.apply(XtreamBitOperator.this.value());
    }

    default XtreamBitOperator mapIf(Function<Long, Long> mapper, Boolean condition) {
        if (condition) {
            return map(mapper);
        }
        return this;
    }

    default XtreamBitOperator mapIf(Function<Long, Long> mapper, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return map(mapper);
        }
        return this;
    }

    default XtreamBitOperator mapIf(Function<Long, Long> mapper, Supplier<Boolean> supplier) {
        if (supplier.get()) {
            return map(mapper);
        }
        return this;
    }

    /**
     * @param offset 偏移量: 0 ~ 63
     * @param status {@code true}: 将 {@code offset} 偏移处的 bit 设置为 {@code 1};
     *               {@code false}: 将 {@code offset} 偏移处的 bit 设置为 {@code 0};
     * @return {@code offset} 偏移处的 bit 被设置为 {@code status} 后的 {@code BitOperator} 实例
     */
    default XtreamBitOperator setWithStatus(int offset, boolean status) {
        if (status) {
            return this.set(offset);
        }
        return this.reset(offset);
    }

    /**
     * @param offset    偏移量: 0 ~ 63
     * @param status    {@code true}: 将 {@code offset} 偏移处的 bit 设置为 {@code 1};
     *                  {@code false}: 将 {@code offset} 偏移处的 bit 设置为 {@code 0};
     * @param condition 为 {@code true} 时，执行 {@link  #setWithStatus(int, boolean)} 操作，否则不执行任何操作
     */
    default XtreamBitOperator setWithStatusIf(int offset, boolean status, boolean condition) {
        if (condition) {
            return this.setWithStatus(offset, status);
        }
        return this;
    }

    /**
     * @param offset    偏移量: 0 ~ 63
     * @param status    {@code true}: 将 {@code offset} 偏移处的 bit 设置为 {@code 1};
     *                  {@code false}: 将 {@code offset} 偏移处的 bit 设置为 {@code 0};
     * @param condition 为 {@code true} 时，执行 {@link  #setWithStatus(int, boolean)} 操作，否则不执行任何操作
     */
    default XtreamBitOperator setWithStatusIf(int offset, boolean status, Supplier<Boolean> condition) {
        if (condition.get()) {
            return this.setWithStatus(offset, status);
        }
        return this;
    }

    /**
     * @param offset    偏移量: 0 ~ 63
     * @param status    {@code true}: 将 {@code offset} 偏移处的 bit 设置为 {@code 1};
     *                  {@code false}: 将 {@code offset} 偏移处的 bit 设置为 {@code 0};
     * @param predicate 为 {@code true} 时，执行 {@link  #setWithStatus(int, boolean)} 操作，否则不执行任何操作
     */
    default XtreamBitOperator setWithStatusIf(int offset, boolean status, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return this.setWithStatus(offset, status);
        }
        return this;
    }

    default XtreamBitOperator set(int offset) {
        XtreamAssertions.assertThat(offset >= 0 && offset < Long.SIZE, "offset >= 0 && offset < Long.SIZE");
        return map(it -> Numbers.setBitAt(it, offset));
    }

    default XtreamBitOperator setIf(int offset, boolean condition) {
        if (condition) {
            return this.set(offset);
        }
        return this;
    }

    default XtreamBitOperator setIf(int offset, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return this.set(offset);
        }
        return this;
    }


    default XtreamBitOperator setIf(int offset, Supplier<Boolean> condition) {
        if (condition.get()) {
            return this.set(offset);
        }
        return this;
    }

    default XtreamBitOperator setRange(int offset, int length) {
        XtreamAssertions.assertThat(offset >= 0 && offset < Long.SIZE, "offset >= 0 && offset < Long.SIZE");
        XtreamAssertions.assertThat(length > 0 && offset + length <= Long.SIZE, "length > 0 && offset+length <= Long.SIZE");
        return map(it -> it | (~0L >>> (Long.SIZE - length) << offset));
    }

    default XtreamBitOperator setRangeIf(int offset, int length, boolean condition) {
        if (condition) {
            return setRange(offset, length);
        }
        return this;
    }

    default XtreamBitOperator setRangeIf(int offset, int length, Supplier<Boolean> condition) {
        if (condition.get()) {
            return setRange(offset, length);
        }
        return this;
    }

    default XtreamBitOperator setRangeIf(int offset, int length, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return setRange(offset, length);
        }
        return this;
    }

    default XtreamBitOperator reset(int offset) {
        XtreamAssertions.assertThat(offset >= 0 && offset < Long.SIZE, "offset >= 0 && offset < Long.SIZE");
        return map(it -> Numbers.resetBitAt(it, offset));
    }

    default XtreamBitOperator resetIf(int offset, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return this.reset(offset);
        }
        return this;
    }

    default XtreamBitOperator resetIf(int offset, Supplier<Boolean> condition) {
        if (condition.get()) {
            return this.reset(offset);
        }
        return this;
    }

    default XtreamBitOperator resetIf(int offset, Boolean condition) {
        if (condition) {
            return this.reset(offset);
        }
        return this;
    }

    default XtreamBitOperator resetRange(int offset, int length) {
        XtreamAssertions.assertThat(offset >= 0 && offset < Long.SIZE, "offset >= 0 && offset < Long.SIZE");
        XtreamAssertions.assertThat(length > 0 && offset + length <= Long.SIZE, "length > 0 && offset + length <= Long.SIZE");
        return map(it -> it & (~(~0L >>> (Long.SIZE - length) << offset)));
    }

    default XtreamBitOperator resetRangeIf(int offset, int length, boolean condition) {
        if (condition) {
            return this.resetRange(offset, length);
        }
        return this;
    }

    default XtreamBitOperator resetRangeIf(int offset, int length, Supplier<Boolean> condition) {
        if (condition.get()) {
            return this.resetRange(offset, length);
        }
        return this;
    }

    default XtreamBitOperator resetRangeIf(int offset, int length, Predicate<Long> predicate) {
        if (predicate.test(this.value())) {
            return this.resetRange(offset, length);
        }
        return this;
    }

    default int get(int offset) {
        return Numbers.getBitAt(this.value(), offset);
    }

    default int rangedIntValue(int offset, int length) {
        XtreamAssertions.assertThat(offset >= 0, "offset >= 0");
        XtreamAssertions.assertThat(length > 0 && offset + length <= Long.SIZE, "length > 0 && offset + length <= Long.SIZE");
        final int number = this.intValue();
        return (number << (Integer.SIZE - (offset + length)))
                >>> (Integer.SIZE - length);
    }

    default long rangedUnsignedIntValue(int offset, int length) {
        final int x = this.rangedIntValue(offset, length);
        return ((long) x) & 0XFFFFFFFFL;
    }

    default long rangedLongValue(int offset, int length) {
        XtreamAssertions.assertThat(offset >= 0, "offset >= 0");
        XtreamAssertions.assertThat(length > 0 && offset + length <= Long.SIZE, "length > 0 && offset + length <= Long.SIZE");
        final long number = this.longValue();
        return (number << (Long.SIZE - (offset + length)))
                >>> (Long.SIZE - length);
    }

    default BigInteger rangedUnsignedLongValue(int offset, int length) {
        return toUnsignedBigInteger(this.rangedLongValue(offset, length));
    }

    default XtreamBitOperator longValue(Consumer<Long> consumer) {
        consumer.accept(this.longValue());
        return this;
    }

    default long longValue() {
        return this.value();
    }

    /**
     * @return 等值的 无符号 {@link BigInteger} 对象
     */
    default BigInteger unsignedLongValue() {
        return toUnsignedBigInteger(this.value());
    }

    default long dwordValue() {
        return this.value();
    }

    default int wordValue() {
        return this.intValue();
    }

    default XtreamBitOperator intValue(Consumer<Integer> consumer) {
        consumer.accept(this.intValue());
        return this;
    }

    default int intValue() {
        return (int) this.value();
    }

    default XtreamBitOperator shortValue(Consumer<Short> consumer) {
        consumer.accept(this.shortValue());
        return this;
    }

    default short shortValue() {
        return (short) this.value();
    }

    default XtreamBitOperator byteValue(Consumer<Byte> consumer) {
        consumer.accept(this.byteValue());
        return this;
    }

    default byte byteValue() {
        return (byte) this.value();
    }

    default XtreamBitOperator boolValue(Consumer<Boolean> consumer) {
        consumer.accept(this.boolValue());
        return this;
    }

    default boolean boolValue() {
        return this.value() == 1L;
    }


    default XtreamBitOperator hexString(int minLeadingZeros, Consumer<String> consumer) {
        consumer.accept(this.hexString(minLeadingZeros));
        return this;
    }

    default XtreamBitOperator hexString(Consumer<String> consumer) {
        consumer.accept(this.hexString());
        return this;
    }

    default String hexString(int minLeadingZeros) {
        final String hexString = this.hexString();
        if (hexString.length() < minLeadingZeros) {
            return "0".repeat(minLeadingZeros - hexString.length()) + hexString;

        }
        return hexString;
    }

    default String hexString() {
        return Long.toHexString(this.value());
    }

    default XtreamBitOperator binaryString(int minLeadingZeros, Consumer<String> consumer) {
        consumer.accept(this.binaryString(minLeadingZeros));
        return this;
    }

    default XtreamBitOperator binaryString(Consumer<String> consumer) {
        consumer.accept(this.binaryString());
        return this;
    }

    default String binaryString(int minLeadingZeros) {
        return binaryString(minLeadingZeros, this.value());
    }

    default String binaryString() {
        return Long.toBinaryString(this.value());
    }

    static String binaryString(int minLeadingZeros, int value) {
        final String string = Integer.toBinaryString(value);
        if (string.length() < minLeadingZeros) {
            return "0".repeat(minLeadingZeros - string.length()) + string;
        }
        return string;
    }

    static String binaryString(int minLeadingZeros, long value) {
        final String string = Long.toBinaryString(value);
        if (string.length() < minLeadingZeros) {
            return "0".repeat(minLeadingZeros - string.length()) + string;
        }
        return string;
    }

    /**
     * 这个方法是从 {@code java.lang.Long#toUnsignedBigInteger(long)} 中复制过来的。
     */
    static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L) {
            return BigInteger.valueOf(i);
        } else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            // return (upper << 32) + lower
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper)))
                    .shiftLeft(32)
                    .add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }
}

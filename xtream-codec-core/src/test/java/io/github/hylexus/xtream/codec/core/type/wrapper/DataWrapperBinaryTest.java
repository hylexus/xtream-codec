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

package io.github.hylexus.xtream.codec.core.type.wrapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 包装类型的精确字节、数值转换、字符集、空值及修改后状态测试。
 *
 * @author Codex (AI)
 */
class DataWrapperBinaryTest {

    static Stream<Arguments> binaryValues() {
        return Stream.of(
                Arguments.of(new I8Wrapper(Byte.MIN_VALUE), "80"),
                Arguments.of(new I8Wrapper((byte) 0), "00"),
                Arguments.of(new I8Wrapper(Byte.MAX_VALUE), "7f"),
                Arguments.of(new U8Wrapper((short) 0), "00"),
                Arguments.of(new U8Wrapper((short) 128), "80"),
                Arguments.of(new U8Wrapper((short) 255), "ff"),
                Arguments.of(new U8Wrapper((short) -1), "ff"),
                Arguments.of(new I16Wrapper(Short.MIN_VALUE), "8000"),
                Arguments.of(new I16Wrapper((short) 0), "0000"),
                Arguments.of(new I16Wrapper(Short.MAX_VALUE), "7fff"),
                Arguments.of(new U16Wrapper(0), "0000"),
                Arguments.of(new U16Wrapper(32768), "8000"),
                Arguments.of(new U16Wrapper(65535), "ffff"),
                Arguments.of(new U16Wrapper(-1), "ffff"),
                Arguments.of(new I32Wrapper(Integer.MIN_VALUE), "80000000"),
                Arguments.of(new I32Wrapper(0), "00000000"),
                Arguments.of(new I32Wrapper(Integer.MAX_VALUE), "7fffffff"),
                Arguments.of(new U32Wrapper(0L), "00000000"),
                Arguments.of(new U32Wrapper(2147483648L), "80000000"),
                Arguments.of(new U32Wrapper(4294967295L), "ffffffff"),
                Arguments.of(new U32Wrapper(-1L), "ffffffff"),
                Arguments.of(new StringWrapperGbk("中文"), "d6d0cec4"),
                Arguments.of(new StringWrapperGbk(""), ""),
                Arguments.of(new StringWrapperUtf8("中文😀"), "e4b8ade69687f09f9880"),
                Arguments.of(new StringWrapperUtf8(""), ""),
                Arguments.of(new StringWrapperBcd("001234"), "001234"),
                Arguments.of(new StringWrapperBcd("123"), "0123"),
                Arguments.of(new StringWrapperBcd(""), ""),
                Arguments.of(new BytesDataWrapper(new byte[]{0, 127, (byte) 128, (byte) 255}), "007f80ff"),
                Arguments.of(new BytesDataWrapper(new byte[0]), ""));
    }

    @ParameterizedTest
    @MethodSource("binaryValues")
    void writesExactBytesWithoutChangingReaderIndexOrPrefix(DataWrapper<?> value, String hex) {
        assertBinary(value, hex);
    }

    static Stream<Arguments> numericConversions() {
        return Stream.of(
                Arguments.of(new I8Wrapper((byte) -17), -17, -17, -17, 239, 65519, 4294967279L, "-17"),
                Arguments.of(new U8Wrapper((short) 239), -17, 239, 239, 239, 239, 239L, "239"),
                Arguments.of(new I16Wrapper((short) -12817), -17, -12817, -12817, 239, 52719, 4294954479L, "-12817"),
                Arguments.of(new U16Wrapper(52719), -17, -12817, 52719, 239, 52719, 52719L, "52719"),
                Arguments.of(new I32Wrapper(0x89abcdef), -17, -12817, 0x89abcdef, 239, 52719, 2309737967L, "-1985229329"),
                Arguments.of(new U32Wrapper(2309737967L), -17, -12817, 0x89abcdef, 239, 52719, 2309737967L, "2309737967"));
    }

    @ParameterizedTest
    @MethodSource("numericConversions")
    void narrowsSignedValuesAndMasksUnsignedAliases(DataWrapper<?> value, int i8, int i16, int i32,
                                                   int u8, int u16, long u32, String text) {
        assertThat(value.asI8()).isEqualTo((byte) i8);
        assertThat(value.asI16()).isEqualTo((short) i16);
        assertThat(value.asI32()).isEqualTo(i32);
        assertThat(value.asU8()).isEqualTo((short) u8);
        assertThat(value.asByte()).isEqualTo((short) u8);
        assertThat(value.asU16()).isEqualTo(u16);
        assertThat(value.asWord()).isEqualTo(u16);
        assertThat(value.asU32()).isEqualTo(u32);
        assertThat(value.asDword()).isEqualTo(u32);
        assertThat(value.asString()).isEqualTo(text);
        assertThat(value.asString(StandardCharsets.UTF_8)).isEqualTo(text);
    }

    static Stream<Arguments> unsetNumbers() {
        return Stream.of(Arguments.of(new I8Wrapper(), 1), Arguments.of(new U8Wrapper(), 1),
                Arguments.of(new I16Wrapper(), 2), Arguments.of(new U16Wrapper(), 2),
                Arguments.of(new I32Wrapper(), 4), Arguments.of(new U32Wrapper(), 4));
    }

    @ParameterizedTest
    @MethodSource("unsetNumbers")
    void leavesUnsetNumbersUnwrittenAndRejectsNumericConversion(DataWrapper<?> value, int width) {
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        try {
            value.writeTo(output);
            assertThat(output.writerIndex()).isEqualTo(2);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(output, 0, 2)).isEqualTo("55aa");
            assertThat(value.length()).isEqualTo(width);
            assertThat(value.asBytes()).isNull();
            assertThat(value.asString()).isNull();
            assertThat(value.asBcd()).isNull();
            assertThatNullPointerException().isThrownBy(value::asI8);
            assertThatNullPointerException().isThrownBy(value::asI16);
            assertThatNullPointerException().isThrownBy(value::asI32);
        } finally {
            output.release();
        }
    }

    static Stream<Arguments> unsetStrings() {
        return Stream.of(Arguments.of(new StringWrapperGbk(), true),
                Arguments.of(new StringWrapperBcd(), true), Arguments.of(new StringWrapperUtf8(), false));
    }

    @ParameterizedTest
    @MethodSource("unsetStrings")
    void preservesExistingUnsetStringBehavior(DataWrapper<?> value, boolean allowsEmptyWrite) {
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        try {
            assertThat(value.length()).isZero();
            assertThat(value.asBytes()).isNull();
            assertThat(value.asString()).isNull();
            assertThat(value.asBcd()).isNull();
            if (allowsEmptyWrite) {
                value.writeTo(output);
            } else {
                assertThatNullPointerException().isThrownBy(() -> value.writeTo(output));
            }
            assertThat(output.writerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55");
            assertThatThrownBy(value::asI8).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(value::asI16).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(value::asI32).isInstanceOf(UnsupportedOperationException.class);
        } finally {
            output.release();
        }
    }

    @Test
    void numericSettersReturnSameWrapperAndReplaceEncodedValue() {
        final I8Wrapper i8 = new I8Wrapper((byte) 0);
        final U8Wrapper u8 = new U8Wrapper((short) 0);
        final I16Wrapper i16 = new I16Wrapper((short) 0);
        final U16Wrapper u16 = new U16Wrapper(0);
        final I32Wrapper i32 = new I32Wrapper(0);
        final U32Wrapper u32 = new U32Wrapper(0L);
        assertThat(i8.setValue((byte) -1)).isSameAs(i8);
        assertThat(u8.setValue((short) 255)).isSameAs(u8);
        assertThat(i16.setValue((short) -1)).isSameAs(i16);
        assertThat(u16.setValue(65535)).isSameAs(u16);
        assertThat(i32.setValue(-1)).isSameAs(i32);
        assertThat(u32.setValue(4294967295L)).isSameAs(u32);
        assertBinary(i8, "ff");
        assertBinary(u8, "ff");
        assertBinary(i16, "ffff");
        assertBinary(u16, "ffff");
        assertBinary(i32, "ffffffff");
        assertBinary(u32, "ffffffff");
    }

    @Test
    void stringSettersUpdateByteLengthAndPreserveLeadingBcdZeros() {
        final StringWrapperGbk gbk = new StringWrapperGbk("A");
        final StringWrapperUtf8 utf8 = new StringWrapperUtf8("A");
        final StringWrapperBcd bcd = new StringWrapperBcd("12");
        assertThat(gbk.setValue("中文")).isSameAs(gbk);
        assertThat(utf8.setValue("中文😀")).isSameAs(utf8);
        assertThat(bcd.setValue("00123")).isSameAs(bcd);
        assertBinary(gbk, "d6d0cec4");
        assertBinary(utf8, "e4b8ade69687f09f9880");
        assertBinary(bcd, "000123");
        assertThat(gbk.asString()).isEqualTo("中文");
        assertThat(utf8.asString()).isEqualTo("中文😀");
        assertThat(bcd.asString()).isEqualTo("00123");
        assertThat(bcd.asBcd()).isEqualTo("000123");
        assertThat(bcd.setValue(null)).isSameAs(bcd);
        assertThat(bcd.length()).isZero();
        assertThat(bcd.asBytes()).isNull();
    }

    @Test
    void interpretsBytePrefixesAsBigEndianSignedAndUnsignedValues() {
        final BytesDataWrapper value = new BytesDataWrapper(ByteBufUtil.decodeHexDump("89abcdef"));
        assertThat(value.asI8()).isEqualTo((byte) -119);
        assertThat(value.asU8()).isEqualTo((short) 137);
        assertThat(value.asI16()).isEqualTo((short) -30293);
        assertThat(value.asU16()).isEqualTo(35243);
        assertThat(value.asI32()).isEqualTo(0x89abcdef);
        assertThat(value.asU32()).isEqualTo(2309737967L);
        assertThat(new BytesDataWrapper(ByteBufUtil.decodeHexDump("001234")).asBcd()).isEqualTo("001234");
        assertThat(new BytesDataWrapper(new byte[]{65, 66}).asString()).isEqualTo("AB");
    }

    static Stream<Arguments> charsets() {
        return Stream.of(Arguments.of(Charset.forName("GBK"), "d6d0cec4"),
                Arguments.of(StandardCharsets.UTF_8, "e4b8ade69687"));
    }

    @ParameterizedTest
    @MethodSource("charsets")
    void decodesByteWrapperWithExplicitCharset(Charset charset, String hex) {
        assertThat(new BytesDataWrapper(ByteBufUtil.decodeHexDump(hex)).asString(charset)).isEqualTo("中文");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void rejectsInsufficientBytesForNumericConversion(int length) {
        final byte[] bytes = new byte[length];
        final BytesDataWrapper value = new BytesDataWrapper(bytes);
        assertThatThrownBy(value::asI32).isInstanceOf(IndexOutOfBoundsException.class);
        if (length < 2) {
            assertThatThrownBy(value::asI16).isInstanceOf(IndexOutOfBoundsException.class);
        }
        if (length == 0) {
            assertThatThrownBy(value::asI8).isInstanceOf(IndexOutOfBoundsException.class);
        }
        assertThat(bytes).containsExactly(new byte[length]);
    }

    private static void assertBinary(DataWrapper<?> value, String hex) {
        final byte[] expected = ByteBufUtil.decodeHexDump(hex);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        try {
            assertThat(value.length()).isEqualTo(expected.length);
            assertThat(value.asBytes()).containsExactly(expected);
            value.writeTo(output);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + hex);
            assertThat(output.writerIndex()).isEqualTo(2 + expected.length);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(value.asBytes()).containsExactly(expected);
        } finally {
            output.release();
        }
    }
}

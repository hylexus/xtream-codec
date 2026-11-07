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

package io.github.hylexus.xtream.codec.core.type.simple;

import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.PaddingConfig;
import io.github.hylexus.xtream.codec.core.type.simple.DataField.DictKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.assertj.core.api.Assertions.*;

class DictKeyTest {

    static Stream<Arguments> numericKeys() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, KeyType.i8, i8(Byte.MIN_VALUE), "80"),
                Arguments.of(tracked, KeyType.i8, i8(Byte.MAX_VALUE), "7f"),
                Arguments.of(tracked, KeyType.u8, u8((short) 0), "00"),
                Arguments.of(tracked, KeyType.u8, u8((short) 255), "ff"),
                Arguments.of(tracked, KeyType.i16, i16(Short.MIN_VALUE), "8000"),
                Arguments.of(tracked, KeyType.i16, i16(Short.MAX_VALUE), "7fff"),
                Arguments.of(tracked, KeyType.i16, i16((short) 0x1234), "1234"),
                Arguments.of(tracked, KeyType.u16, u16(0), "0000"),
                Arguments.of(tracked, KeyType.u16, u16(65535), "ffff"),
                Arguments.of(tracked, KeyType.i32, i32(Integer.MIN_VALUE), "80000000"),
                Arguments.of(tracked, KeyType.i32, i32(Integer.MAX_VALUE), "7fffffff"),
                Arguments.of(tracked, KeyType.i32, i32(0x12345678), "12345678"),
                Arguments.of(tracked, KeyType.u32, u32(0L), "00000000"),
                Arguments.of(tracked, KeyType.u32, u32(2147483648L), "80000000"),
                Arguments.of(tracked, KeyType.u32, u32(4294967295L), "ffffffff"),
                Arguments.of(tracked, KeyType.i64, i64(Long.MIN_VALUE), "8000000000000000"),
                Arguments.of(tracked, KeyType.i64, i64(Long.MAX_VALUE), "7fffffffffffffff"),
                Arguments.of(tracked, KeyType.i64, i64(0x0123456789abcdefL), "0123456789abcdef")));
    }

    @ParameterizedTest
    @MethodSource("numericKeys")
    void parsesAndEncodesNumericKeysWithExactBytes(boolean tracked, KeyType type, DictKey expected, String hex) {
        final DictKey parsed = DictKey.from(expected.getClass(), expected.value().toString());
        assertThat(parsed).isEqualTo(expected);
        assertCodec(tracked, type, parsed, "UTF-8", hex);
    }

    @ParameterizedTest
    @CsvSource({
            "I8, -129", "I8, 128", "U8, -1", "U8, 256",
            "I16, -32769", "I16, 32768", "U16, -1", "U16, 65536",
            "I32, -2147483649", "I32, 2147483648", "U32, -1", "U32, 4294967296",
            "I64, -9223372036854775809", "I64, 9223372036854775808"
    })
    void rejectsOutOfRangeNumericText(String type, String value) throws Exception {
        final Class<? extends DictKey> keyClass = Class.forName(DataField.class.getName() + "$" + type).asSubclass(DictKey.class);
        assertThatThrownBy(() -> DictKey.from(keyClass, value)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(value = KeyType.class, names = {"i8", "u8", "i16", "u16", "i32", "u32", "i64"})
    void rejectsMalformedNumericText(KeyType type) throws Exception {
        final Class<? extends DictKey> keyClass = Class.forName(DataField.class.getName() + "$" + type.name().toUpperCase(Locale.ROOT)).asSubclass(DictKey.class);
        for (final String value : new String[]{"", "1.5", " 1", "0xff"}) {
            assertThatThrownBy(() -> DictKey.from(keyClass, value)).isInstanceOf(NumberFormatException.class);
        }
    }

    static Stream<Class<? extends DictKey>> unsupportedTextConversions() {
        return Stream.of(DataField.GenericString.class, DataField.GbkString.class, DataField.Gb2312String.class,
                DataField.Utf8String.class, DataField.Bcd8421String.class, DataField.HexString.class);
    }

    @ParameterizedTest
    @MethodSource("unsupportedTextConversions")
    void rejectsTypesWithoutNumericTextConversion(Class<? extends DictKey> type) {
        assertThatThrownBy(() -> DictKey.from(type, "1"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown dict key type: " + type.getSimpleName());
    }

    static Stream<Arguments> stringKeys() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, KeyType.str, string("AB", "US-ASCII"), "US-ASCII", "4142"),
                Arguments.of(tracked, KeyType.str, string("中", "UTF-16BE"), "UTF-16BE", "4e2d"),
                Arguments.of(tracked, KeyType.str_gbk, gbkString("中"), "UTF-8", "d6d0"),
                Arguments.of(tracked, KeyType.str_gb_2312, gb2312String("中"), "UTF-8", "d6d0"),
                Arguments.of(tracked, KeyType.str_utf8, utf8String("中"), "GBK", "e4b8ad")));
    }

    @ParameterizedTest
    @MethodSource("stringKeys")
    void preservesStringKeyTypeCharsetAndByteLength(boolean tracked, KeyType type, DictKey key, String charset, String hex) {
        assertCodec(tracked, type, key, charset, hex);
    }

    @ParameterizedTest
    @EnumSource(value = KeyType.class, names = {"str", "str_gbk", "str_gb_2312", "str_utf8"})
    void emptyStringKeyDoesNotConsumeFollowingByte(KeyType type) {
        final ByteBuf input = Unpooled.buffer().writeByte(0x7e);
        try {
            final DictKey key = DictKey.deserialize(type, input, 0, "UTF-8");
            assertThat(key.value()).isEqualTo("");
            assertThat(input.readerIndex()).isZero();
            assertThat(input.readUnsignedByte()).isEqualTo((short) 126);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @EnumSource(value = KeyType.class, names = {"i8", "u8", "i16", "u16", "i32", "u32", "i64"})
    void truncatedNumericKeyLeavesReaderIndexUnchanged(KeyType type) {
        final int size = type.dataType().sizeInBytes();
        final ByteBuf input = Unpooled.buffer().writeByte(0x55).writeZero(size - 1).readerIndex(1);
        try {
            assertThatThrownBy(() -> DictKey.deserialize(type, input, size, "UTF-8")).isInstanceOf(IndexOutOfBoundsException.class);
            assertThat(input.readerIndex()).isEqualTo(1);
        } finally {
            input.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void encodesBcdHexAndPaddedStringKeys(boolean tracked) {
        final DictKey[] keys = {
                bcd8421String("12345"), hexString("aB01"),
                string("中", "UTF-8", new PaddingConfig(PaddingType.LEFT, (byte) '0', 5)),
                gb2312String("中", new PaddingConfig(PaddingType.RIGHT, (byte) '0', 4))
        };
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        final CodecTracker tracker = new CodecTracker();
        tracker.beginEncode(0, null);
        try {
            for (final DictKey key : keys) {
                if (tracked) {
                    key.encodeWithTracker(output, tracker, "key");
                } else {
                    key.encode(output);
                }
            }
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55012345ab013030e4b8add6d03030");
        } finally {
            output.release();
        }
    }

    private void assertCodec(boolean tracked, KeyType type, DictKey expected, String charset, String hex) {
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = new CodecTracker();
        encoder.beginEncode(0, null);
        final CodecTracker decoder = new CodecTracker();
        decoder.beginDecode(0, null);
        try {
            if (tracked) {
                expected.encodeWithTracker(output, encoder, "key");
            } else {
                expected.encode(output);
            }
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo("55" + hex);
            output.writeByte(0x7e).readerIndex(1);
            final DictKey decoded = tracked
                    ? DictKey.deserializeWithTracker(decoder, type, output, hex.length() / 2, charset, "key")
                    : DictKey.deserialize(type, output, hex.length() / 2, charset);
            assertThat(decoded).isEqualTo(expected);
            assertThat(output.readerIndex()).isEqualTo(1 + hex.length() / 2);
            assertThat(output.readUnsignedByte()).isEqualTo((short) 126);
            if (tracked) {
                assertTrace(encoder, hex);
                assertTrace(decoder, hex);
            }
        } finally {
            output.release();
        }
    }

    private void assertTrace(CodecTracker tracker, String hex) {
        assertThat(tracker.getTrace().getRoot().getChildren()).singleElement().satisfies(node -> {
            assertThat(node.getName()).isEqualTo("key");
            assertThat(node.getByteStart()).isEqualTo(1);
            assertThat(node.getByteEnd()).isEqualTo(1 + hex.length() / 2);
            assertThat(node.getHex()).isEqualTo(hex);
            assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        });
    }
}

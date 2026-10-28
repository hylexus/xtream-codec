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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态字节字段的输入类型、填充剥离和严格字符集边界测试。
 *
 * @author Codex (AI)
 */
class DynamicByteBoundaryTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void encodesSignedByteArraysAsUnsignedWireBytesWithoutMutatingInput(boolean tracked) {
        final byte[] input = {0, 127, (byte) 128, (byte) 255};
        assertEquals(List.of(0, 127, 128, 255), roundTrip(bytes(4), input, "007f80ff", tracked));
        assertArrayEquals(new byte[]{0, 127, (byte) 128, (byte) 255}, input);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsExactIntegerRepresentationsInsideByteLists(boolean tracked) {
        final List<?> input = List.of(BigDecimal.ZERO, BigInteger.valueOf(255), "128", (short) 127);
        assertEquals(List.of(0, 255, 128, 127), roundTrip(bytes(4), input, "00ff807f", tracked));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsAValidSurrogatePairAtTheExactUtf8ByteBoundary(boolean tracked) {
        final DynamicField field = DynamicField.field("data", "string").length(DynamicLength.fixed(4, "byte")).build();
        assertEquals("😀", roundTrip(field, "😀", "f09f9880", tracked));
    }

    @ParameterizedTest
    @MethodSource("invalidBytes")
    void rejectsInvalidByteValuesWithFieldLocation(Object input, String message) {
        for (final boolean tracked : new boolean[]{false, true}) {
            assertEncodingRejected(bytes(1), input, message, tracked);
        }
    }

    static Stream<Arguments> invalidBytes() {
        return Stream.of(
                Arguments.of(List.of(-1), "超出 0..255"),
                Arguments.of(List.of(256), "超出 0..255"),
                Arguments.of(List.of(new BigDecimal("1.5")), "无效整数"),
                Arguments.of(List.of(1D), "整数必须"),
                Arguments.of(List.of(true), "整数必须"),
                Arguments.of(Arrays.asList((Object) null), "整数必须"),
                Arguments.of(List.of("invalid"), "无效整数"),
                Arguments.of(List.of(BigInteger.ONE.shiftLeft(40)), "BigInteger out of int range"),
                Arguments.of("00", "必须为字节数组或 0..255 数组"),
                Arguments.of(new byte[]{1, 2}, "超过字段或输出边界"));
    }

    @ParameterizedTest
    @MethodSource("paddingCases")
    void padsAndStripsOnlyTheSelectedEdgeBeforeCharsetDecoding(String side, int padByte, boolean strip,
                                                               String input, String hex, String expected) {
        final DynamicField field = DynamicField.field("data", "string")
                .length(DynamicLength.fixed(hex.length() / 2, "byte"))
                .options(Map.of("padByte", padByte, "padSide", side, "stripPadding", strip)).build();
        for (final boolean tracked : new boolean[]{false, true}) {
            assertEquals(expected, roundTrip(field, input, hex, tracked));
        }
    }

    static Stream<Arguments> paddingCases() {
        return Stream.of(
                Arguments.of("left", 32, true, "a b ", "2061206220", "a b "),
                Arguments.of("right", 32, true, " a b", "2061206220", " a b"),
                Arguments.of("left", 32, true, "", "2020202020", ""),
                Arguments.of("right", 32, true, "", "2020202020", ""),
                Arguments.of("left", 32, true, "abcde", "6162636465", "abcde"),
                Arguments.of("right", 32, true, "abcde", "6162636465", "abcde"),
                Arguments.of("left", 32, false, "ab", "2020206162", "   ab"),
                Arguments.of("right", 32, false, "ab", "6162202020", "ab   "),
                Arguments.of("left", 255, true, "中", "ffffe4b8ad", "中"),
                Arguments.of("right", 255, true, "中", "e4b8adffff", "中"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"c328", "e4b8", "80", "c0af", "eda080", "f4908080"})
    void rejectsMalformedUtf8WithoutCommittingReadPosition(String hex) {
        final byte[] input = HexFormat.of().parseHex(hex);
        final DynamicPlan plan = plan(DynamicField.field("data", "string")
                .length(DynamicLength.fixed(input.length, "byte")).build());
        for (final boolean tracked : new boolean[]{false, true}) {
            final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).writeByte(7).writeBytes(input).writeByte(0x7e).writeByte(0x66);
            try {
                buffer.readerIndex(1);
                final CodecTracker tracker = tracker(tracked);
                final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
                assertLocation(error, "输入包含非法字符编码", (1L + input.length) * 8, tracker);
                assertEquals(1, buffer.readerIndex());
                assertEquals("07" + hex + "7e66", ByteBufUtil.hexDump(buffer));
                assertEquals(1, buffer.refCnt());
            } finally {
                buffer.release();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uD800", "\uDC00", "a\uD800b"})
    void rejectsUnpairedJavaSurrogatesRatherThanReplacingThem(String input) {
        final DynamicField field = DynamicField.field("data", "string").length(DynamicLength.fixed(4, "byte")).build();
        for (final boolean tracked : new boolean[]{false, true}) {
            assertEncodingRejected(field, input, "字符串包含字符集无法编码的字符", tracked);
        }
    }

    private static DynamicField bytes(int size) {
        return DynamicField.field("data", "bytes").length(DynamicLength.fixed(size, "byte")).build();
    }

    private static DynamicPlan plan(DynamicField field) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("prefix", "u8").build(), field, DynamicField.field("tail", "u8").build()));
    }

    private static Object roundTrip(DynamicField field, Object input, String hex, boolean tracked) {
        final DynamicPlan plan = plan(field);
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            final long bits = (hex.length() / 2L + 2) * 8;
            assertEquals(bits, plan.encode(DynamicValues.of(Map.of("prefix", 7, "data", input, "tail", 126)), buffer, tracker(tracked)));
            assertEquals("5507" + hex + "7e", ByteBufUtil.hexDump(buffer));
            buffer.writeByte(0x66).readerIndex(1);
            final DynamicDecodeResult result = plan.decode(buffer, tracker(tracked));
            assertEquals(bits, result.consumedBits());
            assertEquals(bits / 8 + 1, buffer.readerIndex());
            assertEquals(0x66, buffer.readUnsignedByte());
            assertEquals(7L, result.values().fields().get("prefix"));
            assertEquals(126L, result.values().fields().get("tail"));
            return result.values().fields().get("data");
        } finally {
            buffer.release();
        }
    }

    private static void assertEncodingRejected(DynamicField field, Object input, String message, boolean tracked) {
        final DynamicPlan plan = plan(field);
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            final CodecTracker tracker = tracker(tracked);
            final DynamicCodecException error = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of("prefix", 7, "data", input, "tail", 126)), buffer, tracker));
            assertLocation(error, message, 8, tracker);
            assertEquals(0x55, buffer.getUnsignedByte(0));
            assertEquals(0, buffer.readerIndex());
            assertEquals(1, buffer.refCnt());
        } finally {
            buffer.release();
        }
    }

    private static @Nullable CodecTracker tracker(boolean tracked) {
        return tracked ? new CodecTracker() : null;
    }

    private static void assertLocation(DynamicCodecException error, String message, long bitOffset, @Nullable CodecTracker tracker) {
        assertEquals("$.data", error.fieldPath());
        assertEquals(bitOffset, error.bitOffset());
        assertTrue(error.getMessage().contains(message), error::getMessage);
        if (tracker != null) {
            assertTrue(tracker.getTrace().getDiagnostics().stream()
                    .anyMatch(diagnostic -> "ERROR".equals(diagnostic.level()) && Long.valueOf(bitOffset).equals(diagnostic.bitOffset())));
        }
    }
}

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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态浮点数特殊值、字节序和失败时缓冲区状态测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicFloatBoundaryTest {
    static Stream<Arguments> finiteValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("big", "little").flatMap(endian -> Stream.of(
                Arguments.of(tracked, endian, 32, 0D, 0D, "00000000"),
                Arguments.of(tracked, endian, 32, -0D, -0D, "80000000"),
                Arguments.of(tracked, endian, 32, Float.MIN_VALUE, (double) Float.MIN_VALUE, "00000001"),
                Arguments.of(tracked, endian, 32, Float.MIN_NORMAL, (double) Float.MIN_NORMAL, "00800000"),
                Arguments.of(tracked, endian, 32, Float.MAX_VALUE, (double) Float.MAX_VALUE, "7f7fffff"),
                Arguments.of(tracked, endian, 32, -Float.MAX_VALUE, (double) -Float.MAX_VALUE, "ff7fffff"),
                Arguments.of(tracked, endian, 32, 0.1D, (double) 0.1F, "3dcccccd"),
                Arguments.of(tracked, endian, 64, 0D, 0D, "0000000000000000"),
                Arguments.of(tracked, endian, 64, -0D, -0D, "8000000000000000"),
                Arguments.of(tracked, endian, 64, Double.MIN_VALUE, Double.MIN_VALUE, "0000000000000001"),
                Arguments.of(tracked, endian, 64, Double.MIN_NORMAL, Double.MIN_NORMAL, "0010000000000000"),
                Arguments.of(tracked, endian, 64, Double.MAX_VALUE, Double.MAX_VALUE, "7fefffffffffffff"),
                Arguments.of(tracked, endian, 64, -Double.MAX_VALUE, -Double.MAX_VALUE, "ffefffffffffffff"))));
    }

    @ParameterizedTest
    @MethodSource("finiteValues")
    void preservesFiniteWireBitsIncludingSignedZeroAndSubnormalValues(
            boolean tracked, String endian, int bits, Number supplied, double expected, String bigHex) {

        final DynamicPlan plan = plan(bits, endian);
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracked ? new CodecTracker() : null;
        final CodecTracker decodeTracker = tracked ? new CodecTracker() : null;
        try {
            plan.encode(values(supplied), buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e" + wireHex(bigHex, endian) + "7f");
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decodeTracker);
            assertThat(result.values().fields()).containsEntry("head", 126L).containsEntry("tail", 127L);
            assertThat(result.values().fields().get("value")).isExactlyInstanceOf(Double.class);
            final double decoded = (Double) result.values().fields().get("value");
            assertThat(Double.doubleToRawLongBits(decoded)).isEqualTo(Double.doubleToRawLongBits(expected));
            assertThat(result.consumedBytes()).isEqualTo(bits / 8 + 2);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encodeTracker != null && decodeTracker != null) {
                assertThat(encodeTracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(decodeTracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("big", "little").flatMap(endian ->
                Stream.of(32, 64).flatMap(bits -> Stream.concat(
                        Stream.of(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, "1.5", null),
                        bits == 32 ? Stream.of(Double.MAX_VALUE, -Double.MAX_VALUE) : Stream.empty())
                        .map(value -> Arguments.of(tracked, endian, bits, value)))));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void rejectsNonFiniteOrNonNumericInputWithoutWritingTheFloat(
            boolean tracked, String endian, int bits, @Nullable Object supplied) {

        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan(bits, endian).encode(values(supplied), buffer, tracker));
            assertThat(error).hasMessageContaining("有限数值");
            assertThat(error.fieldPath()).isEqualTo("$.value");
            assertThat(error.bitOffset()).isEqualTo(8);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e");
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidWireValues() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("big", "little").flatMap(endian -> Stream.of(
                Arguments.of(tracked, endian, 32, "7fc00000"),
                Arguments.of(tracked, endian, 32, "7f800000"),
                Arguments.of(tracked, endian, 32, "ff800000"),
                Arguments.of(tracked, endian, 64, "7ff8000000000000"),
                Arguments.of(tracked, endian, 64, "7ff0000000000000"),
                Arguments.of(tracked, endian, 64, "fff0000000000000"))));
    }

    @ParameterizedTest
    @MethodSource("invalidWireValues")
    void rejectsNonFiniteWireValueWithoutCommittingCallerReaderIndex(boolean tracked, String endian, int bits, String bigHex) {
        final ByteBuf buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("557e" + wireHex(bigHex, endian) + "7f66"))
                .readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan(bits, endian).decode(buffer, tracker));
            assertThat(error).hasMessageContaining("有限数值");
            assertThat(error.fieldPath()).isEqualTo("$.value");
            assertThat(error.bitOffset()).isEqualTo(8 + bits);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("557e" + wireHex(bigHex, endian) + "7f66");
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(tracker.getTrace().getDiagnostics()).isNotEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    private static DynamicPlan plan(int bits, String endian) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), DynamicField.field("value", "float").bits(bits).endian(endian).build(),
                DynamicField.field("tail", "u8").build()));
    }

    private static DynamicValues values(@Nullable Object value) {
        final Map<String, Object> input = new HashMap<>();
        input.put("head", 126);
        input.put("value", value);
        input.put("tail", 127);
        return DynamicValues.of(input);
    }

    private static String wireHex(String bigHex, String endian) {
        if (endian.equals("big")) {
            return bigHex;
        }
        final StringBuilder result = new StringBuilder();
        for (int i = bigHex.length(); i > 0; i -= 2) {
            result.append(bigHex, i - 2, i);
        }
        return result.toString();
    }
}

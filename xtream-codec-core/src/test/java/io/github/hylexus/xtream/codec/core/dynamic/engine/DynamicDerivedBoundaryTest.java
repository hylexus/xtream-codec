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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 派生位段反向合成的补码、重叠和失败状态测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicDerivedBoundaryTest {
    static Stream<Arguments> signedViews() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("big", "little").flatMap(endian -> Stream.of(
                Arguments.of(tracked, endian, "i8", 7, 1, 0, 0L, 0L, "00"),
                Arguments.of(tracked, endian, "i8", 7, 1, 1, -128L, 1L, "80"),
                Arguments.of(tracked, endian, "i8", 0, 8, 127, 127L, 127L, "7f"),
                Arguments.of(tracked, endian, "i16", 12, 4, 8, -32768L, 8L, "8000"),
                Arguments.of(tracked, endian, "i16", 0, 16, 65535, -1L, 65535L, "ffff"),
                Arguments.of(tracked, endian, "i64", 63, 1, 1, "-9223372036854775808", 1L, "8000000000000000"),
                Arguments.of(tracked, endian, "i64", 0, 64, "18446744073709551615", -1L, "18446744073709551615", "ffffffffffffffff"))));
    }

    @ParameterizedTest
    @MethodSource("signedViews")
    void composesSignedSourceUsingItsDeclaredBitWidth(boolean tracked, String endian, String type, int offset, int width,
                                                     Object supplied, Object expectedSource, Object expectedView, String bigHex) {

        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("source", type).endian(endian).build(),
                view("view", offset, width),
                DynamicField.field("tail", "u8").build()));
        final String hex = endian.equals("little") ? reverseBytes(bigHex) : bigHex;
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encodeTracker = tracked ? new CodecTracker() : null;
        final CodecTracker decodeTracker = tracked ? new CodecTracker() : null;
        try {
            plan.encode(values(Map.of("view", supplied)), buffer, encodeTracker);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e" + hex + "7f");
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decodeTracker);
            assertThat(result.values().fields()).containsEntry("source", expectedSource).containsEntry("view", expectedView)
                    .containsEntry("head", 126L).containsEntry("tail", 127L);
            assertThat(result.consumedBytes()).isEqualTo(bigHex.length() / 2 + 2);
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

    static Stream<Arguments> consistentViews() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, Map.of("low", 12, "shifted", 3), 12L, "0c"),
                Arguments.of(tracked, Map.of("shifted", 3), 12L, "0c"),
                Arguments.of(tracked, Map.of("low", 15, "shifted", 3), 15L, "0f")));
    }

    @ParameterizedTest
    @MethodSource("consistentViews")
    void acceptsConsistentOverlapAndLeavesUnspecifiedBitsZero(boolean tracked, Map<String, Object> input, long expected, String hex) {
        final DynamicPlan plan = overlappingPlan();
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            plan.encode(values(input), buffer, tracked ? new CodecTracker() : null);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("557e" + hex + "7f");
            buffer.readerIndex(1);
            assertThat(plan.decode(buffer, tracked ? new CodecTracker() : null).values().fields())
                    .containsEntry("source", expected).containsEntry("low", expected).containsEntry("shifted", 3L);
            assertThat(buffer.readerIndex()).isEqualTo(4);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidViews() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, Map.of("low", -1), "位宽"),
                Arguments.of(tracked, Map.of("low", 16), "位宽"),
                Arguments.of(tracked, Map.of("shifted", 16), "位宽"),
                Arguments.of(tracked, Map.of("low", 0, "shifted", 1), "冲突")));
    }

    @ParameterizedTest
    @MethodSource("invalidViews")
    void rejectsInvalidOrConflictingViewsBeforeWritingSource(boolean tracked, Map<String, Object> input, String message) {
        assertEncodingFailure(overlappingPlan(), input, tracked, message);
    }

    static Stream<Arguments> explicitOutOfRangeSources() {
        return Stream.of(false, true).map(tracked -> Arguments.of(tracked, Map.of("source", 128, "view", 1)));
    }

    @ParameterizedTest
    @MethodSource("explicitOutOfRangeSources")
    void explicitSourceStillMustBeInSignedRange(boolean tracked, Map<String, Object> input) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), DynamicField.field("source", "i8").build(),
                view("view", 7, 1), DynamicField.field("tail", "u8").build()));
        assertEncodingFailure(plan, input, tracked, "范围");
    }

    private static void assertEncodingFailure(DynamicPlan plan, Map<String, Object> input, boolean tracked, String message) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(input), buffer, tracker));
            assertThat(error).hasMessageContaining(message);
            assertThat(error.fieldPath()).isEqualTo("$.source");
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

    private static DynamicPlan overlappingPlan() {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), DynamicField.field("source", "u8").build(),
                view("low", 0, 4), view("shifted", 2, 4), DynamicField.field("tail", "u8").build()));
    }

    private static DynamicField view(String id, int offset, int width) {
        return DynamicField.field(id, "derived").ref("source").options(Map.of("bitOffset", offset, "bitWidth", width)).build();
    }

    private static DynamicValues values(Map<String, Object> fields) {
        final Map<String, Object> input = new HashMap<>(fields);
        input.put("head", 126);
        input.put("tail", 127);
        return DynamicValues.of(input);
    }

    private static String reverseBytes(String hex) {
        final StringBuilder result = new StringBuilder();
        for (int i = hex.length(); i > 0; i -= 2) {
            result.append(hex, i - 2, i);
        }
        return result.toString();
    }
}

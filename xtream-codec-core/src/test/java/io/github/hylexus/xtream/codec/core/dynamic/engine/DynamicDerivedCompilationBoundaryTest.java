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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicBitOrder;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 派生位段编译时的范围溢出与跨字节限制测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicDerivedCompilationBoundaryTest {
    static Stream<Arguments> invalidRanges() {
        return Stream.of(false, true).flatMap(json -> Stream.of(
                Arguments.of(json, 64, 1),
                Arguments.of(json, 63, 2),
                Arguments.of(json, 0, 65),
                Arguments.of(json, Integer.MAX_VALUE, 1),
                Arguments.of(json, 64, Integer.MAX_VALUE),
                Arguments.of(json, Integer.MAX_VALUE, Integer.MAX_VALUE),
                Arguments.of(json, 0, Integer.MAX_VALUE),
                Arguments.of(json, Integer.MAX_VALUE - 7, 8)));
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    void rejectsOutOfSourceRangeEvenWhenOffsetPlusWidthOverflowsInt(boolean json, int offset, int width) {
        final DynamicSchema schema = schema("u64", "big", DynamicBitOrder.MSB0, offset, width);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                () -> DynamicCompiler.compile(imported(schema, json)));
        assertThat(error).hasMessageContaining("位于源整数范围内");
        assertThat(error.fieldPath()).isEqualTo("$.view");
        assertThat(error.bitOffset()).isEqualTo(-1);
    }

    static Stream<Arguments> invalidLittleEndianRanges() {
        return Stream.of(false, true).flatMap(json -> Stream.of(8, 12).map(width -> Arguments.of(json, width)));
    }

    @ParameterizedTest
    @MethodSource("invalidLittleEndianRanges")
    void rejectsPartialByteSpanningViewsForLittleEndianMsb0(boolean json, int width) {
        final DynamicSchema schema = schema("u16", "little", DynamicBitOrder.MSB0, 4, width);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                () -> DynamicCompiler.compile(imported(schema, json)));
        assertThat(error).hasMessageContaining("小端 msb0 位段跨字节");
        assertThat(error.fieldPath()).isEqualTo("$.view");
        assertThat(error.bitOffset()).isEqualTo(-1);
    }

    static Stream<Arguments> validRanges() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, "u64", "big", DynamicBitOrder.MSB0, 63, 1,
                        "9223372036854775808", 1L, "8000000000000000"),
                Arguments.of(tracked, "u64", "big", DynamicBitOrder.MSB0, 0, 64,
                        "18446744073709551615", "18446744073709551615", "ffffffffffffffff"),
                Arguments.of(tracked, "u16", "little", DynamicBitOrder.MSB0, 4, 4, 4660, 3L, "3412"),
                Arguments.of(tracked, "u16", "little", DynamicBitOrder.MSB0, 0, 16, 4660, 4660L, "3412"),
                Arguments.of(tracked, "u16", "little", DynamicBitOrder.LSB0, 4, 8, 4660, 35L, "3412"),
                Arguments.of(tracked, "u16", "big", DynamicBitOrder.MSB0, 4, 8, 4660, 35L, "1234")));
    }

    @ParameterizedTest
    @MethodSource("validRanges")
    void acceptsExactEndpointsAndSupportedCrossByteLayouts(boolean tracked, String type, String endian,
                                                          DynamicBitOrder order, int offset, int width,
                                                          Object source, Object expected, String hex) {

        final DynamicPlan plan = DynamicCompiler.compile(imported(schema(type, endian, order, offset, width), true));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(DynamicValues.of(Map.of("source", source, "view", expected)), buffer, encoder))
                    .isEqualTo(hex.length() * 4L);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).containsEntry("view", expected);
            assertThat(result.consumedBytes()).isEqualTo(hex.length() / 2);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                assertThat(encoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(decoder.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(decoder.getTrace().getRoot().getChildren().get(1).getValue()).isEqualTo(expected);
            }
        } finally {
            buffer.release();
        }
    }

    private static DynamicSchema schema(String type, String endian, DynamicBitOrder order, int offset, int width) {
        return DynamicSchema.of(
                DynamicField.field("source", type).endian(endian).bitOrder(order).build(),
                DynamicField.field("view", "derived").ref("source").options(Map.of("bitOffset", offset, "bitWidth", width)).build());
    }

    private static DynamicSchema imported(DynamicSchema schema, boolean json) {
        return json ? DynamicSchemaJson.read(DynamicSchemaJson.write(schema)) : schema;
    }
}

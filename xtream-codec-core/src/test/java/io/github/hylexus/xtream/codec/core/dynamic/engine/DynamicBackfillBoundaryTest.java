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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态长度回填的容量、完整字节边界和失败后复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicBackfillBoundaryTest {

    static Stream<Arguments> capacities() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, "byte", 0, 0, true),
                Arguments.of(tracked, "byte", 255, 255, true),
                Arguments.of(tracked, "byte", 256, 256, false),
                Arguments.of(tracked, "bit", 0, 0, true),
                Arguments.of(tracked, "bit", 31, 248, true),
                Arguments.of(tracked, "bit", 32, 256, false)));
    }

    @ParameterizedTest
    @MethodSource("capacities")
    void checksCapacityInDeclaredUnitsAndRecoversAfterOverflow(boolean tracked, String unit, int bytes, int units, boolean valid) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("size", "u8").computedLengthOf("body").options(Map.of("lengthUnit", unit)).build(),
                DynamicField.field("body", "hex").length(DynamicLength.ref("size", unit)).build(),
                DynamicField.field("tail", "u8").build()));
        final String body = "ab".repeat(bytes);
        final DynamicValues values = values(body);
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            if (valid) {
                assertThat(plan.encode(values, buffer, tracker)).isEqualTo((3L + bytes) * 8);
                final String expected = "7e" + "%02x".formatted(units) + body + "7f";
                assertThat(ByteBufUtil.hexDump(buffer, 2, buffer.writerIndex() - 2)).isEqualTo(expected);
                if (tracker != null) {
                    assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "size")
                            .singleElement().satisfies(node -> assertThat(node.getValue()).isEqualTo((long) units));
                }
                buffer.readerIndex(2).writeByte(0x66);
                final var result = plan.decode(buffer, tracked ? new CodecTracker() : null);
                assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "size", (long) units, "body", body, "tail", 127L));
                assertThat(result.consumedBits()).isEqualTo((3L + bytes) * 8);
                assertThat(result.remainingBytes()).isEqualTo(1);
                assertThat(buffer.readerIndex()).isEqualTo(5 + bytes);
                assertThat(buffer.getUnsignedByte(buffer.readerIndex())).isEqualTo((short) 0x66);
            } else {
                final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                        () -> plan.encode(values, buffer, tracker));
                assertFailure(error, (2L + bytes) * 8, tracker);
                assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa7e00" + body);
                assertThat(buffer.readerIndex()).isEqualTo(1);
                if (tracker != null) {
                    assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "size")
                            .singleElement().satisfies(node -> assertThat(node.getValue()).isEqualTo(0L));
                    assertThat(tracker.getTrace().getRoot().getChildren()).filteredOn("name", "body")
                            .singleElement().satisfies(node -> assertThat(node.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS));
                }
                assertRecovery(plan, unit, tracked);
            }
            assertThat(values.fields()).containsEntry("size", 999);
            assertThat(buffer.getUnsignedShort(0)).isEqualTo(0x55aa);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> partialBytes() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("byte", "bit").flatMap(unit -> Stream.of(
                Arguments.of(tracked, unit, 3, "e0"),
                Arguments.of(tracked, unit, 7, "fe"),
                Arguments.of(tracked, unit, 9, "ff80"),
                Arguments.of(tracked, unit, 255, "ff".repeat(31) + "fe"))));
    }

    @ParameterizedTest
    @MethodSource("partialBytes")
    void rejectsPartialByteMeasurementsButAllowsBitMeasurements(boolean tracked, String unit, int bits, String encoded) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("size", "u8").computedLengthOf("body").options(Map.of("lengthUnit", unit)).build(),
                DynamicField.field("body", "bits").bits(bits).build(),
                DynamicField.field("align", "align").alignTo(8).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicValues values = values("1".repeat(bits));
            if (unit.equals("byte")) {
                final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                        () -> plan.encode(values, buffer, tracker));
                assertFailure(error, 16L + bits, tracker);
                assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("557e00" + encoded);
                assertThat(buffer.readerIndex()).isEqualTo(1);
                if (tracker != null) {
                    assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("align");
                }
            } else {
                final long size = (bits + 7L) / 8 + 3;
                assertThat(plan.encode(values, buffer, tracker)).isEqualTo(size * 8);
                assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("557e" + "%02x".formatted(bits) + encoded + "7f");
                buffer.writeByte(0x66);
                final var result = plan.decode(buffer, tracked ? new CodecTracker() : null);
                assertThat(result.values().fields()).containsEntry("size", (long) bits).containsEntry("body", "1".repeat(bits))
                        .containsEntry("align", null).containsEntry("tail", 127L);
                assertThat(result.consumedBits()).isEqualTo(size * 8);
                assertThat(result.remainingBytes()).isEqualTo(1);
                assertThat(buffer.readerIndex()).isEqualTo(1 + size);
            }
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }

    private static DynamicValues values(String body) {
        return DynamicValues.of(Map.of("head", 126, "size", 999, "body", body, "tail", 127));
    }

    private static void assertFailure(DynamicCodecException error, long offset, @Nullable CodecTracker tracker) {
        assertThat(error).hasMessageContaining("回填长度超出整数位宽或完整字节");
        assertThat(error.fieldPath()).isEqualTo("$.body");
        assertThat(error.bitOffset()).isEqualTo(offset);
        if (tracker != null) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getRoot().getBitEnd()).isEqualTo(offset);
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("tail");
            assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.level()).isEqualTo("ERROR");
                assertThat(diagnostic.bitOffset()).isEqualTo(offset);
            });
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    private static void assertRecovery(DynamicPlan plan, String unit, boolean tracked) {
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker tracker = tracked ? new CodecTracker() : null;
            assertThat(plan.encode(values("ab"), buffer, tracker)).isEqualTo(32);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo(unit.equals("byte") ? "7e01ab7f" : "7e08ab7f");
            assertThat(plan.decode(buffer).values().fields()).containsEntry("body", "ab").containsEntry("tail", 127L);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            }
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }
}

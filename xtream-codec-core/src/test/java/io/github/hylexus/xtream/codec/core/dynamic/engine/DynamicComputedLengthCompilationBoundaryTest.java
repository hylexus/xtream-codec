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
 * 回填区间的同级、顺序及端点编译校验与合法编解码测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicComputedLengthCompilationBoundaryTest {
    static Stream<Arguments> invalidRanges() {
        return Stream.of(false, true).flatMap(json -> Stream.of(
                "missing", "missing..last", "first..missing", "before", "size",
                "before..last", "first..size", "last..first", "outside").map(range -> Arguments.of(json, range)));
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    void requiresExistingOrderedSiblingEndpointsAfterTheLengthField(boolean json, String range) {
        final DynamicSchema schema = schema(range, json);
        final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
        assertThat(error).hasMessageContaining("长度区间必须指向当前字段之后的有序字段");
        assertThat(error.fieldPath()).isEqualTo("$.body.size");
        assertThat(error.bitOffset()).isEqualTo(-1);
    }

    static Stream<Arguments> validRanges() {
        return Stream.of(false, true).flatMap(json -> Stream.of(false, true).flatMap(tracked ->
                Stream.of("first", "first..first", "first..last").map(range -> Arguments.of(json, tracked, range))));
    }

    @ParameterizedTest
    @MethodSource("validRanges")
    void compilesValidSingleFieldAndContiguousRangesAndBackfillsTheirActualLength(boolean json, boolean tracked, String range) {
        final DynamicPlan plan = DynamicCompiler.compile(schema(range, json));
        final int length = range.equals("first..last") ? 3 : 1;
        final String wire = "7e55" + "%02x".formatted(length) + "1122337f";
        final DynamicValues supplied = DynamicValues.of(Map.of("outside", 126,
                "body", Map.of("before", 85, "first", 17, "last", 0x2233), "tail", 127));
        final Map<String, Object> expected = Map.of("outside", 126L,
                "body", Map.of("before", 85L, "size", (long) length, "first", 17L, "last", 0x2233L), "tail", 127L);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(wire)).writeByte(0x66).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(supplied, output, encoder)).isEqualTo(56L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(9);
            final DynamicDecodeResult result = plan.decode(input, decoder);
            assertThat(result.values().fields()).isEqualTo(expected);
            assertThat(result.consumedBits()).isEqualTo(56L);
            assertThat(result.consumedBytes()).isEqualTo(7);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(9);
            assertThat(input.getUnsignedByte(9)).isEqualTo((short) 0x66);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo("55aa" + wire + "66");
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracked) {
                for (final CodecTracker tracker : new CodecTracker[]{encoder, decoder}) {
                    assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                    final var size = tracker.getTrace().getRoot().getChildren().get(1).getChildren().get(1);
                    assertThat(size.getPath()).isEqualTo("body.size");
                    assertThat(size.getValue()).isEqualTo((long) length);
                    assertThat(size.getBitStart()).isEqualTo(16L);
                    assertThat(size.getBitEnd()).isEqualTo(24L);
                }
            }
        } finally {
            input.release();
            output.release();
        }
    }

    private static DynamicSchema schema(String range, boolean json) {
        final DynamicSchema schema = DynamicSchema.of(
                DynamicField.field("outside", "u8").build(),
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("before", "u8").build(),
                        DynamicField.field("size", "u8").computedLengthOf(range).build(),
                        DynamicField.field("first", "u8").build(),
                        DynamicField.field("last", "u16").build()).build(),
                DynamicField.field("tail", "u8").build());
        return json ? DynamicSchemaJson.read(DynamicSchemaJson.write(schema)) : schema;
    }
}

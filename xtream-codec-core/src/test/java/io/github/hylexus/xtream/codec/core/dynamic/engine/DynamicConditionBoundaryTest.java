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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceView;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态条件的等值边界、多位掩码和相邻数组项局部状态测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicConditionBoundaryTest {
    static Stream<Arguments> conditions() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, "gt", "7", List.of(8, 7, 6, 8), List.of(true, false, false, true),
                        "7e081200a007a106a2081203a3beef"),
                Arguments.of(tracked, "gte", "7", List.of(7, 6, 8, 7), List.of(true, false, true, true),
                        "7e071200a006a1081202a2071203a3beef"),
                Arguments.of(tracked, "lt", "7", List.of(6, 7, 8, 6), List.of(true, false, false, true),
                        "7e061200a007a108a2061203a3beef"),
                Arguments.of(tracked, "lte", "7", List.of(7, 8, 6, 7), List.of(true, false, true, true),
                        "7e071200a008a1061202a2071203a3beef"),
                Arguments.of(tracked, "bitClear", "6", List.of(1, 2, 4, 6, 0, 1, 7), List.of(true, false, false, false, true, true, false),
                        "7e011200a002a104a206a3001204a4011205a507a6beef"),
                Arguments.of(tracked, "bitSet", "6", List.of(6, 2, 4, 0, 7, 6), List.of(true, false, false, false, true, true),
                        "7e061200a002a104a200a3071204a4061205a5beef")));
    }

    @ParameterizedTest
    @MethodSource("conditions")
    void conditionsControlBytesAndStayLocalToEachArrayItem(boolean tracked, String operator, String expected,
                                                          List<Integer> sources, List<Boolean> present, String wire) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.fixed(sources.size(), "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("source", "u8").build(),
                                DynamicField.field("value", "u16").when(new DynamicCondition("source", operator, expected)).build(),
                                DynamicField.field("after", "u8").build()).build()).build(),
                DynamicField.field("tail", "u16").build()));
        // 被跳过项仍提供不同的 value，确认条件决定布局，不会读取或沿用这些值。
        final List<Map<String, Object>> supplied = IntStream.range(0, sources.size()).mapToObj(index -> Map.<String, Object>of(
                "source", sources.get(index), "value", 0x1200 + index, "after", 0xa0 + index)).toList();
        final List<Map<String, Object>> decodedItems = IntStream.range(0, sources.size()).mapToObj(index -> present.get(index)
                ? Map.<String, Object>of("source", sources.get(index).longValue(), "value", 0x1200L + index, "after", 0xa0L + index)
                : Map.<String, Object>of("source", sources.get(index).longValue(), "after", 0xa0L + index)).toList();
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.buffer().writeShort(0x55aa).writeBytes(ByteBufUtil.decodeHexDump(wire)).writeByte(0x66).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(DynamicValues.of(Map.of("head", 126, "items", supplied, "tail", 0xbeef)), output, encoder))
                    .isEqualTo(wire.length() * 4L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            final DynamicDecodeResult result = plan.decode(input, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "items", decodedItems, "tail", 0xbeefL));
            assertThat(result.consumedBits()).isEqualTo(wire.length() * 4L);
            assertThat(result.consumedBytes()).isEqualTo(wire.length() / 2);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            assertThat(ByteBufUtil.hexDump(input, 0, input.writerIndex())).isEqualTo("55aa" + wire + "66");
            assertThat(output.refCnt()).isEqualTo(1);
            assertThat(input.refCnt()).isEqualTo(1);
            assertTrace(encoder, present, wire);
            assertTrace(decoder, present, wire);
        } finally {
            output.release();
            input.release();
        }
    }

    private static void assertTrace(@Nullable CodecTracker tracker, List<Boolean> present, String wire) {
        if (tracker == null) {
            return;
        }
        final CodecTraceView.Node root = tracker.toTraceView().root();
        assertThat(root.status()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(root.byteStart()).isZero();
        assertThat(root.byteEnd()).isEqualTo(wire.length() / 2);
        assertThat(root.children()).extracting(CodecTraceView.Node::name).containsExactly("head", "items", "tail");
        final CodecTraceView.Node items = root.children().get(1);
        assertThat(items.children()).hasSize(present.size());
        int position = 1;
        for (int index = 0; index < present.size(); index++) {
            final CodecTraceView.Node item = items.children().get(index);
            final boolean included = present.get(index);
            final int end = position + (included ? 4 : 2);
            assertThat(item.status()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(item.byteStart()).isEqualTo(position);
            assertThat(item.byteEnd()).isEqualTo(end);
            assertThat(item.children()).extracting(CodecTraceView.Node::name)
                    .containsExactly(included ? new String[]{"source", "value", "after"} : new String[]{"source", "after"});
            assertThat(item.children()).allSatisfy(node -> assertThat(node.status()).isEqualTo(CodecTraceStatus.SUCCESS));
            if (included) {
                final CodecTraceView.Node value = item.children().get(1);
                assertThat(value.byteStart()).isEqualTo(position + 1);
                assertThat(value.byteEnd()).isEqualTo(position + 3);
                assertThat(value.hex()).isEqualTo(String.format("%04x", 0x1200 + index));
            }
            final CodecTraceView.Node after = item.children().getLast();
            assertThat(after.byteStart()).isEqualTo(end - 1);
            assertThat(after.byteEnd()).isEqualTo(end);
            assertThat(after.hex()).isEqualTo(String.format("%02x", 0xa0 + index));
            position = end;
        }
        assertThat(items.byteStart()).isEqualTo(1);
        assertThat(items.byteEnd()).isEqualTo(position);
        assertThat(root.children().getLast().byteStart()).isEqualTo(position);
        assertThat(root.children().getLast().hex()).isEqualTo("beef");
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.isNotTracing()).isTrue();
    }
}

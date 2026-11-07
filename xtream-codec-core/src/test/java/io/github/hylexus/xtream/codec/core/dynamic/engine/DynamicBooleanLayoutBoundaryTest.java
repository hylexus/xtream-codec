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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
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
 * 布尔表达式的右侧求值、取反结果和相邻数组项条件布局测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicBooleanLayoutBoundaryTest {
    static Stream<Arguments> operations() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, "or", List.of(false, true, false, true), "7e00a0011201a100a2011203a37f"),
                Arguments.of(tracked, "not", List.of(true, false, true, false), "7e001200a001a1001202a201a37f")));
    }

    @ParameterizedTest
    @MethodSource("operations")
    void booleanResultsControlBytesAndRemainLocalToEachItem(boolean tracked, String operator, List<Boolean> enabled, String wire) {
        final DynamicExpression operand = DynamicExpression.op("eq", DynamicExpression.ref("source"), DynamicExpression.value(1));
        final DynamicExpression expression = operator.equals("or")
                ? DynamicExpression.op("or", DynamicExpression.value(false), operand) : DynamicExpression.op("not", operand);
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("items", "array").length(DynamicLength.fixed(4, "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("source", "u8").build(),
                                DynamicField.field("enabled", "derived").expression(expression).build(),
                                DynamicField.field("value", "u16").when(new DynamicCondition("enabled", "eq", "true")).build(),
                                DynamicField.field("after", "u8").build()).build()).build(),
                DynamicField.field("tail", "u8").build()));
        // 每项都提供不同的 value，验证条件跳过时既不写入，也不沿用前一项的值。
        final List<Map<String, Object>> supplied = IntStream.range(0, 4).mapToObj(index -> Map.<String, Object>of(
                "source", index % 2, "value", 0x1200 + index, "after", 0xa0 + index)).toList();
        final List<Map<String, Object>> expected = IntStream.range(0, 4).mapToObj(index -> enabled.get(index)
                ? Map.<String, Object>of("source", (long) (index % 2), "enabled", true, "value", 0x1200L + index, "after", 0xa0L + index)
                : Map.<String, Object>of("source", (long) (index % 2), "enabled", false, "after", 0xa0L + index)).toList();
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("55aa" + wire + "66")).readerIndex(2);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(DynamicValues.of(Map.of("head", 126, "items", supplied, "tail", 127)), output, encoder))
                    .isEqualTo(wire.length() * 4L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + wire);
            assertThat(output.readerIndex()).isEqualTo(1);
            final DynamicDecodeResult result = plan.decode(input, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "items", expected, "tail", 127L));
            assertThat(result.consumedBits()).isEqualTo(wire.length() * 4L);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(input.readerIndex()).isEqualTo(2 + wire.length() / 2);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            assertTrace(encoder, enabled, wire);
            assertTrace(decoder, enabled, wire);
        } finally {
            input.release();
            output.release();
        }
    }

    private static void assertTrace(@Nullable CodecTracker tracker, List<Boolean> enabled, String wire) {
        if (tracker == null) {
            return;
        }
        final CodecTraceView.Node root = tracker.toTraceView().root();
        assertThat(root.status()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(root.byteStart()).isZero();
        assertThat(root.byteEnd()).isEqualTo(wire.length() / 2);
        final var items = root.children().get(1).children();
        assertThat(items).hasSize(4);
        int position = 1;
        for (int i = 0; i < 4; i++) {
            final var children = items.get(i).children();
            assertThat(children).extracting(CodecTraceView.Node::name).containsExactlyElementsOf(enabled.get(i)
                    ? List.of("source", "enabled", "value", "after") : List.of("source", "enabled", "after"));
            assertThat(items.get(i).byteStart()).isEqualTo(position);
            final var derived = children.get(1);
            assertThat(derived.byteStart()).isEqualTo(position + 1);
            assertThat(derived.byteEnd()).isEqualTo(position + 1);
            assertThat(derived.value()).isEqualTo(enabled.get(i));
            if (enabled.get(i)) {
                final var value = children.get(2);
                assertThat(value.byteStart()).isEqualTo(position + 1);
                assertThat(value.byteEnd()).isEqualTo(position + 3);
                assertThat(value.hex()).isEqualTo(wire.substring((position + 1) * 2, (position + 3) * 2));
            }
            position += enabled.get(i) ? 4 : 2;
            assertThat(items.get(i).byteEnd()).isEqualTo(position);
            assertThat(children.getLast().byteStart()).isEqualTo(position - 1);
            assertThat(children.getLast().hex()).isEqualTo(wire.substring((position - 1) * 2, position * 2));
        }
        assertThat(root.children().getLast().byteStart()).isEqualTo(position);
        assertThat(root.children().getLast().hex()).isEqualTo("7f");
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.isNotTracing()).isTrue();
    }
}

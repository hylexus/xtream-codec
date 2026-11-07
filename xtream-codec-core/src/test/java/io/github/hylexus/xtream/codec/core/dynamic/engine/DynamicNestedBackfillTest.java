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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 嵌套回填值向后续表达式、容器值及跟踪节点传递的测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicNestedBackfillTest {
    static Stream<Arguments> conditionalRanges() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 0, 3L, "0003112233037f"),
                Arguments.of(tracked, 1, 4L, "010411aa2233047f")));
    }

    @ParameterizedTest
    @MethodSource("conditionalRanges")
    void exposesFinalNestedLengthToContainerCopyAndGeneratedExpression(boolean tracked, int flags, long size, String hex) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("flags", "u8").build(),
                        DynamicField.field("size", "u8").computedLengthOf("first..last").build(),
                        DynamicField.field("first", "u8").build(),
                        DynamicField.field("optional", "hex").length(DynamicLength.fixed(1, "byte"))
                                .when(new DynamicCondition("flags", "bitSet", "1")).build(),
                        DynamicField.field("last", "u16").build()).build(),
                DynamicField.field("copy", "derived").expression(DynamicExpression.ref("body")).build(),
                DynamicField.field("echo", "u8").generated(DynamicGeneratedValue.expression(DynamicExpression.ref("body.size"))).build(),
                DynamicField.field("tail", "u8").build()));
        final Map<String, Object> body = Map.of("flags", flags, "size", 99, "first", 17, "optional", "aa", "last", 8755);
        final Map<String, Object> expected = flags == 0
                ? Map.of("flags", 0L, "size", 3L, "first", 17L, "last", 8755L)
                : Map.of("flags", 1L, "size", 4L, "first", 17L, "optional", "aa", "last", 8755L);
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            plan.encode(DynamicValues.of(Map.of("body", body, "tail", 127)), buffer, encoder);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("body", expected, "copy", expected, "echo", size, "tail", 127L));
            assertThat(body).containsEntry("size", 99).containsEntry("optional", "aa");
            assertConsumed(buffer, result, hex.length() / 2);
            assertCopiedTrace(encoder, "body", expected);
            assertCopiedTrace(decoder, "body", expected);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    final var fields = tracker.getTrace().getRoot().getChildren();
                    final var length = fields.get(0).getChildren().get(1);
                    assertThat(length.getValue()).isEqualTo(size);
                    assertThat(length.getBitStart()).isEqualTo(8L);
                    assertThat(length.getBitEnd()).isEqualTo(16L);
                    assertThat(fields.get(2).getValue()).isEqualTo(size);
                }
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void isolatesEachArrayElementsBackfillIncludingEmptyContent(boolean tracked) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("groups", "array").length(DynamicLength.fixed(3, "item"))
                        .item(DynamicField.field("group", "struct").fields(
                                DynamicField.field("size", "u8").computedLengthOf("payload").build(),
                                DynamicField.field("payload", "hex").length(DynamicLength.ref("size", "byte")).build(),
                                DynamicField.field("echo", "derived").expression(DynamicExpression.ref("size")).build()).build()).build(),
                DynamicField.field("copy", "derived").expression(DynamicExpression.ref("groups")).build(),
                DynamicField.field("tail", "u8").build()));
        final List<Map<String, Object>> groups = List.of(
                Map.of("size", 99, "payload", "aa"), Map.of("size", 88, "payload", ""), Map.of("size", 77, "payload", "bbcc"));
        final List<Map<String, Object>> expected = List.of(
                Map.of("size", 1L, "payload", "aa", "echo", 1L), Map.of("size", 0L, "payload", "", "echo", 0L),
                Map.of("size", 2L, "payload", "bbcc", "echo", 2L));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            plan.encode(DynamicValues.of(Map.of("groups", groups, "tail", 127)), buffer, encoder);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("5501aa0002bbcc7f");
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("groups", expected, "copy", expected, "tail", 127L));
            assertThat(groups).extracting(group -> group.get("size")).containsExactly(99, 88, 77);
            assertConsumed(buffer, result, 7);
            assertCopiedTrace(encoder, "groups", expected);
            assertCopiedTrace(decoder, "groups", expected);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    final var elements = tracker.getTrace().getRoot().getChildren().getFirst().getChildren();
                    assertThat(elements).extracting(CodecTraceNode::getValue).containsExactlyElementsOf(expected);
                    assertThat(elements).extracting(CodecTraceNode::getBitStart).containsExactly(0L, 16L, 24L);
                    assertThat(elements).extracting(CodecTraceNode::getBitEnd).containsExactly(16L, 24L, 48L);
                    assertThat(elements).extracting(element -> element.getChildren().getFirst().getValue()).containsExactly(1L, 0L, 2L);
                }
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> skippedLengths() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, 0, "007f", Map.of("flags", 0L)),
                Arguments.of(tracked, 1, "0101aa7f", Map.of("flags", 1L, "size", 1L, "payload", "aa"))));
    }

    @ParameterizedTest
    @MethodSource("skippedLengths")
    void skippedLengthIsNotReintroducedIntoCopiedContainer(boolean tracked, int flags, String hex, Map<String, Object> expected) {
        final DynamicCondition present = new DynamicCondition("flags", "bitSet", "1");
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").fields(
                        DynamicField.field("flags", "u8").build(),
                        DynamicField.field("size", "u8").computedLengthOf("payload").when(present).build(),
                        DynamicField.field("payload", "hex").length(DynamicLength.fixed(1, "byte")).when(present).build()).build(),
                DynamicField.field("copy", "derived").expression(DynamicExpression.ref("body")).build(),
                DynamicField.field("tail", "u8").build()));
        final Map<String, Object> body = Map.of("flags", flags, "size", 99, "payload", "aa");
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            plan.encode(DynamicValues.of(Map.of("body", body, "tail", 127)), buffer, encoder);
            assertThat(ByteBufUtil.hexDump(buffer)).isEqualTo("55" + hex);
            buffer.writeByte(0x66).readerIndex(1);
            final var result = plan.decode(buffer, decoder);
            assertThat(result.values().fields()).isEqualTo(Map.of("body", expected, "copy", expected, "tail", 127L));
            assertThat(body).containsEntry("size", 99).containsEntry("payload", "aa");
            assertConsumed(buffer, result, hex.length() / 2);
            assertCopiedTrace(encoder, "body", expected);
            assertCopiedTrace(decoder, "body", expected);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    assertThat(tracker.getTrace().getRoot().getChildren().getFirst().getChildren())
                            .extracting(CodecTraceNode::getName)
                            .containsExactlyElementsOf(flags == 0 ? List.of("flags") : List.of("flags", "size", "payload"));
                }
            }
        } finally {
            buffer.release();
        }
    }

    private static void assertCopiedTrace(@Nullable CodecTracker tracker, String source, Object expected) {
        if (tracker != null) {
            final var root = tracker.getTrace().getRoot();
            assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
            assertThat(root.getChildren().get(0).getName()).isEqualTo(source);
            assertThat(root.getChildren().get(0).getValue()).isEqualTo(expected);
            assertThat(root.getChildren().get(1).getName()).isEqualTo("copy");
            assertThat(root.getChildren().get(1).getValue()).isEqualTo(expected);
            assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        }
    }

    private static void assertConsumed(ByteBuf buffer, DynamicDecodeResult result, int bytes) {
        assertThat(result.consumedBytes()).isEqualTo(bytes);
        assertThat(result.remainingBytes()).isEqualTo(1);
        assertThat(buffer.readerIndex()).isEqualTo(1 + bytes);
        assertThat(buffer.readUnsignedByte()).isEqualTo((short) 0x66);
        assertThat(buffer.refCnt()).isEqualTo(1);
    }
}

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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态集合按项计数前缀的容量、条目顺序和异常后复用测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicItemPrefixBoundaryTest {

    static Stream<Arguments> capacities() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("array", "tlv", "map", "pair")
                .flatMap(type -> Stream.of(0, 255, 256).map(count -> Arguments.of(tracked, type, count))));
    }

    @ParameterizedTest
    @MethodSource("capacities")
    void checksItemCountBeforeWritingPrefixOrElements(boolean tracked, String type, int count) {
        final DynamicField.Builder items = DynamicField.field("items", type).length(DynamicLength.prefix(8, "item"));
        if (type.equals("array")) {
            items.item(DynamicField.field("item", "u16").build());
        } else {
            items.variants(Map.of("1", DynamicField.field("value", "u16").build()));
        }
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), items.build(), DynamicField.field("tail", "u8").build()));
        if (count < 256) {
            assertRoundTrip(plan, type, count, tracked);
            return;
        }

        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(type, count), output, tracker));
            assertThat(error).hasMessageContaining("长度超出前缀范围");
            assertThat(error.fieldPath()).isEqualTo("$.items");
            assertThat(error.bitOffset()).isEqualTo(8L);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa7e");
            assertThat(output.writerIndex()).isEqualTo(3);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getBitEnd()).isEqualTo(8L);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "items");
                assertThat(root.getChildren().getFirst().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                final CodecTraceNode failed = root.getChildren().getLast();
                assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(failed.getBitStart()).isEqualTo(8L);
                assertThat(failed.getBitEnd()).isEqualTo(8L);
                assertThat(failed.getChildren()).isEmpty();
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.level()).isEqualTo("ERROR");
                    assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
                    assertThat(diagnostic.byteOffset()).isEqualTo(1);
                    assertThat(diagnostic.bitOffset()).isEqualTo(8L);
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
            assertRoundTrip(plan, type, 2, tracked);
        } finally {
            output.release();
        }
    }

    private static DynamicValues values(String type, int count) {
        final List<?> items = IntStream.range(0, count).mapToObj(index -> type.equals("array")
                ? (Object) (0x1200L + index) : Map.of("key", 1L, "value", 0x1200L + index)).toList();
        return DynamicValues.of(Map.of("head", 126L, "items", items, "tail", 127L));
    }

    private static void assertRoundTrip(DynamicPlan plan, String type, int count, boolean tracked) {
        final String header = switch (type) {
            case "array" -> "";
            case "pair" -> "01";
            default -> "0102";
        };
        final String body = IntStream.range(0, count).mapToObj(index -> header + "12%02x".formatted(index))
                .collect(Collectors.joining());
        final String expected = "7e" + "%02x".formatted(count) + body + "7f";
        final long bits = expected.length() * 4L;
        final DynamicValues values = values(type, count);
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker encoder = tracked ? new CodecTracker() : null;
        final CodecTracker decoder = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(values, buffer, encoder)).isEqualTo(bits);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(buffer.readerIndex()).isEqualTo(1);
            buffer.writeByte(0x66).readerIndex(2);
            final var decoded = plan.decode(buffer, decoder);
            assertThat(decoded.values().fields()).isEqualTo(values.fields());
            assertThat(decoded.consumedBits()).isEqualTo(bits);
            assertThat(decoded.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(2 + expected.length() / 2);
            assertThat(buffer.getUnsignedByte(buffer.readerIndex())).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (encoder != null && decoder != null) {
                for (final CodecTracker tracker : List.of(encoder, decoder)) {
                    final CodecTraceNode root = tracker.getTrace().getRoot();
                    assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(root.getBitEnd()).isEqualTo(bits);
                    assertThat(root.getChildren()).extracting(CodecTraceNode::getName)
                            .containsExactly("head", "prependLengthField(items)", "items", "tail");
                    final CodecTraceNode prefix = root.getChildren().get(1);
                    assertThat(prefix.getKind()).isEqualTo(CodecTraceNodeKind.LENGTH_FIELD);
                    assertThat(prefix.getValue()).isEqualTo((long) count);
                    assertThat(prefix.getAttributes()).containsEntry("unit", "item");
                    assertThat(prefix.getBitStart()).isEqualTo(8L);
                    assertThat(prefix.getBitEnd()).isEqualTo(16L);
                    assertThat(root.getChildren().get(2).getChildren()).hasSize(count);
                    assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                    assertThat(tracker.isNotTracing()).isTrue();
                }
            }
        } finally {
            buffer.release();
        }
    }
}

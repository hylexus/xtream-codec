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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * TLV 头部写入的资源限制、父容器边界和跟踪失败状态测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicEntryHeaderFailureTest {

    static Stream<Arguments> boundaries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(8, 16, 32).flatMap(width ->
                Stream.of("output", "parent").flatMap(boundary -> Stream.of("Tag", "Length")
                        .map(part -> Arguments.of(tracked, width, boundary, part)))));
    }

    @ParameterizedTest
    @MethodSource("boundaries")
    void rejectsIncompleteHeaderAndRetainsCompletedParts(boolean tracked, int width, String boundary, String part) {
        final DynamicPlan plan = plan(width, boundary.equals("parent"));
        final int capacity = part.equals("Tag") ? width / 8 - 1 : width / 4 - 1;
        final int bodyLength = width / 4 + 1;
        final int declared = boundary.equals("parent") ? capacity : bodyLength;
        final DynamicLimits limits = boundary.equals("output")
                ? new DynamicLimits(64, 2 + capacity, 8, 8) : DynamicLimits.DEFAULT;
        final long offset = part.equals("Tag") ? 16L : 16L + width;
        final String key = "0".repeat(width / 4 - 1) + "1";
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(declared), output, limits, tracker));
            assertThat(error).hasMessageContaining("输出超过边界或资源上限");
            assertThat(error.fieldPath()).isEqualTo("$.body.items");
            assertThat(error.bitOffset()).isEqualTo(offset);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex()))
                    .isEqualTo("55aa7e" + "%02x".formatted(declared) + (part.equals("Tag") ? "" : key));
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.writerIndex()).isEqualTo(2 + (int) (offset / 8));
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracker != null) {
                final CodecTraceNode root = tracker.getTrace().getRoot();
                assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(root.getBitEnd()).isEqualTo(offset);
                assertThat(root.getChildren()).extracting(CodecTraceNode::getName).containsExactly("head", "bound", "body");
                assertThat(root.getChildren().get(0).getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(root.getChildren().get(1).getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                final CodecTraceNode body = root.getChildren().get(2);
                assertThat(body.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                final CodecTraceNode items = body.getChildren().getFirst();
                assertThat(items.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(items.getChildren()).hasSize(1);
                final CodecTraceNode entry = items.getChildren().getFirst();
                assertThat(entry.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(entry.getChildren()).extracting(CodecTraceNode::getName)
                        .containsExactlyElementsOf(part.equals("Tag") ? List.of("Tag") : List.of("Tag", "Length"));
                final CodecTraceNode failed = entry.getChildren().getLast();
                assertThat(failed.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
                assertThat(failed.getValue()).isNull();
                assertThat(failed.getBitStart()).isEqualTo(offset);
                assertThat(failed.getBitEnd()).isEqualTo(offset);
                if (part.equals("Length")) {
                    final CodecTraceNode tag = entry.getChildren().getFirst();
                    assertThat(tag.getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                    assertThat(tag.getValue()).isEqualTo(1L);
                    assertThat(tag.getBitStart()).isEqualTo(16L);
                    assertThat(tag.getBitEnd()).isEqualTo(offset);
                }
                assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.level()).isEqualTo("ERROR");
                    assertThat(diagnostic.nodeId()).isEqualTo(failed.getId());
                    assertThat(diagnostic.bitOffset()).isEqualTo(offset);
                    assertThat(diagnostic.byteOffset()).isEqualTo((int) (offset / 8));
                });
                assertThat(tracker.isNotTracing()).isTrue();
            }
            assertFitsExactly(plan, width, tracked);
        } finally {
            output.release();
        }
    }

    private static DynamicPlan plan(int width, boolean bounded) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("bound", "u8").build(),
                DynamicField.field("body", "struct").length(bounded ? DynamicLength.ref("bound", "byte") : null).fields(
                        DynamicField.field("items", "tlv").length(DynamicLength.fixed(1, "item"))
                                .options(Map.of("keyBits", width, "lengthBits", width))
                                .variants(Map.of("1", DynamicField.field("value", "u8").build())).build()).build(),
                DynamicField.field("tail", "u8").build()));
    }

    private static DynamicValues values(int bound) {
        return DynamicValues.of(Map.of("head", 126, "bound", bound,
                "body", Map.of("items", List.of(Map.of("key", 1, "value", 171))), "tail", 127));
    }

    private static void assertFitsExactly(DynamicPlan plan, int width, boolean tracked) {
        final int bodyLength = width / 4 + 1;
        final int length = bodyLength + 3;
        final DynamicLimits limits = new DynamicLimits(length + 1, length, 8, 8);
        final ByteBuf buffer = Unpooled.buffer().writeShort(0x55aa).readerIndex(2);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            assertThat(plan.encode(values(bodyLength), buffer, limits, tracker)).isEqualTo(length * 8L);
            final String number = "0".repeat(width / 4 - 1) + "1";
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex()))
                    .isEqualTo("55aa7e" + "%02x".formatted(bodyLength) + number + number + "ab7f");
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
            }
            buffer.writeByte(0x66);
            final var result = plan.decode(buffer, limits, tracked ? new CodecTracker() : null);
            assertThat(result.values().fields()).isEqualTo(Map.of("head", 126L, "bound", (long) bodyLength,
                    "body", Map.of("items", List.of(Map.of("key", 1L, "value", 171L))), "tail", 127L));
            assertThat(result.consumedBits()).isEqualTo(length * 8L);
            assertThat(result.remainingBytes()).isEqualTo(1);
            assertThat(buffer.readerIndex()).isEqualTo(2 + length);
            assertThat(buffer.getUnsignedByte(buffer.readerIndex())).isEqualTo((short) 0x66);
            assertThat(buffer.refCnt()).isEqualTo(1);
        } finally {
            buffer.release();
        }
    }
}

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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicGeneratedValue;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 位串和集合计数的非法输入、部分输出及后续调用恢复测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicInvalidInputTest {
    static Stream<Arguments> invalidBits() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("msb0", "lsb0").flatMap(order -> Stream.of(
                Arguments.of(tracked, order, true, null, 8L, "", ""),
                Arguments.of(tracked, order, false, null, 8L, "", ""),
                Arguments.of(tracked, order, false, 12, 8L, "", ""),
                Arguments.of(tracked, order, false, true, 8L, "", ""),
                Arguments.of(tracked, order, false, "", 8L, "", ""),
                Arguments.of(tracked, order, false, "101", 8L, "", ""),
                Arguments.of(tracked, order, false, "101010101", 8L, "", ""),
                Arguments.of(tracked, order, false, "x0101010", 8L, "", ""),
                Arguments.of(tracked, order, false, "10x00000", 10L, "80", "01"),
                Arguments.of(tracked, order, false, "1010101x", 15L, "aa", "55"))));
    }

    @ParameterizedTest
    @MethodSource("invalidBits")
    void rejectsInvalidBitsAtTheFirstInvalidPositionAndAllowsLaterCalls(
            boolean tracked, String order, boolean missing, @Nullable Object supplied, long offset, String msbHex, String lsbHex) {

        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("value", "bits").bits(8).bitOrder(order).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        final String partialHex = "7e" + (order.equals("msb0") ? msbHex : lsbHex);
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values("value", supplied, missing), buffer, tracker));
            assertThat(error).hasMessageContaining("bits");
            assertFailure(error, "$.value", offset, partialHex, buffer, tracker);

            buffer.clear().writeByte(0x55).readerIndex(1);
            final CodecTracker retryTracker = tracked ? new CodecTracker() : null;
            assertThat(plan.encode(values("value", "10101010", false), buffer, retryTracker)).isEqualTo(24);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex()))
                    .isEqualTo(order.equals("msb0") ? "557eaa7f" : "557e557f");
            assertThat(buffer.readerIndex()).isEqualTo(1);
            final var decoded = plan.decode(buffer, tracked ? new CodecTracker() : null);
            assertThat(decoded.values().fields()).isEqualTo(Map.of("head", 126L, "value", "10101010", "tail", 127L));
            assertThat(decoded.consumedBytes()).isEqualTo(3);
            assertThat(decoded.remainingBytes()).isZero();
            assertThat(buffer.readerIndex()).isEqualTo(4);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (retryTracker != null) {
                assertThat(retryTracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(retryTracker.getTrace().getDiagnostics()).isEmpty();
            }
        } finally {
            buffer.release();
        }
    }

    static Stream<Arguments> invalidCounts() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, true, null),
                Arguments.of(tracked, false, null),
                Arguments.of(tracked, false, "12"),
                Arguments.of(tracked, false, 12),
                Arguments.of(tracked, false, Map.of("a", 1)),
                Arguments.of(tracked, false, new int[]{1, 2}),
                Arguments.of(tracked, false, new Object[]{1, 2}),
                Arguments.of(tracked, false, Set.of(1, 2))));
    }

    @ParameterizedTest
    @MethodSource("invalidCounts")
    void rejectsMissingOrNonListCountTargetsBeforeWritingTheCountAndAllowsLaterCalls(
            boolean tracked, boolean missing, @Nullable Object supplied) {

        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(),
                DynamicField.field("count", "u8").generated(DynamicGeneratedValue.countOf("items")).build(),
                DynamicField.field("items", "array").length(DynamicLength.ref("count", "item"))
                        .item(DynamicField.field("item", "u8").build()).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55).readerIndex(1);
        final CodecTracker tracker = tracked ? new CodecTracker() : null;
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values("items", supplied, missing), buffer, tracker));
            assertThat(error).hasMessageContaining("countOf 目标必须提供集合值: items");
            assertFailure(error, "$.count", 8, "7e", buffer, tracker);
            if (tracker != null) {
                assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("items");
            }

            buffer.clear().writeByte(0x55).readerIndex(1);
            final DynamicValues valid = values("items", List.of(1, 2), false);
            final CodecTracker retryTracker = tracked ? new CodecTracker() : null;
            assertThat(plan.encode(valid, buffer, retryTracker)).isEqualTo(40);
            assertThat(valid.fields()).containsEntry("count", 99);
            assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("557e0201027f");
            assertThat(buffer.readerIndex()).isEqualTo(1);
            final var decoded = plan.decode(buffer, tracked ? new CodecTracker() : null);
            assertThat(decoded.values().fields()).isEqualTo(Map.of("head", 126L, "count", 2L, "items", List.of(1L, 2L), "tail", 127L));
            assertThat(decoded.consumedBytes()).isEqualTo(5);
            assertThat(decoded.remainingBytes()).isZero();
            assertThat(buffer.readerIndex()).isEqualTo(6);
            assertThat(buffer.refCnt()).isEqualTo(1);
            if (retryTracker != null) {
                assertThat(retryTracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(retryTracker.getTrace().getDiagnostics()).isEmpty();
                assertThat(retryTracker.getTrace().getRoot().getChildren()).filteredOn("name", "count")
                        .singleElement().extracting("value").isEqualTo(2L);
            }
        } finally {
            buffer.release();
        }
    }

    private static void assertFailure(DynamicCodecException error, String path, long offset, String partialHex,
                                      ByteBuf buffer, @Nullable CodecTracker tracker) {
        assertThat(error.fieldPath()).isEqualTo(path);
        assertThat(error.bitOffset()).isEqualTo(offset);
        assertThat(ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex())).isEqualTo("55" + partialHex);
        assertThat(buffer.writerIndex()).isEqualTo(1 + partialHex.length() / 2);
        assertThat(buffer.readerIndex()).isEqualTo(1);
        assertThat(buffer.refCnt()).isEqualTo(1);
        if (tracker != null) {
            final var root = tracker.getTrace().getRoot();
            assertThat(root.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(root.getBitEnd()).isEqualTo(offset);
            assertThat(root.getChildren()).extracting("name").doesNotContain("tail");
            assertThat(root.getChildren().getLast().getPath()).isEqualTo(path.substring(2));
            assertThat(root.getChildren().getLast().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(partialHex);
            assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.level()).isEqualTo("ERROR");
                assertThat(diagnostic.bitOffset()).isEqualTo(offset);
                assertThat(diagnostic.byteOffset()).isEqualTo((int) (offset / 8));
                assertThat(diagnostic.nodeId()).isEqualTo(root.getChildren().getLast().getId());
            });
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }

    private static DynamicValues values(String id, @Nullable Object value, boolean missing) {
        final Map<String, Object> fields = new HashMap<>();
        fields.put("head", 126);
        fields.put("tail", 127);
        fields.put("count", 99);
        if (!missing) {
            fields.put(id, value);
        }
        return DynamicValues.of(fields);
    }
}

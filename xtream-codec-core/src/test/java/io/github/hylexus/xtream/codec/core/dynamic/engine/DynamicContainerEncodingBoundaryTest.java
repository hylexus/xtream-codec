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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceDirection;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 动态容器编码的容量、长度契约和非法条目测试。
 *
 * @author Codex (AI)
 */
class DynamicContainerEncodingBoundaryTest {

    static Stream<Arguments> prefixLengths() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of("struct", "array").flatMap(type -> Stream.of(
                Arguments.of(tracked, type, "byte", 0, true),
                Arguments.of(tracked, type, "byte", 255, true),
                Arguments.of(tracked, type, "byte", 256, false),
                Arguments.of(tracked, type, "bit", 31, true),
                Arguments.of(tracked, type, "bit", 32, false))));
    }

    @ParameterizedTest
    @MethodSource("prefixLengths")
    void checksContainerPrefixCapacityInDeclaredUnits(boolean tracked, String type, String unit, int size, boolean valid) {
        final DynamicLength length = DynamicLength.prefix(8, unit);
        final DynamicField field = "struct".equals(type)
                ? DynamicField.field("data", type).length(length).fields(
                        DynamicField.field("raw", "hex").length(DynamicLength.remaining("byte")).build()).build()
                : DynamicField.field("data", type).length(length).item(DynamicField.field("item", "u8").build()).build();
        final Object input = "struct".equals(type) ? Map.of("raw", "ab".repeat(size)) : Collections.nCopies(size, 0xab);
        final DynamicPlan plan = plan(field);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            if (!valid) {
                final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                        () -> plan.encode(values(input), output, tracked ? tracker : null));
                assertFailure(error, "$.data", (2L + size) * 8, "容器长度超出前缀范围", output,
                        "0700" + "ab".repeat(size), tracker, tracked);
                if (tracked) {
                    assertThat(tracker.toTraceView().root().children()).filteredOn("kind", CodecTraceNodeKind.LENGTH_FIELD)
                            .singleElement().satisfies(prefix -> {
                                assertThat(prefix.value()).isNull();
                                assertThat(prefix.status()).isEqualTo(CodecTraceStatus.ERROR);
                                assertThat(prefix.hex()).isEqualTo("00");
                            });
                }
                return;
            }
            final int units = "bit".equals(unit) ? size * 8 : size;
            final String expected = "07" + String.format("%02x", units) + "ab".repeat(size) + "7e";
            assertThat(plan.encode(values(input), output, tracked ? tracker : null)).isEqualTo((3L + size) * 8);
            assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + expected);
            assertThat(output.readerIndex()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
            if (tracked) {
                assertThat(tracker.getTrace().getDirection()).isEqualTo(CodecTraceDirection.ENCODE);
                assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
                assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
                assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
                assertThat(tracker.toTraceView().root().children()).filteredOn("name", "prependLengthField(data)")
                        .singleElement().satisfies(prefix -> {
                            assertThat(prefix.value()).isEqualTo((long) units);
                            assertThat(prefix.bitStart()).isEqualTo(8);
                            assertThat(prefix.bitEnd()).isEqualTo(16);
                            assertThat(prefix.hex()).isEqualTo(String.format("%02x", units));
                        });
                assertThat(tracker.isNotTracing()).isTrue();
            }
            output.readerIndex(2);
            final DynamicDecodeResult decoded = plan.decode(output);
            assertThat(decoded.consumedBits()).isEqualTo((3L + size) * 8);
            assertThat(decoded.values().fields().get("data")).isEqualTo("struct".equals(type)
                    ? input : Collections.nCopies(size, 0xabL));
            assertThat(decoded.values().fields().get("tail")).isEqualTo(0x7eL);
            assertThat(output.readerIndex()).isEqualTo(output.writerIndex());
        } finally {
            output.release();
        }
    }

    static Stream<Arguments> invalidContainers() {
        return Stream.of(false, true).flatMap(tracked -> Stream.of(
                Arguments.of(tracked, DynamicField.field("data", "struct").length(DynamicLength.fixed(2, "byte"))
                                .fields(DynamicField.field("value", "u8").build()).build(),
                        Map.of("value", 0xab), "07ab", "$.data", 16L, "容器未恰好消费声明的长度"),
                Arguments.of(tracked, DynamicField.field("data", "struct").length(DynamicLength.fixed(1, "byte"))
                                .fields(DynamicField.field("value", "u16").build()).build(),
                        Map.of("value", 0xabcd), "07", "$.data.value", 8L, "输出超过边界或资源上限"),
                Arguments.of(tracked, DynamicField.field("data", "array").length(DynamicLength.fixed(2, "item"))
                                .item(DynamicField.field("item", "u8").build()).build(),
                        List.of(0xab), "07ab", "$.data", 16L, "集合长度与边界不匹配"),
                Arguments.of(tracked, DynamicField.field("data", "array").length(DynamicLength.fixed(1, "item"))
                                .item(DynamicField.field("item", "u8").build()).build(),
                        List.of(0xab, 0xcd), "07abcd", "$.data", 24L, "集合长度与边界不匹配"),
                Arguments.of(tracked, DynamicField.field("data", "struct").length(DynamicLength.prefix(8, "byte"))
                                .fields(DynamicField.field("value", "u3").build()).build(),
                        Map.of("value", 5), "0700a0", "$.data", 19L, "容器长度超出前缀范围"),
                Arguments.of(tracked, DynamicField.field("data", "array").length(DynamicLength.prefix(8, "byte"))
                                .item(DynamicField.field("item", "u3").build()).build(),
                        List.of(5), "0700a0", "$.data", 19L, "容器长度超出前缀范围"),
                Arguments.of(tracked, DynamicField.field("data", "struct")
                                .fields(DynamicField.field("value", "u8").build()).build(),
                        List.of(1), "07", "$.data", 8L, "struct 值必须为对象"),
                Arguments.of(tracked, DynamicField.field("data", "array").length(DynamicLength.fixed(1, "item"))
                                .item(DynamicField.field("item", "u8").build()).build(),
                        Map.of("value", 1), "07", "$.data", 8L, "array 值必须为数组"),
                Arguments.of(tracked, entries("tlv"), Map.of("key", 1), "07", "$.data", 8L, "条目值必须为有序数组"),
                Arguments.of(tracked, DynamicField.field("data", "array").length(DynamicLength.fixed(1, "item"))
                                .item(DynamicField.field("item", "align").alignTo(8).build()).build(),
                        List.of(1), "07", "$.data", 8L, "集合元素未推进游标")));
    }

    @ParameterizedTest
    @MethodSource("invalidContainers")
    void rejectsInvalidContainerWithoutWritingTail(boolean tracked, DynamicField field, Object input,
                                                  String expected, String path, long bitOffset, String message) {
        assertRejected(tracked, field, input, expected, path, bitOffset, message);
    }

    static Stream<Arguments> invalidEntries() {
        return Stream.of(false, true).flatMap(tracked -> Stream.concat(
                Stream.of("tlv", "map", "pair").flatMap(type -> Stream.of(
                        Arguments.of(tracked, entries(type), List.of(Map.of("key", -1, "value", 1)),
                                "07", "$.data", 8L, "条目键超出位宽"),
                        Arguments.of(tracked, entries(type), List.of(Map.of("key", 256, "value", 1)),
                                "07", "$.data", 8L, "条目键超出位宽"))),
                Stream.of(
                        Arguments.of(tracked, entries("tlv"), List.of(Map.of("key", 2)),
                                "070200", "$.data", 24L, "未知条目需要 rawHex 字符串"),
                        Arguments.of(tracked, entries("map"), List.of(Map.of("key", 2, "rawHex", 3)),
                                "070200", "$.data", 24L, "未知条目需要 rawHex 字符串"),
                        Arguments.of(tracked, entries("pair"), List.of(Map.of("key", 2, "rawHex", "ab")),
                                "0702", "$.data", 16L, "pair 的未知键没有可确定的值边界"),
                        Arguments.of(tracked, entries("tlv"), List.of(1),
                                "07", "$.data", 8L, "条目必须为对象"),
                        Arguments.of(tracked, DynamicField.field("data", "tlv").length(DynamicLength.fixed(2, "item"))
                                        .variants(Map.of("1", DynamicField.field("value", "u8").build())).build(),
                                List.of(Map.of("key", 1, "value", 0xab)), "070101ab", "$.data", 32L, "条目数与声明不符"),
                        Arguments.of(tracked, DynamicField.field("data", "tlv").length(DynamicLength.fixed(1, "item"))
                                        .variants(Map.of("1", DynamicField.field("value", "u3").build())).build(),
                                List.of(Map.of("key", 1, "value", 5)), "070100a0", "$.data", 27L, "条目值长度超出完整字节或前缀范围"))));
    }

    @ParameterizedTest
    @MethodSource("invalidEntries")
    void rejectsInvalidEntryAndPreservesWrittenHeaders(boolean tracked, DynamicField field, Object input,
                                                      String expected, String path, long bitOffset, String message) {
        assertRejected(tracked, field, input, expected, path, bitOffset, message);
    }

    private static DynamicField entries(String type) {
        return DynamicField.field("data", type).length(DynamicLength.fixed(1, "item"))
                .variants(Map.of("1", DynamicField.field("value", "u8").build())).build();
    }

    private static DynamicPlan plan(DynamicField field) {
        return DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("head", "u8").build(), field, DynamicField.field("tail", "u8").build()));
    }

    private static DynamicValues values(Object input) {
        return DynamicValues.of(Map.of("head", 7, "data", input, "tail", 0x7e));
    }

    private static void assertRejected(boolean tracked, DynamicField field, Object input,
                                       String expected, String path, long bitOffset, String message) {
        final DynamicPlan plan = plan(field);
        final ByteBuf output = Unpooled.buffer().writeShort(0x55aa).readerIndex(1);
        final CodecTracker tracker = new CodecTracker();
        try {
            final DynamicCodecException error = catchThrowableOfType(DynamicCodecException.class,
                    () -> plan.encode(values(input), output, tracked ? tracker : null));
            assertFailure(error, path, bitOffset, message, output, expected, tracker, tracked);
        } finally {
            output.release();
        }
    }

    private static void assertFailure(DynamicCodecException error, String path, long bitOffset, String message,
                                      ByteBuf output, String expected, CodecTracker tracker, boolean tracked) {
        assertThat(error).hasMessageContaining(message);
        assertThat(error.fieldPath()).isEqualTo(path);
        assertThat(error.bitOffset()).isEqualTo(bitOffset);
        assertThat(ByteBufUtil.hexDump(output, 0, output.writerIndex())).isEqualTo("55aa" + expected);
        assertThat(output.readerIndex()).isEqualTo(1);
        assertThat(output.refCnt()).isEqualTo(1);
        if (tracked) {
            assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
            assertThat(tracker.getTrace().getPayloadHex()).isEqualTo(expected);
            assertThat(tracker.getTrace().getRoot().getBitEnd()).isEqualTo(bitOffset);
            assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.bitOffset()).isEqualTo(bitOffset);
                assertThat(diagnostic.byteOffset()).isEqualTo((int) (bitOffset / 8));
            });
            assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").doesNotContain("tail");
            assertThat(tracker.isNotTracing()).isTrue();
        }
    }
}

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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceView;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 有序键值条目、未知标签和长度截断测试。
 * 通过重复键和未知标签验证原始条目顺序及 HEX 回退值能被重新编码。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicEntriesTest {
    @ParameterizedTest
    @ValueSource(strings = {"tlv", "map", "pair"})
    void shouldTraceOrderedEntriesWithSeparateHeadersAndNestedValues(String type) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", type).length(DynamicLength.fixed(3, "item"))
                        .options("pair".equals(type) ? Map.of("keyBits", 16) : Map.of("keyBits", 16, "lengthBits", 16))
                        .variants(Map.of(
                                "1", DynamicField.field("number", "u16").name("数值").build(),
                                "2", DynamicField.field("text", "struct").name("文本").fields(
                                        DynamicField.field("desc", "string").length(DynamicLength.prefix(8, "byte")).build()
                                ).build())).build()));
        final DynamicValues values = DynamicValues.of(Map.of("items", List.of(
                Map.of("key", 2, "value", Map.of("desc", "A")),
                Map.of("key", 1, "value", 0x1234),
                Map.of("key", 2, "value", Map.of("desc", "B")))));
        final ByteBuf production = Unpooled.buffer();
        final ByteBuf tracked = Unpooled.buffer().writeByte(0x7e);
        try {
            final CodecTracker encoder = new CodecTracker();
            plan.encode(values, production);
            plan.encode(values, tracked, encoder);
            final String expected = "pair".equals(type) ? "000201410001123400020142"
                    : "000200020141000100021234000200020142";
            assertEquals(expected, ByteBufUtil.hexDump(production));
            assertEquals(expected, ByteBufUtil.hexDump(tracked, 1, tracked.readableBytes() - 1));
            tracked.readerIndex(1);
            final CodecTracker decoder = new CodecTracker();
            assertEquals(plan.decode(production), plan.decode(tracked, decoder));
            final int entryBytes = "pair".equals(type) ? 4 : 6;
            final String keyName = "tlv".equals(type) ? "Tag" : "Key";
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                final var items = view.root().children().getFirst().children();
                assertEquals(List.of("items[0]", "items[1]", "items[2]"), items.stream().map(CodecTraceView.Node::path).toList());
                assertEquals(List.of(2L, 1L, 2L), items.stream().map(entry -> entry.attributes().get("key")).toList());
                for (int index = 0; index < items.size(); index++) {
                    final var entry = items.get(index);
                    assertEquals(CodecTraceNodeKind.MAP_ENTRY, entry.kind());
                    assertEquals(index, entry.attributes().get("entryIndex"));
                    assertEquals("pair".equals(type) ? 2 : 3, entry.children().size());
                    final long start = (long) index * entryBytes * 8;
                    assertEquals(start, entry.bitStart());
                    assertEquals(start + entryBytes * 8, entry.bitEnd());
                    assertEntryPart(entry.children().getFirst(), keyName, "u16", start, start + 16);
                    assertEquals(index == 1 ? 1L : 2L, entry.children().getFirst().value());
                    if (!"pair".equals(type)) {
                        final var length = entry.children().get(1);
                        assertEntryPart(length, "Length", "u16", start + 16, start + 32);
                        assertEquals(CodecTraceNodeKind.LENGTH_FIELD, length.kind());
                        assertEquals(2L, length.value());
                        assertEquals("byte", length.attributes().get("unit"));
                        assertEquals("0002", length.hex());
                    }
                    final var value = entry.children().getLast();
                    assertEntryPart(value, "Value", index == 1 ? "u16" : "struct",
                            start + (entryBytes - 2) * 8, start + entryBytes * 8);
                    assertEquals(index == 1 ? "数值" : "文本", value.attributes().get("displayName"));
                    if (index != 1) {
                        assertEquals(List.of(CodecTraceNodeKind.LENGTH_FIELD, CodecTraceNodeKind.FIELD),
                                value.children().stream().map(CodecTraceView.Node::kind).toList());
                        assertEquals("items[" + index + "].Value.desc", value.children().getLast().path());
                    }
                }
            }
        } finally {
            production.release();
            tracked.release();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 16, 32})
    void shouldTraceUnknownAndEmptyTlvValuesWithConfiguredWidths(int width) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.prefix(8, "item"))
                        .options(Map.of("keyBits", width, "lengthBits", width)).variants(Map.of()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("items", List.of(
                    Map.of("key", 99, "rawHex", "AABB"), Map.of("key", 1, "rawHex", "")))), buffer, encoder);
            final CodecTracker decoder = new CodecTracker();
            final var decoded = plan.decode(buffer, decoder).values().fields();
            assertEquals(List.of(Map.of("key", 99L, "rawHex", "aabb"), Map.of("key", 1L, "rawHex", "")), decoded.get("items"));
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                final var items = view.root().children().getLast().children();
                final var first = items.getFirst();
                assertEntryPart(first.children().get(0), "Tag", "u" + width, 8, 8 + width);
                assertEntryPart(first.children().get(1), "Length", "u" + width, 8 + width, 8 + 2L * width);
                assertEntryPart(first.children().get(2), "Value", "hex", 8 + 2L * width, 24 + 2L * width);
                assertEquals("aabb", first.children().getLast().value());
                assertEquals(2L, first.children().get(1).value());
                final var empty = items.getLast().children().getLast();
                assertEquals(empty.bitStart(), empty.bitEnd());
                assertEquals("", empty.value());
                assertEquals(0L, items.getLast().children().get(1).value());
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldKeepCompletedEntryHeadersOnReadAndWriteFailures() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.fixed(1, "item"))
                        .variants(Map.of("1", DynamicField.field("number", "u16").build())).build()));
        for (final String hex : List.of("", "01", "0103ff")) {
            final ByteBuf input = Unpooled.wrappedBuffer(java.util.HexFormat.of().parseHex(hex));
            try {
                final CodecTracker decoder = new CodecTracker();
                assertThrows(DynamicCodecException.class, () -> plan.decode(input, decoder));
                assertEquals(0, input.readerIndex());
                final var entry = decoder.toTraceView().root().children().getFirst().children().getFirst();
                assertEquals(CodecTraceStatus.ERROR, entry.status());
                assertEquals("items[0]", entry.path());
                final var tag = entry.children().getFirst();
                assertEquals(hex.isEmpty() ? CodecTraceStatus.ERROR : CodecTraceStatus.SUCCESS, tag.status());
                if (!hex.isEmpty()) {
                    assertEquals(1L, tag.value());
                    final var length = entry.children().get(1);
                    assertEquals(hex.length() > 2 ? CodecTraceStatus.SUCCESS : CodecTraceStatus.ERROR, length.status());
                    assertEquals(hex.length() > 2 ? 3L : null, length.value());
                }
                assertEquals(1, decoder.toTraceView().diagnostics().size());
            } finally {
                input.release();
            }
        }
        final ByteBuf output = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            assertThrows(DynamicCodecException.class, () -> plan.encode(
                    DynamicValues.of(Map.of("items", List.of(Map.of("key", 1, "value", "invalid")))), output, encoder));
            final var entry = encoder.toTraceView().root().children().getFirst().children().getFirst();
            assertEquals(CodecTraceStatus.SUCCESS, entry.children().get(0).status());
            assertEquals(CodecTraceStatus.ERROR, entry.children().get(1).status());
            assertNull(entry.children().get(1).value());
            assertEquals(CodecTraceStatus.ERROR, entry.children().get(2).status());
            assertEquals(CodecTraceStatus.ERROR, entry.status());
            assertEquals("0100", ByteBufUtil.hexDump(output));
        } finally {
            output.release();
        }
    }

    private static void assertEntryPart(CodecTraceView.Node part, String name, String type, long start, long end) {
        assertEquals(name, part.name());
        assertEquals(type, part.attributes().get("dataType"));
        assertEquals(start, part.bitStart());
        assertEquals(end, part.bitEnd());
        assertEquals((int) (start / 8), part.byteStart());
        assertEquals((int) ((end + 7) / 8), part.byteEnd());
        assertEquals(CodecTraceStatus.SUCCESS, part.status());
    }

    @ParameterizedTest
    @ValueSource(strings = {"tlv", "map", "pair"})
    void shouldIsolateConditionalValuesInRepeatedEntryVariants(String type) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", type).length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("1", DynamicField.field("item", "struct").fields(
                                DynamicField.field("flag", "u8").build(),
                                DynamicField.field("a", "u8")
                                        .when(new DynamicCondition("flag", "eq", "1")).build(),
                                DynamicField.field("b", "u8")
                                        .when(new DynamicCondition("a", "ne", "0")).build()).build())).build(),
                DynamicField.field("tail", "u16").build()));
        final List<Map<String, Object>> entries = List.of(
                Map.of("key", 1, "value", Map.of("flag", 1, "a", 7, "b", 9)),
                Map.of("key", 1, "value", Map.of("flag", 0)));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("items", entries, "tail", 0x0bee)), buffer);
            assertEquals("pair".equals(type) ? "0101070901000bee" : "01030107090101000bee",
                    ByteBufUtil.hexDump(buffer));
            final Map<String, Object> decoded = plan.decode(buffer).values().fields();
            assertEquals(List.of(
                    Map.of("key", 1L, "value", Map.of("flag", 1L, "a", 7L, "b", 9L)),
                    Map.of("key", 1L, "value", Map.of("flag", 0L))), decoded.get("items"));
            assertEquals(0x0beeL, decoded.get("tail"));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPreserveUnknownAndDuplicateTlvEntries() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.remaining("byte"))
                        .options(Map.of("keyBits", 8, "lengthBits", 8))
                        .variants(Map.of("1", DynamicField.field("text", "string")
                                .length(DynamicLength.remaining("byte")).build())).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final List<Map<String, Object>> entries = List.of(
                    Map.of("key", 1, "value", "A"),
                    Map.of("key", 99, "rawHex", "aabb"),
                    Map.of("key", 1, "value", "B"));
            plan.encode(DynamicValues.of(Map.of("items", entries)), buffer);
            assertEquals("0101416302aabb010142", ByteBufUtil.hexDump(buffer));
            final List<?> decoded = (List<?>) plan.decode(buffer).values().fields().get("items");
            assertEquals(3, decoded.size());
            assertEquals(Map.of("key", 99L, "rawHex", "aabb"), decoded.get(1));
            assertEquals(Map.of("key", 1L, "value", "A"), decoded.get(0));
            assertEquals(Map.of("key", 1L, "value", "B"), decoded.get(2));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldEncodePairAndMapWithOrderedEntries() {
        final DynamicPlan pair = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("pairs", "pair").length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("1", DynamicField.field("number", "u8").build())).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            pair.encode(DynamicValues.of(Map.of("pairs", List.of(
                    Map.of("key", 1, "value", 3), Map.of("key", 1, "value", 4)))), buffer);
            assertEquals("01030104", ByteBufUtil.hexDump(buffer));
            assertEquals(2, ((List<?>) pair.decode(buffer).values().fields().get("pairs")).size());
        } finally {
            buffer.release();
        }

        final DynamicPlan map = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("entries", "map").length(DynamicLength.fixed(2, "item"))
                        .variants(Map.of("7", DynamicField.field("value", "u8").build())).build()));
        final ByteBuf mapBuffer = Unpooled.buffer();
        try {
            map.encode(DynamicValues.of(Map.of("entries", List.of(
                    Map.of("key", 7, "value", 1), Map.of("key", 7, "value", 2)))), mapBuffer);
            assertEquals("070101070102", ByteBufUtil.hexDump(mapBuffer));
            assertEquals(List.of(Map.of("key", 7L, "value", 1L), Map.of("key", 7L, "value", 2L)),
                    map.decode(mapBuffer).values().fields().get("entries"));
        } finally {
            mapBuffer.release();
        }
    }

    @Test
    void shouldRejectTruncatedTlvValue() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "tlv").length(DynamicLength.remaining("byte"))
                        .variants(Map.of()).build()));
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 3, 9});
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
            assertEquals("$.items", error.fieldPath());
        } finally {
            buffer.release();
        }
    }
}

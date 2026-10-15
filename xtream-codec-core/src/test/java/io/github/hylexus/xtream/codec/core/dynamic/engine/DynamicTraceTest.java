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
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceStatus;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceView;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 位范围、字节范围和失败诊断合同测试。
 * 比较启用与关闭 tracker 的字节和值，确认跟踪不会改变执行结果且保留部分失败节点。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicTraceTest {
    private static final DynamicPlan PLAN = DynamicCompiler.compile(DynamicSchema.of(
            DynamicField.field("a", "u3").build(),
            DynamicField.field("b", "u5").build(),
            DynamicField.field("c", "u12").build(),
            DynamicField.field("align", "align").alignTo(8).build()));

    @Test
    void shouldExposeDeclaredTypesForEncodeAndDecode() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("kind", "u8").build(),
                DynamicField.field("signed", "i16").build(),
                DynamicField.field("ratio", "float").bits(32).build(),
                DynamicField.field("terminal", "bcd").length(DynamicLength.fixed(2, "byte")).build(),
                DynamicField.field("items", "array").length(DynamicLength.fixed(1, "item"))
                        .item(DynamicField.field("item", "struct").fields(
                                DynamicField.field("desc", "string").length(DynamicLength.prefix(8, "byte")).build()
                        ).build()).build(),
                DynamicField.field("body", "choice").ref("kind").variants(Map.of(
                        "1", DynamicField.field("raw", "hex").length(DynamicLength.fixed(1, "byte")).build()
                )).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            final CodecTracker decoder = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("kind", 1, "signed", -2, "ratio", 1.5,
                    "terminal", "0012", "items", List.of(Map.of("desc", "a")), "body", "ff")), buffer, encoder);
            plan.decode(buffer, decoder);
            final List<String> expected = List.of("u8", "i16", "float", "bcd", "array", "struct", "u8", "string", "choice", "hex");
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                assertEquals(expected, view.nodes().stream().skip(1).map(node -> node.attributes().get("dataType")).toList());
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldKeepTrackedAndUntrackedBytesAndValuesIdentical() {
        final ByteBuf production = Unpooled.buffer();
        final ByteBuf tracked = Unpooled.buffer();
        try {
            final DynamicValues values = DynamicValues.of(Map.of("a", 5, "b", 17, "c", 0xabc));
            final CodecTracker tracker = new CodecTracker();
            PLAN.encode(values, production);
            PLAN.encode(values, tracked, tracker);
            assertEquals(ByteBufUtil.hexDump(production), ByteBufUtil.hexDump(tracked));
            final var nodes = tracker.toTraceView().root().children();
            assertEquals(0L, nodes.get(0).bitStart());
            assertEquals(3L, nodes.get(0).bitEnd());
            assertEquals(3L, nodes.get(1).bitStart());
            assertEquals(8L, nodes.get(1).bitEnd());
            assertEquals(0, nodes.get(0).byteStart());
            assertEquals(1, nodes.get(0).byteEnd());
            assertEquals(0, nodes.get(1).byteStart());
            assertEquals(1, nodes.get(1).byteEnd());
            assertEquals(1, nodes.get(2).byteStart());
            assertEquals(3, nodes.get(2).byteEnd());
            assertTrue(tracker.toTraceView().nodeIdsByByteOffset().get(0).contains(nodes.get(0).id()));
            assertTrue(tracker.toTraceView().nodeIdsByByteOffset().get(0).contains(nodes.get(1).id()));
            assertEquals(PLAN.decode(production), PLAN.decode(tracked, new CodecTracker()));
        } finally {
            production.release();
            tracked.release();
        }
    }

    @Test
    void shouldPreserveCompletedNodesAndBitFailureOffset() {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xb1, (byte) 0xab});
        try {
            final CodecTracker tracker = new CodecTracker();
            final DynamicCodecException error = assertThrows(DynamicCodecException.class, () -> PLAN.decode(buffer, tracker));
            assertEquals("$.c", error.fieldPath());
            assertEquals(8, error.bitOffset());
            final var view = tracker.toTraceView();
            assertEquals(CodecTraceStatus.ERROR, view.root().status());
            assertEquals(CodecTraceStatus.SUCCESS, view.root().children().get(0).status());
            assertEquals(CodecTraceStatus.SUCCESS, view.root().children().get(1).status());
            assertEquals(CodecTraceStatus.ERROR, view.root().children().get(2).status());
            assertEquals("u12", view.root().children().get(2).attributes().get("dataType"));
            assertEquals(8L, view.diagnostics().getFirst().bitOffset());
            assertEquals("b1ab", view.payloadHex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldSeparateNestedStringPrefixesForEncodeAndDecode() {
        for (final int width : new int[]{8, 16, 32}) {
            final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                    DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                            .item(DynamicField.field("item", "struct").fields(
                                    DynamicField.field("code", "u8").build(),
                                    DynamicField.field("desc", "string").length(DynamicLength.prefix(width, "byte")).build()
                            ).build()).build()));
            final var values = DynamicValues.of(Map.of("items", List.of(
                    Map.of("code", 1, "desc", "sss"), Map.of("code", 2, "desc", "aaa"))));
            final ByteBuf production = Unpooled.buffer();
            final ByteBuf tracked = Unpooled.buffer().writeByte(0x7e);
            try {
                final CodecTracker encoder = new CodecTracker();
                plan.encode(values, production);
                plan.encode(values, tracked, encoder);
                // 跟踪坐标以本次编码起点为零，已有缓冲区内容不计入字段偏移。
                assertEquals(ByteBufUtil.hexDump(production), ByteBufUtil.hexDump(tracked, 1, tracked.readableBytes() - 1));
                assertStringPrefixes(encoder.toTraceView(), width);
                tracked.readerIndex(1);
                final CodecTracker decoder = new CodecTracker();
                assertEquals(plan.decode(production), plan.decode(tracked, decoder));
                assertStringPrefixes(decoder.toTraceView(), width);
            } finally {
                production.release();
                tracked.release();
            }
        }
    }

    @Test
    void shouldTraceBackfilledContainerAndItemCountPrefixes() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.prefix(16, "bit")).fields(
                        DynamicField.field("flag", "u3").build(),
                        DynamicField.field("value", "u5").build()).build(),
                DynamicField.field("bytes", "array").length(DynamicLength.prefix(8, "byte"))
                        .item(DynamicField.field("item", "u8").build()).build(),
                DynamicField.field("items", "array").length(DynamicLength.prefix(8, "item"))
                        .item(DynamicField.field("item", "u16").build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("body", Map.of("flag", 5, "value", 17),
                    "bytes", List.of(1, 2), "items", List.of(4660))), buffer, encoder);
            assertEquals("0008b1020102011234", ByteBufUtil.hexDump(buffer));
            final CodecTracker decoder = new CodecTracker();
            plan.decode(buffer, decoder);
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                final var fields = view.root().children();
                assertPrefix(fields.get(1), fields.get(0), 0, 16, 8, 24);
                assertEquals("bit", fields.get(0).attributes().get("unit"));
                assertPrefix(fields.get(3), fields.get(2), 24, 8, 2, 48);
                assertPrefix(fields.get(5), fields.get(4), 48, 8, 1, 72);
                assertEquals("item", fields.get(4).attributes().get("unit"));
                assertEquals("0008", fields.get(0).hex());
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldKeepEmptyContentSeparateFromTheNextField() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("desc", "string").length(DynamicLength.prefix(8, "byte")).build(),
                DynamicField.field("tail", "u8").build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("desc", "", "tail", 126)), buffer, encoder);
            final CodecTracker decoder = new CodecTracker();
            plan.decode(buffer, decoder);
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                final var fields = view.root().children();
                assertPrefix(fields.get(1), fields.get(0), 0, 8, 0, 8);
                assertEquals(1, fields.get(2).byteStart());
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPreserveReadPrefixButNotAnUnfinishedBackfillAsSuccess() {
        final DynamicPlan text = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("desc", "string").length(DynamicLength.prefix(8, "byte")).build()));
        final DynamicPlan container = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("body", "struct").length(DynamicLength.prefix(8, "byte")).fields(
                        DynamicField.field("a", "u8").build(), DynamicField.field("b", "u8").build()).build()));
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{3, 0x73});
        final ByteBuf output = Unpooled.buffer();
        try {
            final CodecTracker decoder = new CodecTracker();
            assertThrows(DynamicCodecException.class, () -> text.decode(input, decoder));
            final var readFields = decoder.toTraceView().root().children();
            final var failedText = readFields.get(1);
            assertPrefix(failedText, readFields.get(0), 0, 8, 3, 8);
            assertEquals(CodecTraceStatus.ERROR, failedText.status());
            final CodecTracker encoder = new CodecTracker();
            assertThrows(DynamicCodecException.class, () -> container.encode(
                    DynamicValues.of(Map.of("body", Map.of("a", 1, "b", "invalid"))), output, encoder));
            final var writeFields = encoder.toTraceView().root().children();
            final var failedContainer = writeFields.get(1);
            final var prefix = writeFields.getFirst();
            assertEquals(CodecTraceNodeKind.LENGTH_FIELD, prefix.kind());
            assertEquals(CodecTraceStatus.ERROR, prefix.status());
            assertNull(prefix.value());
            assertEquals(8L, failedContainer.attributes().get("contentBitStart"));
            assertEquals(16L, failedContainer.attributes().get("contentBitEnd"));
        } finally {
            input.release();
            output.release();
        }
    }

    @Test
    void shouldPlaceEachArrayItemPrefixBeforeItsContentAtTheSameLevel() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("items", "array").length(DynamicLength.fixed(2, "item"))
                        .item(DynamicField.field("item", "string").length(DynamicLength.prefix(8, "byte")).build()).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final CodecTracker encoder = new CodecTracker();
            plan.encode(DynamicValues.of(Map.of("items", List.of("a", "bb"))), buffer, encoder);
            assertEquals("0161026262", ByteBufUtil.hexDump(buffer));
            final CodecTracker decoder = new CodecTracker();
            plan.decode(buffer, decoder);
            for (final var view : List.of(encoder.toTraceView(), decoder.toTraceView())) {
                final var items = view.root().children().getFirst().children();
                assertEquals(4, items.size());
                assertPrefix(items.get(1), items.get(0), 0, 8, 1, 16);
                assertPrefix(items.get(3), items.get(2), 16, 8, 2, 40);
                assertEquals("items.prependLengthField([0])", items.get(0).path());
                assertEquals("items.prependLengthField([1])", items.get(2).path());
            }
        } finally {
            buffer.release();
        }
    }

    private static void assertStringPrefixes(CodecTraceView view, int width) {
        final var items = view.root().children().getFirst().children();
        for (int index = 0; index < 2; index++) {
            final var fields = items.get(index).children();
            assertEquals(3, fields.size());
            final var prefix = fields.get(1);
            final var field = fields.get(2);
            final long start = 8L + index * (width + 32L);
            assertPrefix(field, prefix, start, width, 3, start + width + 24);
            assertTrue(field.children().isEmpty());
            assertEquals("items[" + index + "].prependLengthField(desc)", prefix.path());
            assertTrue(view.nodeIdsByByteOffset().get((int) (start / 8)).contains(prefix.id()));
            assertFalse(view.nodeIdsByByteOffset().get((int) ((start + width) / 8)).contains(prefix.id()));
        }
    }

    private static void assertPrefix(CodecTraceView.Node field, CodecTraceView.Node prefix,
                                     long start, int bits, long value, long contentEnd) {
        assertEquals(CodecTraceNodeKind.LENGTH_FIELD, prefix.kind());
        assertEquals("u" + bits, prefix.attributes().get("dataType"));
        assertEquals(field.parentId(), prefix.parentId());
        assertEquals("prependLengthField(" + field.name() + ")", prefix.name());
        assertEquals(field.path(), prefix.attributes().get("fieldPath"));
        assertEquals(start, prefix.bitStart());
        assertEquals(start + bits, prefix.bitEnd());
        assertEquals(value, prefix.value());
        assertEquals(CodecTraceStatus.SUCCESS, prefix.status());
        assertEquals(start, field.bitStart());
        assertEquals(contentEnd, field.bitEnd());
        assertEquals(start + bits, field.attributes().get("contentBitStart"));
        assertEquals(contentEnd, field.attributes().get("contentBitEnd"));
    }
}

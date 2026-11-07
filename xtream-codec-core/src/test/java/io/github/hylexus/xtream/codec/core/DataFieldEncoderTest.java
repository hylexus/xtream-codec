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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataFieldEncoderTest {

    private final DataFieldEncoder encoder = new DataFieldEncoder();
    private final EntityCodec entityCodec = EntityCodec.DEFAULT;

    static Stream<Arguments> scalarFields() {
        return Stream.of(
                Arguments.of(i8((byte) -128), "80"),
                Arguments.of(u8((short) 255), "ff"),
                Arguments.of(i16((short) -2), "fffe"),
                Arguments.of(u16(65535), "ffff"),
                Arguments.of(i32(Integer.MIN_VALUE), "80000000"),
                Arguments.of(u32(4294967295L), "ffffffff"),
                Arguments.of(i64(Long.MIN_VALUE), "8000000000000000"),
                Arguments.of(f32(1.5f), "3fc00000"),
                Arguments.of(f64(-2.5d), "c004000000000000"),
                Arguments.of(bcd8421String("123456"), "123456"),
                Arguments.of(hexString("00aBff"), "00abff"),
                Arguments.of(gbkString("中"), "d6d0"),
                Arguments.of(gb2312String("中"), "d6d0"),
                Arguments.of(utf8String("中"), "e4b8ad"),
                Arguments.of(string("A", "UTF-16BE"), "0041"),
                Arguments.of(byteSequence(new byte[]{0, -1}), "00ff"),
                Arguments.of(new CustomField("AB", PrependLengthFieldType.none), "4142")
        );
    }

    @ParameterizedTest
    @MethodSource("scalarFields")
    void shouldEncodeScalarBytesAndRecordTheirExactRange(DataField field, String expected) {
        final ByteBuf output = Unpooled.buffer();
        try {
            output.writeByte(0x55);
            entityCodec.encode(field, output);
            assertEquals("55" + expected, ByteBufUtil.hexDump(output));

            output.clear().writeByte(0x55);
            final CodecTracker tracker = new CodecTracker();
            entityCodec.encode(field, output, tracker);
            assertEquals("55" + expected, ByteBufUtil.hexDump(output));
            final List<CodecTraceNode> nodes = tracker.getTrace().getRoot().getChildren();
            assertEquals(1, nodes.size());
            assertEquals(CodecTraceNodeKind.FIELD, nodes.getFirst().getKind());
            assertEquals(1, nodes.getFirst().getByteStart());
            assertEquals(output.writerIndex(), nodes.getFirst().getByteEnd());
            assertEquals(expected, nodes.getFirst().getHex());
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"u8, 03e4b8ad", "u16, 0003e4b8ad", "u32, 00000003e4b8ad"})
    void shouldBackfillLengthUsingEncodedBytes(PrependLengthFieldType lengthType, String expected) {
        final DataField field = utf8String(lengthType, "中");
        final ByteBuf output = Unpooled.buffer();
        try {
            entityCodec.encode(field, output);
            assertEquals(expected, ByteBufUtil.hexDump(output));
            output.clear();
            final CodecTracker tracker = new CodecTracker();
            entityCodec.encode(field, output, tracker);
            assertEquals(expected, ByteBufUtil.hexDump(output));
            final CodecTraceNode length = tracker.getTrace().getRoot().getChildren().getFirst();
            assertEquals(CodecTraceNodeKind.LENGTH_FIELD, length.getKind());
            assertEquals(3, length.getValue());
            assertEquals(0, length.getByteStart());
            assertEquals(lengthType.getByteCounts(), length.getByteEnd());
        } finally {
            output.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldEncodeNestedStructAndSequence(boolean tracked) {
        final DataField field = struct(PrependLengthFieldType.u16, List.of(
                u8((short) 1),
                sequence(PrependLengthFieldType.u8, List.of(u16(0x0203), utf8String("中")))));
        assertEncoded(field, tracked, "000701050203e4b8ad");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldEncodeOrderedDictionaryWithIndependentEntryLengths(boolean tracked) {
        final Map<DataField.U8, DataField> values = new LinkedHashMap<>();
        values.put(u8((short) 1), byteSequence(new byte[]{0x11, 0x22}));
        values.put(u8((short) 2), utf8String("A"));
        assertEncoded(dict(DataField.U8.class, LengthFieldType.u16, values), tracked, "010002112202000141");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldEncodeTlvWithLengthOfNestedValue(boolean tracked) {
        final DataField field = tlv(u16(0x1234), LengthFieldType.u8, struct(List.of(i8((byte) -1), utf8String("中"))));
        assertEncoded(field, tracked, "123404ffe4b8ad");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldSkipNullFieldsAndNullValuesInIterable(boolean tracked) {
        final ByteBuf output = Unpooled.buffer();
        final CodecTracker tracker = new CodecTracker();
        final FieldCodec.SerializeContext context = context(tracker, ByteBufAllocator.DEFAULT);
        final List<DataField> fields = Arrays.asList(null, i32((Integer) null), u8((short) 7));
        try {
            if (tracked) {
                encoder.encodeWithTracker(context, (DataField) null, output);
                encoder.encodeWithTracker(context, fields, output);
            } else {
                encoder.encode(context, (DataField) null, output);
                encoder.encode(context, fields, output);
            }
            assertEquals("07", ByteBufUtil.hexDump(output));
            assertEquals(tracked ? 1 : 0, tracker.getTrace().getRoot().getChildren().size());
        } finally {
            output.release();
        }
    }

    @Test
    void shouldReleaseTemporaryBufferWhenCustomTlvValueFails() {
        final ByteBuf temporary = Unpooled.buffer();
        final ByteBuf output = Unpooled.buffer();
        final ByteBufAllocator allocator = mock(ByteBufAllocator.class);
        when(allocator.buffer()).thenReturn(temporary);
        final DataField.CustomDataField value = mock(DataField.CustomDataField.class);
        final IllegalStateException failure = new IllegalStateException("自定义编码失败");
        doThrow(failure).when(value).writeTo(any());
        try {
            assertSame(failure, assertThrows(IllegalStateException.class, () ->
                    encoder.encode(context(null, allocator), tlv(u8((short) 1), LengthFieldType.u8, value), output)));
            assertEquals(0, temporary.refCnt());
        } finally {
            if (temporary.refCnt() > 0) {
                temporary.release();
            }
            output.release();
        }
    }

    private FieldCodec.SerializeContext context(CodecTracker tracker, ByteBufAllocator allocator) {
        final EntityEncoder entityEncoder = entityCodec.entityEncoder();
        return new DefaultSerializeContext(allocator, entityEncoder, new Object(), XtreamField.ALL_VERSION,
                entityEncoder.getBeanMetadataRegistry(), tracker);
    }

    private void assertEncoded(DataField field, boolean tracked, String expected) {
        final ByteBuf output = Unpooled.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            entityCodec.encode(XtreamField.ALL_VERSION, field, output, tracked ? tracker : null);
            assertEquals(expected, ByteBufUtil.hexDump(output));
            if (tracked) {
                assertFalse(tracker.getTrace().getRoot().getChildren().isEmpty());
                assertRanges(tracker.getTrace().getRoot(), output);
            }
        } finally {
            output.release();
        }
    }

    private void assertRanges(CodecTraceNode node, ByteBuf output) {
        for (CodecTraceNode child : node.getChildren()) {
            assertTrue(child.getByteStart() >= node.getByteStart());
            assertTrue(child.getByteEnd() <= node.getByteEnd());
            assertEquals(ByteBufUtil.hexDump(output, child.getByteStart(), child.getByteEnd() - child.getByteStart()), child.getHex());
            assertRanges(child, output);
        }
    }

    record CustomField(String value, PrependLengthFieldType prependLengthFieldType) implements DataField.CustomDataField {
        @Override
        public String name() {
            return "自定义字段";
        }

        @Override
        public void writeTo(ByteBuf output) {
            output.writeCharSequence(value, StandardCharsets.US_ASCII);
        }
    }
}

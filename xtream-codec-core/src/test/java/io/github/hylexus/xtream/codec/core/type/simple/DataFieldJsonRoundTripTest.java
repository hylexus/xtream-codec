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

package io.github.hylexus.xtream.codec.core.type.simple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.PaddingConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 对照四种公开入口验证运行时字段模型的 JSON 转换及实际编码结果。
 *
 * @author Codex (AI)
 */
class DataFieldJsonRoundTripTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, Object> ATTRIBUTES = Map.of("x-note", "中文", "x-config", Map.of("enabled", true));

    @ParameterizedTest
    @MethodSource("scalarAliases")
    void aliasesPreserveScalarTypesValuesAndMetadata(String aliases, DataField expected, String hex) throws Exception {
        final ObjectNode input = serializedField(expected);
        for (final String alias : aliases.split(",")) {
            input.put("type", alias);
            for (final InputMode mode : InputMode.values()) {
                final DataField actual = parse(input, mode);
                assertInstanceOf(expected.getClass(), actual);
                assertEquals(expected.type(), actual.type());
                assertEquals("payload", actual.name());
                assertEquals(ATTRIBUTES, actual.attributes());
                assertEquals(expected.prependLengthFieldType(), actual.prependLengthFieldType());
                if (expected.value() instanceof byte[] bytes) {
                    assertArrayEquals(bytes, assertInstanceOf(byte[].class, actual.value()));
                } else {
                    assertEquals(expected.value(), actual.value());
                }
                assertEquals(serializedField(expected), serializedField(actual));
                assertEncoded(actual, hex, false);
                assertEncoded(actual, hex, true);
            }
        }
    }

    static Stream<Arguments> scalarAliases() {
        final PrependLengthFieldType none = PrependLengthFieldType.none;
        final PaddingConfig padding = PaddingConfig.none();
        return Stream.of(
                Arguments.of("i8,int8", i8("payload", (byte) -128, ATTRIBUTES), "80"),
                Arguments.of("u8,uint8", u8("payload", (short) 255, ATTRIBUTES), "ff"),
                Arguments.of("i16,int16", i16("payload", (short) -2, ATTRIBUTES), "fffe"),
                Arguments.of("u16,uint16", u16("payload", 65535, ATTRIBUTES), "ffff"),
                Arguments.of("i32,int32", i32("payload", Integer.MIN_VALUE, ATTRIBUTES), "80000000"),
                Arguments.of("u32,uint32", u32("payload", 4294967295L, ATTRIBUTES), "ffffffff"),
                Arguments.of("i64,int64", i64("payload", Long.MIN_VALUE, ATTRIBUTES), "8000000000000000"),
                Arguments.of("f32,float32", f32("payload", 1.5f, ATTRIBUTES), "3fc00000"),
                Arguments.of("f64,float64", f64("payload", -2.5d, ATTRIBUTES), "c004000000000000"),
                Arguments.of("str,string", string("payload", none, "中", "UTF-16BE", padding, ATTRIBUTES), "4e2d"),
                Arguments.of("gbk_str,gbk_string", gbkString("payload", none, "中", padding, ATTRIBUTES), "d6d0"),
                Arguments.of("gb2312_str,gb2312_string", gb2312String("payload", none, "中", padding, ATTRIBUTES), "d6d0"),
                Arguments.of("utf8_str,utf8_string", utf8String("payload", none, "中😀", padding, ATTRIBUTES), "e4b8adf09f9880"),
                Arguments.of("hex,hex_string", new DataField.HexString("payload", none, "00abff", padding, ATTRIBUTES), "00abff"),
                Arguments.of("bcd_8421,bcd_8421_string", new DataField.Bcd8421String("payload", none, "001234", padding, ATTRIBUTES), "001234"),
                Arguments.of("byte_sequence,byte_seq,bytes", byteSequence("payload", none, new byte[]{0, -128, -1}, ATTRIBUTES), "0080ff"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "sequence", "seq"})
    void sequenceAliasesPreserveElementOrder(String alias) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"seq","value":[{"type":"u8","value":2},{"type":"u8","value":1}]}
                """);
        input.put("type", alias);
        for (final InputMode mode : InputMode.values()) {
            final DataField.Sequence sequence = assertInstanceOf(DataField.Sequence.class, parse(input, mode));
            assertEquals(List.of((short) 2, (short) 1), sequence.value().stream().map(DataField::value).toList());
            assertEncoded(sequence, "0201", false);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"map", "dict", "dictionary"})
    void dictionaryAliasesPreserveKeyTypesOrderAndLengths(String alias) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"dict","keyType":"u8","valueLengthType":"U16",
                 "value":{"2":{"type":"u8","value":17},"1":{"type":"u8","value":34}}}
                """);
        input.put("type", alias);
        for (final InputMode mode : InputMode.values()) {
            final DataField.Dict<?> dict = assertInstanceOf(DataField.Dict.class, parse(input, mode));
            assertSame(DataField.U8.class, dict.keyType());
            assertSame(LengthFieldType.u16, dict.valueLengthType());
            assertSame(PrependLengthFieldType.none, dict.prependLengthFieldType());
            assertEquals(List.of((short) 2, (short) 1), dict.value().keySet().stream().map(DataField::value).toList());
            assertEncoded(dict, "0200011101000122", false);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nestedContainersPreserveTypesConfigurationsAndExactBytes(boolean tracked) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"struct","name":"packet","prependLengthFieldType":"u16","x-note":"嵌套",
                 "value":[{"type":"u8","value":1},
                  {"type":"seq","prependLengthFieldType":"u8","value":[
                   {"type":"u16","value":515},
                   {"type":"tlv","tag":{"type":"u8","value":7},"length":"u8",
                    "value":{"type":"utf8_string","value":"中"}}]},
                  {"type":"dict","keyType":"u8","valueLengthType":"u8","value":{
                   "2":{"type":"byte_seq","value":[0,-1]},
                   "1":{"type":"gb2312_string","value":"中"}}}]}
                """);
        for (final InputMode mode : InputMode.values()) {
            final DataField.Struct struct = assertInstanceOf(DataField.Struct.class, parse(input, mode));
            assertEquals("packet", struct.name());
            assertEquals(Map.of("x-note", "嵌套"), struct.attributes());
            assertSame(PrependLengthFieldType.u16, struct.prependLengthFieldType());
            final DataField.Sequence sequence = assertInstanceOf(DataField.Sequence.class, struct.value().get(1));
            assertSame(PrependLengthFieldType.u8, sequence.prependLengthFieldType());
            final DataField.SimpleTlvDataField<?> tlv = assertInstanceOf(DataField.SimpleTlvDataField.class, sequence.value().get(1));
            assertSame(LengthFieldType.u8, tlv.length());
            assertInstanceOf(DataField.U8.class, tlv.tag());
            assertInstanceOf(DataField.Utf8String.class, tlv.value());
            final DataField.Dict<?> dict = assertInstanceOf(DataField.Dict.class, struct.value().get(2));
            final List<DataField> entries = new ArrayList<>(dict.value().values());
            assertArrayEquals(new byte[]{0, -1}, assertInstanceOf(DataField.ByteSequence.class, entries.getFirst()).value());
            assertInstanceOf(DataField.Gb2312String.class, entries.get(1));
            final String hex = "0011010702030703e4b8ad020200ff0102d6d0";
            assertEncoded(struct, hex, tracked);
            final DataField restored = parse(serializedField(struct), mode);
            assertEquals(serializedField(struct), serializedField(restored));
            assertEncoded(restored, hex, tracked);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEFT", "RIGHT"})
    void genericStringPreservesCharsetPaddingAndLengthPrefix(String side) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"string","value":"中文","charset":"GBK","prependLengthFieldType":"u8",
                 "paddingConfig":{"paddingType":"%s","paddingElement":0,"minEncodedLength":6}}
                """.formatted(side));
        for (final InputMode mode : InputMode.values()) {
            final DataField.GenericString field = assertInstanceOf(DataField.GenericString.class, parse(input, mode));
            assertEquals("GBK", field.charset());
            assertEquals(side, field.paddingConfig().paddingType().name());
            assertEquals(0, field.paddingConfig().paddingElement());
            assertEquals(6, field.paddingConfig().minEncodedLength());
            assertEquals(serializedField(field), serializedField(parse(serializedField(field), mode)));
            assertEncoded(field, "04d6d0cec4", false);
            assertEncoded(field, "04d6d0cec4", true);
        }
    }

    @ParameterizedTest
    @EnumSource(InputMode.class)
    void dictionaryRoundTripPreservesCustomNameAndFlattenedAttributes(InputMode mode) throws Exception {
        final Map<DataField.U16, DataField> values = new LinkedHashMap<>();
        values.put(u16(2), utf8String("中"));
        final DataField.Dict<?> original = dict("business-dictionary", PrependLengthFieldType.u8,
                DataField.U16.class, LengthFieldType.u8, values, ATTRIBUTES);
        final ObjectNode serialized = serializedField(original);
        assertEquals("中文", serialized.get("x-note").asText());
        final DataField.Dict<?> restored = assertInstanceOf(DataField.Dict.class, parse(serialized, mode));
        assertAll(() -> assertEquals(original.name(), restored.name()),
                () -> assertEquals(ATTRIBUTES, restored.attributes()));
        assertEquals(serialized, serializedField(restored));
        assertEncoded(restored, "06000203e4b8ad", false);
        assertEncoded(restored, "06000203e4b8ad", true);
    }

    @ParameterizedTest
    @MethodSource("dictionaryKeyTypes")
    void dictionaryKeysPreserveNumericTypesAndExactBytes(DataField.DictKey expectedKey, String hex) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"dict","name":"numeric-key","prependLengthFieldType":"none",
                 "keyType":"%s","valueLengthType":"u8","value":{"%s":{"type":"u8","value":7}}}
                """.formatted(expectedKey.type(), expectedKey.value()));
        for (final InputMode mode : InputMode.values()) {
            final DataField.Dict<?> restored = assertInstanceOf(DataField.Dict.class, parse(input, mode));
            final DataField.DictKey actualKey = restored.value().keySet().iterator().next();
            assertSame(expectedKey.getClass(), restored.keyType());
            assertSame(expectedKey.getClass(), actualKey.getClass());
            assertEquals(expectedKey.value(), actualKey.value());
            assertEquals(serializedField(restored), serializedField(parse(serializedField(restored), mode)));
            assertEncoded(restored, hex + "0107", false);
            assertEncoded(restored, hex + "0107", true);
        }
    }

    static Stream<Arguments> dictionaryKeyTypes() {
        return Stream.of(
                Arguments.of(i8((byte) -128), "80"),
                Arguments.of(u8((short) 255), "ff"),
                Arguments.of(i16((short) -32768), "8000"),
                Arguments.of(u16(65535), "ffff"),
                Arguments.of(i32(Integer.MIN_VALUE), "80000000"),
                Arguments.of(u32(4294967295L), "ffffffff"),
                Arguments.of(i64(Long.MIN_VALUE), "8000000000000000"));
    }

    @ParameterizedTest
    @EnumSource(InputMode.class)
    void emptyDictionaryAcceptsAbsentOrNullNameWithoutInventingMetadata(InputMode mode) throws Exception {
        for (final String name : List.of("", ",\"name\":null")) {
            final ObjectNode input = (ObjectNode) JSON.readTree("""
                    {"type":"dict","keyType":"u8","valueLengthType":"u8","value":{}%s}
                    """.formatted(name));
            final DataField.Dict<?> restored = assertInstanceOf(DataField.Dict.class, parse(input, mode));
            assertEquals("Dict", restored.name());
            assertNotNull(restored.attributes());
            assertTrue(restored.attributes().isEmpty());
            assertTrue(restored.value().isEmpty());
            assertEncoded(restored, "", false);
            assertEncoded(restored, "", true);
        }
    }

    @ParameterizedTest
    @EnumSource(InputMode.class)
    void dictionaryExtensionValuesPreserveNullsListsAndNestedObjects(InputMode mode) throws Exception {
        final ObjectNode input = (ObjectNode) JSON.readTree("""
                {"type":"dict","name":"extensions","keyType":"u8","valueLengthType":"u8","value":{},
                 "x-null":null,"x-list":[1,true,{"nested":"中文"}],"attributes":{"a":111}}
                """);
        final DataField.Dict<?> restored = assertInstanceOf(DataField.Dict.class, parse(input, mode));
        assertNotNull(restored.attributes());
        assertEquals(3, restored.attributes().size());
        assertTrue(restored.attributes().containsKey("x-null"));
        assertNull(restored.attributes().get("x-null"));
        assertEquals(List.of(1, true, Map.of("nested", "中文")), restored.attributes().get("x-list"));
        assertEquals(Map.of("a", 111), restored.attributes().get("attributes"));
        input.put("prependLengthFieldType", "none");
        assertEquals(input, serializedField(restored));
    }

    @Test
    void supportsCustomDataFieldTypeNamesThroughAllInputs() throws Exception {
        final CustomAscii expected = new CustomAscii("AZ", PrependLengthFieldType.u8);
        for (final InputMode mode : InputMode.values()) {
            final DataField field = parse(serializedField(expected), mode);
            assertEquals(expected, assertInstanceOf(CustomAscii.class, field));
            assertEncoded(field, "02415a", false);
        }
    }

    @Test
    void mixedListsPreserveNullsAndEmptyListsRemainEmpty() throws Exception {
        final String json = mixedListToJsonString(java.util.Arrays.asList(u8((short) 1), null, utf8String("A")));
        final JsonNode tree = JSON.readTree(json);
        assertTrue(tree.get(1).isNull());
        for (final List<DataField> fields : List.of(parseSimpleFieldsFromJson(json),
                parseSimpleFieldsFromObject(JSON.convertValue(tree, Object.class)))) {
            assertEquals(3, fields.size());
            assertNull(fields.get(1));
            assertInstanceOf(DataField.U8.class, fields.getFirst());
            assertInstanceOf(DataField.Utf8String.class, fields.get(2));
        }
        assertEquals("[]", mixedListToJsonString(List.of()));
        assertTrue(parseSimpleFieldsFromJson("[]").isEmpty());
        assertTrue(parseSimpleFieldsFromObject(List.of()).isEmpty());
    }

    private static ObjectNode serializedField(DataField field) throws Exception {
        return (ObjectNode) JSON.readTree(mixedListToJsonString(List.of(field))).get(0);
    }

    private static DataField parse(ObjectNode input, InputMode mode) {
        final Map<String, Object> object = JSON.convertValue(input, new TypeReference<>() {
        });
        return switch (mode) {
            case JSON -> parseSimpleFieldFromJson(input.toString());
            case OBJECT -> parseSimpleFieldFromObject(object);
            case JSON_LIST -> parseSimpleFieldsFromJson("[" + input + "]").getFirst();
            case OBJECT_LIST -> parseSimpleFieldsFromObject(List.of(object)).getFirst();
        };
    }

    private static void assertEncoded(DataField field, String hex, boolean tracked) {
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        try {
            final CodecTracker tracker = new CodecTracker();
            if (tracked) {
                EntityCodec.DEFAULT.encode(field, output, tracker);
            } else {
                EntityCodec.DEFAULT.encode(field, output);
            }
            assertEquals("55" + hex, ByteBufUtil.hexDump(output));
            assertEquals(0, output.readerIndex());
        } finally {
            output.release();
        }
    }

    enum InputMode {
        JSON, OBJECT, JSON_LIST, OBJECT_LIST
    }

    public record CustomAscii(String value, PrependLengthFieldType prependLengthFieldType) implements DataField.CustomDataField {
        @Override
        public void writeTo(ByteBuf output) {
            output.writeCharSequence(value, StandardCharsets.US_ASCII);
        }
    }
}

/*
 * Copyright 2024 the original author or authors.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DataFields {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        OBJECT_MAPPER.registerModule(new SimpleModule()
                .addKeySerializer(DataField.DictKey.class, new DictKeyJsonSerializer())
                .addDeserializer(DataField.Dict.class, new DictJsonDeserializer(OBJECT_MAPPER))
        );
    }

    private DataFields() {
        throw new UnsupportedOperationException("No instance");
    }

    public static String mixedListToJsonString(List<?> list) {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        for (Object item : list) {
            arr.add(OBJECT_MAPPER.valueToTree(item));
        }
        return arr.toString();
    }

    public static DataField parseSimpleFieldFromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, DataField.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataField parseSimpleFieldFromObject(Object object) {
        return OBJECT_MAPPER.convertValue(object, DataField.class);
    }

    public static List<DataField> parseSimpleFieldsFromObject(Object object) {
        return OBJECT_MAPPER.convertValue(object, LIST_TYPE_REFERENCE);
    }

    public static List<DataField> parseSimpleFieldsFromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, LIST_TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static final TypeReference<List<DataField>> LIST_TYPE_REFERENCE = new TypeReference<>() {
    };

    // region 工厂方法

    public static DataField.I8 i8(String name, Byte value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.I8(name, value, attributes);
    }

    public static DataField.I8 i8(String name, Byte value) {
        return new DataField.I8(name, value, null);
    }

    public static DataField.I8 i8(Byte value) {
        return new DataField.I8(null, value, null);
    }

    public static DataField.U8 u8(String name, Short value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.U8(name, value, attributes);
    }

    public static DataField.U8 u8(String name, Short value) {
        return new DataField.U8(name, value, null);
    }

    public static DataField.U8 u8(Short value) {
        return new DataField.U8(null, value, null);
    }

    public static DataField.I16 i16(String name, Short value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.I16(name, value, attributes);
    }

    public static DataField.I16 i16(String name, Short value) {
        return new DataField.I16(name, value, null);
    }

    public static DataField.I16 i16(Short value) {
        return new DataField.I16(null, value, null);
    }

    public static DataField.U16 u16(String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.U16(name, value, attributes);
    }

    public static DataField.U16 u16(String name, Integer value) {
        return new DataField.U16(name, value, null);
    }

    public static DataField.U16 u16(Integer value) {
        return new DataField.U16(null, value, null);
    }

    public static DataField.U16 word(String name, Integer value) {
        return new DataField.U16(name, value, null);
    }

    public static DataField.U16 word(Integer value) {
        return new DataField.U16(null, value, null);
    }

    public static DataField.I32 i32(String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.I32(name, value, attributes);
    }

    public static DataField.I32 i32(String name, Integer value) {
        return new DataField.I32(name, value, null);
    }

    public static DataField.I32 i32(Integer value) {
        return new DataField.I32(null, value, null);
    }

    public static DataField.U32 u32(String name, Long value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.U32(name, value, attributes);
    }

    public static DataField.U32 u32(String name, Long value) {
        return new DataField.U32(name, value, null);
    }

    public static DataField.U32 u32(Long value) {
        return new DataField.U32(null, value, null);
    }

    public static DataField.U32 dword(String name, Long value) {
        return new DataField.U32(name, value, null);
    }

    public static DataField.U32 dword(Long value) {
        return new DataField.U32(null, value, null);
    }

    public static DataField.F32 f32(String name, Float value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.F32(name, value, attributes);
    }

    public static DataField.F32 f32(String name, Float value) {
        return new DataField.F32(name, value, null);
    }

    public static DataField.F32 f32(Float value) {
        return new DataField.F32(null, value, null);
    }

    public static DataField.I64 i64(String name, Long value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.I64(name, value, attributes);
    }

    public static DataField.I64 i64(String name, Long value) {
        return new DataField.I64(name, value, null);
    }

    public static DataField.I64 i64(Long value) {
        return new DataField.I64(null, value, null);
    }

    public static DataField.F64 f64(String name, Double value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.F64(name, value, attributes);
    }

    public static DataField.F64 f64(String name, Double value) {
        return new DataField.F64(name, value, null);
    }

    public static DataField.F64 f64(Double value) {
        return new DataField.F64(null, value, null);
    }

    public static DataField.GenericString string(String name, PrependLengthFieldType prependLengthFieldType, String value, String charset, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.GenericString(name, prependLengthFieldType, value, charset, attributes);
    }

    public static DataField.GenericString string(PrependLengthFieldType prependLengthFieldType, String value, String charset) {
        return new DataField.GenericString(null, prependLengthFieldType, value, charset, null);
    }

    public static DataField.GenericString string(String value, String charset) {
        return new DataField.GenericString(null, null, value, charset, null);
    }

    public static DataField.GbkString gbkString(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.GbkString(name, prependLengthFieldType, value, attributes);
    }

    public static DataField.GbkString gbkString(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.GbkString(name, prependLengthFieldType, value, null);
    }

    public static DataField.GbkString gbkString(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.GbkString(null, prependLengthFieldType, value, null);
    }

    public static DataField.GbkString gbkString(String name, String value) {
        return new DataField.GbkString(name, PrependLengthFieldType.none, value, null);
    }

    public static DataField.GbkString gbkString(String value) {
        return new DataField.GbkString(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.HexString hexString(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.HexString(name, prependLengthFieldType, value, null);
    }

    public static DataField.HexString hexString(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.HexString(null, prependLengthFieldType, value, null);
    }

    public static DataField.HexString hexString(String name, String value) {
        return new DataField.HexString(name, PrependLengthFieldType.none, value, null);
    }

    public static DataField.HexString hexString(String value) {
        return new DataField.HexString(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Bcd8421String bcd8421String(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Bcd8421String(name, prependLengthFieldType, value, null);
    }

    public static DataField.Bcd8421String bcd8421String(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Bcd8421String(null, prependLengthFieldType, value, null);
    }

    public static DataField.Bcd8421String bcd8421String(String name, String value) {
        return new DataField.Bcd8421String(name, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Bcd8421String bcd8421String(String value) {
        return new DataField.Bcd8421String(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Gb2312String gb2312String(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.Gb2312String(name, prependLengthFieldType, value, attributes);
    }

    public static DataField.Gb2312String gb2312String(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Gb2312String(null, prependLengthFieldType, value, null);
    }

    public static DataField.Gb2312String gb2312String(String name, String value) {
        return new DataField.Gb2312String(name, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Gb2312String gb2312String(String value) {
        return new DataField.Gb2312String(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Utf8String utf8String(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.Utf8String(name, prependLengthFieldType, value, attributes);
    }

    public static DataField.Utf8String utf8String(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Utf8String(name, prependLengthFieldType, value, null);
    }

    public static DataField.Utf8String utf8String(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Utf8String(null, prependLengthFieldType, value, null);
    }

    public static DataField.Utf8String utf8String(String name, String value) {
        return new DataField.Utf8String(name, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Utf8String utf8String(String value) {
        return new DataField.Utf8String(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.Struct struct(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> fields, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.Struct(name, prependLengthFieldType, fields, attributes);
    }

    public static DataField.Struct struct(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> fields) {
        return new DataField.Struct(name, prependLengthFieldType, fields, null);
    }

    public static DataField.Struct struct(PrependLengthFieldType prependLengthFieldType, List<DataField> fields) {
        return new DataField.Struct(null, prependLengthFieldType, fields, null);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    public static DataField.Struct struct(@Nullable DataField @Nullable ... fields) {
        if (fields == null || fields.length == 0) {
            return new DataField.Struct(null, PrependLengthFieldType.none, List.of(), null);
        }
        final List<@Nullable DataField> fieldList = Arrays.stream(fields).filter(Objects::nonNull).toList();
        return new DataField.Struct(null, PrependLengthFieldType.none, fieldList, null);
    }

    public static DataField.Struct struct(List<DataField> fields) {
        return new DataField.Struct(null, PrependLengthFieldType.none, fields, null);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(String name, PrependLengthFieldType prependLengthFieldType, Class<K> keyType, DataField.ValueLengthType valueLengthType, Map<K, DataField> value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.Dict<>(name, prependLengthFieldType, keyType, valueLengthType, value, attributes);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(String name, PrependLengthFieldType prependLengthFieldType, Class<K> keyType, DataField.ValueLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(name, prependLengthFieldType, keyType, valueLengthType, value, null);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(PrependLengthFieldType prependLengthFieldType, Class<K> keyType, DataField.ValueLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(null, prependLengthFieldType, keyType, valueLengthType, value, null);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(Class<K> keyType, DataField.ValueLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(null, null, keyType, valueLengthType, value, null);
    }

    public static <K extends DataField.DictKey> DataField.SimpleTlvDataField<K> tlv(String name, PrependLengthFieldType prependLengthFieldType, K tag, DataField.ValueLengthType valueLengthType, DataField value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.SimpleTlvDataField<>(name, prependLengthFieldType, tag, valueLengthType, value, attributes);
    }

    public static <K extends DataField.DictKey> DataField.SimpleTlvDataField<K> tlv(String name, PrependLengthFieldType prependLengthFieldType, K tag, DataField.ValueLengthType valueLengthType, DataField value) {
        return new DataField.SimpleTlvDataField<>(name, prependLengthFieldType, tag, valueLengthType, value, null);
    }

    public static <K extends DataField.DictKey> DataField.SimpleTlvDataField<K> tlv(PrependLengthFieldType prependLengthFieldType, K tag, DataField.ValueLengthType valueLengthType, DataField value) {
        return new DataField.SimpleTlvDataField<>(null, prependLengthFieldType, tag, valueLengthType, value, null);
    }

    public static <K extends DataField.DictKey> DataField.SimpleTlvDataField<K> tlv(K tag, DataField.ValueLengthType valueLengthType, DataField value) {
        return new DataField.SimpleTlvDataField<>(null, null, tag, valueLengthType, value, null);
    }

    public static DataField.Sequence sequence(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.Sequence(name, prependLengthFieldType, value, attributes);
    }

    public static DataField.Sequence sequence(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> value) {
        return new DataField.Sequence(name, prependLengthFieldType, value, null);
    }

    public static DataField.Sequence sequence(PrependLengthFieldType prependLengthFieldType, List<DataField> value) {
        return new DataField.Sequence(null, prependLengthFieldType, value, null);
    }

    public static DataField.Sequence sequence(List<DataField> value) {
        return new DataField.Sequence(null, PrependLengthFieldType.none, value, null);
    }

    public static DataField.ByteSequence byteSequence(String name, PrependLengthFieldType prependLengthFieldType, byte[] value, @Nullable Map<String, @Nullable Object> attributes) {
        return new DataField.ByteSequence(name, prependLengthFieldType, value, attributes);
    }

    public static DataField.ByteSequence byteSequence(String name, PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new DataField.ByteSequence(name, prependLengthFieldType, value, null);
    }

    public static DataField.ByteSequence byteSequence(PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new DataField.ByteSequence(null, prependLengthFieldType, value, null);
    }

    public static DataField.ByteSequence byteSequence(byte[] value) {
        return new DataField.ByteSequence(null, PrependLengthFieldType.none, value, null);
    }
    // endregion 工厂方法

}

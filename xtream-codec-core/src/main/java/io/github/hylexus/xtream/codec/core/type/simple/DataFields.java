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

    public static DataField.I8 i8(String name, Byte value) {
        return new DataField.I8(name, value);
    }

    public static DataField.I8 i8(Byte value) {
        return new DataField.I8(value);
    }

    public static DataField.U8 u8(String name, Short value) {
        return new DataField.U8(name, value);
    }

    public static DataField.U8 u8(Short value) {
        return new DataField.U8(value);
    }

    public static DataField.I16 i16(String name, Short value) {
        return new DataField.I16(name, value);
    }

    public static DataField.I16 i16(Short value) {
        return new DataField.I16(value);
    }

    public static DataField.U16 u16(String name, Integer value) {
        return new DataField.U16(name, value);
    }

    public static DataField.U16 u16(Integer value) {
        return new DataField.U16(value);
    }

    public static DataField.U16 word(String name, Integer value) {
        return new DataField.U16(name, value);
    }

    public static DataField.U16 word(Integer value) {
        return new DataField.U16(value);
    }

    public static DataField.I32 i32(String name, Integer value) {
        return new DataField.I32(name, value);
    }

    public static DataField.I32 i32(Integer value) {
        return new DataField.I32(value);
    }

    public static DataField.U32 u32(String name, Long value) {
        return new DataField.U32(name, value);
    }

    public static DataField.U32 u32(Long value) {
        return new DataField.U32(value);
    }

    public static DataField.U32 dword(String name, Long value) {
        return new DataField.U32(name, value);
    }

    public static DataField.U32 dword(Long value) {
        return new DataField.U32(value);
    }

    public static DataField.F32 f32(String name, Float value) {
        return new DataField.F32(name, value);
    }

    public static DataField.F32 f32(Float value) {
        return new DataField.F32(value);
    }

    public static DataField.I64 i64(String name, Long value) {
        return new DataField.I64(name, value);
    }

    public static DataField.I64 i64(Long value) {
        return new DataField.I64(value);
    }

    public static DataField.F64 f64(String name, Double value) {
        return new DataField.F64(name, value);
    }

    public static DataField.F64 f64(Double value) {
        return new DataField.F64(value);
    }

    public static DataField.GbkString gbkString(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.GbkString(name, prependLengthFieldType, value);
    }

    public static DataField.GbkString gbkString(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.GbkString(null, prependLengthFieldType, value);
    }

    public static DataField.GbkString gbkString(String name, String value) {
        return new DataField.GbkString(name, PrependLengthFieldType.none, value);
    }

    public static DataField.GbkString gbkString(String value) {
        return new DataField.GbkString(null, PrependLengthFieldType.none, value);
    }

    public static DataField.Gb2312String gb2312String(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Gb2312String(name, prependLengthFieldType, value);
    }

    public static DataField.Gb2312String gb2312String(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Gb2312String(null, prependLengthFieldType, value);
    }

    public static DataField.Gb2312String gb2312String(String name, String value) {
        return new DataField.Gb2312String(name, PrependLengthFieldType.none, value);
    }

    public static DataField.Gb2312String gb2312String(String value) {
        return new DataField.Gb2312String(null, PrependLengthFieldType.none, value);
    }

    public static DataField.Utf8String utf8String(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Utf8String(name, prependLengthFieldType, value);
    }

    public static DataField.Utf8String utf8String(PrependLengthFieldType prependLengthFieldType, String value) {
        return new DataField.Utf8String(null, prependLengthFieldType, value);
    }

    public static DataField.Utf8String utf8String(String name, String value) {
        return new DataField.Utf8String(name, PrependLengthFieldType.none, value);
    }

    public static DataField.Utf8String utf8String(String value) {
        return new DataField.Utf8String(null, PrependLengthFieldType.none, value);
    }

    public static DataField.Struct struct(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> fields) {
        return new DataField.Struct(name, prependLengthFieldType, fields);
    }

    public static DataField.Struct struct(PrependLengthFieldType prependLengthFieldType, List<DataField> fields) {
        return new DataField.Struct(null, prependLengthFieldType, fields);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    public static DataField.Struct struct(@Nullable DataField @Nullable ... fields) {
        if (fields == null || fields.length == 0) {
            return new DataField.Struct(null, PrependLengthFieldType.none, List.of());
        }
        final List<@Nullable DataField> fieldList = Arrays.stream(fields).filter(Objects::nonNull).toList();
        return new DataField.Struct(null, PrependLengthFieldType.none, fieldList);
    }

    public static DataField.Struct struct(List<DataField> fields) {
        return new DataField.Struct(null, PrependLengthFieldType.none, fields);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(String name, PrependLengthFieldType prependLengthFieldType, Class<K> keyType, DataField.KeyLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(name, prependLengthFieldType, keyType, valueLengthType, value);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(PrependLengthFieldType prependLengthFieldType, Class<K> keyType, DataField.KeyLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(null, prependLengthFieldType, keyType, valueLengthType, value);
    }

    public static <K extends DataField.DictKey> DataField.Dict<K> dict(Class<K> keyType, DataField.KeyLengthType valueLengthType, Map<K, DataField> value) {
        return new DataField.Dict<>(null, null, keyType, valueLengthType, value);
    }

    public static DataField.Sequence sequence(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> value) {
        return new DataField.Sequence(name, prependLengthFieldType, value);
    }

    public static DataField.Sequence sequence(PrependLengthFieldType prependLengthFieldType, List<DataField> value) {
        return new DataField.Sequence(null, prependLengthFieldType, value);
    }

    public static DataField.Sequence sequence(List<DataField> value) {
        return new DataField.Sequence(null, PrependLengthFieldType.none, value);
    }

    public static DataField.ByteSequence byteSequence(String name, PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new DataField.ByteSequence(name, prependLengthFieldType, value);
    }

    public static DataField.ByteSequence byteSequence(PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new DataField.ByteSequence(null, prependLengthFieldType, value);
    }

    public static DataField.ByteSequence byteSequence(byte[] value) {
        return new DataField.ByteSequence(null, PrependLengthFieldType.none, value);
    }
    // endregion 工厂方法

}

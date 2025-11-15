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

public final class SimpleFields {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        OBJECT_MAPPER.registerModule(new SimpleModule()
                .addKeySerializer(SimpleField.DictKey.class, new DictKeyJsonSerializer())
                .addDeserializer(SimpleField.Dict.class, new DictJsonDeserializer(OBJECT_MAPPER))
        );
    }

    private SimpleFields() {
        throw new UnsupportedOperationException("No instance");
    }

    public static String mixedListToJsonString(List<?> list) {
        final ArrayNode arr = OBJECT_MAPPER.createArrayNode();
        for (Object item : list) {
            arr.add(OBJECT_MAPPER.valueToTree(item));
        }
        return arr.toString();
    }

    public static SimpleField parseSimpleFieldFromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, SimpleField.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<SimpleField> parseSimpleFieldsFromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, LIST_TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static final TypeReference<List<SimpleField>> LIST_TYPE_REFERENCE = new TypeReference<>() {
    };

    // region 工厂方法

    public static SimpleField.I8 i8(String name, Byte value) {
        return new SimpleField.I8(name, value);
    }

    public static SimpleField.I8 i8(Byte value) {
        return new SimpleField.I8(value);
    }

    public static SimpleField.U8 u8(String name, Short value) {
        return new SimpleField.U8(name, value);
    }

    public static SimpleField.U8 u8(Short value) {
        return new SimpleField.U8(value);
    }

    public static SimpleField.I16 i16(String name, Short value) {
        return new SimpleField.I16(name, value);
    }

    public static SimpleField.I16 i16(Short value) {
        return new SimpleField.I16(value);
    }

    public static SimpleField.U16 u16(String name, Integer value) {
        return new SimpleField.U16(name, value);
    }

    public static SimpleField.U16 u16(Integer value) {
        return new SimpleField.U16(value);
    }

    public static SimpleField.U16 word(String name, Integer value) {
        return new SimpleField.U16(name, value);
    }

    public static SimpleField.U16 word(Integer value) {
        return new SimpleField.U16(value);
    }

    public static SimpleField.I32 i32(String name, Integer value) {
        return new SimpleField.I32(name, value);
    }

    public static SimpleField.I32 i32(Integer value) {
        return new SimpleField.I32(value);
    }

    public static SimpleField.U32 u32(String name, Long value) {
        return new SimpleField.U32(name, value);
    }

    public static SimpleField.U32 u32(Long value) {
        return new SimpleField.U32(value);
    }

    public static SimpleField.U32 dword(String name, Long value) {
        return new SimpleField.U32(name, value);
    }

    public static SimpleField.U32 dword(Long value) {
        return new SimpleField.U32(value);
    }

    public static SimpleField.F32 f32(String name, Float value) {
        return new SimpleField.F32(name, value);
    }

    public static SimpleField.F32 f32(Float value) {
        return new SimpleField.F32(value);
    }

    public static SimpleField.I64 i64(String name, Long value) {
        return new SimpleField.I64(name, value);
    }

    public static SimpleField.I64 i64(Long value) {
        return new SimpleField.I64(value);
    }

    public static SimpleField.F64 f64(String name, Double value) {
        return new SimpleField.F64(name, value);
    }

    public static SimpleField.F64 f64(Double value) {
        return new SimpleField.F64(value);
    }

    public static SimpleField.StrGbk strGbk(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrGbk(name, prependLengthFieldType, value);
    }

    public static SimpleField.StrGbk strGbk(PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrGbk(null, prependLengthFieldType, value);
    }

    public static SimpleField.StrGbk strGbk(String name, String value) {
        return new SimpleField.StrGbk(name, PrependLengthFieldType.none, value);
    }

    public static SimpleField.StrGbk strGbk(String value) {
        return new SimpleField.StrGbk(null, PrependLengthFieldType.none, value);
    }

    public static SimpleField.StrGb2312 strGb2312(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrGb2312(name, prependLengthFieldType, value);
    }

    public static SimpleField.StrGb2312 strGb2312(PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrGb2312(null, prependLengthFieldType, value);
    }

    public static SimpleField.StrGb2312 strGb2312(String name, String value) {
        return new SimpleField.StrGb2312(name, PrependLengthFieldType.none, value);
    }

    public static SimpleField.StrGb2312 strGb2312(String value) {
        return new SimpleField.StrGb2312(null, PrependLengthFieldType.none, value);
    }

    public static SimpleField.StrUtf8 strUtf8(String name, PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrUtf8(name, prependLengthFieldType, value);
    }

    public static SimpleField.StrUtf8 strUtf8(PrependLengthFieldType prependLengthFieldType, String value) {
        return new SimpleField.StrUtf8(null, prependLengthFieldType, value);
    }

    public static SimpleField.StrUtf8 strUtf8(String name, String value) {
        return new SimpleField.StrUtf8(name, PrependLengthFieldType.none, value);
    }

    public static SimpleField.StrUtf8 strUtf8(String value) {
        return new SimpleField.StrUtf8(null, PrependLengthFieldType.none, value);
    }

    public static SimpleField.Struct struct(String name, PrependLengthFieldType prependLengthFieldType, List<SimpleField> fields) {
        return new SimpleField.Struct(name, prependLengthFieldType, fields);
    }

    public static SimpleField.Struct struct(PrependLengthFieldType prependLengthFieldType, List<SimpleField> fields) {
        return new SimpleField.Struct(null, prependLengthFieldType, fields);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    public static SimpleField.Struct struct(@Nullable SimpleField @Nullable ... fields) {
        if (fields == null || fields.length == 0) {
            return new SimpleField.Struct(null, PrependLengthFieldType.none, List.of());
        }
        final List<@Nullable SimpleField> fieldList = Arrays.stream(fields).filter(Objects::nonNull).toList();
        return new SimpleField.Struct(null, PrependLengthFieldType.none, fieldList);
    }

    public static SimpleField.Struct struct(List<SimpleField> fields) {
        return new SimpleField.Struct(null, PrependLengthFieldType.none, fields);
    }

    public static <K extends SimpleField.DictKey> SimpleField.Dict<K> dict(String name, PrependLengthFieldType prependLengthFieldType, Class<K> keyType, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
        return new SimpleField.Dict<>(name, prependLengthFieldType, keyType, valueLengthType, value);
    }

    public static <K extends SimpleField.DictKey> SimpleField.Dict<K> dict(PrependLengthFieldType prependLengthFieldType, Class<K> keyType, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
        return new SimpleField.Dict<>(null, prependLengthFieldType, keyType, valueLengthType, value);
    }

    public static <K extends SimpleField.DictKey> SimpleField.Dict<K> dict(Class<K> keyType, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
        return new SimpleField.Dict<>(null, null, keyType, valueLengthType, value);
    }

    public static SimpleField.Sequence sequence(String name, PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
        return new SimpleField.Sequence(name, prependLengthFieldType, value);
    }

    public static SimpleField.Sequence sequence(PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
        return new SimpleField.Sequence(null, prependLengthFieldType, value);
    }

    public static SimpleField.Sequence sequence(List<SimpleField> value) {
        return new SimpleField.Sequence(null, PrependLengthFieldType.none, value);
    }

    public static SimpleField.ByteSequence byteSequence(String name, PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new SimpleField.ByteSequence(name, prependLengthFieldType, value);
    }

    public static SimpleField.ByteSequence byteSequence(PrependLengthFieldType prependLengthFieldType, byte[] value) {
        return new SimpleField.ByteSequence(null, prependLengthFieldType, value);
    }

    public static SimpleField.ByteSequence byteSequence(byte[] value) {
        return new SimpleField.ByteSequence(null, PrependLengthFieldType.none, value);
    }
    // endregion 工厂方法

}

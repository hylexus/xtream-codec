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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataFieldTypeIdResolver implements TypeIdResolver {

    @SuppressWarnings({"NullAway", "NotNullFieldNotInitialized"})
    private JavaType baseType;

    private static final Map<String, Class<? extends DataField>> TYPE_REGISTRY = new HashMap<>();

    static {
        TYPE_REGISTRY.put("i8", DataField.I8.class);
        TYPE_REGISTRY.put("int8", DataField.I8.class);

        TYPE_REGISTRY.put("u8", DataField.U8.class);
        TYPE_REGISTRY.put("uint8", DataField.U8.class);

        TYPE_REGISTRY.put("i16", DataField.I16.class);
        TYPE_REGISTRY.put("int16", DataField.I16.class);

        TYPE_REGISTRY.put("u16", DataField.U16.class);
        TYPE_REGISTRY.put("uint16", DataField.U16.class);

        TYPE_REGISTRY.put("i32", DataField.I32.class);
        TYPE_REGISTRY.put("int32", DataField.I32.class);

        TYPE_REGISTRY.put("u32", DataField.U32.class);
        TYPE_REGISTRY.put("uint32", DataField.U32.class);

        TYPE_REGISTRY.put("i64", DataField.I64.class);
        TYPE_REGISTRY.put("int64", DataField.I64.class);

        TYPE_REGISTRY.put("f32", DataField.F32.class);
        TYPE_REGISTRY.put("float32", DataField.F32.class);

        TYPE_REGISTRY.put("f64", DataField.F64.class);
        TYPE_REGISTRY.put("float64", DataField.F64.class);

        TYPE_REGISTRY.put("str", DataField.GenericString.class);
        TYPE_REGISTRY.put("string", DataField.GenericString.class);

        TYPE_REGISTRY.put("gbk_str", DataField.GbkString.class);
        TYPE_REGISTRY.put("gbk_string", DataField.GbkString.class);

        TYPE_REGISTRY.put("gb2312_str", DataField.Gb2312String.class);
        TYPE_REGISTRY.put("gb2312_string", DataField.Gb2312String.class);

        TYPE_REGISTRY.put("utf8_str", DataField.Utf8String.class);
        TYPE_REGISTRY.put("utf8_string", DataField.Utf8String.class);

        TYPE_REGISTRY.put("hex", DataField.HexString.class);
        TYPE_REGISTRY.put("hex_string", DataField.HexString.class);

        TYPE_REGISTRY.put("bcd_8421", DataField.Bcd8421String.class);
        TYPE_REGISTRY.put("bcd_8421_string", DataField.Bcd8421String.class);

        TYPE_REGISTRY.put("byte_sequence", DataField.ByteSequence.class);
        TYPE_REGISTRY.put("byte_seq", DataField.ByteSequence.class);
        TYPE_REGISTRY.put("bytes", DataField.ByteSequence.class);

        TYPE_REGISTRY.put("struct", DataField.Struct.class);

        TYPE_REGISTRY.put("list", DataField.Sequence.class);
        TYPE_REGISTRY.put("sequence", DataField.Sequence.class);
        TYPE_REGISTRY.put("seq", DataField.Sequence.class);

        TYPE_REGISTRY.put("map", DataField.Dict.class);
        TYPE_REGISTRY.put("dict", DataField.Dict.class);
        TYPE_REGISTRY.put("dictionary", DataField.Dict.class);

        TYPE_REGISTRY.put("tlv", DataField.SimpleTlvDataField.class);
    }

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        if (value instanceof DataField dataField) {
            return dataField.type();
        }
        return value.getClass().getSimpleName();
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public String idFromBaseType() {
        throw new IllegalStateException("Base type 'SimpleField' cannot be serialized directly");
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        Class<? extends DataField> clazz = TYPE_REGISTRY.get(id);
        if (clazz == null) {
            try {
                @SuppressWarnings("unchecked") final Class<? extends DataField> cls = (Class<? extends DataField>) Class.forName(id);
                clazz = cls;
            } catch (ClassNotFoundException e) {
                throw new JsonMappingException(null, "Unknown type id: `" + id + "`. Supported: " + TYPE_REGISTRY.keySet());
            }
        }
        return context.constructSpecializedType(baseType, clazz);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "SimpleField types";
    }
}

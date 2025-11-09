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

public class SimpleFieldTypeIdResolver implements TypeIdResolver {

    @SuppressWarnings("NullAway")
    private JavaType baseType;

    private static final Map<String, Class<? extends SimpleField>> TYPE_REGISTRY = new HashMap<>();

    static {
        TYPE_REGISTRY.put("i8", SimpleField.I8.class);
        TYPE_REGISTRY.put("int8", SimpleField.I8.class);

        TYPE_REGISTRY.put("u8", SimpleField.U8.class);
        TYPE_REGISTRY.put("uint8", SimpleField.U8.class);

        TYPE_REGISTRY.put("i16", SimpleField.I16.class);
        TYPE_REGISTRY.put("int16", SimpleField.I16.class);

        TYPE_REGISTRY.put("u16", SimpleField.U16.class);
        TYPE_REGISTRY.put("uint16", SimpleField.U16.class);

        TYPE_REGISTRY.put("i32", SimpleField.I32.class);
        TYPE_REGISTRY.put("int32", SimpleField.I32.class);

        TYPE_REGISTRY.put("u32", SimpleField.U32.class);
        TYPE_REGISTRY.put("uint32", SimpleField.U32.class);

        TYPE_REGISTRY.put("i64", SimpleField.I64.class);
        TYPE_REGISTRY.put("int64", SimpleField.I64.class);

        TYPE_REGISTRY.put("f32", SimpleField.F32.class);
        TYPE_REGISTRY.put("float32", SimpleField.F32.class);

        TYPE_REGISTRY.put("f64", SimpleField.F64.class);
        TYPE_REGISTRY.put("float64", SimpleField.F64.class);

        TYPE_REGISTRY.put("str", SimpleField.Str.class);
        TYPE_REGISTRY.put("string", SimpleField.Str.class);

        TYPE_REGISTRY.put("str_gbk", SimpleField.StrGbk.class);
        TYPE_REGISTRY.put("string_gbk", SimpleField.StrGbk.class);

        TYPE_REGISTRY.put("str_gb2312", SimpleField.StrGb2312.class);
        TYPE_REGISTRY.put("string_gb2312", SimpleField.StrGb2312.class);

        TYPE_REGISTRY.put("str_utf8", SimpleField.StrUtf8.class);
        TYPE_REGISTRY.put("string_utf8", SimpleField.StrUtf8.class);

        TYPE_REGISTRY.put("byte_seq", SimpleField.ByteSequence.class);
        TYPE_REGISTRY.put("bytes", SimpleField.ByteSequence.class);

        TYPE_REGISTRY.put("struct", SimpleField.Struct.class);

        TYPE_REGISTRY.put("list", SimpleField.Sequence.class);
        TYPE_REGISTRY.put("sequence", SimpleField.Sequence.class);
        TYPE_REGISTRY.put("seq", SimpleField.Sequence.class);

        TYPE_REGISTRY.put("map", SimpleField.Dict.class);
        TYPE_REGISTRY.put("dict", SimpleField.Dict.class);
        TYPE_REGISTRY.put("dictionary", SimpleField.Dict.class);
    }

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        if (value instanceof SimpleField simpleField) {
            return simpleField.type();
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
        Class<? extends SimpleField> clazz = TYPE_REGISTRY.get(id);
        if (clazz == null) {
            try {
                @SuppressWarnings("unchecked") final Class<? extends SimpleField> cls = (Class<? extends SimpleField>) Class.forName(id);
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

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

package io.github.hylexus.xtream.codec.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class XtreamCodecDebugJsonSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        switch (value) {
            case byte[] array -> writeJsonArray(array, jsonGenerator);
            case DataWrapper<?> wrapper -> {
                switch (wrapper) {
                    case U32Wrapper u32Wrapper -> provider.defaultSerializeValue(u32Wrapper.asU32(), jsonGenerator);
                    case U16Wrapper u16Wrapper -> provider.defaultSerializeValue(u16Wrapper.asU16(), jsonGenerator);
                    case U8Wrapper u8Wrapper -> provider.defaultSerializeValue(u8Wrapper.asU8(), jsonGenerator);
                    case StringWrapperGbk stringWrapper -> provider.defaultSerializeValue(stringWrapper.asString(), jsonGenerator);
                    case StringWrapperUtf8 stringWrapper -> provider.defaultSerializeValue(stringWrapper.asString(), jsonGenerator);
                    case StringWrapperBcd stringWrapper -> provider.defaultSerializeValue(stringWrapper.asString(), jsonGenerator);
                    case I32Wrapper i32Wrapper -> provider.defaultSerializeValue(i32Wrapper.asI32(), jsonGenerator);
                    case I16Wrapper i16Wrapper -> provider.defaultSerializeValue(i16Wrapper.asI16(), jsonGenerator);
                    case I8Wrapper i8Wrapper -> provider.defaultSerializeValue(i8Wrapper.asI8(), jsonGenerator);
                    case BytesDataWrapper bytesDataWrapper -> writeJsonArray(bytesDataWrapper.asBytes(), jsonGenerator);
                    default -> provider.defaultSerializeValue(value, jsonGenerator);
                }
            }
            case null, default -> provider.defaultSerializeValue(value, jsonGenerator);
        }
    }

    private static void writeJsonArray(byte @Nullable [] array, JsonGenerator jsonGenerator) throws IOException {
        if (array == null) {
            return;
        }
        jsonGenerator.writeStartArray();
        for (byte b : array) {
            jsonGenerator.writeNumber(b);
        }
        jsonGenerator.writeEndArray();
    }

}

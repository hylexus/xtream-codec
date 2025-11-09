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

package io.github.hylexus.xtream.codec.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.type.simple.DictJsonDeserializer;
import io.github.hylexus.xtream.codec.core.type.simple.DictKeyJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.simple.SimpleField;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@NullMarked
@ApiStatus.Experimental
public class SimpleFieldEncoder {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        OBJECT_MAPPER.registerModule(new SimpleModule()
                .addKeySerializer(SimpleField.DictKey.class, new DictKeyJsonSerializer())
                .addDeserializer(SimpleField.Dict.class, new DictJsonDeserializer(OBJECT_MAPPER))
        );
    }

    public void encode(FieldCodec.SerializeContext context, Iterable<? extends @Nullable SimpleField> simpleFields, ByteBuf output) {
        for (final SimpleField simpleField : simpleFields) {
            if (simpleField == null) {
                continue;
            }
            this.encode(context, simpleField, output);
        }
    }

    public void encode(FieldCodec.SerializeContext context, @Nullable SimpleField simpleField, ByteBuf output) {
        if (simpleField == null) {
            return;
        }
        final PrependLengthFieldType prependLengthFieldType = simpleField.prependLengthFieldType();
        final int prependLengthFieldTypeByteCounts = prependLengthFieldType.getByteCounts();
        if (prependLengthFieldTypeByteCounts <= 0) {
            this.doEncodeField(context, output, simpleField);
        } else {
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeField(context, output, simpleField);

            final int afterEncode = output.writerIndex();
            final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            output.writerIndex(afterEncode);
        }
    }

    private void doEncodeField(FieldCodec.SerializeContext context, ByteBuf output, SimpleField simpleField) {
        switch (simpleField) {
            case SimpleField.I8(Byte value) -> output.writeByte(value);
            case SimpleField.U8(Short value) -> output.writeByte(value);
            case SimpleField.I16(Short value) -> output.writeShort(value);
            case SimpleField.U16(Integer value) -> output.writeShort(value);
            case SimpleField.I32(Integer value) -> output.writeInt(value);
            case SimpleField.U32(Long value) -> output.writeInt(value.intValue());
            case SimpleField.I64(Long value) -> output.writeLong(value);
            case SimpleField.F32(Float value) -> output.writeFloat(value);
            case SimpleField.F64(Double value) -> output.writeDouble(value);
            case SimpleField.Str(String value, String charset, var ignored) -> encodeString(output, value, Charset.forName(charset));
            case SimpleField.StrGbk(String value, var ignored) -> encodeString(output, value, XtreamConstants.CHARSET_GBK);
            case SimpleField.StrGb2312(String value, var ignored) -> encodeString(output, value, XtreamConstants.CHARSET_GB_2312);
            case SimpleField.StrUtf8(String value, var ignored) -> encodeString(output, value, XtreamConstants.CHARSET_UTF8);
            case SimpleField.ByteSequence(byte[] value, PrependLengthFieldType ignored) -> output.writeBytes(value);
            case SimpleField.Struct(List<SimpleField> value, var ignored) -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case SimpleField.Sequence(List<SimpleField> value, var ignored) -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case SimpleField.Dict<?> simpleMap -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, simpleMap);
                final Map<? extends SimpleField.DictKey, SimpleField> map = simpleMap.value();
                final ByteBuf temp = context.bufferFactory().buffer();
                for (Map.Entry<? extends SimpleField.DictKey, SimpleField> entry : map.entrySet()) {
                    try {
                        // 1. key
                        final SimpleField.DictKey key = entry.getKey();
                        this.doEncodeField(newContext, output, key);
                        // 2. value
                        final SimpleField value = entry.getValue();
                        this.doEncodeField(newContext, temp, value);
                        // 3. valueLength
                        final int valueLength = temp.writerIndex();
                        simpleMap.valueLengthType().writeTo(output, valueLength);
                        output.writeBytes(temp);
                    } finally {
                        temp.clear();
                    }
                }
            }
            case SimpleField.CustomSimpleField customSimpleField -> customSimpleField.writeTo(output);
        }
    }

    private static void encodeString(ByteBuf output, String value, Charset charset) {
        output.writeCharSequence(value, charset);
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

    static final TypeReference<List<SimpleField>> LIST_TYPE_REFERENCE = new TypeReference<>() {
    };

    public static List<SimpleField> parseSimpleFieldListFromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, LIST_TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

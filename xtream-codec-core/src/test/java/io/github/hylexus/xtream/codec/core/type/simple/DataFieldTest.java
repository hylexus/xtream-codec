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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.type.PaddingConfig;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;

class DataFieldTest extends BaseEntityCodecTest {

    final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    record CustomDataField1(
            String value, U16 haha,
            PrependLengthFieldType prependLengthFieldType
    ) implements DataField.CustomDataField {

        @Override
        public void writeTo(@NotNull ByteBuf output) {
            output.writeCharSequence(value, StandardCharsets.UTF_8);
        }

    }

    @Test
    void test7() throws Exception {
        final DataField.SimpleTlvDataField<DataField.U8> tlv = tlv(u8((short) 1), LengthFieldType.u32, gbkString("111"));
        System.out.println(objectMapper.writeValueAsString(tlv));
    }

    @Test
    void test6() throws Exception {
        final Object data = new DataField.GbkString(null, PrependLengthFieldType.u8, "222", PaddingConfig.none(), Map.of("a", 1, "b", "222"));
        System.out.println("Encode: " + objectMapper.writeValueAsString(data));
        final DataField dataField = parseSimpleFieldFromJson("""
                {
                    "type": "gbk_string",
                    "name": "GbkString",
                    "prependLengthFieldType": "u8",
                    "value": "222",
                    "attributes": {
                        "a": 111
                    },
                    "x-a": 111,
                    "x-b": "111"
                }
                """);
        System.out.println("Decode: " + dataField);
        System.out.println(objectMapper.writeValueAsString(dataField));
        System.out.println(parseSimpleFieldFromObject(Map.of("type", "u8", "value", 1, "name", "n", "desc", "...")));
        System.out.println(parseSimpleFieldsFromObject(List.of(Map.of("type", "u8", "value", 1, "name", "n", "desc", "..."), Map.of("type", "u8", "value", 1, "name", "n", "desc", "..."))));
    }

    @Test
    void test5() throws Exception {
        final String json = objectMapper.writeValueAsString(new CustomDataField1("haha", u16(2), PrependLengthFieldType.u8));
        System.out.println(json);
        final Object o = DataFields.parseSimpleFieldFromJson(json);
        System.out.println(o);
    }

    @Test
    void test3() throws Exception {
        final Object data = gbkString(PrependLengthFieldType.u8, "222");
        System.out.println(objectMapper.writeValueAsString(data));

        final List<Object> data1 = List.of(
                gbkString(PrependLengthFieldType.u8, "222"),
                string(PrependLengthFieldType.u16, "222", "utf-8"),
                i8((byte) 1),
                u8((short) 2),
                struct(List.of(i16((short) 11), u16(22))),
                sequence(List.of(i32(111), u32(222L))),
                dict(
                        DataField.U16.class,
                        LengthFieldType.u8,
                        Map.of(
                                u16(1), gbkString("1111"),
                                u16(2), gbkString("2222"),
                                u16(3), gb2312String("2222"),
                                u16(4), byteSequence(new byte[]{11, 22}),
                                u16(5), utf8String("xxx"),
                                u16(6), new CustomDataField1("value", u16(111), PrependLengthFieldType.none)
                        )
                ),
                byteSequence(PrependLengthFieldType.u8, new byte[]{1, 2, 3}),
                tlv(u8((short) 1), LengthFieldType.u16, gbkString("222")),
                new CustomDataField1("haha", u16(1), PrependLengthFieldType.u8)
        );
        System.out.println(DataFields.mixedListToJsonString(data1));
    }


    @Test
    void test4() {
        String json = """
                [
                    {
                        "type": "gbk_string",
                        "name": "GbkString",
                        "prependLengthFieldType": "u8",
                        "value": "222"
                    },
                    {
                        "type": "string",
                        "name": "GenericString",
                        "prependLengthFieldType": "u16",
                        "value": "222",
                        "charset": "utf-8"
                    },
                    {
                        "type": "i8",
                        "name": "I8",
                        "value": 1
                    },
                    {
                        "type": "u8",
                        "name": "U8",
                        "value": 2
                    },
                    {
                        "type": "struct",
                        "name": "Struct",
                        "prependLengthFieldType": "none",
                        "value": [
                            {
                                "type": "i16",
                                "name": "I16",
                                "value": 11
                            },
                            {
                                "type": "u16",
                                "name": "U16",
                                "value": 22
                            }
                        ]
                    },
                    {
                        "type": "seq",
                        "name": "Sequence",
                        "prependLengthFieldType": "none",
                        "value": [
                            {
                                "type": "i32",
                                "name": "I32",
                                "value": 111
                            },
                            {
                                "type": "u32",
                                "name": "U32",
                                "value": 222
                            }
                        ]
                    },
                    {
                        "type": "dict",
                        "name": "Dict",
                        "prependLengthFieldType": "none",
                        "keyType": "u16",
                        "valueLengthType": "u8",
                        "value": {
                            "1": {
                                "type": "gbk_string",
                                "name": "GbkString",
                                "prependLengthFieldType": "none",
                                "value": "1111"
                            },
                            "2": {
                                "type": "gbk_string",
                                "name": "GbkString",
                                "prependLengthFieldType": "none",
                                "value": "2222"
                            },
                            "3": {
                                "type": "gb2312_string",
                                "name": "Gb2312String",
                                "prependLengthFieldType": "none",
                                "value": "2222"
                            },
                            "4": {
                                "type": "byte_seq",
                                "name": "ByteSequence",
                                "prependLengthFieldType": "none",
                                "value": [
                                    11,
                                    22
                                ]
                            },
                            "5": {
                                "type": "utf8_string",
                                "name": "Utf8String",
                                "prependLengthFieldType": "none",
                                "value": "xxx"
                            },
                            "6": {
                                "type": "io.github.hylexus.xtream.codec.core.type.simple.DataFieldTest$CustomDataField1",
                                "value": "value",
                                "haha": {
                                    "type": "u16",
                                    "name": "U16",
                                    "value": 111
                                },
                                "prependLengthFieldType": "none"
                            }
                        }
                    },
                    {
                        "type": "byte_seq",
                        "name": "ByteSequence",
                        "prependLengthFieldType": "u8",
                        "value": [
                            1,
                            2,
                            3
                        ]
                    },
                    {
                        "type": "tlv",
                        "name": "Tlv",
                        "prependLengthFieldType": "none",
                        "tag": {
                            "type": "u8",
                            "name": "U8",
                            "value": 1
                        },
                        "length": "u16",
                        "value": {
                            "type": "gbk_string",
                            "name": "GbkString",
                            "prependLengthFieldType": "none",
                            "value": "222"
                        }
                    },
                    {
                        "type": "io.github.hylexus.xtream.codec.core.type.simple.DataFieldTest$CustomDataField1",
                        "value": "haha",
                        "haha": {
                            "type": "u16",
                            "name": "U16",
                            "value": 1
                        },
                        "prependLengthFieldType": "u8"
                    }
                ]
                """;
        final Object object = DataFields.parseSimpleFieldsFromJson(json);
        System.out.println(object);
    }

    @Test
    void test2() throws Exception {
        final Object data = gbkString(PrependLengthFieldType.u8, "222");
        System.out.println(objectMapper.writeValueAsString(data));
        final ByteBuf buffer = allocator.buffer();
        this.entityCodec.encode(data, buffer);
        System.out.println(FormatUtils.toHexString(buffer));
    }

    @Test
    void test() throws Exception {
        final List<DataField> data = List.of(
                i8((byte) 1),
                u32(222L),
                gbkString(PrependLengthFieldType.u8, "222"),
                gbkString(PrependLengthFieldType.u16, "222"),
                utf8String(PrependLengthFieldType.u8, "222")
        );
        final ObjectWriter objectWriter = objectMapper.writerFor(new TypeReference<List<DataField>>() {
        });
        System.out.println(objectWriter.writeValueAsString(data));
        final ByteBuf buffer = allocator.buffer();
        this.entityCodec.encode(data, buffer);
        System.out.println(FormatUtils.toHexString(buffer));
    }

}

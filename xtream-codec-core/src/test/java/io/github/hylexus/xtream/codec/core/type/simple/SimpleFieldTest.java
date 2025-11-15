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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.type.simple.SimpleFields.*;

class SimpleFieldTest extends BaseEntityCodecTest {

    final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    record CustomSimpleField1(String value, U16 haha, PrependLengthFieldType prependLengthFieldType) implements SimpleField.CustomSimpleField {

        @Override
        public void writeTo(@NotNull ByteBuf output) {
            output.writeCharSequence(value, StandardCharsets.UTF_8);
        }

    }

    @Test
    void test5() throws Exception {
        final String json = objectMapper.writeValueAsString(new CustomSimpleField1("haha", u16(2), PrependLengthFieldType.u8));
        System.out.println(json);
        final Object o = SimpleFields.parseSimpleFieldFromJson(json);
        System.out.println(o);
    }

    @Test
    void test3() throws Exception {
        final Object data = strGbk(PrependLengthFieldType.u8, "222");
        System.out.println(objectMapper.writeValueAsString(data));

        final List<Object> data1 = List.of(
                strGbk(PrependLengthFieldType.u8, "222"),
                new SimpleField.Str("222", "utf-8", PrependLengthFieldType.u16),
                i8((byte) 1),
                u8((short) 2),
                struct(List.of(i16((short) 11), u16(22))),
                sequence(List.of(i32(111), u32(222L))),
                dict(
                        SimpleField.U16.class,
                        SimpleField.KeyLengthType.u8,
                        Map.of(
                                u16(1), strGbk("1111"),
                                u16(2), strGbk("2222"),
                                u16(3), strGb2312("2222"),
                                u16(4), byteSequence(new byte[]{11, 22}),
                                u16(5), strUtf8("xxx")
                        )
                ),
                byteSequence(PrependLengthFieldType.u8, new byte[]{1, 2, 3}),
                new CustomSimpleField1("haha", u16(1), PrependLengthFieldType.u8)
        );
        System.out.println(SimpleFields.mixedListToJsonString(data1));
    }


    @Test
    void test4() {
        String json = """
                [
                    {
                        "type": "str_gbk",
                        "value": "222",
                        "prependLengthFieldType": "u8"
                    },
                    {
                        "type": "str",
                        "value": "222",
                        "charset": "utf-8",
                        "prependLengthFieldType": "u16"
                    },
                    {
                        "type": "i8",
                        "value": 1
                    },
                    {
                        "type": "u8",
                        "value": 2
                    },
                    {
                        "type": "struct",
                        "value": [
                            {
                                "type": "i16",
                                "value": 11
                            },
                            {
                                "type": "u16",
                                "value": 22
                            }
                        ],
                        "prependLengthFieldType": "none"
                    },
                    {
                        "type": "seq",
                        "value": [
                            {
                                "type": "i32",
                                "value": 111
                            },
                            {
                                "type": "u32",
                                "value": 222
                            }
                        ],
                        "prependLengthFieldType": "none"
                    },
                    {
                        "type": "dict",
                        "keyType": "u16",
                        "valueLengthType": "u8",
                        "value": {
                            "1": {
                                "type": "str_gbk",
                                "value": "1111",
                                "prependLengthFieldType": "none"
                            },
                            "2": {
                                "type": "str_gbk",
                                "value": "2222",
                                "prependLengthFieldType": "none"
                            }
                        },
                        "prependLengthFieldType": "none"
                    },
                    {
                        "type": "byte_seq",
                        "value": [
                            1,
                            2,
                            3
                        ],
                        "prependLengthFieldType": "u8"
                    },
                    {
                        "type": "io.github.hylexus.xtream.codec.core.type.simple.SimpleFieldTest$CustomSimpleField1",
                        "value": "haha",
                        "haha": {
                            "type": "u16",
                            "value": 1
                        },
                        "prependLengthFieldType": "u8"
                    }
                ]
                """;
        final Object object = SimpleFields.parseSimpleFieldsFromJson(json);
        System.out.println(object);
    }

    @Test
    void test2() throws Exception {
        final Object data = strGbk(PrependLengthFieldType.u8, "222");
        System.out.println(objectMapper.writeValueAsString(data));
        final ByteBuf buffer = allocator.buffer();
        this.entityCodec.encode(data, buffer);
        System.out.println(FormatUtils.toHexString(buffer));
    }

    @Test
    void test() throws Exception {
        final List<SimpleField> data = List.of(
                i8((byte) 1),
                strGbk(PrependLengthFieldType.u8, "222")
        );
        final ObjectWriter objectWriter = objectMapper.writerFor(new TypeReference<List<SimpleField>>() {
        });
        System.out.println(objectWriter.writeValueAsString(data));
        final ByteBuf buffer = allocator.buffer();
        this.entityCodec.encode(data, buffer);
        System.out.println(FormatUtils.toHexString(buffer));
    }

}

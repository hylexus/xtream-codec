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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamDateTimeField;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.*;
import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.KeyType.*;

@Data
@Accessors(chain = true)
public class MapFieldEntity {

    @Preset.RustStyle.u8
    private int messageId;

    @XtreamMapField(
            key = {
                    @XtreamMapField.Key(version = 0, type = u16),
                    @XtreamMapField.Key(version = {1}, type = u32),
                    @XtreamMapField.Key(version = 2, type = str_gbk, sizeInBytes = 8, paddingType = PaddingType.LEFT, paddingElement = 'x'),
                    // @XtreamMapField.Key(version = 2, type = str, sizeInBytes = 11,charset = "UTF-8"),
                    // @XtreamMapField.Key(version = 2, type = str, sizeInBytes = 8,charset = "gb2312"),
            },
            valueLength = {
                    @XtreamMapField.ValueLength(version = 0, type = ValueLengthType.u8),
                    @XtreamMapField.ValueLength(version = {1, 2}, type = ValueLengthType.u16),
            },
            value = @Value(
                    // 编码(序列化)配置
                    encoder = @ValueEncoder(
                            params = {
                                    @EncoderParam(version = {0, 2}, charset = "gbk"),
                                    @EncoderParam(version = 1, charset = "UTF-8"),
                            },
                            matchers = {
                                    @ValueMatcher(version = 0, matchU16 = 61, valueType = XtreamDataType.string_utf8),
                                    @ValueMatcher(version = 0, matchU16 = 62, valueCodec = StringFieldCodecs.StringFieldCodec.class),
                                    @ValueMatcher(version = 0, matchU16 = 63, valueCodec = StringFieldCodecs.StringFieldCodecGb2312.class),
                                    @ValueMatcher(version = 0, matchU16 = 12, valueType = XtreamDataType.u16_le),
                                    @ValueMatcher(version = 0, matchU16 = 13, valueType = XtreamDataType.u32),
                                    @ValueMatcher(version = 0, matchU16 = 14, valueType = XtreamDataType.u32),

                                    @ValueMatcher(version = 1, matchU32 = 61, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(version = 1, matchU32 = 62, valueCodec = StringFieldCodecs.StringFieldCodec.class, charset = "gbk"),
                                    @ValueMatcher(version = 1, matchU32 = 63, valueCodec = StringFieldCodecs.StringFieldCodecGbk.class),
                                    @ValueMatcher(version = 1, matchU32 = 12, valueType = XtreamDataType.u16_le),
                                    @ValueMatcher(version = 1, matchU32 = 14, valueType = XtreamDataType.u32),

                                    @ValueMatcher(version = 2, matchString = "配置项61", valueType = XtreamDataType.string_utf8),
                                    @ValueMatcher(version = 2, matchString = "配置项62", valueCodec = StringFieldCodecs.StringFieldCodec.class),
                                    @ValueMatcher(version = 2, matchString = "配置项63", valueCodec = StringFieldCodecs.StringFieldCodecGb2312.class),
                                    @ValueMatcher(version = 2, matchString = "配置项12", valueType = XtreamDataType.u16_le),
                                    @ValueMatcher(version = 2, matchString = "配置项13", valueType = XtreamDataType.u32),
                                    @ValueMatcher(version = 2, matchString = "配置14", valueType = XtreamDataType.u32),
                            }
                    ),
                    // 解码(反序列化)配置
                    decoder = @ValueDecoder(
                            params = {
                                    @DecoderParam(version = 1, charset = "UTF-8"),
                                    @DecoderParam(version = {0, 2}, charset = "gbk"),
                            },
                            matchers = {
                                    @ValueMatcher(version = 0, matchU16 = 11, valueType = XtreamDataType.i8),
                                    @ValueMatcher(version = 0, matchU16 = 12, valueType = XtreamDataType.u16_le),
                                    @ValueMatcher(version = 0, matchU16 = 13, valueType = XtreamDataType.u32),
                                    @ValueMatcher(version = 0, matchU16 = 14, valueCodec = U32FieldCodecs.U32FieldCodec.class),
                                    @ValueMatcher(version = 0, matchU16 = 61, valueType = XtreamDataType.string_utf8),
                                    @ValueMatcher(version = 0, matchU16 = 62, valueType = XtreamDataType.string, charset = "GBK"),
                                    @ValueMatcher(version = 0, matchU16 = 63, valueCodec = StringFieldCodecs.StringFieldCodecGb2312.class),
                                    @ValueMatcher(version = 0, matchU16 = 64, valueType = XtreamDataType.string),
                                    @ValueMatcher(version = 0, matchU16 = 71, valueEntity = NestedEntity01.class),

                                    @ValueMatcher(version = 1, matchU32 = 11L, valueType = XtreamDataType.i8),
                                    @ValueMatcher(version = 1, matchU32 = 12L, valueType = XtreamDataType.u16),
                                    @ValueMatcher(version = 1, matchU32 = 13L, valueType = XtreamDataType.i8),
                                    @ValueMatcher(version = 1, matchU32 = 14L, valueCodec = U32FieldCodecs.U32FieldCodec.class),
                                    @ValueMatcher(version = 1, matchU32 = 61L, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(version = 1, matchU32 = 62L, valueType = XtreamDataType.string, charset = "GBK"),
                                    @ValueMatcher(version = 1, matchU32 = 63L, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(version = 1, matchU32 = 64L, valueCodec = StringFieldCodecs.StringFieldCodec.class, charset = "utf-8"),
                                    @ValueMatcher(version = 1, matchU32 = 71L, valueEntity = NestedEntity01.class),

                                    @ValueMatcher(version = 2, matchString = "配置项11", valueType = XtreamDataType.i8),
                                    @ValueMatcher(version = 2, matchString = "配置项12", valueType = XtreamDataType.u16_le),
                                    @ValueMatcher(version = 2, matchString = "配置项13", valueType = XtreamDataType.u32),
                                    // 下面这个 `x` 是自动 `paddingType = PaddingType.LEFT, paddingElement = 'x'` 添加的
                                    @ValueMatcher(version = 2, matchString = "xx配置14", valueCodec = U32FieldCodecs.U32FieldCodec.class),
                                    @ValueMatcher(version = 2, matchString = "配置项61", valueType = XtreamDataType.string_utf8),
                                    @ValueMatcher(version = 2, matchString = "配置项62", valueType = XtreamDataType.string, charset = "GBK"),
                                    @ValueMatcher(version = 2, matchString = "配置项63", valueCodec = StringFieldCodecs.StringFieldCodecGb2312.class),
                                    @ValueMatcher(version = 2, matchString = "配置项64", valueType = XtreamDataType.string),
                                    @ValueMatcher(version = 2, matchString = "配置项71", valueEntity = NestedEntity01.class),
                            },
                            fallbackMatchers = {
                                    @FallbackValueMatcher(version = ALL_VERSION, valueType = XtreamDataType.string_hex, charset = "HEX"),
                                    @FallbackValueMatcher(version = {1, 2}, valueCodec = StringFieldCodecs.StringFieldCodecHex.class),
                            }
                    )
            )
    )
    private Map<Object, Object> map;

    @Data
    @Accessors(chain = true)
    public static class NestedEntity01 {
        @Preset.RustStyle.u16
        private int id;

        @Preset.RustStyle.str(charset = "gbk", prependLengthFieldType = PrependLengthFieldType.u8)
        private String name;

        @XtreamDateTimeField(charset = "utf-8", pattern = "yyyy-MM-dd HH:mm:ss", prependLengthFieldType = PrependLengthFieldType.u8)
        private Date date;
    }
}

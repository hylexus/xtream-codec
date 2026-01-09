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

package io.github.hylexus.xtream.codec.core.record;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RustStyleRecordTest {

    public record RecordBasedEntityRustStyle(
            @Preset.RustStyle.u16 int messageId,
            @Preset.RustStyle.struct RecordBasedUserInfoRustStyle userInfo
    ) {
    }

    public record RecordBasedUserInfoRustStyle(
            @Preset.RustStyle.u32 long id,
            @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8) String name,
            @Preset.RustStyle.simple_map(
                    desc = "附加信息",
                    key = @Key(type = KeyType.u16),
                    valueLength = @ValueLength(type = ValueLengthType.u8),
                    value = @Value(
                            encoder = @ValueEncoder(
                                    params = {@EncoderParam(charset = "GBK")},
                                    matchers = {
                                            @ValueMatcher(matchU16 = 1, valueType = XtreamDataType.u32),
                                            @ValueMatcher(matchU16 = 2, valueType = XtreamDataType.string_utf8),
                                            // charset 从 params 继承而来(GBK)
                                            @ValueMatcher(matchU16 = 3, valueType = XtreamDataType.string),
                                            @ValueMatcher(matchU16 = 4, valueType = XtreamDataType.string_gbk),
                                            @ValueMatcher(matchU16 = 5, valueType = XtreamDataType.string, charset = "utf-8"),
                                            @ValueMatcher(matchU16 = 6, valueType = XtreamDataType.string, charset = "utf-8"),
                                            @ValueMatcher(matchU16 = 7, valueType = XtreamDataType.string_gb_2312),
                                            @ValueMatcher(matchU16 = 8, valueType = XtreamDataType.i8),
                                            @ValueMatcher(matchU16 = 9, valueType = XtreamDataType.u8),
                                            @ValueMatcher(matchU16 = 10, valueType = XtreamDataType.i16),
                                            @ValueMatcher(matchU16 = 11, valueType = XtreamDataType.i16_le),
                                            @ValueMatcher(matchU16 = 12, valueType = XtreamDataType.u16),
                                            @ValueMatcher(matchU16 = 13, valueType = XtreamDataType.u16_le),
                                            @ValueMatcher(matchU16 = 14, valueType = XtreamDataType.i32),
                                            @ValueMatcher(matchU16 = 15, valueType = XtreamDataType.i32_le),
                                            @ValueMatcher(matchU16 = 16, valueType = XtreamDataType.u32),
                                            @ValueMatcher(matchU16 = 17, valueType = XtreamDataType.u32_le),
                                            @ValueMatcher(matchU16 = 18, valueType = XtreamDataType.i64),
                                            @ValueMatcher(matchU16 = 19, valueType = XtreamDataType.i64_le),
                                    }
                            ),
                            decoder = @ValueDecoder(
                                    params = {@DecoderParam(charset = "UTF-8")},
                                    matchers = {
                                            @ValueMatcher(matchU16 = 1, valueType = XtreamDataType.u32),
                                            // charset 从 params 继承而来(UTF-8)
                                            @ValueMatcher(matchU16 = 2, valueType = XtreamDataType.string),
                                            @ValueMatcher(matchU16 = 3, valueType = XtreamDataType.string_gbk),
                                            @ValueMatcher(matchU16 = 4, valueCodec = StringFieldCodecs.StringFieldCodecGbk.class),
                                            @ValueMatcher(matchU16 = 5, valueCodec = StringFieldCodecs.StringFieldCodec.class, charset = "utf-8"),
                                            @ValueMatcher(matchU16 = 6, valueType = XtreamDataType.string, charset = "utf-8"),
                                            // 这里没匹配7，走 FallbackValueMatcher 逻辑(hexString)
                                            @ValueMatcher(matchU16 = 8, valueType = XtreamDataType.i8_as_int),
                                            @ValueMatcher(matchU16 = 9, valueType = XtreamDataType.u8),
                                            @ValueMatcher(matchU16 = 10, valueType = XtreamDataType.i16),
                                            @ValueMatcher(matchU16 = 11, valueType = XtreamDataType.i16_le),
                                            @ValueMatcher(matchU16 = 12, valueType = XtreamDataType.u16),
                                            @ValueMatcher(matchU16 = 13, valueType = XtreamDataType.u16_le),
                                            @ValueMatcher(matchU16 = 14, valueType = XtreamDataType.i32),
                                            @ValueMatcher(matchU16 = 15, valueType = XtreamDataType.i32_le),
                                            @ValueMatcher(matchU16 = 16, valueType = XtreamDataType.u32),
                                            @ValueMatcher(matchU16 = 17, valueType = XtreamDataType.u32_le),
                                            @ValueMatcher(matchU16 = 18, valueType = XtreamDataType.i64),
                                            @ValueMatcher(matchU16 = 19, valueType = XtreamDataType.i64_le),
                                    },
                                    // 没有匹配到的key 统一在这里处理
                                    fallbackMatchers = {
                                            // 转为 hexString
                                            @FallbackValueMatcher(valueCodec = StringFieldCodecs.StringFieldCodecHex.class)
                                    }
                            )
                    )
            )
            Map<Integer, Object> extraInfo
    ) {
    }

    @Test
    void testCodec() {
        final EntityCodec entityCodec = EntityCodec.DEFAULT;
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final RecordBasedEntityRustStyle sourceEntity = createEntity();
            entityCodec.encode(sourceEntity, buffer);
            // System.out.println(FormatUtils.toHexString(buffer));

            final RecordBasedEntityRustStyle decodedEntity = entityCodec.decode(RecordBasedEntityRustStyle.class, buffer);
            // System.out.println(sourceEntity);
            // System.out.println(decodedEntity);

            doCompare(sourceEntity, decodedEntity);
        } finally {
            assertEquals(1, buffer.refCnt());
            buffer.release();
        }
    }

    private void doCompare(RecordBasedEntityRustStyle sourceEntity, RecordBasedEntityRustStyle decodedEntity) {
        assertEquals(sourceEntity.messageId(), decodedEntity.messageId());
        final RecordBasedUserInfoRustStyle sourceUserInfo = sourceEntity.userInfo();
        final RecordBasedUserInfoRustStyle decodedUserInfo = decodedEntity.userInfo();
        assertEquals(sourceUserInfo.id(), decodedUserInfo.id());
        assertEquals(sourceUserInfo.name(), decodedUserInfo.name());
        final Map<Integer, Object> sourceExtraInfo = sourceUserInfo.extraInfo();
        final Map<Integer, Object> decodedExtraInfo = decodedUserInfo.extraInfo();
        assertEquals(sourceExtraInfo.size(), decodedExtraInfo.size());
        for (Map.Entry<Integer, Object> entry : sourceExtraInfo.entrySet()) {
            final Object sourceValue = entry.getValue();
            final Integer key = entry.getKey();
            final Object decodedValue = decodedExtraInfo.get(key);
            if (Objects.equals(key, 7)) {
                final String hexString = FormatUtils.toHexString(((String) sourceValue).getBytes(XtreamConstants.CHARSET_GBK));
                assertEquals(hexString, decodedValue);
            } else {
                Assertions.assertEquals(sourceValue, decodedValue);
            }
        }
    }

    private RecordBasedEntityRustStyle createEntity() {
        final Map<Integer, Object> extraInfo = new LinkedHashMap<>();
        extraInfo.put(1, 123L);
        extraInfo.put(2, "内容2");
        extraInfo.put(3, "内容3");
        extraInfo.put(4, "内容4");
        extraInfo.put(5, "内容5");
        extraInfo.put(6, "内容6");
        extraInfo.put(7, "内容7。。。fallback");
        extraInfo.put(8, -128);
        extraInfo.put(9, (short) 255);
        extraInfo.put(10, Short.MIN_VALUE);
        extraInfo.put(11, Short.MAX_VALUE);
        extraInfo.put(12, (1 << 16) - 1);
        extraInfo.put(13, 0);
        extraInfo.put(14, Integer.MAX_VALUE);
        extraInfo.put(15, Integer.MIN_VALUE);
        extraInfo.put(16, 0L);
        extraInfo.put(17, (1L << 32) - 1);
        extraInfo.put(18, Long.MIN_VALUE);
        extraInfo.put(19, Long.MAX_VALUE);

        return new RecordBasedEntityRustStyle(
                1,
                new RecordBasedUserInfoRustStyle(22, "张三", extraInfo)
        );
    }

}

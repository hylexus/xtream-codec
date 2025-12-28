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

package io.github.hylexus.xtream.codec.core.impl.codec.pair;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueDecoderCommonParam;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueMatcher;
import io.github.hylexus.xtream.codec.core.annotation.pair.XtreamPairFieldSequence;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.tlv.TLVDebugEntity01;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperBcd;
import io.github.hylexus.xtream.codec.core.type.wrapper.U32Wrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PairDebugEntity01 {

    @Preset.RustStyle.u32
    private Long id;


    @XtreamPairFieldSequence(
            prependLengthFieldType = PrependLengthFieldType.u16,
            decoder = @XtreamPairFieldSequence.Decoder(
                    key = {
                            @Key(version = XtreamField.ALL_VERSION, type = KeyType.u16),
                            @Key(version = 1, type = KeyType.u16),
                            @Key(version = 2, type = KeyType.u32),
                    },
                    value = @XtreamPairFieldSequence.Value(
                            commonParams = {
                                    @ValueDecoderCommonParam(version = XtreamField.ALL_VERSION, charset = "GBK"),
                                    @ValueDecoderCommonParam(version = 1, charset = "utf-8"),
                            },
                            matchers = {
                                    @ValueMatcher(version = XtreamField.ALL_VERSION, length = 6, matchU16 = 1, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(version = XtreamField.ALL_VERSION, length = 13, matchU16 = 2, valueType = XtreamDataType.string_utf8),
                                    @ValueMatcher(version = XtreamField.ALL_VERSION, length = 4, matchU16 = 3, valueType = XtreamDataType.u32),
                                    @ValueMatcher(version = XtreamField.ALL_VERSION, length = 2, matchU16 = 4, valueType = XtreamDataType.string_hex),
                                    @ValueMatcher(version = XtreamField.ALL_VERSION, length = 15, matchU16 = 5, valueEntity = TLVDebugEntity01.Item01.class),
                                    // @ValueMatcher(version = XtreamField.ALL_VERSION, length = 3, matchU16 = 6, valueType = XtreamDataType.string_hex),
                                    @ValueMatcher(version = 1, length = 1, matchU16 = 1, valueType = XtreamDataType.string, charset = "gbk"),
                                    @ValueMatcher(version = 1, length = 1, matchU16 = 2, valueCodec = StringFieldCodecs.StringFieldCodecUtf8.class),
                                    @ValueMatcher(version = 1, length = 1, matchU16 = 3, valueEntity = U32Wrapper.class),
                                    @ValueMatcher(version = 1, length = 1, matchU16 = 4, valueEntity = StringWrapperBcd.class),
                            }
                    )
            )
    )
    private List<Pair> pairList;

    @Preset.RustStyle.str
    private String desc;

    public record Item01(
            @Preset.RustStyle.u32 Long id,
            @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8) String name
    ) {
    }
}

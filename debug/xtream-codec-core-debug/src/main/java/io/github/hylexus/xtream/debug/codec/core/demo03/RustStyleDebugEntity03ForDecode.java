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

package io.github.hylexus.xtream.debug.codec.core.demo03;

import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author hylexus
 */
@Getter
@Setter
@ToString
public class RustStyleDebugEntity03ForDecode {

    @Preset.RustStyle.u16
    private int msgId;

    @Preset.RustStyle.simple_map(
            key = @XtreamMapField.Key(type = XtreamMapField.KeyType.u16),
            valueLength = @XtreamMapField.ValueLength(type = XtreamMapField.ValueLengthType.u8),
            value = @XtreamMapField.Value(
                    decoder = @XtreamMapField.ValueDecoder(
                            params = @XtreamMapField.DecoderParam(charset = "gbk"),
                            matchers = {
                                    @XtreamMapField.ValueMatcher(matchU16 = 1, valueType = XtreamDataType.string_utf8),
                                    @XtreamMapField.ValueMatcher(matchU16 = 2, valueType = XtreamDataType.string_utf8),
                                    @XtreamMapField.ValueMatcher(matchU16 = 3, valueType = XtreamDataType.u16),
                                    @XtreamMapField.ValueMatcher(matchU16 = 4, valueType = XtreamDataType.string)
                            },
                            fallbackMatchers = @XtreamMapField.FallbackValueMatcher(valueCodec = StringFieldCodecs.StringFieldCodecHex.class)
                    )
            )
    )
    private Map<Integer, Object> attr;
}

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

import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamFieldMapDescriptor;
import io.github.hylexus.xtream.codec.core.type.Preset;
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

    @Preset.RustStyle.map
    @XtreamFieldMapDescriptor(
            keyDescriptor = @XtreamFieldMapDescriptor.KeyDescriptor(type = XtreamFieldMapDescriptor.KeyType.u16),
            valueLengthFieldDescriptor = @XtreamFieldMapDescriptor.ValueLengthFieldDescriptor(length = 1),
            valueDecoderDescriptors = @XtreamFieldMapDescriptor.ValueDecoderDescriptors(
                    defaultValueDecoderDescriptor = @XtreamFieldMapDescriptor.ValueDecoderDescriptor(
                            javaType = byte[].class,
                            config = @XtreamField(charset = "utf-8", signedness = NumberSignedness.NONE)
                    ),
                    valueDecoderDescriptors = {
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU16 = 1, javaType = String.class),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU16 = 2, javaType = String.class, config = @XtreamField(), valueLengthFieldSize = 2),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU16 = 3, javaType = Integer.class, config = @XtreamField(signedness = NumberSignedness.UNSIGNED, length = 2), valueLengthFieldSize = 2),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU16 = 4, javaType = String.class),
                    }
            )
    )
    private Map<Integer, Object> attr;
}

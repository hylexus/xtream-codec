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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.AbstractMapFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.U16FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U32FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U8FieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 位置信息汇报 0x0200
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CodecDebugEntity02 {
    // 报警标志  DWORD(4)
    @Preset.JtStyle.Dword
    private long alarmFlag;

    // 状态  DWORD(4)
    @Preset.JtStyle.Dword
    private long status;

    // 纬度  DWORD(4)
    @Preset.JtStyle.Dword
    private long latitude;

    // 经度  DWORD(4)
    @Preset.JtStyle.Dword
    private long longitude;

    // 高程  WORD(2)
    @Preset.JtStyle.Word
    private int altitude;

    // 速度  WORD(2)
    @Preset.JtStyle.Word
    private int speed;

    // 方向  WORD(2)
    @Preset.JtStyle.Word
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime
    private LocalDateTime time;


    // 长度：消息体长度减去前面的 28 字节
    @Preset.JtStyle.Map(desc = "附加项列表", fieldCodec = LocationExtraItemFieldCodec.class)
    private Map<Short, Object> extraItems;

    public static class LocationExtraItemFieldCodec extends AbstractMapFieldCodec<Short, U8FieldCodecs.U8FieldCodec> {
        public LocationExtraItemFieldCodec() {
        }

        @Override
        protected FieldCodec<?> getKeyFieldCodec() {
            return U8FieldCodecs.SHORT_INSTANCE;
        }

        @Override
        protected U8FieldCodecs.U8FieldCodec getValueLengthFieldCodec() {
            return U8FieldCodecs.SHORT_INSTANCE;
        }

        @Override
        protected FieldCodec<?> getValueFieldCodec(Short key) {
            return switch (key) {
                case 0x01 -> U32FieldCodecs.LONG_INSTANCE;
                case 0x02 -> U16FieldCodecs.INTEGER_INSTANCE;
                default -> throw new UnsupportedOperationException();
            };
        }

    }
}

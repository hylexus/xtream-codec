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

package io.github.hylexus.xtream.debug.ext.jt808.message;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.impl.codec.BytesFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x64;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x65;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x66;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x67;
import io.github.hylexus.xtream.codec.ext.jt808.spec.AbstractJt808Message;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.StringJoiner;

import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.*;

public class DemoLocationMsg03 extends AbstractJt808Message {

    public DemoLocationMsg03(Jt808Request request) {
        super(request);
    }

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
    @Preset.JtStyle.SimpleMap(
            key = @Key(type = KeyType.u8),
            valueLength = @ValueLength(type = ValueLengthType.u8),
            value = @Value(
                    encoder = @ValueEncoder(
                            params = @EncoderParam(charset = XtreamConstants.CHARSET_NAME_GBK),
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = XtreamDataType.u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = XtreamDataType.u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = XtreamDataType.u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = XtreamDataType.u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                                    @ValueMatcher(matchU8 = 0x11, valueType = XtreamDataType.byte_array, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = XtreamDataType.u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = XtreamDataType.u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = XtreamDataType.u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                                    @ValueMatcher(matchU8 = 0x64, valueEntity = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                                    @ValueMatcher(matchU8 = 0x65, valueEntity = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                                    @ValueMatcher(matchU8 = 0x66, valueEntity = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                                    @ValueMatcher(matchU8 = 0x67, valueEntity = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                            }
                    ),
                    decoder = @ValueDecoder(
                            params = @DecoderParam(charset = XtreamConstants.CHARSET_NAME_GBK),
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = XtreamDataType.u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = XtreamDataType.u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = XtreamDataType.u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = XtreamDataType.u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                                    @ValueMatcher(matchU8 = 0x11, valueType = XtreamDataType.byte_array, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = XtreamDataType.u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = XtreamDataType.u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = XtreamDataType.u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                                    @ValueMatcher(matchU8 = 0x64, valueEntity = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                                    @ValueMatcher(matchU8 = 0x65, valueEntity = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                                    @ValueMatcher(matchU8 = 0x66, valueEntity = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                                    @ValueMatcher(matchU8 = 0x67, valueEntity = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                            },
                            fallbackMatchers = @FallbackValueMatcher(valueCodec = BytesFieldCodecs.BytesFieldCodecByteArray.class)
                    )
            )
    )
    private Map<Short, Object> extraItems;

    @Override
    public String toString() {
        return new StringJoiner(", ", DemoLocationMsg03.class.getSimpleName() + "[", "]")
                .add("alarmFlag=" + alarmFlag)
                .add("status=" + status)
                .add("latitude=" + latitude)
                .add("longitude=" + longitude)
                .add("altitude=" + altitude)
                .add("speed=" + speed)
                .add("direction=" + direction)
                .add("time=" + time)
                .add("extraItems=" + extraItems)
                .add("protocolType=" + protocolType)
                .add("requestId='" + requestId + "'")
                .add("traceId='" + traceId + "'")
                .add("serverType=" + serverType)
                .add("header=" + header)
                .add("calculatedCheckSum=" + calculatedCheckSum)
                .add("originalCheckSum=" + originalCheckSum)
                .toString();
    }
}

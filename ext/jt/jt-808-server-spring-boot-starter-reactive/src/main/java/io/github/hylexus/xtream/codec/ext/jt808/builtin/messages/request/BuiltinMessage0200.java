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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x64;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x65;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x66;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x67;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField.*;
import static io.github.hylexus.xtream.codec.core.type.Preset.JtStyle;
import static io.github.hylexus.xtream.codec.core.type.XtreamDataType.*;

/**
 * 位置信息汇报 0x0200
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0200, desc = "位置信息汇报")
public class BuiltinMessage0200 {
    @Preset.JtStyle.Dword(desc = "报警标志")
    private long alarmFlag;

    @Preset.JtStyle.Dword(desc = "状态")
    private long status;

    @Preset.JtStyle.Dword(desc = "纬度")
    private long latitude;

    @Preset.JtStyle.Dword(desc = "经度")
    private long longitude;

    @Preset.JtStyle.Word(desc = "高程")
    private int altitude;

    @Preset.JtStyle.Word(desc = "速度")
    private int speed;

    @Preset.JtStyle.Word(desc = "方向")
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime(desc = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * @param locationType 位置类型
     * @param locationId   区域或路段ID; 若位置类型为0，无该字段
     */
    public record Item0x11(
            @JtStyle.Byte short locationType,
            @JtStyle.Word(condition = "#locationType != 0") @Nullable Long locationId) {
    }

    /**
     * @param locationType 位置类型
     * @param locationId   区域或路段ID
     * @param direction    方向；0：进；1：出
     */
    public record Item0x12(
            @Preset.JtStyle.Byte short locationType,
            @Preset.JtStyle.Dword long locationId,
            @Preset.JtStyle.Byte short direction) {
    }

    /**
     * @param lineId     路线ID
     * @param locationId 路段行驶时间（秒）
     * @param result     结果。0：不足；1：过长
     */
    public record Item0x13(
            @Preset.JtStyle.Dword long lineId,
            @Preset.JtStyle.Word int locationId,
            @Preset.JtStyle.Byte short result) {
    }

    // 长度：消息体长度减去前面的 28 字节
    @XtreamMapField(
            desc = "附加项列表",
            key = @Key(type = KeyType.u8),
            valueLength = @ValueLength(type = ValueLengthType.u8),
            value = @Value(
                    encoder = @ValueEncoder(
                            params = {@EncoderParam(charset = "GBK")},
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 始计数"),
                                    @ValueMatcher(matchU8 = 0x05, valueType = byte_array, desc = "胎压"),
                                    @ValueMatcher(matchU8 = 0x06, valueType = i16_as_int, desc = "车厢温度"),
                                    @ValueMatcher(matchU8 = 0x11, valueEntity = Item0x11.class, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x2A, valueType = u16, desc = "IO 状态位，表 32"),
                                    @ValueMatcher(matchU8 = 0x2B, valueType = i32, desc = "模拟量，bit0-15,AD0,bit16-31,AD1"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                                    @ValueMatcher(matchU8 = 0x64, valueEntity = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                                    @ValueMatcher(matchU8 = 0x65, valueEntity = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                                    @ValueMatcher(matchU8 = 0x66, valueEntity = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                                    @ValueMatcher(matchU8 = 0x67, valueEntity = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                            }
                    ),
                    decoder = @ValueDecoder(
                            params = {@DecoderParam(charset = "GBK")},
                            matchers = {
                                    @ValueMatcher(matchU8 = 0x01, valueType = u32, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                                    @ValueMatcher(matchU8 = 0x02, valueType = u16, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                                    @ValueMatcher(matchU8 = 0x03, valueType = u16, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                                    @ValueMatcher(matchU8 = 0x04, valueType = u16, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                                    @ValueMatcher(matchU8 = 0x05, valueType = byte_array, desc = "胎压"),
                                    @ValueMatcher(matchU8 = 0x06, valueType = i16_as_int, desc = "车厢温度"),
                                    @ValueMatcher(matchU8 = 0x11, valueEntity = Item0x11.class, desc = "长度1或5；超速报警附加信息见 表 28"),
                                    @ValueMatcher(matchU8 = 0x12, valueEntity = Item0x12.class, desc = "进出区域/路线报警附加信息消息"),
                                    @ValueMatcher(matchU8 = 0x13, valueEntity = Item0x13.class, desc = "路线行驶时间不足/过长报警附加信息"),
                                    @ValueMatcher(matchU8 = 0x25, valueType = u32, desc = "扩展车辆信号状态位，定义见 表 31"),
                                    @ValueMatcher(matchU8 = 0x2A, valueType = u16, desc = "IO 状态位，表 32"),
                                    @ValueMatcher(matchU8 = 0x2B, valueType = i32, desc = "模拟量，bit0-15,AD0,bit16-31,AD1"),
                                    @ValueMatcher(matchU8 = 0x30, valueType = u8, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                                    @ValueMatcher(matchU8 = 0x31, valueType = u8, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                                    @ValueMatcher(matchU8 = 0x64, valueEntity = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                                    @ValueMatcher(matchU8 = 0x65, valueEntity = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                                    @ValueMatcher(matchU8 = 0x66, valueEntity = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                                    @ValueMatcher(matchU8 = 0x67, valueEntity = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                            },
                            fallbackMatchers = {
                                    @FallbackValueMatcher(valueCodec = StringFieldCodecs.StringFieldCodecHex.class)
                            }
                    )
            )
    )
    private Map<Short, Object> extraItems;


}

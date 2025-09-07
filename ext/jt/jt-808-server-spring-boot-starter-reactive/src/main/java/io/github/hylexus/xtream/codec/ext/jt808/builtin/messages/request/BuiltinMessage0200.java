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
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamFieldMapDescriptor;
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
import org.springframework.format.annotation.DateTimeFormat;

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

    // 长度：消息体长度减去前面的 28 字节
    @Preset.JtStyle.Map(desc = "附加项列表")
    @XtreamFieldMapDescriptor(
            keyDescriptor = @XtreamFieldMapDescriptor.KeyDescriptor(type = XtreamFieldMapDescriptor.KeyType.u8),
            valueLengthFieldDescriptor = @XtreamFieldMapDescriptor.ValueLengthFieldDescriptor(length = 1),
            // 编码配置(如果你不需要编码；可以不要这个配置；这里添加编码配置是为了测试编码逻辑)
            valueEncoderDescriptors = @XtreamFieldMapDescriptor.ValueEncoderDescriptors(
                    defaultValueEncoderDescriptor = @XtreamFieldMapDescriptor.ValueEncoderDescriptor(config = @XtreamField(signedness = NumberSignedness.UNSIGNED)),
                    valueEncoderDescriptors = {
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x01, config = @XtreamField(length = 4, signedness = NumberSignedness.UNSIGNED), javaType = Long.class, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x02, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x03, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x04, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x11, javaType = byte[].class, desc = "长度1或5；超速报警附加信息见 表 28"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x25, config = @XtreamField(length = 4, signedness = NumberSignedness.UNSIGNED), javaType = Long.class, desc = "扩展车辆信号状态位，定义见 表 31"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x30, config = @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED), javaType = Short.class, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x31, config = @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED), javaType = Short.class, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x64, javaType = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x65, javaType = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x66, javaType = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x67, javaType = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                    }
            ),
            // 解码配置
            valueDecoderDescriptors = @XtreamFieldMapDescriptor.ValueDecoderDescriptors(
                    defaultValueDecoderDescriptor = @XtreamFieldMapDescriptor.ValueDecoderDescriptor(javaType = byte[].class,config = @XtreamField(signedness = NumberSignedness.UNSIGNED)),
                    valueDecoderDescriptors = {
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x01, config = @XtreamField(length = 4, signedness = NumberSignedness.UNSIGNED), javaType = Long.class, desc = "里程，DWORD，1/10km，对应车上里程表读数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x02, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "油量，WORD，1/10L，对应车上油量表读数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x03, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "行驶记录功能获取的速度，WORD，1/10km/h"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x04, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "需要人工确认报警事件的 ID，WORD，从 1 开始计数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x11, javaType = byte[].class, desc = "长度1或5；超速报警附加信息见 表 28"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x25, config = @XtreamField(length = 4, signedness = NumberSignedness.UNSIGNED), javaType = Long.class, desc = "扩展车辆信号状态位，定义见 表 31"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x30, config = @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED), javaType = Short.class, desc = "数据类型为 BYTE，无线通信网络信号强度"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x31, config = @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED), javaType = Short.class, desc = "数据类型为 BYTE，GNSS定位卫星数"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x64, javaType = LocationItem0x64.class, desc = "苏标: 高级驾驶辅助报警信息，定义见表 4-15"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x65, javaType = LocationItem0x65.class, desc = "苏标: 驾驶员状态监测系统报警信息，定义见表 4-17"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x66, javaType = LocationItem0x66.class, desc = "苏标: 胎压监测系统报警信息，定义见表 4-18"),
                            @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU8 = 0x67, javaType = LocationItem0x67.class, desc = "苏标: 盲区监测系统报警信息，定义见表 4-20"),
                    }
            )
    )
    private Map<Short, Object> extraItems;

}

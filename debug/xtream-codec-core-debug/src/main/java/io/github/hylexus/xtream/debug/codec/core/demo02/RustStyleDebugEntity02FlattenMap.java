/*
 * Copyright 2024-present the original author or authors.
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

package io.github.hylexus.xtream.debug.codec.core.demo02;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.ext.*;
import io.github.hylexus.xtream.codec.core.annotation.map.XtreamMapField;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U16FieldCodecs;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public class RustStyleDebugEntity02FlattenMap {

    // region 消息头
    // byte[0-2)    消息ID word(16)
    @Preset.RustStyle.u16
    private int msgId;

    // byte[2-4)    消息体属性 word(16)
    @Preset.RustStyle.u16
    private int msgBodyProps;

    // byte[4]     协议版本号
    @Preset.RustStyle.u8
    private short protocolVersion;

    // byte[5-15)    终端手机号或设备ID bcd[10]
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 10)
    private String terminalId;

    // byte[15-17)    消息流水号 word(16)
    @Preset.RustStyle.u16
    private int msgSerialNo;

    // byte[17-21)    消息包封装项
    // @Preset.RustStyle.u32(condition = "hasSubPackage()")
    @Preset.RustStyle.u32(conditions = @Expression(spel = "hasSubPackage()", mvel = "self.hasSubPackage()", aviator = "self.hasSubPackage"))
    private Long subPackageInfo;
    // endregion 消息头

    // region 消息体
    // 报警标志  DWORD(4)
    @Preset.RustStyle.u32
    private long alarmFlag;

    // 状态  DWORD(4)
    @Preset.RustStyle.u32
    private long status;

    // 纬度  DWORD(4)
    @Preset.RustStyle.u32
    private long latitude;

    // 经度  DWORD(4)
    @Preset.RustStyle.u32
    private long longitude;

    // 高程  WORD(2)
    @Preset.RustStyle.u16
    private int altitude;

    // 高程  WORD(2)
    @Preset.RustStyle.u16
    private int speed;

    // 方向  WORD(2)
    @Preset.RustStyle.u16
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
    private String time;

    @Preset.RustStyle.simple_map(
            // 长度: 三种表达式选一个即可
            lengthExpressions = @Expression(
                    spel = "msgBodyLength() - 28",
                    mvel = "self.msgBodyLength() - 28",
                    aviator = "self.msgBodyLength - 28"
            ),
            // key: u8 类型(1字节) 1~255 用 short 避免溢出
            key = @Key(type = KeyType.u8),
            // key 的长度字段: u8 类型(1字节)
            valueLength = @ValueLength(type = LengthFieldType.u8),
            // value: 根据 key 的值来决定类型
            value = @XtreamMapField.Value(
                    encoder = @XtreamMapField.ValueEncoder(
                            params = @XtreamMapField.EncoderParam(charset = "gbk"),
                            matchers = {
                                    // 0x01 dword(u32): 里程，单位为 1/10km
                                    @ValueMatcher(matchU8 = 0x01, valueType = XtreamDataType.u32),
                                    // 0x02 word(u16): 油量，单位为 1/10L
                                    @ValueMatcher(matchU8 = 0x02, valueType = XtreamDataType.u16),
                                    // 0x03 word(u16): 行驶记录功能获取的速度，单位为 1/10km/h
                                    @ValueMatcher(matchU8 = 0x03, valueCodec = U16FieldCodecs.U16FieldCodec.class),
                                    // 0x04 word(u16): 需要人工确认报警事件的ID，从 1 开始
                                    @ValueMatcher(matchU8 = 0x04, valueType = XtreamDataType.u16),
                            }
                    ),
                    decoder = @XtreamMapField.ValueDecoder(
                            params = @XtreamMapField.DecoderParam(charset = "gbk"),
                            matchers = {
                                    // 0x01 dword(u32): 里程，单位为 1/10km
                                    @ValueMatcher(matchU8 = 0x01, valueType = XtreamDataType.u32),
                                    // 0x02 word(u16): 油量，单位为 1/10L
                                    @ValueMatcher(matchU8 = 0x02, valueType = XtreamDataType.u16),
                                    // 0x03 word(u16): 行驶记录功能获取的速度，单位为 1/10km/h
                                    @ValueMatcher(matchU8 = 0x03, valueCodec = U16FieldCodecs.U16FieldCodec.class),
                                    // 0x04 word(u16): 需要人工确认报警事件的ID，从 1 开始
                                    @ValueMatcher(matchU8 = 0x04, valueType = XtreamDataType.u16),
                            },
                            // 其他 key 都转为十六进制字符串
                            fallbackMatchers = {
                                    @FallbackValueMatcher(valueCodec = StringFieldCodecs.StringFieldCodecHex.class)
                            }
                    )
            )
    )
    private Map<Short, Object> extraItems;
    // endregion 消息体

    // 校验码
    @Preset.RustStyle.i8
    private byte checkSum;

    // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
    public int msgBodyLength() {
        return msgBodyProps & 0x3ff;
    }

    // for Aviator
    @SuppressWarnings("unused")
    public int getMsgBodyLength() {
        return msgBodyProps & 0x3ff;
    }

    // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
    public boolean hasSubPackage() {
        // return ((msgBodyProperty & 0x2000) >> 13) == 1;
        return (msgBodyProps & 0x2000) > 0;
    }

    // for Aviator
    @SuppressWarnings("unused")
    public boolean isHasSubPackage() {
        // return ((msgBodyProperty & 0x2000) >> 13) == 1;
        return (msgBodyProps & 0x2000) > 0;
    }

    @Setter
    @Getter
    @ToString
    public static class ExtraItem {
        // 附加信息ID   BYTE(1~255)
        @Preset.RustStyle.u8
        private short id;
        // 附加信息长度   BYTE(1~255)
        @Preset.RustStyle.u8
        private short contentLength;
        // 附加信息内容  BYTE[N]
        // @Preset.RustStyle.byte_array(lengthExpression = "getContentLength()")
        @Preset.RustStyle.byte_array(lengthExpressions = @Expression(spel = "getContentLength()", mvel = "self.getContentLength()", aviator = "self.contentLength"))
        private byte[] content;

        public ExtraItem() {
        }

        public ExtraItem(short id, short length, byte[] content) {
            this.id = id;
            this.contentLength = length;
            this.content = content;
        }
    }
}

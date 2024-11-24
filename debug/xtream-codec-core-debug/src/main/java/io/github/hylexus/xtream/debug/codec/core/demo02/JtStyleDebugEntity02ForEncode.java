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

package io.github.hylexus.xtream.debug.codec.core.demo02;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.debug.codec.core.utilsforunittest.DebugEntity02ForJunitPurpose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class JtStyleDebugEntity02ForEncode implements DebugEntity02ForJunitPurpose {

    // region 消息头
    // byte[0-2)    消息ID word(16)
    @Preset.JtStyle.Word
    private int msgId;

    // byte[2-4)    消息体属性 word(16)
    @Preset.JtStyle.Word
    private int msgBodyProps;

    // byte[4]     协议版本号
    @Preset.JtStyle.Byte
    private byte protocolVersion;

    // byte[5-15)    终端手机号或设备ID bcd[10]
    @Preset.JtStyle.Bcd
    private String terminalId;

    // byte[15-17)    消息流水号 word(16)
    @Preset.JtStyle.Word
    private int msgSerialNo;

    // byte[17-21)    消息包封装项
    @Preset.JtStyle.Dword
    private Long subPackageInfo;
    // endregion 消息头

    // region 消息体
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

    // 高程  WORD(2)
    @Preset.JtStyle.Word
    private int speed;

    // 方向  WORD(2)
    @Preset.JtStyle.Word
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.Bcd
    private String time;

    @Preset.JtStyle.List
    private List<ExtraItem> extraItems;
    // endregion 消息体

    // 校验码
    @Preset.JtStyle.Byte
    private byte checkSum;

    /**
     * 实现的这个接口仅仅是为了 Junit 中赋值、取值方便（没有其他任何目的）
     */
    @Setter
    @Getter
    @ToString
    public static class ExtraItem implements ExtraItemForJunitPurpose {
        // 附加信息ID   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short id;
        // 附加信息长度   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short contentLength;
        // 附加信息内容  BYTE[N]
        @Preset.JtStyle.Bytes(lengthExpression = "getLength()")
        private byte[] content;

        public ExtraItem() {
        }

        public ExtraItem(short id, short contentLength, byte[] content) {
            this.id = id;
            this.contentLength = contentLength;
            this.content = content;
        }
    }
}
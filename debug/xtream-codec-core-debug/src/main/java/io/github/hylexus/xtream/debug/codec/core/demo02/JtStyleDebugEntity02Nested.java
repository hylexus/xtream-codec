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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class JtStyleDebugEntity02Nested {

    // 消息头
    @Preset.JtStyle.Object
    private Header header;

    // 消息体
    @Preset.JtStyle.Object(lengthExpression = "header.msgBodyLength()")
    private Body body;

    // 校验码
    @Preset.JtStyle.Byte
    private short checkSum;

    @Getter
    @Setter
    @ToString
    public static class Header {
        // byte[0-2)    消息ID word(16)
        @Preset.JtStyle.Word
        private int msgId;

        // byte[2-4)    消息体属性 word(16)
        @Preset.JtStyle.Word
        private int msgBodyProps;

        // byte[4]     协议版本号
        @Preset.JtStyle.Byte
        private short protocolVersion;

        // byte[5-15)    终端手机号或设备ID bcd[10]
        @Preset.JtStyle.Bcd(length = 10)
        private String terminalId;

        // byte[15-17)    消息流水号 word(16)
        @Preset.JtStyle.Word
        private int msgSerialNo;

        // byte[17-21)    消息包封装项
        @Preset.JtStyle.Dword(condition = "hasSubPackage()")
        private Long subPackageInfo;

        // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
        public int msgBodyLength() {
            return msgBodyProps & 0x3ff;
        }

        // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
        public boolean hasSubPackage() {
            // return ((msgBodyProperty & 0x2000) >> 13) == 1;
            return (msgBodyProps & 0x2000) > 0;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Body {
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
        @Preset.JtStyle.Bcd(length = 6)
        private String time;

        @Preset.JtStyle.List
        private List<ExtraItem> extraItems;
    }

    @Setter
    @Getter
    @ToString
    public static class ExtraItem {
        // 附加信息ID   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short id;
        // 附加信息长度   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short contentLength;
        // 附加信息内容  BYTE[N]
        @Preset.JtStyle.Bytes(lengthExpression = "getContentLength()")
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

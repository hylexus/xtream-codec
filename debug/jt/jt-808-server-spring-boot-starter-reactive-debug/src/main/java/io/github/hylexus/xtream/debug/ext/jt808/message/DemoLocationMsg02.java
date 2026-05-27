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

package io.github.hylexus.xtream.debug.ext.jt808.message;

import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class DemoLocationMsg02 {

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

    // 长度：消息体长度减去前面的 28 字节
    @Preset.JtStyle.List
    private List<ExtraItem> extraItems;

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

        public ExtraItem(short id, short length, byte[] content) {
            this.id = id;
            this.contentLength = length;
            this.content = content;
        }
    }
}

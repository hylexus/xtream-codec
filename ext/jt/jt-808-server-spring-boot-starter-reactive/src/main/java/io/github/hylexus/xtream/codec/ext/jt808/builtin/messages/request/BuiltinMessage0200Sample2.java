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
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec.BuiltinLocationMessageExtraItemFieldCodec;
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
@Jt808ResponseBody(messageId = 0x0200, desc = "位置信息汇报(示例2)")
public class BuiltinMessage0200Sample2 {
    // 报警标志  DWORD(4)
    @Preset.JtStyle.Dword(desc = "报警标志")
    private long alarmFlag;

    // 状态  DWORD(4)
    @Preset.JtStyle.Dword(desc = "状态")
    private long status;

    // 纬度  DWORD(4)
    @Preset.JtStyle.Dword(desc = "纬度")
    private long latitude;

    // 经度  DWORD(4)
    @Preset.JtStyle.Dword(desc = "经度")
    private long longitude;

    // 高程  WORD(2)
    @Preset.JtStyle.Word(desc = "高程")
    private int altitude;

    // 速度  WORD(2)
    @Preset.JtStyle.Word(desc = "速度")
    private int speed;

    // 方向  WORD(2)
    @Preset.JtStyle.Word(desc = "方向")
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime(desc = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    // 长度：消息体长度减去前面的 28 字节
    @Preset.JtStyle.Object(desc = "附加项", fieldCodec = BuiltinLocationMessageExtraItemFieldCodec.class)
    private Map<Short, Object> extraItems;

}

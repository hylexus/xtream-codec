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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.mixed;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.simple.SimpleField;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0200, desc = "混合消息测试")
public class MixedEntity02 {
    //    @Preset.RustStyle.u32(desc = "报警标志")
    //    private long alarmFlag;
    //
    //    @Preset.RustStyle.u32(desc = "状态")
    //    private long status;

    @Preset.RustStyle.u32(desc = "纬度")
    private long latitude;

    @Preset.RustStyle.u32(desc = "经度")
    private long longitude;

    @Preset.RustStyle.u16(desc = "高程")
    private int altitude;

    @Preset.RustStyle.u16(desc = "速度")
    private int speed;

    // @Preset.RustStyle.u16(desc = "方向")
    @Preset.RustStyle.basic
    private SimpleField.U16 direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime(desc = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    @Preset.RustStyle.basic
    private SimpleField.Dict<SimpleField.U8> extraItems;
}

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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 苏标-表-4-16 报警标识号格式
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@SuppressWarnings("NullAway.Init")
public class AlarmIdentifier {
    // 终端ID BYTE[7] 7个字节，由大写字母和数字组成
    @Preset.JtStyle.Str(length = 7, desc = "终端ID 7个字节，由大写字母和数字组成")
    private String terminalId;

    // 时间   BCD[6]  YY-MM-DD-hh-mm-ss （GMT+8时间）
    @Preset.JtStyle.BcdDateTime(desc = "时间")
    // 或者
    // @XtreamDateTimeField(pattern = "yyMMddHHmmss", length = 6, charset = XtreamConstants.CHARSET_NAME_BCD_8421)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    // 序号   BYTE    同一时间点报警的序号，从0循环累加
    @Preset.JtStyle.Byte(desc = "序号 同一时间点报警的序号，从0循环累加")
    private short sequence;

    // 附件数量 BYTE    表示该报警对应的附件数量
    @Preset.JtStyle.Byte(desc = "附件数量 表示该报警对应的附件数量")
    private short attachmentCount;

    // 预留 BYTE
    @Preset.JtStyle.Byte(desc = "预留")
    private short reserved = 0;
}

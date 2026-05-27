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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage9208;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Jt808AlarmAttachmentInfoVo {

    /**
     * UUID string
     */
    private String id;

    /**
     * 终端ID
     */
    private String terminalId;

    /**
     * 平台分配的报警唯一编号
     *
     * @see BuiltinMessage1210#getAlarmNo()
     * @see BuiltinMessage9208#getAlarmNo()
     */
    private String alarmNo;

    /**
     * 报警时间
     *
     * @see AlarmIdentifier#getTime()
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime alarmTime;
    /**
     * 同一时间点报警的序号，从0循环累加
     *
     * @see AlarmIdentifier#getSequence()
     */
    private short alarmSequence;
    /**
     * 报警附件数量
     *
     * @see AlarmIdentifier#getAttachmentCount()
     */
    private short attachmentCount;
    /**
     * 终端 ID 7个字节，由大写字母和数字组成
     *
     * @see AlarmIdentifier#getTerminalId()
     */
    private String clientId;
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * <li>0x00：图片</li>
     * <li>0x01：音频</li>
     * <li>0x02：视频</li>
     * <li>0x03：文本</li>
     * <li>0x04：其它</li>
     */
    private short fileType;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 数据写入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private String previewUrl;

}

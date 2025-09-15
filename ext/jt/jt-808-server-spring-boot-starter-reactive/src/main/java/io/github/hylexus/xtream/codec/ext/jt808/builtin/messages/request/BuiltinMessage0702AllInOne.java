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
import io.github.hylexus.xtream.codec.core.annotation.Padding;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 驾驶员身份信息采集上报(多版本合一)
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0702, desc = "驾驶员身份信息采集上报(多版本合一)")
public class BuiltinMessage0702AllInOne {
    /**
     * 状态
     * <li>0x01：从业资格证IC卡插入（驾驶员上班）</li>
     * <li>0x02：从业资格证IC卡拔出（驾驶员下班）</li>
     */
    @Preset.JtStyle.Byte(version = {2013, 2019}, desc = "状态(2013 || 2019)")
    private Short status;

    /**
     * 插卡/拔卡时间，YY-MM-DD-hh-mm-ss；
     * <p>
     * 以下字段在状态为0x01时才有效并做填充。
     */
    @Preset.JtStyle.BcdDateTime(version = {2013, 2019}, desc = "插卡/拔卡时间(2013 || 2019)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;


    /**
     * IC卡读取结果
     * <li>0x00：IC卡读卡成功</li>
     * <li>0x01：读卡失败，原因为卡片密钥认证未通过</li>
     * <li>0x02：读卡失败，原因为卡片已被锁定</li>
     * <li>0x03：读卡失败，原因为卡片被拔出</li>
     * <li>0x04：读卡失败，原因为数据校验错误</li>
     * <p>
     * 以下字段在IC 卡读取结果等于0x00时才有效。
     */
    @Preset.JtStyle.Byte(version = {2013, 2019}, desc = "IC卡读取结果(2013 || 2019)")
    private Short icCardReadResult;

    /**
     * 驾驶员姓名
     */
    // prependLengthFieldType: 前置一个 u8类型的字段 表示 驾驶员姓名长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8)
    private String driverName;

    @Preset.JtStyle.Str(version = 2011, length = 20, desc = "驾驶员身份证编码(2011)")
    private String driverIdCardNo;

    @Preset.JtStyle.Str(version = 2011, length = 40, desc = "从业资格证编码")
    @Preset.JtStyle.Str(version = {2013, 2019}, length = 20, paddingRight = @Padding(minEncodedLength = 20), desc = "从业资格证编码")
    private String professionalLicenseNo;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 发证机构名称长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "发证机构名称")
    private String certificateAuthorityName;

    @Preset.JtStyle.BcdDateTime(version = {2013, 2019}, length = 4, pattern = "yyyyMMdd", desc = "到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate certificateExpiresDate;

    // todo 合并为一个字段
    @Preset.JtStyle.Str(version = 2019, length = 20, desc = "驾驶员身份证编码")
    private String driverIdCardNoV2019;

}

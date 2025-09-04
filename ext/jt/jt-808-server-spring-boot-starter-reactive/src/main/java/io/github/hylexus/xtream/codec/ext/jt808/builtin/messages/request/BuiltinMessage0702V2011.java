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

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 驾驶员身份信息采集上报
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BuiltinMessage0702V2011 {

    /**
     * 驾驶员姓名
     */
    // prependLengthFieldType: 前置一个 u8类型的字段 表示 驾驶员姓名长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8)
    private String driverName;

    /**
     * 驾驶员身份证编码
     */
    @Preset.JtStyle.Str(length = 20)
    private String driverIdCardNo;

    /**
     * 从业资格证编码
     */
    @Preset.JtStyle.Str(length = 40)
    private String professionalLicenseNo;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 发证机构名称长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8)
    private String certificateAuthorityName;

}

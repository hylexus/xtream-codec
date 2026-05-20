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

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 终端鉴权 0x0102
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0102, desc = "终端鉴权(2019)")
public class BuiltinMessage0102V2019 {

    /**
     * byte[1,n)    STRING  鉴权码内容
     * <p>
     * prependLengthFieldType: 前置一个 u8 字段表示鉴权码长度
     */
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "鉴权码内容")
    private String authenticationCode;

    /**
     * byte[n+1,n+1+15)     BYTE[]  IMEI
     */
    @Preset.JtStyle.Str(length = 15, desc = "IMEI(15)")
    private String imei;

    /**
     * byte[n+16,n+16+20)   BYTE[20]    软件版本号
     * <p>
     * 位数不足时后补 `0x00`
     */
    @Preset.JtStyle.Str(length = 20, desc = "软件版本号(20)")
    private String softwareVersion;

    /**
     * @return 去掉可能存在的后补 `0x00` 的字符串
     */
    public String getFormatedSoftwareVersion() {
        return XtreamBytes.trimTailing(this.getSoftwareVersion(), (byte) 0x00);
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public BuiltinMessage0102V2019 setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
        return this;
    }

    public String getImei() {
        return imei;
    }

    public BuiltinMessage0102V2019 setImei(String imei) {
        this.imei = imei;
        return this;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public BuiltinMessage0102V2019 setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0102V2019.class.getSimpleName() + "[", "]")
                .add("authenticationCode='" + authenticationCode + "'")
                .add("imei='" + imei + "'")
                .add("softwareVersion='" + softwareVersion + "'")
                .toString();
    }
}

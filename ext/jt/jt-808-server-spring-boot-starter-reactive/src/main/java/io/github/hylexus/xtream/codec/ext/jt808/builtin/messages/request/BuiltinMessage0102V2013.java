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

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 终端鉴权 0x0102
 * <p>
 * 消息体格式和 V2011 一致
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0102, desc = "终端鉴权(2013)")
public class BuiltinMessage0102V2013 {

    /**
     * 鉴权码 STRING 终端重连后上报鉴权码
     */
    @Preset.JtStyle.Str(desc = "鉴权码")
    private String authenticationCode;

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public BuiltinMessage0102V2013 setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0102V2013.class.getSimpleName() + "[", "]")
                .add("authenticationCode='" + authenticationCode + "'")
                .toString();
    }
}

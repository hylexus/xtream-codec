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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 终端 RSA 公钥
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x0A00, desc = "终端 RSA 公钥")
public class BuiltinMessage0A00 {

    /**
     * 终端RSA公钥{e,n}中的e
     */
    @SuppressWarnings("checkstyle:membername")
    @Preset.JtStyle.Dword(desc = "终端RSA公钥{e,n}中的e")
    private long e;

    /**
     * RSA公钥{e,n}中的n
     */
    @SuppressWarnings("checkstyle:membername")
    @Preset.JtStyle.Bytes(length = 128, desc = "RSA公钥{e,n}中的n")
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] n;
}

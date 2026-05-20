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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 平台 RSA 公钥
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8A00, desc = "平台 RSA 公钥")
public class BuiltinMessage8A00 {

    @SuppressWarnings("checkstyle:membername")
    @Preset.JtStyle.Dword(desc = "平台RSA公钥{e,n}中的e")
    private long e;

    @SuppressWarnings("checkstyle:membername")
    @Preset.JtStyle.Bytes(length = 128, desc = "平台RSA公钥{e,n}中的n")
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] n;

    public long getE() {
        return e;
    }

    public BuiltinMessage8A00 setE(long e) {
        this.e = e;
        return this;
    }

    public byte[] getN() {
        return n;
    }

    public BuiltinMessage8A00 setN(byte[] n) {
        this.n = n;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8A00.class.getSimpleName() + "[", "]")
                .add("e=" + e)
                .add("n=" + Arrays.toString(n))
                .toString();
    }
}

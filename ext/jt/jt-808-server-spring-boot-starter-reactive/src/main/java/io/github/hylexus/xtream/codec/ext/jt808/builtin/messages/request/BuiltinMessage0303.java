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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 信息点播/取消
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0303, desc = "信息点播/取消")
public class BuiltinMessage0303 {

    @Preset.JtStyle.Byte(desc = "信息类型")
    private short type;

    @Preset.JtStyle.Byte(desc = "点播/取消标志 0：取消；1：点播")
    private short flag;

    public short getType() {
        return type;
    }

    public BuiltinMessage0303 setType(short type) {
        this.type = type;
        return this;
    }

    public short getFlag() {
        return flag;
    }

    public BuiltinMessage0303 setFlag(short flag) {
        this.flag = flag;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0303.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("flag=" + flag)
                .toString();
    }
}

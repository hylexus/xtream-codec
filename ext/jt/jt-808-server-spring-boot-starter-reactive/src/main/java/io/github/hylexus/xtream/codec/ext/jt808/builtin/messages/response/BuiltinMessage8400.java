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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 电话回拨
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8400, desc = "电话回拨")
public class BuiltinMessage8400 {

    /**
     * 标志
     * <p>
     * 0:普通通话；1:监听
     */
    @Preset.JtStyle.Byte(desc = "标志")
    private short flag;

    @Preset.JtStyle.Str(desc = "电话号码 最长为20字节")
    private String phoneNumber;

    public short getFlag() {
        return flag;
    }

    public BuiltinMessage8400 setFlag(short flag) {
        this.flag = flag;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public BuiltinMessage8400 setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8400.class.getSimpleName() + "[", "]")
                .add("flag=" + flag)
                .add("phoneNumber='" + phoneNumber + "'")
                .toString();
    }
}

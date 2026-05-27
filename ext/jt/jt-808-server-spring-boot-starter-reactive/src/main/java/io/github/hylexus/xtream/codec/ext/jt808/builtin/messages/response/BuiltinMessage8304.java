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
 * 信息服务
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8304, desc = "信息服务")
public class BuiltinMessage8304 {

    @Preset.JtStyle.Byte(desc = "信息类型")
    private short type;

    @Preset.JtStyle.Word(desc = "信息长度")
    private int length;

    @Preset.JtStyle.Str(desc = "信息内容")
    private String content;

    public short getType() {
        return type;
    }

    public BuiltinMessage8304 setType(short type) {
        this.type = type;
        return this;
    }

    public int getLength() {
        return length;
    }

    public BuiltinMessage8304 setLength(int length) {
        this.length = length;
        return this;
    }

    public String getContent() {
        return content;
    }

    public BuiltinMessage8304 setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8304.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("length=" + length)
                .add("content='" + content + "'")
                .toString();
    }
}

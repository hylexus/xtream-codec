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

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 文本信息下发
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8300, desc = "文本信息下发(2019)")
public class BuiltinMessage8300V2019 {
    /**
     * 标志
     * <li>bit[0] -- 1:紧急</li>
     * <li>bit[1] -- 保留</li>
     * <li>bit[2] -- 1：终端显示器显示</li>
     * <li>bit[3] -- 1：终端TTS播读</li>
     * <li>bit[4] -- 1：广告屏显示</li>
     * <li>bit[5] -- 0：中心导航信息，1：CAN故障码信息</li>
     * <li>bit[6~7] -- 保留</li>
     */
    @Preset.JtStyle.Byte(desc = "标志")
    private short identifier;

    /**
     * 文本类型
     * <p>
     * 1: 通知; 2: 服务
     */
    @Preset.JtStyle.Byte(desc = "文本类型")
    private short textType;
    /**
     * 文本信息
     * <p>
     * 最长为 1024 字节，经 GBK 编码
     */
    @Preset.JtStyle.Str(desc = "文本信息")
    private String text;

    public short getIdentifier() {
        return identifier;
    }

    public BuiltinMessage8300V2019 setIdentifier(short identifier) {
        this.identifier = identifier;
        return this;
    }

    public short getTextType() {
        return textType;
    }

    public BuiltinMessage8300V2019 setTextType(short textType) {
        this.textType = textType;
        return this;
    }

    public String getText() {
        return text;
    }

    public BuiltinMessage8300V2019 setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8300V2019.class.getSimpleName() + "[", "]")
                .add("identifier=" + identifier)
                .add("textType=" + textType)
                .add("text='" + text + "'")
                .toString();
    }
}

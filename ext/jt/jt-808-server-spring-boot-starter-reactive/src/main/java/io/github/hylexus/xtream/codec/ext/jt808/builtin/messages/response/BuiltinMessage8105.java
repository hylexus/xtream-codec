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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.codec.Message8105CodecSample;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.CommandValue;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 查询终端参数
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8105)
public class BuiltinMessage8105 {

    /**
     * <li>1: 无线升级</li>
     * <li>2:控制终端连接指定服务器</li>
     * <li>3: 终端关机；无命令参数</li>
     * <li>4: 终端复位；无命令参数</li>
     * <li>5: 终端回复出厂设置；无命令参数</li>
     * <li>6: 终关闭数据通信；无命令参数</li>
     * <li>7: 关闭所有无线通信；无命令参数</li>
     */
    @Preset.JtStyle.Byte
    private short commandWord;

    @XtreamField(
            // 这里是一个自定义的编解码器
            fieldCodec = Message8105CodecSample.class
    )
    private List<CommandValue<?>> commandValue;

    @SuppressWarnings("lombok")
    public short getCommandWord() {
        return this.commandWord;
    }

    public BuiltinMessage8105 setCommandWord(short commandWord) {
        this.commandWord = commandWord;
        return this;
    }

    public List<CommandValue<?>> getCommandValue() {
        return commandValue;
    }

    public BuiltinMessage8105 setCommandValue(List<CommandValue<?>> commandValue) {
        this.commandValue = commandValue;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8105.class.getSimpleName() + "[", "]")
                .add("commandWord=" + commandWord)
                .add("commandValue=" + commandValue)
                .toString();
    }
}

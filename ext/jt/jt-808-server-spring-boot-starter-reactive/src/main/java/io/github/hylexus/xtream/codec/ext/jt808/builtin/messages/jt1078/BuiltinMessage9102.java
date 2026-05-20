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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.jt1078;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

@Jt808ResponseBody(messageId = 0x9102, desc = "音视频实时传输控制")
public class BuiltinMessage9102 {

    @Preset.JtStyle.Byte(desc = "逻辑通道号")
    private short channelNumber;

    // 控制指令
    // 0: 关闭音视频传输指令
    // 1: 切换码流(增加暂停和继续)
    // 2: 暂停该通过所有流的发送
    // 3: 恢复暂停前流的发送，与暂停前的流类型一致
    // 4: 关闭双向对讲
    @Preset.JtStyle.Byte(desc = "控制指令")
    private short command;

    // 关闭音视频类型
    // 0: 关闭该通过有关的音视频数据
    // 1: 只关闭该通过有关的音频，保留该通道有关的视频
    // 2: 只关闭该通过有关的视频，保留该通道有关的音频
    @Preset.JtStyle.Byte(desc = "关闭音视频类型")
    private short mediaTypeToClose;

    // 切换码流类型
    // 将之前申请的码流切换为新申请的码流，音频与切换之前保持一致
    // 新申请的码流:
    // 0: 主码流
    // 1: 子码流
    @Preset.JtStyle.Byte(desc = "切换码流类型")
    private short streamType;

    public short getChannelNumber() {
        return channelNumber;
    }

    public BuiltinMessage9102 setChannelNumber(short channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    public short getCommand() {
        return command;
    }

    public BuiltinMessage9102 setCommand(short command) {
        this.command = command;
        return this;
    }

    public short getMediaTypeToClose() {
        return mediaTypeToClose;
    }

    public BuiltinMessage9102 setMediaTypeToClose(short mediaTypeToClose) {
        this.mediaTypeToClose = mediaTypeToClose;
        return this;
    }

    public short getStreamType() {
        return streamType;
    }

    public BuiltinMessage9102 setStreamType(short streamType) {
        this.streamType = streamType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage9102.class.getSimpleName() + "[", "]")
                .add("channelNumber=" + channelNumber)
                .add("command=" + command)
                .add("mediaTypeToClose=" + mediaTypeToClose)
                .add("streamType=" + streamType)
                .toString();
    }
}

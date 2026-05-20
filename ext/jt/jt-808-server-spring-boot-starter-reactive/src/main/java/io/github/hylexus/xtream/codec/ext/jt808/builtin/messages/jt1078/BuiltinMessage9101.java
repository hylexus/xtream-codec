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

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;


@Jt808ResponseBody(messageId = 0x9101, desc = "音视频实时传输请求")
public class BuiltinMessage9101 {

    // prependLengthFieldType = u8: 自动编码一个 u8 类型的字段表示IP长度字段
    @Preset.JtStyle.Str(desc = "服务器IP地址", prependLengthFieldType = PrependLengthFieldType.u8)
    private String serverIp;

    @Preset.JtStyle.Word(desc = "实时视频服务器TCP端口号")
    private int serverPortTcp;

    @Preset.JtStyle.Word(desc = "实时视频服务器UDP端口号")
    private int serverPortUdp;

    @Preset.JtStyle.Byte(desc = "逻辑通道号")
    private short channelNumber;

    @Preset.JtStyle.Byte(desc = "数据类型")
    private short dataType;

    @Preset.JtStyle.Byte(desc = "码流类型")
    private short streamType;

    public String getServerIp() {
        return serverIp;
    }

    public BuiltinMessage9101 setServerIp(String serverIp) {
        this.serverIp = serverIp;
        return this;
    }

    public int getServerPortTcp() {
        return serverPortTcp;
    }

    public BuiltinMessage9101 setServerPortTcp(int serverPortTcp) {
        this.serverPortTcp = serverPortTcp;
        return this;
    }

    public int getServerPortUdp() {
        return serverPortUdp;
    }

    public BuiltinMessage9101 setServerPortUdp(int serverPortUdp) {
        this.serverPortUdp = serverPortUdp;
        return this;
    }

    public short getChannelNumber() {
        return channelNumber;
    }

    public BuiltinMessage9101 setChannelNumber(short channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    public short getDataType() {
        return dataType;
    }

    public BuiltinMessage9101 setDataType(short dataType) {
        this.dataType = dataType;
        return this;
    }

    public short getStreamType() {
        return streamType;
    }

    public BuiltinMessage9101 setStreamType(short streamType) {
        this.streamType = streamType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage9101.class.getSimpleName() + "[", "]")
                .add("serverIp='" + serverIp + "'")
                .add("serverPortTcp=" + serverPortTcp)
                .add("serverPortUdp=" + serverPortUdp)
                .add("channelNumber=" + channelNumber)
                .add("dataType=" + dataType)
                .add("streamType=" + streamType)
                .toString();
    }
}

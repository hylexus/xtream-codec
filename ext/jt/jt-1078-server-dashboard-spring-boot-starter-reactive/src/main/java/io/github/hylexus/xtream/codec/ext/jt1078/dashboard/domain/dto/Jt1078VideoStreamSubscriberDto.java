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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.StringJoiner;

public class Jt1078VideoStreamSubscriberDto {

    @NotNull(message = "终端手机号不能为空")
    @NotEmpty(message = "终端手机号不能为空")
    private String sim;

    // 逻辑通道号
    private short channel;

    // 超时时间(秒)
    // 超过 timeout 秒之后依然没有收到终端的数据 ==> 自动关闭当前订阅(websocket)
    private int timeout = 10;

    // 将 byte[] 以 base64 编码后再发送给客户端(jackson内置默认逻辑)?
    private boolean byteArrayAsBase64 = false;

    private int naluDecoderRingBufferSize = 1 << 18;

    @JsonProperty("hasAudio")
    private boolean hasAudio = true;

    @JsonProperty("hasVideo")
    private boolean hasVideo = true;

    public String getSim() {
        return sim;
    }

    public Jt1078VideoStreamSubscriberDto setSim(String sim) {
        this.sim = sim;
        return this;
    }

    public short getChannel() {
        return channel;
    }

    public Jt1078VideoStreamSubscriberDto setChannel(short channel) {
        this.channel = channel;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Jt1078VideoStreamSubscriberDto setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isByteArrayAsBase64() {
        return byteArrayAsBase64;
    }

    public Jt1078VideoStreamSubscriberDto setByteArrayAsBase64(boolean byteArrayAsBase64) {
        this.byteArrayAsBase64 = byteArrayAsBase64;
        return this;
    }

    public int getNaluDecoderRingBufferSize() {
        return naluDecoderRingBufferSize;
    }

    public Jt1078VideoStreamSubscriberDto setNaluDecoderRingBufferSize(int naluDecoderRingBufferSize) {
        this.naluDecoderRingBufferSize = naluDecoderRingBufferSize;
        return this;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public Jt1078VideoStreamSubscriberDto setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
        return this;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public Jt1078VideoStreamSubscriberDto setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt1078VideoStreamSubscriberDto.class.getSimpleName() + "[", "]")
                .add("sim='" + sim + "'")
                .add("channel=" + channel)
                .add("timeout=" + timeout)
                .add("byteArrayAsBase64=" + byteArrayAsBase64)
                .add("naluDecoderRingBufferSize=" + naluDecoderRingBufferSize)
                .add("hasAudio=" + hasAudio)
                .add("hasVideo=" + hasVideo)
                .toString();
    }
}

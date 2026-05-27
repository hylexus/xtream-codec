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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.StringJoiner;

public class DashboardCommand9102Dto {

    @NotNull(message = "sim不能为空")
    @NotEmpty(message = "sim不能为空")
    private String sim;

    /**
     * 逻辑通道号
     */
    @NotNull(message = "channelNumber不能为空")
    private Integer channelNumber;

    /**
     * <ul>
     *     <li>0--关闭音视频传输指令</li>
     *     <li>1--切换码流</li>
     *     <li>2--暂停该通道所有流的发送</li>
     *     <li>3--恢复暂停前流的发送,与暂停前的流类型一致</li>
     *     <li>4--关闭双向对讲</li>
     * </ul>
     */
    @NotNull(message = "command不能为空")
    private Integer command;

    /**
     * <ul>
     *     <li>0--关闭该通道有关的音视频数据</li>
     *     <li>1--只关闭该通道有关的音频,保留该通道有关的视频</li>
     *     <li>2--只关闭该通道有关的视频,保留该通道有关的音频</li>
     * </ul>
     */
    @NotNull(message = "mediaTypeToClose不能为空")
    private Integer mediaTypeToClose;

    /**
     * <ul>
     *     <li>0-主码流</li>
     *     <li>1-子码流</li>
     * </ul>
     */
    @NotNull(message = "streamType不能为空")
    private Integer streamType;

    private Duration timeout = Duration.ofSeconds(10);

    public String getSim() {
        return sim;
    }

    public DashboardCommand9102Dto setSim(String sim) {
        this.sim = sim;
        return this;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    public DashboardCommand9102Dto setChannelNumber(Integer channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    public Integer getCommand() {
        return command;
    }

    public DashboardCommand9102Dto setCommand(Integer command) {
        this.command = command;
        return this;
    }

    public Integer getMediaTypeToClose() {
        return mediaTypeToClose;
    }

    public DashboardCommand9102Dto setMediaTypeToClose(Integer mediaTypeToClose) {
        this.mediaTypeToClose = mediaTypeToClose;
        return this;
    }

    public Integer getStreamType() {
        return streamType;
    }

    public DashboardCommand9102Dto setStreamType(Integer streamType) {
        this.streamType = streamType;
        return this;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public DashboardCommand9102Dto setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DashboardCommand9102Dto.class.getSimpleName() + "[", "]")
                .add("sim='" + sim + "'")
                .add("channelNumber=" + channelNumber)
                .add("command=" + command)
                .add("mediaTypeToClose=" + mediaTypeToClose)
                .add("streamType=" + streamType)
                .add("timeout=" + timeout)
                .toString();
    }
}

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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.StringJoiner;

public class DashboardCommand9101Dto {

    @NotNull(message = "sim不能为空")
    @NotEmpty(message = "sim不能为空")
    private String sim;

    @NotNull(message = "jt1078ServerIp不能为空")
    @NotEmpty(message = "jt1078ServerIp不能为空")
    private String jt1078ServerIp;

    @NotNull(message = "jt1078ServerPortTcp不能为空")
    @Max(value = 65535, message = "jt1078ServerPortTcp 不能超过 65535")
    @Min(value = 1, message = "jt1078ServerPortTcp 不能小于 1")
    private Integer jt1078ServerPortTcp;

    @NotNull(message = "jt1078ServerPortUdp不能为空")
    @Max(value = 65535, message = "jt1078ServerPortUdp 不能超过 65535")
    @Min(value = 1, message = "jt1078ServerPortUdp 不能小于 1")
    private Integer jt1078ServerPortUdp;

    @NotNull(message = "channelNumber不能为空")
    private Integer channelNumber;

    @NotNull(message = "dataType不能为空")
    private Integer dataType;

    @NotNull(message = "streamType不能为空")
    private Integer streamType;

    private Duration timeout = Duration.ofSeconds(10);

    public String getSim() {
        return sim;
    }

    public DashboardCommand9101Dto setSim(String sim) {
        this.sim = sim;
        return this;
    }

    public String getJt1078ServerIp() {
        return jt1078ServerIp;
    }

    public DashboardCommand9101Dto setJt1078ServerIp(String jt1078ServerIp) {
        this.jt1078ServerIp = jt1078ServerIp;
        return this;
    }

    public Integer getJt1078ServerPortTcp() {
        return jt1078ServerPortTcp;
    }

    public DashboardCommand9101Dto setJt1078ServerPortTcp(Integer jt1078ServerPortTcp) {
        this.jt1078ServerPortTcp = jt1078ServerPortTcp;
        return this;
    }

    public Integer getJt1078ServerPortUdp() {
        return jt1078ServerPortUdp;
    }

    public DashboardCommand9101Dto setJt1078ServerPortUdp(Integer jt1078ServerPortUdp) {
        this.jt1078ServerPortUdp = jt1078ServerPortUdp;
        return this;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    public DashboardCommand9101Dto setChannelNumber(Integer channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    public Integer getDataType() {
        return dataType;
    }

    public DashboardCommand9101Dto setDataType(Integer dataType) {
        this.dataType = dataType;
        return this;
    }

    public Integer getStreamType() {
        return streamType;
    }

    public DashboardCommand9101Dto setStreamType(Integer streamType) {
        this.streamType = streamType;
        return this;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public DashboardCommand9101Dto setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DashboardCommand9101Dto.class.getSimpleName() + "[", "]")
                .add("sim='" + sim + "'")
                .add("jt1078ServerIp='" + jt1078ServerIp + "'")
                .add("jt1078ServerPortTcp=" + jt1078ServerPortTcp)
                .add("jt1078ServerPortUdp=" + jt1078ServerPortUdp)
                .add("channelNumber=" + channelNumber)
                .add("dataType=" + dataType)
                .add("streamType=" + streamType)
                .add("timeout=" + timeout)
                .toString();
    }

}

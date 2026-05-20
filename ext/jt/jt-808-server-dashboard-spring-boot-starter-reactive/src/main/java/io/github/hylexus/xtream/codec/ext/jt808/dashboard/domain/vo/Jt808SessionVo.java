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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;

import java.time.Instant;
import java.util.StringJoiner;

public class Jt808SessionVo {
    private String id;
    private String terminalId;
    private Jt808ServerType serverType;
    private Jt808ProtocolVersion protocolVersion;
    private XtreamInbound.Type protocolType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant creationTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant lastCommunicateTime;

    public String getId() {
        return id;
    }

    public Jt808SessionVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public Jt808SessionVo setTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    public Jt808ServerType getServerType() {
        return serverType;
    }

    public Jt808SessionVo setServerType(Jt808ServerType serverType) {
        this.serverType = serverType;
        return this;
    }

    public Jt808ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public Jt808SessionVo setProtocolVersion(Jt808ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public XtreamInbound.Type getProtocolType() {
        return protocolType;
    }

    public Jt808SessionVo setProtocolType(XtreamInbound.Type protocolType) {
        this.protocolType = protocolType;
        return this;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Jt808SessionVo setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Instant getLastCommunicateTime() {
        return lastCommunicateTime;
    }

    public Jt808SessionVo setLastCommunicateTime(Instant lastCommunicateTime) {
        this.lastCommunicateTime = lastCommunicateTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt808SessionVo.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("terminalId='" + terminalId + "'")
                .add("serverType=" + serverType)
                .add("protocolVersion=" + protocolVersion)
                .add("protocolType=" + protocolType)
                .add("creationTime=" + creationTime)
                .add("lastCommunicateTime=" + lastCommunicateTime)
                .toString();
    }
}

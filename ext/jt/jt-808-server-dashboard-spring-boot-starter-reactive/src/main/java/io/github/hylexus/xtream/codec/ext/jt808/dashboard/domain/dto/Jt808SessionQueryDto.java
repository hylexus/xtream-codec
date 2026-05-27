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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto;

import io.github.hylexus.xtream.codec.base.web.domain.dto.PageableDto;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;

import java.util.StringJoiner;

public class Jt808SessionQueryDto extends PageableDto {
    private String terminalId;
    private Jt808ServerType serverType;
    private Jt808ProtocolVersion protocolVersion;
    private XtreamInbound.Type protocolType;

    public String getTerminalId() {
        return terminalId;
    }

    public Jt808SessionQueryDto setTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    public Jt808ServerType getServerType() {
        return serverType;
    }

    public Jt808SessionQueryDto setServerType(Jt808ServerType serverType) {
        this.serverType = serverType;
        return this;
    }

    public Jt808ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public Jt808SessionQueryDto setProtocolVersion(Jt808ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public XtreamInbound.Type getProtocolType() {
        return protocolType;
    }

    public Jt808SessionQueryDto setProtocolType(XtreamInbound.Type protocolType) {
        this.protocolType = protocolType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt808SessionQueryDto.class.getSimpleName() + "[", "]")
                .add("terminalId='" + terminalId + "'")
                .add("serverType=" + serverType)
                .add("protocolVersion=" + protocolVersion)
                .add("protocolType=" + protocolType)
                .toString();
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamOutbound;
import io.netty.buffer.ByteBufAllocator;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author hylexus
 */
public class DefaultJt808Session extends AbstractXtreamOutbound implements Jt808Session {
    private final XtreamSessionManager<Jt808Session> sessionManager;

    private final String id;
    private final Jt808ServerType role;
    private final Instant creationTime;
    private volatile Instant lastCommunicateTime;
    private volatile boolean verified;
    protected final Map<String, Object> attributes = new HashMap<>();
    // 下面几个属性只有在请求被解析之后才能确定具体值
    private final Jt808ProtocolVersion protocolVersion;
    private final String terminalId;

    public DefaultJt808Session(String id, Jt808ServerType role, XtreamInbound.Type type, Jt808ProtocolVersion protocolVersion, String terminalId, NettyOutbound outbound, InetSocketAddress remoteAddress, XtreamSessionManager<Jt808Session> sessionManager) {
        super(ByteBufAllocator.DEFAULT, outbound, type, remoteAddress);
        this.id = id;
        this.role = role;
        this.sessionManager = sessionManager;
        this.protocolVersion = protocolVersion;
        this.terminalId = terminalId;
        this.creationTime = this.lastCommunicateTime = Instant.now();
        this.verified = false;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Jt808ServerType role() {
        return this.role;
    }

    @Override
    public Instant creationTime() {
        return this.creationTime;
    }

    @Override
    public Instant lastCommunicateTime() {
        return this.lastCommunicateTime;
    }

    @Override
    public Jt808Session lastCommunicateTime(Instant current) {
        this.lastCommunicateTime = current;
        return this;
    }

    @Override
    public Jt808ProtocolVersion protocolVersion() {
        return this.protocolVersion;
    }


    @Override
    public String terminalId() {
        return this.terminalId;
    }

    @Override
    public Map<String, Object> attributes() {
        return this.attributes;
    }

    @Override
    public boolean verified() {
        return this.verified;
    }

    @Override
    public Jt808Session verified(boolean verified) {
        this.verified = verified;
        return this;
    }

    @Override
    public void invalidate(XtreamSessionEventListener.SessionCloseReason reason) {
        this.attributes().clear();
        this.sessionManager.closeSession(this, reason);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultJt808Session.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("role=" + role)
                .add("protocolVersion=" + protocolVersion)
                .add("terminalId='" + terminalId + "'")
                .add("remoteAddress='" + remoteAddress() + "'")
                .toString();
    }
}

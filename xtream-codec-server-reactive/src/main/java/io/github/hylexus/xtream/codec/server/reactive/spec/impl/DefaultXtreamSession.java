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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;


import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
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
public class DefaultXtreamSession extends AbstractXtreamOutbound implements XtreamSession {
    protected final XtreamSessionManager<XtreamSession> sessionManager;
    protected final String id;
    protected final Map<String, Object> attributes = new HashMap<>();
    private final Instant creationTime;
    private volatile Instant lastCommunicateTime;

    public DefaultXtreamSession(String id, XtreamInbound.Type type, NettyOutbound outbound, InetSocketAddress remoteAddress, XtreamSessionManager<XtreamSession> sessionManager) {
        super(ByteBufAllocator.DEFAULT, outbound, type, remoteAddress);
        this.id = id;
        this.sessionManager = sessionManager;
        this.creationTime = this.lastCommunicateTime = Instant.now();
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Map<String, Object> attributes() {
        return this.attributes;
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
    public XtreamSession lastCommunicateTime(Instant current) {
        this.lastCommunicateTime = creationTime;
        return this;
    }

    @Override
    public void invalidate(XtreamSessionEventListener.SessionCloseReason reason) {
        this.attributes().clear();
        this.sessionManager.closeSession(this, reason);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultXtreamSession.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("remoteAddress=" + remoteAddress)
                .toString();
    }
}

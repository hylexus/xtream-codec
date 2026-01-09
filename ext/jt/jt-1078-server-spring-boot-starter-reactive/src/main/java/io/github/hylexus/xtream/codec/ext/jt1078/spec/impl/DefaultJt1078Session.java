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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamOutbound;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DefaultJt1078Session extends AbstractXtreamOutbound implements Jt1078Session {
    private final XtreamSessionManager<Jt1078Session> sessionManager;
    private final String id;
    private final Instant creationTime;
    private Instant lastCommunicateTime;
    protected final Map<String, Object> attributes = new HashMap<>();
    private final int simLength;
    private final String convertedSim;
    private final String rawSim;
    private final short channelNumber;

    private @Nullable Jt1078PayloadType audioType;
    private @Nullable Jt1078PayloadType videoType;

    public DefaultJt1078Session(ByteBufAllocator byteBufAllocator, NettyOutbound delegate, XtreamSessionManager<Jt1078Session> sessionManager, String id, XtreamInbound.Type type, int simLength, String rawSim, String convertedSim, short channelNumber, InetSocketAddress remoteAddress) {
        super(byteBufAllocator, delegate, type, remoteAddress);
        this.id = id;
        this.simLength = simLength;
        this.sessionManager = sessionManager;
        this.convertedSim = convertedSim;
        this.rawSim = rawSim;
        this.channelNumber = channelNumber;
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
    public Jt1078Session lastCommunicateTime(Instant current) {
        this.lastCommunicateTime = current;
        return this;
    }

    @Override
    public int simLength() {
        return this.simLength;
    }

    @Override
    public void invalidate(XtreamSessionEventListener.SessionCloseReason reason) {
        this.attributes().clear();
        this.sessionManager.closeSession(this, reason);
    }

    @Override
    public String rawSim() {
        return this.rawSim;
    }

    @Override
    public String convertedSim() {
        return this.convertedSim;
    }

    @Override
    public short channelNumber() {
        return this.channelNumber;
    }

    @Override
    public @Nullable Jt1078PayloadType audioType() {
        return this.audioType;
    }

    @Override
    public Jt1078Session audioType(Jt1078PayloadType type) {
        this.audioType = type;
        return this;
    }

    @Override
    public @Nullable Jt1078PayloadType videoType() {
        return this.videoType;
    }

    @Override
    public Jt1078Session videoType(Jt1078PayloadType type) {
        this.videoType = type;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultJt1078Session.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("convertedSim='" + convertedSim + "'")
                .add("rawSim='" + rawSim + "'")
                .add("channelNumber=" + channelNumber)
                .add("lastCommunicateTime=" + lastCommunicateTime)
                .add("creationTime=" + creationTime)
                .toString();
    }

}

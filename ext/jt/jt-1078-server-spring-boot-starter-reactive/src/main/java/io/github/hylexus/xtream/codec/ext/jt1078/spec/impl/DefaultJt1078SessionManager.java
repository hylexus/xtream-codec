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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Session;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamSessionManager;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

public class DefaultJt1078SessionManager
        extends AbstractXtreamSessionManager<Jt1078Session>
        implements Jt1078SessionManager {

    public DefaultJt1078SessionManager(boolean udpSessionIdleStateCheckerEnabled, UdpSessionIdleStateCheckerProps checkerProps, XtreamSessionIdGenerator idGenerator) {
        super(udpSessionIdleStateCheckerEnabled, checkerProps, idGenerator);
    }

    @Override
    @Deprecated
    protected Jt1078Session doCreateSession(String sessionId, XtreamExchange exchange) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Jt1078Session getUdpSession(InetSocketAddress remoteAddress) {
        final String sessionId = this.sessionIdGenerator.generateUdpSessionId(remoteAddress);
        return this.sessions.get(sessionId);
    }

    @Override
    public Mono<Jt1078Session> createSession(String sessionId, Mono<Jt1078Session> sessionLoader) {
        return sessionLoader.map(session -> {
            this.sessions.put(sessionId, session);
            if (session.type() == XtreamInbound.Type.TCP) {
                this.tcpSessionCount.incrementAndGet();
            } else {
                this.udpSessionCount.incrementAndGet();
            }
            this.invokeListener(listener -> listener.afterSessionCreate(session));
            return session;
        });
    }

    @Override
    public void createSession(Jt1078Session session) {
        this.sessions.put(session.id(), session);
        if (session.type() == XtreamInbound.Type.TCP) {
            this.tcpSessionCount.incrementAndGet();
        } else {
            this.udpSessionCount.incrementAndGet();
        }
        this.invokeListener(listener -> listener.afterSessionCreate(session));
    }

}

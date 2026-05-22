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

import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamIntervalChecker;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author hylexus
 */
public abstract class AbstractXtreamSessionManager<S extends XtreamSession> implements XtreamSessionManager<S> {
    private static final Logger log = LoggerFactory.getLogger(AbstractXtreamSessionManager.class);
    protected final XtreamSessionIdGenerator sessionIdGenerator;
    protected final ConcurrentMap<String, S> sessions = new ConcurrentHashMap<>();
    protected final AtomicLong tcpSessionCount = new AtomicLong(0);
    protected final AtomicLong udpSessionCount = new AtomicLong(0);
    protected final @Nullable XtreamIntervalChecker checker;
    protected final @Nullable Duration maxIdleTime;
    protected final Clock clock = Clock.system(ZoneId.of("Asia/Shanghai"));
    protected final Lock lock = new ReentrantLock();
    protected final List<XtreamSessionEventListener<S>> listenerList = new ArrayList<>();

    public AbstractXtreamSessionManager(
            boolean udpSessionIdleStateCheckerEnabled,
            @Nullable UdpSessionIdleStateCheckerProps checkerProps,
            XtreamSessionIdGenerator idGenerator) {

        this.sessionIdGenerator = idGenerator;
        if (udpSessionIdleStateCheckerEnabled && checkerProps != null) {
            this.checker = new XtreamIntervalChecker(
                    "session-checker",
                    checkerProps.getCheckInterval(),
                    checkerProps.getCheckBackoffTime(),
                    (name, interval) -> {
                        log.debug("{} interval: {}", name, interval);
                        this.doCheckUdpSessionStatus();
                        return Mono.empty();
                    }
            );
            this.checker.start();
        } else {
            this.checker = null;
        }
        this.maxIdleTime = checkerProps != null ? checkerProps.getMaxIdleTime() : null;
    }

    @Override
    public XtreamSessionIdGenerator sessionIdGenerator() {
        return this.sessionIdGenerator;
    }

    @Override
    public void addListener(XtreamSessionEventListener<S> listener) {
        this.listenerList.add(listener);
    }

    void doCheckUdpSessionStatus() {
        if (sessions.isEmpty()) {
            return;
        }

        // 加锁失败就等下一次
        if (this.lock.tryLock()) {
            try {
                final Iterator<S> iterator = this.sessions.values().iterator();
                final Instant now = this.clock.instant();
                while (iterator.hasNext()) {
                    final S session = iterator.next();
                    // TCP 连接由 IdleStateHandler 处理
                    // @see io.github.hylexus.xtream.codec.ext.jt808.spec.XtreamTcpHeatBeatHandler
                    if (session.type() == XtreamInbound.Type.UDP) {
                        if (isExpired(session, now)) {
                            log.info("Session expired, remove it: {}", session);
                            session.invalidate(XtreamSessionEventListener.DefaultSessionCloseReason.EXPIRED);
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    protected boolean isExpired(S session, Instant now) {
        return now.minus(Objects.requireNonNull(this.maxIdleTime)).isAfter(session.lastCommunicateTime());
    }

    @Override
    public Mono<S> getSession(XtreamExchange exchange) {
        return Mono.defer(() -> {
            final String sessionId = this.sessionIdGenerator.generateSessionId(exchange);
            final S session = this.sessions.get(sessionId);
            return Mono.justOrEmpty(session);
        });
    }

    @Override
    public Mono<S> createSession(String sessionId, XtreamExchange exchange) {
        final S session = this.doCreateSession(sessionId, exchange);
        this.sessions.put(sessionId, session);
        if (session.type() == XtreamInbound.Type.TCP) {
            this.tcpSessionCount.incrementAndGet();
        } else {
            this.udpSessionCount.incrementAndGet();
        }
        this.invokeListener(listener -> listener.afterSessionCreate(session));

        return Mono.just(session);
    }

    protected void invokeListener(Consumer<XtreamSessionEventListener<S>> action) {
        for (XtreamSessionEventListener<S> xtreamSessionEventListener : this.listenerList) {
            try {
                action.accept(xtreamSessionEventListener);
            } catch (Exception e) {
                log.error("Error occurred while invoke listener", e);
            }
        }
    }

    protected abstract S doCreateSession(String sessionId, XtreamExchange exchange);

    @Override
    public Mono<S> getSessionById(String sessionId) {
        final S s = this.sessions.get(sessionId);
        return Mono.justOrEmpty(s);
    }

    @Override
    public void closeSession(S session, XtreamSessionEventListener.SessionCloseReason reason) {
        this.sessions.remove(session.id());
        this.doClose(session, reason);
    }

    @Override
    public boolean closeSessionById(String sessionId, XtreamSessionEventListener.SessionCloseReason reason) {
        final S session = sessions.remove(sessionId);
        if (session != null) {
            this.doClose(session, reason);
            return true;
        }
        return false;
    }

    protected void doClose(S session, XtreamSessionEventListener.SessionCloseReason reason) {
        if (session.type() == XtreamInbound.Type.TCP) {
            this.tcpSessionCount.decrementAndGet();
        } else {
            this.udpSessionCount.decrementAndGet();
        }
        invokeListener(listener -> listener.beforeSessionClose(session, reason));
        session.outbound().withConnection(connection -> {
            try {
                this.beforeConnectionClose(connection, session);
            } finally {
                if (session.type() == XtreamInbound.Type.TCP) {
                    connection.dispose();
                }
            }
        });
    }

    protected void beforeConnectionClose(Connection connection, S session) {
    }

    @Override
    public Stream<S> list() {
        return this.sessions.values().stream();
    }

    @Override
    public long count() {
        return this.sessions.size();
    }

    @Override
    public long countTcp() {
        return this.tcpSessionCount.get();
    }

    @Override
    public long countUdp() {
        return this.udpSessionCount.get();
    }

    @Override
    public void shutdown() {
        if (this.checker != null) {
            this.checker.stop();
        }
    }
}

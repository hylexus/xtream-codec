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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamIntervalChecker;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.SessionCreationRejectedException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.github.hylexus.xtream.codec.server.reactive.spec.exception.SessionCreationRejectedException.Reason.SESSION_NO_LONGER_REGISTERED;
import static io.github.hylexus.xtream.codec.server.reactive.spec.exception.SessionCreationRejectedException.Reason.TCP_CONNECTION_INACTIVE;

/**
 * @author hylexus
 * @author Codex (AI)
 */
public abstract class AbstractXtreamSessionManager<S extends XtreamSession> implements XtreamSessionManager<S> {
    private static final Logger log = LoggerFactory.getLogger(AbstractXtreamSessionManager.class);
    protected final XtreamSessionIdGenerator sessionIdGenerator;
    protected final ConcurrentMap<String, S> sessions = new ConcurrentHashMap<>();
    // 表中仅记录创建通知尚未完成的会话，键按对象身份区分。
    // Optional.empty() 表示创建期间尚无关闭请求，有值时保存待处理的关闭原因。
    private final ConcurrentMap<SessionIdentity, Optional<XtreamSessionEventListener.SessionCloseReason>> creatingSessions = new ConcurrentHashMap<>();
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

    /**
     * {@inheritDoc}
     *
     * @see #doClose(XtreamSession, XtreamSessionEventListener.SessionCloseReason)
     */
    @Override
    public Mono<S> createSession(String sessionId, XtreamExchange exchange) {
        if (this.isTcpConnectionInactive(exchange)) {
            return Mono.error(new SessionCreationRejectedException(sessionId, TCP_CONNECTION_INACTIVE));
        }
        final AtomicBoolean created = new AtomicBoolean();
        // 构造和计数随新实例的原子登记执行，复用已有会话的调用跳过这一步。
        final S session = this.sessions.computeIfAbsent(sessionId, id -> {
            // 等待同键的原子更新期间，TCP 通道可能已经失效。
            if (this.isTcpConnectionInactive(exchange)) {
                return null;
            }
            final S candidate = this.doCreateSession(id, exchange);
            // 在会话可见前登记。旧实例与同键的新实例可能同时处在回调中。
            this.creatingSessions.put(new SessionIdentity(candidate), Optional.empty());
            if (candidate.type() == XtreamInbound.Type.TCP) {
                this.tcpSessionCount.incrementAndGet();
            } else {
                this.udpSessionCount.incrementAndGet();
            }
            created.set(true);
            return candidate;
        });
        if (session == null) {
            return Mono.error(new SessionCreationRejectedException(sessionId, TCP_CONNECTION_INACTIVE));
        }
        if (created.get()) {
            // 用户监听器可以重入会话管理器，不能在 Map 的原子更新内调用。
            try {
                this.onSessionRegistered(session, exchange);
                this.invokeListener(listener -> listener.afterSessionCreate(session));
            } finally {
                // 原子取走登记
                final Optional<XtreamSessionEventListener.SessionCloseReason> deferredClose = this.creatingSessions.remove(new SessionIdentity(session));
                if (deferredClose != null) {
                    // 如果关闭原因已写入(值已经不再是 Optional.empty() 了)，由这里补发通知并完成连接清理
                    deferredClose.ifPresent(reason -> this.finishClose(session, reason));
                }
            }
        }
        if (this.isTcpConnectionInactive(exchange)) {
            this.closeSession(session, XtreamSessionEventListener.DefaultSessionCloseReason.CLOSED_BY_CLIENT);
            return Mono.error(new SessionCreationRejectedException(sessionId, TCP_CONNECTION_INACTIVE));
        }
        // 创建回调可能已关闭本实例，甚至已登记同键的新实例。
        if (this.sessions.get(sessionId) != session) {
            return Mono.error(new SessionCreationRejectedException(sessionId, SESSION_NO_LONGER_REGISTERED));
        }
        return Mono.just(session);
    }

    private boolean isTcpConnectionInactive(XtreamExchange exchange) {
        final XtreamRequest request = exchange.request();
        return request.type() == XtreamInbound.Type.TCP && !request.underlyingChannel().isActive();
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

    protected void onSessionRegistered(S session, XtreamExchange exchange) {
    }

    @Override
    public Mono<S> getSessionById(String sessionId) {
        final S s = this.sessions.get(sessionId);
        return Mono.justOrEmpty(s);
    }

    @Override
    public void closeSession(S session, XtreamSessionEventListener.SessionCloseReason reason) {
        final AtomicBoolean removed = new AtomicBoolean();
        this.sessions.computeIfPresent(session.id(), (id, current) -> {
            // 按对象身份移除，避免旧实例迟到的关闭操作误删同键的新会话。
            if (current == session) {
                removed.set(true);
                return null;
            }
            return current;
        });
        if (removed.get()) {
            this.doClose(session, reason);
        }
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

    /**
     * @see #createSession(String, XtreamExchange)
     */
    protected void doClose(S session, XtreamSessionEventListener.SessionCloseReason reason) {
        if (session.type() == XtreamInbound.Type.TCP) {
            this.tcpSessionCount.decrementAndGet();
        } else {
            this.udpSessionCount.decrementAndGet();
        }

        // 此时会话已移除、计数已扣减。若创建通知仍在进行，仅记录关闭原因。
        final Optional<XtreamSessionEventListener.SessionCloseReason> deferredClose = this.creatingSessions.computeIfPresent(
                new SessionIdentity(session),
                (key, pending) -> Optional.of(reason)
        );
        if (deferredClose == null) {
            // 创建路径已移除登记，本次关闭负责通知监听器和完成连接清理。
            this.finishClose(session, reason);
        }
    }

    private void finishClose(S session, XtreamSessionEventListener.SessionCloseReason reason) {
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

    private record SessionIdentity(XtreamSession session) {
        @Override
        public boolean equals(Object other) {
            return other instanceof SessionIdentity(XtreamSession s) && this.session == s;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.session);
        }
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

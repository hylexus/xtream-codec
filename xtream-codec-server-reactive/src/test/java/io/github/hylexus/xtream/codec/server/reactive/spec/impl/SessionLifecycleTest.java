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
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.SessionCreationRejectedException;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.SessionCreationRejectedException.Reason;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class SessionLifecycleTest {
    private static final XtreamSessionEventListener.SessionCloseReason REASON = XtreamSessionEventListener.DefaultSessionCloseReason.CLOSED_BY_CLIENT;

    @Test
    void concurrentCreationShouldPublishAndNotifyOnce() throws Exception {
        final TestManager manager = new TestManager();
        manager.creationGate = new CountDownLatch(2);
        try (
                final Fixture first = fixture(manager, "same", XtreamInbound.Type.TCP);
                final Fixture second = fixture(manager, "same", XtreamInbound.Type.TCP);
                final var executor = Executors.newFixedThreadPool(2)
        ) {
            final var a = executor.submit(() -> manager.createSession("same", first.exchange()).block());
            final var b = executor.submit(() -> manager.createSession("same", second.exchange()).block());
            assertSame(a.get(5, TimeUnit.SECONDS), b.get(5, TimeUnit.SECONDS));
            assertEquals(1, manager.count());
            assertEquals(1, manager.countTcp());
            assertEquals(1, manager.created.get());
        }
    }

    @Test
    @NullMarked
    void repeatedAndReentrantCloseShouldOnlyCloseOnce() {
        final TestManager manager = new TestManager();
        manager.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                manager.closeSession(session, reason);
            }
        });
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.TCP)) {
            final XtreamSession session = manager.createSession("same", fixture.exchange()).block();
            assertNotNull(session);
            session.invalidate(REASON);
            session.invalidate(REASON);
            assertFalse(manager.closeSessionById("same", REASON));
            assertEquals(0, manager.count());
            assertEquals(0, manager.countTcp());
            assertEquals(1, manager.closed.get());
        }
    }

    @Test
    @NullMarked
    void reentrantCloseShouldNotifyAfterCreationAndRejectExplicitCreation() {
        final TestManager manager = new TestManager();
        final List<String> events = new ArrayList<>();
        manager.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void afterSessionCreate(XtreamSession session) {
                manager.closeSession(session, REASON);
                assertEquals(0, manager.countUdp());
                assertNull(manager.getSessionById(session.id()).block());
            }
        });
        manager.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void afterSessionCreate(XtreamSession session) {
                events.add("create");
            }

            @Override
            public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                events.add("close");
            }
        });
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.UDP)) {
            final var failure = assertThrows(SessionCreationRejectedException.class,
                    () -> manager.createSession("same", fixture.exchange()).block());
            assertEquals("same", failure.getSessionId());
            assertEquals(Reason.SESSION_NO_LONGER_REGISTERED, failure.getReason());
            assertEquals(List.of("create", "close"), events);
            assertEquals(1, manager.closed.get());
        }
    }

    @Test
    @NullMarked
    void concurrentCloseShouldDeferNotificationWithoutBlockingOrRemovingReplacement() throws Exception {
        final TestManager manager = new TestManager();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch resume = new CountDownLatch(1);
        manager.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void afterSessionCreate(XtreamSession session) {
                if (entered.getCount() > 0) {
                    entered.countDown();
                    await(resume);
                }
            }
        });
        try (
                final Fixture first = fixture(manager, "same", XtreamInbound.Type.UDP);
                final Fixture second = fixture(manager, "same", XtreamInbound.Type.UDP);
                final var executor = Executors.newFixedThreadPool(2)
        ) {
            final var creation = executor.submit(() -> first.exchange().session().block());
            final XtreamSession replacement;
            try {
                await(entered);
                final var closed = executor.submit(() -> manager.closeSessionById("same", REASON));
                assertTrue(closed.get(2, TimeUnit.SECONDS));
                assertEquals(0, manager.closed.get());
                replacement = second.exchange().session().block();
                assertNotNull(replacement);
            } finally {
                resume.countDown();
            }
            assertNull(creation.get(2, TimeUnit.SECONDS));
            assertSame(replacement, manager.getSessionById("same").block());
            assertEquals(1, manager.closed.get());
            assertEquals(1, manager.countUdp());
        }
    }

    @Test
    void staleCloseShouldNotRemoveReplacement() {
        final TestManager manager = new TestManager();
        try (
                final Fixture first = fixture(manager, "same", XtreamInbound.Type.TCP);
                final Fixture second = fixture(manager, "same", XtreamInbound.Type.TCP)
        ) {
            final XtreamSession old = manager.createSession("same", first.exchange()).block();
            assertTrue(manager.closeSessionById("same", REASON));
            final XtreamSession replacement = manager.createSession("same", second.exchange()).block();
            assertNotSame(old, replacement);
            manager.closeSession(old, REASON);
            assertSame(replacement, manager.getSessionById("same").block());
            assertEquals(1, manager.countTcp());
            assertEquals(1, manager.closed.get());
        }
    }

    @Test
    void concurrentCloseByObjectAndIdShouldOnlyDecrementOnce() throws Exception {
        final TestManager manager = new TestManager();
        try (
                final Fixture fixture = fixture(manager, "same", XtreamInbound.Type.UDP);
                final var executor = Executors.newFixedThreadPool(2)
        ) {
            final XtreamSession session = manager.createSession("same", fixture.exchange()).block();
            final CountDownLatch start = new CountDownLatch(1);
            final var a = executor.submit(() -> {
                await(start);
                manager.closeSession(session, REASON);
            });
            final var b = executor.submit(() -> {
                await(start);
                manager.closeSessionById("same", REASON);
            });
            start.countDown();
            a.get(5, TimeUnit.SECONDS);
            b.get(5, TimeUnit.SECONDS);
            assertEquals(0, manager.countUdp());
            assertEquals(1, manager.closed.get());
        }
    }

    @Test
    void exchangeShouldKeepSessionAfterItWasClosed() {
        final TestManager manager = new TestManager();
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.UDP)) {
            final XtreamSession session = fixture.exchange().session().block();
            manager.closeSessionById("same", REASON);
            assertSame(session, fixture.exchange().session().block());
            assertEquals(0, manager.count());
            assertEquals(1, manager.created.get());
        }
    }

    @Test
    void exchangeAttributesShouldSurviveMultipleReadsAndMutation() {
        final TestManager manager = new TestManager();
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.UDP)) {
            fixture.exchange().attributes().put("decoded", "body");
            assertEquals("body", fixture.exchange().attributes().get("decoded"));
            assertEquals("body", fixture.exchange().mutate().build().attributes().get("decoded"));
        }
    }

    @Test
    void inactiveTcpChannelShouldNotCreateSession() {
        final TestManager manager = new TestManager();
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.TCP)) {
            fixture.channel().close().syncUninterruptibly();
            final var failure = assertThrows(SessionCreationRejectedException.class,
                    () -> manager.createSession(fixture.exchange()).block());
            assertEquals(Reason.TCP_CONNECTION_INACTIVE, failure.getReason());
            assertNull(fixture.exchange().session().block());
            assertEquals(0, manager.countTcp());
            assertEquals(0, manager.created.get());
        }
    }

    @Test
    void channelClosedDuringCreationShouldNotLeaveSession() {
        final TestManager manager = new TestManager();
        manager.closeDuringCreation = true;
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.TCP)) {
            final var failure = assertThrows(SessionCreationRejectedException.class,
                    () -> manager.createSession("same", fixture.exchange()).block());
            assertEquals(Reason.TCP_CONNECTION_INACTIVE, failure.getReason());
            assertEquals(0, manager.count());
            assertEquals(0, manager.countTcp());
        }
    }

    @Test
    void lastCommunicationShouldUseSuppliedTime() {
        final TestManager manager = new TestManager();
        try (Fixture fixture = fixture(manager, "same", XtreamInbound.Type.UDP)) {
            final XtreamSession session = fixture.exchange().session().block();
            assertNotNull(session);
            final var created = session.creationTime();
            final var updated = created.plus(Duration.ofMinutes(1));
            session.lastCommunicateTime(updated);
            assertEquals(updated, session.lastCommunicateTime());
            assertEquals(created, session.creationTime());
        }
    }

    @Test
    void idleScanClosesOnlyExpiredUdpSessionsAndKeepsTheTransportActive() {
        final var props = new UdpSessionIdleStateCheckerProps().setMaxIdleTime(Duration.ofSeconds(10));
        final var manager = new TestManager(props);
        final List<XtreamSessionEventListener.SessionCloseReason> reasons = new ArrayList<>();
        manager.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                reasons.add(reason);
            }
        });
        manager.doCheckUdpSessionStatus();
        try (
                var expired = fixture(manager, "expired", XtreamInbound.Type.UDP);
                var fresh = fixture(manager, "fresh", XtreamInbound.Type.UDP);
                var tcp = fixture(manager, "tcp", XtreamInbound.Type.TCP)
        ) {
            final var expiredSession = expired.exchange().session().block();
            final var freshSession = fresh.exchange().session().block();
            final var tcpSession = tcp.exchange().session().block();
            assertNotNull(expiredSession);
            assertNotNull(freshSession);
            assertNotNull(tcpSession);
            expiredSession.lastCommunicateTime(Instant.EPOCH);
            freshSession.lastCommunicateTime(Instant.MAX);
            tcpSession.lastCommunicateTime(Instant.EPOCH);
            assertFalse(manager.isExpired(expiredSession, Instant.EPOCH.plusSeconds(10)));
            assertTrue(manager.isExpired(expiredSession, Instant.EPOCH.plusSeconds(11)));

            manager.doCheckUdpSessionStatus();
            manager.doCheckUdpSessionStatus();
            assertEquals(List.of(XtreamSessionEventListener.DefaultSessionCloseReason.EXPIRED), reasons);
            assertNull(manager.getSessionById("expired").block());
            assertSame(freshSession, manager.getSessionById("fresh").block());
            assertSame(tcpSession, manager.getSessionById("tcp").block());
            assertEquals(1, manager.countUdp());
            assertEquals(1, manager.countTcp());
            assertTrue(expired.channel().isActive());
            assertTrue(tcp.channel().isActive());
            manager.closeSession(freshSession, REASON);
            manager.closeSession(tcpSession, REASON);
            assertFalse(tcp.channel().isActive());
        } finally {
            manager.shutdown();
        }
    }

    private static Fixture fixture(DefaultXtreamSessionManager manager, String id, XtreamInbound.Type type) {
        final EmbeddedChannel channel = new EmbeddedChannel();
        final var remote = new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345);
        final NettyOutbound outbound = (NettyOutbound) Proxy.newProxyInstance(SessionLifecycleTest.class.getClassLoader(), new Class<?>[]{NettyOutbound.class}, (proxy, method, args) -> {
            if (method.getName().equals("withConnection")) {
                @SuppressWarnings("unchecked") final Consumer<Connection> consumer = (Consumer<Connection>) args[0];
                consumer.accept(Connection.from(channel));
                return proxy;
            }
            return null;
        });
        final var request = new DefaultXtreamRequest(0, id, ByteBufAllocator.DEFAULT, null, type, Unpooled.buffer(1), channel, remote);
        final var response = new DefaultXtreamResponse(ByteBufAllocator.DEFAULT, outbound, type, remote);
        return new Fixture(new DefaultXtreamExchange(manager, request, response), channel);
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private record Fixture(DefaultXtreamExchange exchange, EmbeddedChannel channel) implements AutoCloseable {
        @Override
        public void close() {
            exchange.request().release();
            channel.finishAndReleaseAll();
        }
    }

    @NullMarked
    private static final class TestManager extends DefaultXtreamSessionManager {
        private final AtomicInteger created = new AtomicInteger();
        private final AtomicInteger closed = new AtomicInteger();
        private @Nullable CountDownLatch creationGate;
        private boolean closeDuringCreation;

        private TestManager() {
            this(null);
        }

        private TestManager(@Nullable UdpSessionIdleStateCheckerProps props) {
            super(false, props, new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator() {
                @Override
                public String generateSessionId(XtreamExchange exchange) {
                    return exchange.request().requestId();
                }
            });
            addListener(new XtreamSessionEventListener<>() {
                @Override
                public void afterSessionCreate(XtreamSession session) {
                    created.incrementAndGet();
                }

                @Override
                public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                    closed.incrementAndGet();
                }
            });
        }

        @Override
        public Mono<XtreamSession> createSession(String id, XtreamExchange exchange) {
            if (creationGate != null) {
                creationGate.countDown();
                await(creationGate);
            }
            return super.createSession(id, exchange);
        }

        @Override
        protected XtreamSession doCreateSession(String id, XtreamExchange exchange) {
            if (closeDuringCreation) {
                exchange.request().underlyingChannel().close().syncUninterruptibly();
            }
            return super.doCreateSession(id, exchange);
        }
    }
}

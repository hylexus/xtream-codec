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

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServers;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.SimpleXtreamRequestHandler;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ChannelBindException;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class XtreamTransportLifecycleTest {
    private static final int SOCKET_TIMEOUT_MILLIS = 2000;

    @Test
    void defaultTcpSessionManagerDoesNotScheduleUdpIdleChecks() throws Exception {
        final var loops = LoopResources.create("tcp-default-session-test", 1, true);
        final var address = new AtomicReference<InetSocketAddress>();
        final var received = new CompletableFuture<DefaultXtreamSession>();
        final SimpleXtreamRequestHandler handler = exchange -> exchange.session()
                .doOnNext(session -> received.complete((DefaultXtreamSession) session)).then();
        final var server = XtreamServers.tcp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .dispatch(builder -> builder.addHandlerMapping(exchange -> Mono.just(handler)).enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var client = new Socket()) {
                client.connect(address.get(), SOCKET_TIMEOUT_MILLIS);
                client.getOutputStream().write(1);
                final var sessions = (DefaultXtreamSessionManager) received.get(2, TimeUnit.SECONDS).sessionManager;
                try {
                    assertNull(sessions.checker);
                } finally {
                    sessions.shutdown();
                }
            }
        } finally {
            stop(server, loops);
        }
    }

    @Test
    void defaultUdpSessionCheckerDoesNotSurviveABindFailure() throws Exception {
        final var loops = LoopResources.create("udp-bind-failure-test", 1, true);
        final var port = new AtomicInteger();
        final var received = new CompletableFuture<DefaultXtreamSession>();
        final SimpleXtreamRequestHandler handler = exchange -> exchange.session()
                .doOnNext(session -> received.complete((DefaultXtreamSession) session)).then();
        final var address = new AtomicReference<InetSocketAddress>();
        try (var probe = new SessionCheckerProbe()) {
            final var server = XtreamServers.udp().bind("127.0.0.1", 0)
                    .customize(netty -> netty.port(port.get()).runOn(loops, false)
                            .doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                    .dispatch(builder -> builder.addHandlerMapping(exchange -> Mono.just(handler)).enableBuiltinHandlers(EntityCodec.DEFAULT))
                    .build();
            try (var occupied = udpClient(); var client = udpClient()) {
                port.set(occupied.getLocalPort());
                final var failure = assertThrows(IllegalStateException.class, server::start);
                assertInstanceOf(ChannelBindException.class, failure.getCause());
                final var activeAfterFailure = probe.active();

                port.set(0);
                server.start();
                send(client, address.get(), 1);
                final var manager = received.get(2, TimeUnit.SECONDS).sessionManager;
                try {
                    assertTrue(activeAfterFailure.isEmpty(), () -> "绑定失败后仍运行的检查任务数 " + activeAfterFailure.size());
                } finally {
                    manager.shutdown();
                }
            } finally {
                stop(server, loops);
            }
        }
    }

    @Test
    void defaultUdpSessionCheckerStopsAndResumesExpiryAfterRestart() throws Exception {
        final var loops = LoopResources.create("udp-default-session-test", 1, true);
        final var bound = new AtomicReference<Connection>();
        final var received = new AtomicReference<>(new CompletableFuture<DefaultXtreamSession>());
        final SimpleXtreamRequestHandler handler = exchange -> exchange.session()
                .doOnNext(session -> received.get().complete((DefaultXtreamSession) session)).then();
        try (var probe = new SessionCheckerProbe()) {
            final var server = XtreamServers.udp().bind("127.0.0.1", 0)
                    .customize(netty -> netty.runOn(loops, false).doOnBound(bound::set))
                    .dispatch(builder -> builder.addHandlerMapping(exchange -> Mono.just(handler)).enableBuiltinHandlers(EntityCodec.DEFAULT))
                    .build();
            try (var client = udpClient()) {
                server.start();
                send(client, (InetSocketAddress) bound.get().channel().localAddress(), 1);
                final var first = received.get().get(2, TimeUnit.SECONDS);
                try {
                    assertEquals(1, probe.active().size());
                    stopUdpAndAwaitCleanup(server, bound.get());
                    assertTrue(probe.active().isEmpty());

                    final var next = new CompletableFuture<DefaultXtreamSession>();
                    received.set(next);
                    server.start();
                    send(client, (InetSocketAddress) bound.get().channel().localAddress(), 2);
                    final var restarted = next.get(2, TimeUnit.SECONDS);
                    final var expired = new CompletableFuture<XtreamSession>();
                    restarted.sessionManager.addListener(new XtreamSessionEventListener<>() {
                        @Override
                        public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                            if (reason == DefaultSessionCloseReason.EXPIRED) {
                                expired.complete(session);
                            }
                        }
                    });
                    assertEquals(first.id(), restarted.id());
                    assertNotSame(first, restarted);
                    restarted.lastCommunicateTime(Instant.EPOCH);
                    assertSame(restarted, expired.get(15, TimeUnit.SECONDS));
                    assertEquals(0, restarted.sessionManager.countUdp());
                    stopUdpAndAwaitCleanup(server, bound.get());
                    assertTrue(probe.active().isEmpty());
                } finally {
                    first.sessionManager.shutdown();
                }
            } finally {
                stop(server, loops);
            }
        }
    }

    @Test
    void tcpPeerDisconnectReleasesEachOutboundReferenceOnce() throws Exception {
        final var loops = LoopResources.create("tcp-outbound-cancel-test", 1, true);
        final var address = new AtomicReference<@Nullable InetSocketAddress>();
        final var sessions = new DefaultXtreamSessionManager(false, null, new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator());
        final var written = Unpooled.directBuffer(1).writeByte(11).retain();
        final var inFlight = Unpooled.directBuffer(1).writeByte(12).retain();
        final var pending = Unpooled.directBuffer(1).writeByte(13).retain();
        final Sinks.One<Void> firstByteReceived = Sinks.one();
        final var handedToNetty = new CompletableFuture<ChannelPromise>();
        final var waitingInPublisher = new CompletableFuture<XtreamRequest>();
        final var terminated = new CompletableFuture<SignalType>();
        final SimpleXtreamRequestHandler handler = exchange -> {
            final var body = Flux.just(written)
                    .concatWith(firstByteReceived.asMono().thenReturn(inFlight))
                    .concatWith(Mono.just(pending).delayUntil(buffer -> Mono.<Void>never()
                            .doOnSubscribe(ignored -> waitingInPublisher.complete(exchange.request()))));
            return exchange.response().writeWith(body).doFinally(terminated::complete);
        };
        final var server = XtreamServers.tcp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .pipeline(pipeline -> pipeline.addFirst(new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object message, ChannelPromise promise) throws Exception {
                        super.write(ctx, message, promise);
                        if (message == inFlight) {
                            handedToNetty.complete(promise);
                        }
                    }

                    @Override
                    public void flush(ChannelHandlerContext ctx) throws Exception {
                        // 将第二个缓冲区留在通道写队列中，直到对端断开。
                        if (!handedToNetty.isDone()) {
                            super.flush(ctx);
                        }
                    }
                }))
                .dispatch(builder -> builder.sessionManager(sessions).addHandlerMapping(exchange -> Mono.just(handler))
                        .enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var client = new Socket()) {
                client.connect(address.get(), SOCKET_TIMEOUT_MILLIS);
                client.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                client.getOutputStream().write(1);
                assertEquals(11, client.getInputStream().read());
                assertEquals(Sinks.EmitResult.OK, firstByteReceived.tryEmitEmpty());
                final var request = waitingInPublisher.get(2, TimeUnit.SECONDS);
                final var write = handedToNetty.get(2, TimeUnit.SECONDS);
                assertFalse(write.isDone());
                assertEquals(1, written.refCnt());
                assertEquals(2, inFlight.refCnt());
                assertEquals(2, pending.refCnt());
                client.shutdownOutput();
                assertEquals(-1, client.getInputStream().read());
                assertEquals(SignalType.CANCEL, terminated.get(2, TimeUnit.SECONDS));
                request.underlyingChannel().eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
                assertEquals(1, written.refCnt());
                assertEquals(1, inFlight.refCnt());
                assertEquals(1, pending.refCnt());
                assertEquals(0, request.payload().refCnt());
            }
        } finally {
            try {
                stop(server, sessions, loops);
            } finally {
                for (final var buffer : List.of(written, inFlight, pending)) {
                    final int references = buffer.refCnt();
                    if (references > 0) {
                        buffer.release(references);
                    }
                }
            }
        }
    }

    @Test
    void tcpFlushesFirstGroupBeforeLaterGroupCompletesAndReleasesOnDisconnect() throws Exception {
        final var loops = LoopResources.create("tcp-grouped-flush-test", 1, true);
        final var address = new AtomicReference<@Nullable InetSocketAddress>();
        final var written = Unpooled.directBuffer(1).writeByte(21).retain();
        final var pending = Unpooled.directBuffer(1).writeByte(22).retain();
        final Sinks.One<Void> firstGroupReceived = Sinks.one();
        final var waitingInSecondGroup = new CompletableFuture<XtreamRequest>();
        final var terminated = new CompletableFuture<SignalType>();
        final SimpleXtreamRequestHandler handler = exchange -> {
            final var secondGroup = firstGroupReceived.asMono().then(Mono.just(pending).delayUntil(buffer -> Mono.<Void>never()
                    .doOnSubscribe(ignored -> waitingInSecondGroup.complete(exchange.request()))));
            return exchange.response().writeAndFlushWith(Flux.just(Mono.just(written), secondGroup)).doFinally(terminated::complete);
        };
        final var server = XtreamServers.tcp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .dispatch(builder -> builder.addHandlerMapping(exchange -> Mono.just(handler)).enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var client = new Socket()) {
                client.connect(address.get(), SOCKET_TIMEOUT_MILLIS);
                client.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                client.getOutputStream().write(1);
                assertEquals(21, client.getInputStream().read());
                assertFalse(terminated.isDone());

                assertEquals(Sinks.EmitResult.OK, firstGroupReceived.tryEmitEmpty());
                final var request = waitingInSecondGroup.get(2, TimeUnit.SECONDS);
                assertEquals(2, pending.refCnt());
                client.shutdownOutput();
                assertEquals(-1, client.getInputStream().read());
                assertEquals(SignalType.CANCEL, terminated.get(2, TimeUnit.SECONDS));
                request.underlyingChannel().eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
                assertEquals(1, written.refCnt());
                assertEquals(1, pending.refCnt());
            }
        } finally {
            try {
                stop(server, loops);
            } finally {
                for (final var buffer : List.of(written, pending)) {
                    final int references = buffer.refCnt();
                    if (references > 0) {
                        buffer.release(references);
                    }
                }
            }
        }
    }

    @Test
    void sendsCommandOnExistingTcpSessionAfterRequestCompletion() throws Exception {
        final var loops = LoopResources.create("tcp-command-test", 1, true);
        final var address = new AtomicReference<@Nullable InetSocketAddress>();
        final String sessionId = "terminal-1";
        final var sessions = new DefaultXtreamSessionManager(false, null, channel -> sessionId);
        final var sender = new DefaultXtreamCommandSender(ByteBufAllocator.DEFAULT, sessions, EntityCodec.DEFAULT);
        final var completedRequest = new CompletableFuture<XtreamRequest>();
        final SimpleXtreamRequestHandler handler = exchange -> Mono.<Void>empty()
                .doAfterTerminate(() -> completedRequest.complete(exchange.request()));
        final var server = XtreamServers.tcp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .dispatch(builder -> builder.sessionManager(sessions).addHandlerMapping(exchange -> Mono.just(handler))
                        .enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var client = new Socket()) {
                client.connect(address.get(), SOCKET_TIMEOUT_MILLIS);
                client.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                client.getOutputStream().write(1);
                final var request = completedRequest.get(2, TimeUnit.SECONDS);
                final var eventLoop = request.underlyingChannel().eventLoop();
                eventLoop.submit(() -> {}).get(2, TimeUnit.SECONDS);
                assertEquals(0, request.payload().refCnt());

                final var command = Unpooled.directBuffer(1).writeByte(31);
                try {
                    sender.sendByteBuf(sessionId, Mono.fromSupplier(command::retain)).block(Duration.ofSeconds(2));
                    assertEquals(31, client.getInputStream().read());
                    eventLoop.submit(() -> {}).get(2, TimeUnit.SECONDS);
                    assertEquals(1, command.refCnt());
                } finally {
                    if (command.refCnt() > 0) {
                        command.release();
                    }
                }
            }
        } finally {
            stop(server, sessions, loops);
        }
    }

    @Test
    void stoppingTcpClosesActiveConnectionsAndReleasesPendingRequestsBeforeRestart() throws Exception {
        final var loops = LoopResources.create("tcp-stop-test", 1, true);
        final var address = new AtomicReference<@Nullable InetSocketAddress>();
        final var sessions = new DefaultXtreamSessionManager(false, null, new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator());
        final var closed = new CompletableFuture<XtreamSession>();
        sessions.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                closed.complete(session);
            }
        });
        final var started = new CompletableFuture<XtreamRequest>();
        final var terminated = new CompletableFuture<SignalType>();
        final SimpleXtreamRequestHandler handler = exchange -> {
            final var payload = exchange.request().payload();
            final int value = payload.getUnsignedByte(payload.readerIndex());
            if (value == 1) {
                return Mono.<Void>never().doOnSubscribe(ignored -> started.complete(exchange.request())).doFinally(terminated::complete);
            }
            return exchange.response().writeWith(Mono.fromSupplier(() -> exchange.bufferFactory().buffer(1).writeByte(value)));
        };
        final var server = XtreamServers.tcp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .pipeline(pipeline -> pipeline.addFirst(new FixedLengthFrameDecoder(1)))
                .dispatch(builder -> builder.sessionManager(sessions).addHandlerMapping(exchange -> Mono.just(handler))
                        .enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var client = new Socket()) {
                final var firstAddress = address.get();
                client.connect(firstAddress, SOCKET_TIMEOUT_MILLIS);
                client.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                client.getOutputStream().write(1);
                final var request = started.get(2, TimeUnit.SECONDS);
                final var payload = request.payload();
                assertTrue(payload.refCnt() > 0);
                assertEquals(1, sessions.countTcp());

                server.stop();
                try (var probe = new Socket()) {
                    assertThrows(IOException.class, () -> probe.connect(firstAddress, SOCKET_TIMEOUT_MILLIS));
                }
                assertEquals(-1, client.getInputStream().read());
                assertEquals(SignalType.CANCEL, terminated.get(2, TimeUnit.SECONDS));
                assertNotNull(closed.get(2, TimeUnit.SECONDS));
                request.underlyingChannel().eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
                assertEquals(0, sessions.countTcp());
                assertEquals(0, payload.refCnt());

                server.start();
                try (var restarted = new Socket()) {
                    restarted.connect(address.get(), SOCKET_TIMEOUT_MILLIS);
                    restarted.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                    restarted.getOutputStream().write(42);
                    assertEquals(42, restarted.getInputStream().read());
                    assertEquals(1, sessions.countTcp());
                }
            }
        } finally {
            stop(server, sessions, loops);
        }
    }

    @Test
    void stoppingUdpCancelsAllInFlightRequestsAndAllowsRestart() throws Exception {
        final var loops = LoopResources.create("udp-stop-test", 1, true);
        final var bound = new AtomicReference<@Nullable Connection>();
        final var started = List.of(new CompletableFuture<XtreamRequest>(), new CompletableFuture<XtreamRequest>());
        final var terminated = List.of(new CompletableFuture<SignalType>(), new CompletableFuture<SignalType>());
        final Sinks.One<Void> release = Sinks.one();
        final SimpleXtreamRequestHandler handler = exchange -> {
            final var request = exchange.request();
            final var payload = request.payload();
            final int value = payload.getUnsignedByte(payload.readerIndex());
            if (value == 1 || value == 2) {
                return release.asMono().doOnSubscribe(ignored -> started.get(value - 1).complete(request))
                        .doFinally(terminated.get(value - 1)::complete);
            }
            return exchange.response().writeWith(Mono.fromSupplier(() -> exchange.bufferFactory().buffer(2)
                    .writeByte(value).writeByte(1)));
        };
        final var server = XtreamServers.udp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound::set))
                .dispatch(builder -> builder.addHandlerMapping(exchange -> Mono.just(handler)).enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try (var first = udpClient(); var second = udpClient()) {
            server.start();
            final var target = (InetSocketAddress) bound.get().channel().localAddress();
            send(first, target, 1);
            send(second, target, 2);
            final var firstRequest = started.getFirst().get(2, TimeUnit.SECONDS);
            final var secondRequest = started.getLast().get(2, TimeUnit.SECONDS);
            assertEquals(first.getLocalSocketAddress(), firstRequest.remoteAddress());
            assertEquals(second.getLocalSocketAddress(), secondRequest.remoteAddress());
            assertTrue(firstRequest.payload().refCnt() > 0);
            assertTrue(secondRequest.payload().refCnt() > 0);

            stopUdpAndAwaitCleanup(server, bound.get());
            assertEquals(SignalType.CANCEL, terminated.getFirst().get(2, TimeUnit.SECONDS));
            assertEquals(SignalType.CANCEL, terminated.getLast().get(2, TimeUnit.SECONDS));
            assertEquals(0, firstRequest.payload().refCnt());
            assertEquals(0, secondRequest.payload().refCnt());

            server.start();
            final var restarted = (InetSocketAddress) bound.get().channel().localAddress();
            send(first, restarted, 3);
            send(second, restarted, 4);
            assertReply(first, restarted, 3, 1);
            assertReply(second, restarted, 4, 1);
        } finally {
            release.tryEmitEmpty();
            stop(server, loops);
        }
    }

    @Test
    void routesUdpRepliesToEachSenderAndRecreatesOnlyTheExpiredSession() throws Exception {
        final var loops = LoopResources.create("udp-session-test", 1, true);
        final var address = new AtomicReference<@Nullable InetSocketAddress>();
        final var ids = new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator();
        final var props = new UdpSessionIdleStateCheckerProps().setMaxIdleTime(Duration.ofMinutes(1))
                .setCheckInterval(Duration.ofMillis(20)).setCheckBackoffTime(Duration.ofSeconds(1));
        final var shutdowns = new AtomicInteger();
        final var sessions = new DefaultXtreamSessionManager(true, props, ids) {
            @Override
            public void shutdown() {
                shutdowns.incrementAndGet();
                super.shutdown();
            }
        };
        final var expired = new CompletableFuture<XtreamSession>();
        sessions.addListener(new XtreamSessionEventListener<>() {
            @Override
            public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                if (reason == DefaultSessionCloseReason.EXPIRED) {
                    expired.complete(session);
                }
            }
        });
        final SimpleXtreamRequestHandler handler = exchange -> exchange.session().flatMap(session -> {
            final var count = (Integer) session.attributes().getOrDefault("count", 0) + 1;
            session.attributes().put("count", count);
            final var payload = exchange.request().payload();
            final var request = payload.getUnsignedByte(payload.readerIndex());
            return exchange.response().writeWith(Mono.fromSupplier(() -> exchange.bufferFactory().buffer(2)
                    .writeByte(request).writeByte(count)));
        });
        final var server = XtreamServers.udp().bind("127.0.0.1", 0)
                .customize(netty -> netty.runOn(loops, false).doOnBound(bound -> address.set((InetSocketAddress) bound.address())))
                .dispatch(builder -> builder.sessionManager(sessions).addHandlerMapping(exchange -> Mono.just(handler))
                        .enableBuiltinHandlers(EntityCodec.DEFAULT))
                .build();
        try {
            server.start();
            try (var first = udpClient(); var second = udpClient()) {
                final var target = address.get();
                send(first, target, 11);
                send(second, target, 21);
                assertReply(first, target, 11, 1);
                assertReply(second, target, 21, 1);
                send(second, target, 22);
                send(first, target, 12);
                assertReply(first, target, 12, 2);
                assertReply(second, target, 22, 2);

                final var firstId = ids.generateUdpSessionId((InetSocketAddress) first.getLocalSocketAddress());
                final var secondId = ids.generateUdpSessionId((InetSocketAddress) second.getLocalSocketAddress());
                final var firstSession = sessions.getSessionById(firstId).block(Duration.ofSeconds(2));
                final var secondSession = sessions.getSessionById(secondId).block(Duration.ofSeconds(2));
                assertNotNull(firstSession);
                assertNotNull(secondSession);
                assertEquals(2, sessions.countUdp());

                firstSession.lastCommunicateTime(Instant.EPOCH);
                assertSame(firstSession, expired.get(2, TimeUnit.SECONDS));
                assertNull(sessions.getSessionById(firstId).block(Duration.ofSeconds(2)));
                assertSame(secondSession, sessions.getSessionById(secondId).block(Duration.ofSeconds(2)));
                assertEquals(1, sessions.countUdp());

                send(second, target, 23);
                send(first, target, 13);
                assertReply(first, target, 13, 1);
                assertReply(second, target, 23, 3);
                final var renewed = sessions.getSessionById(firstId).block(Duration.ofSeconds(2));
                assertNotNull(renewed);
                assertNotSame(firstSession, renewed);
                assertSame(secondSession, sessions.getSessionById(secondId).block(Duration.ofSeconds(2)));
                assertEquals(2, sessions.countUdp());
            }
            server.stop();
            assertEquals(0, shutdowns.get());
        } finally {
            stop(server, sessions, loops);
        }
    }

    private static DatagramSocket udpClient() throws IOException {
        final var socket = new DatagramSocket(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 0));
        socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        return socket;
    }

    private static void send(DatagramSocket socket, InetSocketAddress target, int value) throws IOException {
        final byte[] payload = {(byte) value};
        socket.send(new DatagramPacket(payload, payload.length, target));
    }

    private static void assertReply(DatagramSocket socket, InetSocketAddress sender, int value, int count) throws IOException {
        final byte[] payload = new byte[3];
        final var packet = new DatagramPacket(payload, payload.length);
        socket.receive(packet);
        assertEquals(sender, packet.getSocketAddress());
        assertEquals(2, packet.getLength());
        assertEquals(value, payload[0] & 0xff);
        assertEquals(count, payload[1] & 0xff);
    }

    private static void stop(XtreamServer server, DefaultXtreamSessionManager sessions, LoopResources loops) {
        try {
            server.stop();
        } finally {
            sessions.shutdown();
            loops.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
        }
    }

    private static void stop(XtreamServer server, LoopResources loops) {
        try {
            server.stop();
        } finally {
            loops.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
        }
    }

    private static void stopUdpAndAwaitCleanup(XtreamServer server, Connection bound) throws Exception {
        final var eventLoop = bound.channel().eventLoop();
        server.stop();
        eventLoop.submit(() -> {}).get(2, TimeUnit.SECONDS);
    }

    private static final class SessionCheckerProbe implements AutoCloseable {
        private static final String KEY = SessionCheckerProbe.class.getName();
        private final ConcurrentLinkedQueue<Scheduler> checkers = new ConcurrentLinkedQueue<>();

        private SessionCheckerProbe() {
            assertTrue(Schedulers.addExecutorServiceDecorator(KEY, (owner, executor) -> {
                if (Scannable.from(owner).name().equals("single(\"session-checker\")")) {
                    this.checkers.add(owner);
                }
                return executor;
            }));
        }

        private List<Scheduler> active() {
            return this.checkers.stream().filter(checker -> !checker.isDisposed()).toList();
        }

        @Override
        public void close() {
            Schedulers.removeExecutorServiceDecorator(KEY);
            this.active().forEach(Scheduler::dispose);
        }
    }
}

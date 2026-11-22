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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0200;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0201;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8201;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.processor.BuiltinJt808ServerDefaultEnvironmentPostProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808FlowIdGenerator;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestFilter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionEventListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808FlowIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener.DefaultSessionCloseReason;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener.SessionCloseReason;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventSubscriberInfo;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin.DefaultXtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.TcpXtreamNettyResourceFactory;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.event.ContextClosedEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.BEAN_NAME_JT_808_TCP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER;
import static io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent.XtreamEventType.AFTER_REQUEST_RECEIVED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class Jt808ServerContextLifecycleTest {
    private static final String TERMINAL_ID = "00000000013912344329";
    private static final String HEARTBEAT_FRAME = "7e0002400001000000000139123443290000377e";

    @Test
    void correlatesPositionQueryRepliesReceivedInReverseOrder() {
        final var bound = new CompletableFuture<DisposableServer>();
        serverContextRunner()
                .withBean(TcpNettyServerCustomizer.class, () -> server -> server.doOnBound(bound::complete))
                .withBean(Jt808FlowIdGenerator.class, () -> new DefaultJt808FlowIdGenerator(100))
                .withBean(PositionQueryHandler.class)
                .run(context -> {
                    final var commands = context.getBean(Jt808CommandSender.class);
                    final var encoder = context.getBean(Jt808ResponseEncoder.class);
                    final var bytesProcessor = context.getBean(Jt808BytesProcessor.class);
                    final var handler = context.getBean(PositionQueryHandler.class);
                    try (var client = new Socket()) {
                        client.connect(bound.get(2, TimeUnit.SECONDS).address(), 2000);
                        client.setSoTimeout(5000);
                        client.getOutputStream().write(HexFormat.of().parseHex(HEARTBEAT_FRAME));
                        handler.heartbeatReceived.get(5, TimeUnit.SECONDS);

                        final var first = commands.<BuiltinMessage0201>sendObjectAndWaitingResponseWithTerminalId(
                                TERMINAL_ID, Mono.just(new BuiltinMessage8201()), 0x0201).toFuture();
                        try {
                            final int firstFlowId = readPositionQueryFlowId(client.getInputStream(), bytesProcessor);
                            final var second = commands.<BuiltinMessage0201>sendObjectAndWaitingResponseWithTerminalId(
                                    TERMINAL_ID, Mono.just(new BuiltinMessage8201()), 0x0201).toFuture();
                            try {
                                final int secondFlowId = readPositionQueryFlowId(client.getInputStream(), bytesProcessor);
                                assertNotEquals(firstFlowId, secondFlowId);
                                assertFalse(first.isDone());
                                assertFalse(second.isDone());

                                sendPositionReply(client, encoder, 201, secondFlowId, 31_000_002);
                                final var secondReply = second.get(5, TimeUnit.SECONDS);
                                assertEquals(secondFlowId, secondReply.getFlowId());
                                assertEquals(31_000_002, secondReply.getLocationMessage().getLatitude());
                                assertFalse(first.isDone());

                                sendPositionReply(client, encoder, 202, firstFlowId, 31_000_001);
                                final var firstReply = first.get(5, TimeUnit.SECONDS);
                                assertEquals(firstFlowId, firstReply.getFlowId());
                                assertEquals(31_000_001, firstReply.getLocationMessage().getLatitude());
                            } finally {
                                second.cancel(false);
                            }
                        } finally {
                            first.cancel(false);
                        }
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void readIdleClosesInstructionTcpConnection(boolean sendHeartbeat) {
        final var bound = new CompletableFuture<DisposableServer>();
        final var accepted = new CompletableFuture<Connection>();

        serverContextRunner()
                .withPropertyValues(
                        "jt808-server.instruction-server.tcp-server.session-idle-state-checker.reader-idle-time=2s",
                        "jt808-server.instruction-server.tcp-server.session-idle-state-checker.all-idle-time=0s"
                )
                .withBean(TcpNettyServerCustomizer.class, () -> server -> server.doOnBound(bound::complete).doOnConnection(accepted::complete))
                .withBean(Jt808RequestFilter.class, () -> (exchange, chain) -> Mono.empty())
                .run(context -> {
                    final var sessions = context.getBean(Jt808SessionManager.class);
                    final var created = new CompletableFuture<Jt808Session>();
                    final var closeReasons = new CopyOnWriteArrayList<SessionCloseReason>();
                    sessions.addListener(new Jt808SessionEventListener() {
                        @Override
                        public void afterSessionCreate(Jt808Session session) {
                            created.complete(session);
                        }

                        @Override
                        public void beforeSessionClose(Jt808Session session, SessionCloseReason reason) {
                            closeReasons.add(reason);
                        }
                    });
                    try (var client = new Socket()) {
                        client.connect(bound.get(2, TimeUnit.SECONDS).address(), 2000);
                        client.setSoTimeout(5000);
                        final var channel = accepted.get(2, TimeUnit.SECONDS).channel();
                        final var eventLoop = channel.eventLoop();
                        if (sendHeartbeat) {
                            client.getOutputStream().write(HexFormat.of().parseHex(HEARTBEAT_FRAME));
                            final var session = created.get(2, TimeUnit.SECONDS);
                            assertSame(session, sessions.getSessionById(session.id()).block(Duration.ofSeconds(2)));
                        }

                        assertEquals(-1, client.getInputStream().read());
                        channel.closeFuture().get(2, TimeUnit.SECONDS);
                        eventLoop.submit(() -> {}).get(2, TimeUnit.SECONDS);
                        assertEquals(0, sessions.countTcp());
                        assertEquals(0, sessions.count());
                        assertEquals(sendHeartbeat, created.isDone());
                        assertEquals(sendHeartbeat ? List.of(DefaultSessionCloseReason.EXPIRED) : List.of(), closeReasons);
                    }
                });
    }

    @Test
    void closingContextReleasesInstructionTcpConnectionPortAndEventLoops() {
        final var bound = new CompletableFuture<DisposableServer>();
        final var accepted = new CompletableFuture<Connection>();

        serverContextRunner()
                .withBean(TcpNettyServerCustomizer.class, () -> server -> server.doOnBound(bound::complete).doOnConnection(accepted::complete))
                .run(context -> {
                    final var server = context.getBean(TcpXtreamServer.class);
                    final var loops = context.getBean(TcpXtreamNettyResourceFactory.class).loopResources();
                    try (var client = new Socket()) {
                        final var listener = bound.get(2, TimeUnit.SECONDS);
                        client.connect(listener.address(), 2000);
                        client.setSoTimeout(2000);
                        final var connection = accepted.get(2, TimeUnit.SECONDS);
                        final var selector = listener.channel().eventLoop();
                        final var worker = connection.channel().eventLoop();

                        context.close();

                        assertAll(
                                () -> assertEquals(-1, client.getInputStream().read()),
                                () -> {
                                    try (var rebound = new ServerSocket()) {
                                        rebound.setReuseAddress(true);
                                        rebound.bind(listener.address());
                                    }
                                },
                                () -> {
                                    assertTrue(loops.isDisposed());
                                    assertTrue(selector.terminationFuture().await(5, TimeUnit.SECONDS));
                                    assertTrue(worker.terminationFuture().await(5, TimeUnit.SECONDS));
                                }
                        );
                    } finally {
                        try {
                            server.stop();
                        } finally {
                            if (!loops.isDisposed()) {
                                loops.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
                            }
                        }
                    }
                });
    }

    @Test
    void usesNamedCustomTcpResourceFactoryAndLeavesItsEventLoopsRunning() {
        final var loops = LoopResources.create("jt808-custom-tcp-test", 1, 1, true);
        final TcpXtreamNettyResourceFactory factory = new CustomTcpResourceFactory(loops);
        final var bound = new CompletableFuture<DisposableServer>();
        try {
            serverContextRunner()
                    .withBean(BEAN_NAME_JT_808_TCP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER,
                            TcpXtreamNettyResourceFactory.class, () -> factory)
                    .withBean(TcpNettyServerCustomizer.class, () -> server -> server.doOnBound(bound::complete))
                    .run(context -> {
                        assertSame(factory, context.getBean(TcpXtreamNettyResourceFactory.class));
                        final var listener = bound.get(2, TimeUnit.SECONDS);
                        final var selector = listener.channel().eventLoop();
                        assertSame(loops.onServerSelect(false), selector.parent());

                        factory.dispose();
                        assertFalse(loops.isDisposed());
                        context.close();

                        assertFalse(listener.channel().isOpen());
                        assertFalse(loops.isDisposed());
                        selector.submit(() -> {}).get(2, TimeUnit.SECONDS);
                    });
        } finally {
            loops.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
        }
    }

    private record CustomTcpResourceFactory(LoopResources loopResources) implements TcpXtreamNettyResourceFactory {
        @Override
        public boolean preferNative() {
            return false;
        }
    }

    @Test
    void closingContextDisposesOwnedSchedulersAndKeepsSharedSchedulerUsable() {
        final var shared = Schedulers.newSingle("jt808-shared-scheduler-test", true);
        try {
            serverContextRunner()
                    .withPropertyValues(
                            "jt808-server.instruction-server.tcp-server.enabled=false",
                            "jt808-server.schedulers.non-blocking-handler.parallel.parallelism=1"
                    )
                    .withBean(XtreamSchedulerRegistryCustomizer.class,
                            () -> registry -> registry.registerScheduler("shared-test", shared))
                    .run(context -> {
                        final var registry = context.getBean(XtreamSchedulerRegistry.class);
                        final var parallel = registry.defaultNonBlockingScheduler();
                        final var elastic = registry.defaultBlockingScheduler();
                        try {
                            final var parallelThread = schedulerThread(parallel);
                            final var elasticThread = schedulerThread(elastic);
                            final var sharedThread = schedulerThread(shared);
                            context.close();
                            assertAll(
                                    () -> assertSchedulerTerminated(parallel, parallelThread),
                                    () -> assertSchedulerTerminated(elastic, elasticThread),
                                    () -> assertEquals(sharedThread, schedulerThread(shared))
                            );
                        } finally {
                            parallel.dispose();
                            elastic.dispose();
                        }
                    });
        } finally {
            shared.dispose();
        }
    }

    @Test
    void closingContextDrainsEventsAndCompletesSubscriptionsBeforeDisposingScheduler() {
        serverContextRunner()
                .withPropertyValues("jt808-server.instruction-server.tcp-server.enabled=false")
                .run(context -> {
                    final var publisher = context.getBean(XtreamEventPublisher.class);
                    final var scheduler = context.getBean(XtreamSchedulerRegistry.class).eventPublisherScheduler();
                    final var publishing = new CompletableFuture<Void>();
                    final var releasePublisher = new CompletableFuture<Void>();
                    final var consuming = new CompletableFuture<Thread>();
                    final var releaseSubscriber = new CompletableFuture<Void>();
                    final var closing = new CompletableFuture<Void>();
                    context.addApplicationListener(event -> {
                        if (event instanceof ContextClosedEvent) {
                            closing.complete(null);
                        }
                    });
                    final var events = publisher.subscribe(XtreamEventSubscriberInfo.of(Set.of(AFTER_REQUEST_RECEIVED), Map.of()))
                            .map(XtreamEvent::payload)
                            .doOnNext(payload -> {
                                if ("first".equals(payload)) {
                                    consuming.complete(Thread.currentThread());
                                    releaseSubscriber.orTimeout(5, TimeUnit.SECONDS).join();
                                }
                            })
                            .collectList().toFuture();
                    final var closed = new CompletableFuture<Void>();
                    try {
                        publisher.publish(new XtreamEvent() {
                            @Override
                            public XtreamEventType type() {
                                publishing.complete(null);
                                releasePublisher.orTimeout(5, TimeUnit.SECONDS).join();
                                return AFTER_REQUEST_RECEIVED;
                            }

                            @Override
                            public Object payload() {
                                return "first";
                            }
                        });
                        publishing.get(2, TimeUnit.SECONDS);
                        publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "second"));
                        Thread.ofVirtual().start(() -> {
                            try {
                                context.close();
                                closed.complete(null);
                            } catch (Throwable failure) {
                                closed.completeExceptionally(failure);
                            }
                        });
                        closing.get(2, TimeUnit.SECONDS);
                        assertThrows(TimeoutException.class, () -> closed.get(100, TimeUnit.MILLISECONDS));
                        assertFalse(scheduler.isDisposed());
                        releasePublisher.complete(null);
                        final var subscriberThread = consuming.get(2, TimeUnit.SECONDS);
                        assertThrows(TimeoutException.class, () -> closed.get(100, TimeUnit.MILLISECONDS));
                        assertFalse(scheduler.isDisposed());
                        releaseSubscriber.complete(null);
                        assertEquals(List.of("first", "second"), events.get(2, TimeUnit.SECONDS));
                        closed.get(2, TimeUnit.SECONDS);
                        assertSchedulerTerminated(scheduler, subscriberThread);
                    } finally {
                        releasePublisher.complete(null);
                        releaseSubscriber.complete(null);
                        events.cancel(false);
                    }
                });
    }

    private static int readPositionQueryFlowId(InputStream input, Jt808BytesProcessor bytesProcessor) throws IOException {
        assertEquals(0x7e, input.read());
        final var escaped = Unpooled.buffer(36);
        try {
            int value;
            while ((value = input.read()) != 0x7e) {
                assertTrue(value >= 0 && escaped.readableBytes() < 36, "无效的位置查询指令帧");
                escaped.writeByte(value);
            }
            final var frame = bytesProcessor.doEscapeForReceive(escaped);
            try {
                assertEquals(18, frame.readableBytes());
                assertEquals(0, bytesProcessor.calculateCheckSum(frame));
                assertEquals(0x8201, frame.readUnsignedShort());
                assertEquals(0x4000, frame.readUnsignedShort());
                assertEquals(1, frame.readUnsignedByte());
                assertEquals(TERMINAL_ID, ByteBufUtil.hexDump(frame, frame.readerIndex(), 10));
                return frame.skipBytes(10).readUnsignedShort();
            } finally {
                frame.release();
            }
        } finally {
            escaped.release();
        }
    }

    private static void sendPositionReply(Socket client, Jt808ResponseEncoder encoder, int frameFlowId, int queryFlowId, int latitude) throws IOException {
        final var body = new BuiltinMessage0201().setFlowId(queryFlowId).setLocationMessage(
                new BuiltinMessage0200().setLatitude(latitude).setTime(LocalDateTime.of(2026, 1, 1, 0, 0)));
        final var frame = encoder.encode(body, Jt808ProtocolVersion.VERSION_2019, TERMINAL_ID, frameFlowId,
                BuiltinMessage0201.class.getAnnotation(Jt808ResponseBody.class));
        try {
            client.getOutputStream().write(ByteBufUtil.getBytes(frame));
        } finally {
            frame.release();
        }
    }

    @Jt808RequestHandler
    public static class PositionQueryHandler {
        private final CompletableFuture<Void> heartbeatReceived = new CompletableFuture<>();
        private final Jt808CommandSender commands;

        public PositionQueryHandler(Jt808CommandSender commands) {
            this.commands = commands;
        }

        @Jt808RequestHandlerMapping(messageIds = 0x0002, versions = Jt808ProtocolVersion.VERSION_2019)
        public Mono<Void> heartbeat() {
            this.heartbeatReceived.complete(null);
            return Mono.empty();
        }

        @Jt808RequestHandlerMapping(messageIds = 0x0201, versions = Jt808ProtocolVersion.VERSION_2019)
        public Mono<Void> reply(Jt808Request request, @Jt808RequestBody BuiltinMessage0201 body) {
            final var key = Jt808CommandSender.Jt808CommandKey.of(request.header().terminalId(), request.header().messageId(), body.getFlowId());
            this.commands.setClientResponse(key, body);
            return Mono.empty();
        }
    }

    private static Thread schedulerThread(Scheduler scheduler) throws Exception {
        final var thread = new CompletableFuture<Thread>();
        scheduler.schedule(() -> thread.complete(Thread.currentThread()));
        return thread.get(2, TimeUnit.SECONDS);
    }

    private static void assertSchedulerTerminated(Scheduler scheduler, Thread thread) throws InterruptedException {
        assertTrue(scheduler.isDisposed(), thread::getName);
        assertTrue(thread.join(Duration.ofSeconds(2)), thread::getName);
    }

    private static ApplicationContextRunner serverContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(XtreamExtJt808ServerAutoConfiguration.class))
                .withInitializer(context -> new BuiltinJt808ServerDefaultEnvironmentPostProcessor()
                        .postProcessEnvironment(context.getEnvironment(), new SpringApplication()))
                .withPropertyValues(
                        "jt808-server.instruction-server.tcp-server.host=127.0.0.1",
                        "jt808-server.instruction-server.tcp-server.port=0",
                        "jt808-server.instruction-server.tcp-server.loop-resources.select-count=1",
                        "jt808-server.instruction-server.tcp-server.loop-resources.worker-count=1",
                        "jt808-server.instruction-server.tcp-server.loop-resources.prefer-native=false",
                        "jt808-server.instruction-server.udp-server.enabled=false",
                        "jt808-server.attachment-server.tcp-server.enabled=false",
                        "jt808-server.attachment-server.udp-server.enabled=false"
                );
    }
}

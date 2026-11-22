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

package io.github.hylexus.xtream.codec.ext.jt1078.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt1078.processor.BuiltinJt1078ServerDefaultEnvironmentPostProcessor;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.resources.Jt1078XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.TcpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.UdpXtreamNettyResourceFactory;
import io.netty.channel.EventLoopGroup;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.scheduler.Scheduler;
import reactor.netty.ChannelBindException;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Jt1078ServerContextLifecycleTest {

    @Test
    void closingContextReleasesTcpConnectionBothPortsAndEventLoops() {
        final var tcpBound = new CompletableFuture<DisposableServer>();
        final var tcpAccepted = new CompletableFuture<Connection>();
        final var udpBound = new CompletableFuture<Connection>();
        serverContextRunner()
                .withBean(TcpNettyServerCustomizer.class, () -> server -> server.doOnBound(tcpBound::complete).doOnConnection(tcpAccepted::complete))
                .withBean(UdpNettyServerCustomizer.class, () -> server -> server.doOnBound(udpBound::complete))
                .run(context -> {
                    final var tcpServer = context.getBean(TcpXtreamServer.class);
                    final var udpServer = context.getBean(UdpXtreamServer.class);
                    final var tcpLoops = context.getBean(TcpXtreamNettyResourceFactory.class).loopResources();
                    final var udpLoops = context.getBean(UdpXtreamNettyResourceFactory.class).loopResources();
                    try (var client = new Socket()) {
                        final var tcp = tcpBound.get(2, TimeUnit.SECONDS);
                        final var udp = udpBound.get(2, TimeUnit.SECONDS);
                        final var tcpAddress = tcp.address();
                        final var udpAddress = udp.channel().localAddress();
                        final var tcpSelector = tcp.channel().eventLoop();
                        final var udpWorker = udp.channel().eventLoop();
                        client.connect(tcpAddress, 2000);
                        client.setSoTimeout(2000);
                        final var tcpWorker = tcpAccepted.get(2, TimeUnit.SECONDS).channel().eventLoop();

                        context.close();

                        assertAll(
                                () -> assertEquals(-1, client.getInputStream().read()),
                                () -> assertTcpPortCanRebind(tcpAddress),
                                () -> {
                                    try (var rebound = new DatagramSocket(null)) {
                                        rebound.bind(udpAddress);
                                    }
                                },
                                () -> assertEventLoopsTerminated(tcpLoops, tcpSelector, tcpWorker),
                                () -> assertEventLoopsTerminated(udpLoops, udpWorker)
                        );
                    } finally {
                        try {
                            stop(tcpServer, tcpLoops);
                        } finally {
                            stop(udpServer, udpLoops);
                        }
                    }
                });
    }

    @Test
    void closingContextDisposesAudioVideoSchedulersAndTerminatesWorkers() {
        serverContextRunner().run(context -> {
            final var registry = context.getBean(Jt1078XtreamSchedulerRegistry.class);
            final var codec = registry.audioVideoCodecScheduler();
            final var subscriber = registry.audioVideoSubscriberScheduler();
            try {
                final var codecWorker = schedulerThread(codec);
                final var subscriberWorker = schedulerThread(subscriber);

                context.close();

                assertAll(
                        () -> assertSchedulerTerminated(codec, codecWorker),
                        () -> assertSchedulerTerminated(subscriber, subscriberWorker)
                );
            } finally {
                codec.dispose();
                subscriber.dispose();
            }
        });
    }

    @Test
    void udpBindFailureRollsBackTcpListenerAndEventLoops() throws IOException {
        final var tcpBound = new CompletableFuture<TcpBinding>();
        final var tcpLoops = new AtomicReference<LoopResources>();
        final var udpLoops = new AtomicReference<LoopResources>();
        final var udpWorkers = new CompletableFuture<EventLoopGroup>();
        final var loopback = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        try (var occupied = new DatagramSocket(new InetSocketAddress(loopback, 0))) {
            final int udpPort = occupied.getLocalPort();
            serverContextRunner()
                    .withPropertyValues("jt1078-server.udp-server.port=" + udpPort)
                    .withBean(TcpNettyServerCustomizer.class, () -> server -> server
                            .doOnBind(config -> tcpLoops.set(config.loopResources()))
                            .doOnBound(bound -> tcpBound.complete(new TcpBinding(
                                    bound, bound.address(), bound.channel().eventLoop().parent(), tcpLoops.get().onServer(false)))))
                    .withBean(UdpNettyServerCustomizer.class, () -> server -> server
                            .doOnBind(config -> udpLoops.set(config.loopResources()))
                            .doOnChannelInit((observer, channel, address) -> udpWorkers.complete(channel.eventLoop().parent())))
                    .run(context -> {
                        final var failure = assertInstanceOf(IllegalStateException.class, context.getStartupFailure());
                        final var bindFailure = assertInstanceOf(ChannelBindException.class, failure.getCause());
                        assertEquals(udpPort, bindFailure.localPort());
                        final var tcp = tcpBound.get(2, TimeUnit.SECONDS);
                        assertAll(
                                () -> assertFalse(tcp.server().channel().isOpen()),
                                () -> assertTcpPortCanRebind(tcp.address()),
                                () -> assertEventLoopsTerminated(tcpLoops.get(), tcp.selector(), tcp.workers()),
                                () -> assertEventLoopsTerminated(udpLoops.get(), udpWorkers.get(2, TimeUnit.SECONDS))
                        );
                    });
        } finally {
            assertAll(
                    () -> {
                        final var tcp = tcpBound.getNow(null);
                        if (tcp != null && !tcp.server().isDisposed()) {
                            tcp.server().disposeNow(Duration.ofSeconds(3));
                        }
                    },
                    () -> dispose(tcpLoops.get()),
                    () -> dispose(udpLoops.get())
            );
        }
    }

    private static ApplicationContextRunner serverContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(XtreamExtJt1078ServerAutoConfiguration.class))
                .withInitializer(context -> new BuiltinJt1078ServerDefaultEnvironmentPostProcessor()
                        .postProcessEnvironment(context.getEnvironment(), new SpringApplication()))
                .withPropertyValues(
                        "jt1078-server.tcp-server.enabled=true",
                        "jt1078-server.tcp-server.host=127.0.0.1",
                        "jt1078-server.tcp-server.port=0",
                        "jt1078-server.tcp-server.loop-resources.select-count=1",
                        "jt1078-server.tcp-server.loop-resources.worker-count=1",
                        "jt1078-server.tcp-server.loop-resources.prefer-native=false",
                        "jt1078-server.udp-server.enabled=true",
                        "jt1078-server.udp-server.host=127.0.0.1",
                        "jt1078-server.udp-server.port=0",
                        "jt1078-server.udp-server.loop-resources.worker-count=1",
                        "jt1078-server.udp-server.loop-resources.prefer-native=false"
                );
    }

    private static void assertTcpPortCanRebind(SocketAddress address) throws IOException {
        try (var rebound = new ServerSocket()) {
            rebound.setReuseAddress(true);
            rebound.bind(address);
        }
    }

    private static void assertEventLoopsTerminated(LoopResources resources, EventLoopGroup... eventLoops) throws InterruptedException {
        assertTrue(resources.isDisposed());
        for (final var eventLoop : eventLoops) {
            assertTrue(eventLoop.terminationFuture().await(5, TimeUnit.SECONDS));
        }
    }

    private static Thread schedulerThread(Scheduler scheduler) throws Exception {
        final var thread = new CompletableFuture<Thread>();
        scheduler.schedule(() -> thread.complete(Thread.currentThread()));
        return thread.get(2, TimeUnit.SECONDS);
    }

    private static void assertSchedulerTerminated(Scheduler scheduler, Thread worker) throws InterruptedException {
        assertTrue(scheduler.isDisposed(), worker::getName);
        assertTrue(worker.join(Duration.ofSeconds(2)), worker::getName);
    }

    private static void stop(XtreamServer server, LoopResources resources) {
        try {
            server.stop();
        } finally {
            dispose(resources);
        }
    }

    private static void dispose(@Nullable LoopResources resources) {
        if (resources != null && !resources.isDisposed()) {
            resources.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
        }
    }

    private record TcpBinding(DisposableServer server, SocketAddress address, EventLoopGroup selector, EventLoopGroup workers) {
    }
}

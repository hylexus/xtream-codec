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


package io.github.hylexus.xtream.codec.server.reactive.spec.handler.scan;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServers;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionEventListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.RequestHandlerNotFoundException;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.AbstractSimpleXtreamRequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamSessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.EmptyXtreamHandlerResultHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.LoopResources;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class AnnotationServerRoundTripTest {
    @Test
    void scansAndRoutesRequestsAndWritesEncodedRepliesOnOneTcpConnection() throws Exception {
        try (var fixture = new TcpFixture(); var socket = fixture.connect()) {
            final var out = socket.getOutputStream();
            final var in = socket.getInputStream();

            out.write(new byte[]{1, 40, 2, 10, 3, 11, 9, 12, 1, 41});
            out.flush();

            assertArrayEquals(new byte[]{41, 42}, in.readNBytes(2));
            assertEquals(List.of(10, 11), fixture.handler.observed);
            assertInstanceOf(RequestHandlerNotFoundException.class, fixture.routingError.get(2, TimeUnit.SECONDS));
            assertEquals(1, fixture.sessions.countTcp());
        }
    }

    @Test
    void closesTheRegisteredTcpSessionWhenThePeerDisconnects() throws Exception {
        try (var fixture = new TcpFixture()) {
            final int response;
            try (var socket = fixture.connect()) {
                socket.getOutputStream().write(new byte[]{1, 40});
                response = socket.getInputStream().read();
                assertEquals(1, fixture.sessions.countTcp());
            }
            assertEquals(41, response);
            assertEquals(XtreamSessionEventListener.DefaultSessionCloseReason.CLOSED_BY_CLIENT,
                    fixture.closed.get(2, TimeUnit.SECONDS));
            assertEquals(0, fixture.sessions.countTcp());
            assertEquals(0, fixture.sessions.count());
        }
    }

    @Test
    void disconnectCancelsQueuedHandlerInvocationAndReleasesInboundBuffer() throws Exception {
        final var executor = new ScheduledThreadPoolExecutor(1);
        final var scheduler = Schedulers.fromExecutorService(executor);
        final var running = new CountDownLatch(1);
        final var release = new CountDownLatch(1);
        try (var fixture = new TcpFixture(scheduler)) {
            final var blocker = executor.submit(() -> {
                running.countDown();
                return release.await(10, TimeUnit.SECONDS);
            });
            assertTrue(running.await(2, TimeUnit.SECONDS));

            final XtreamRequest request;
            try (var socket = fixture.connect()) {
                socket.getOutputStream().write(new byte[]{2, 40});
                request = fixture.dispatched.get(2, TimeUnit.SECONDS);
                request.underlyingChannel().eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
                assertEquals(1, executor.getQueue().size());
                assertTrue(request.payload().refCnt() > 0);
            }

            final var channel = request.underlyingChannel();
            channel.closeFuture().get(2, TimeUnit.SECONDS);
            channel.eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
            assertEquals(0, request.payload().refCnt());

            release.countDown();
            assertTrue(blocker.get(2, TimeUnit.SECONDS));
            executor.submit(() -> {}).get(2, TimeUnit.SECONDS);
            assertEquals(0, fixture.handler.monoInvocations.get());
        } finally {
            release.countDown();
            scheduler.dispose();
        }
    }

    @Test
    void rejectedHandlerInvocationReleasesInboundBufferAndNextRequestRecovers() throws Exception {
        final var executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        final var scheduler = Schedulers.fromExecutorService(executor);
        final var running = new CountDownLatch(1);
        final var release = new CountDownLatch(1);
        try (var fixture = new TcpFixture(scheduler); var socket = fixture.connect()) {
            final var blocker = executor.submit(() -> {
                running.countDown();
                return release.await(10, TimeUnit.SECONDS);
            });
            assertTrue(running.await(2, TimeUnit.SECONDS));
            final var queued = executor.submit(() -> {});
            assertEquals(0, executor.getQueue().remainingCapacity());

            socket.getOutputStream().write(new byte[]{2, 40});
            final var request = fixture.dispatched.get(2, TimeUnit.SECONDS);
            assertInstanceOf(RejectedExecutionException.class, fixture.routingError.get(2, TimeUnit.SECONDS));
            request.underlyingChannel().eventLoop().submit(() -> {}).get(2, TimeUnit.SECONDS);
            assertEquals(0, request.payload().refCnt());

            release.countDown();
            assertTrue(blocker.get(2, TimeUnit.SECONDS));
            queued.get(2, TimeUnit.SECONDS);
            socket.getOutputStream().write(new byte[]{1, 41});
            assertEquals(42, socket.getInputStream().read());
            assertEquals(0, fixture.handler.monoInvocations.get());
        } finally {
            release.countDown();
            scheduler.dispose();
        }
    }

    private static final class TcpFixture implements AutoCloseable {
        private final Handler handler = new Handler();
        private final AtomicReference<InetSocketAddress> address = new AtomicReference<>();
        private final CompletableFuture<Throwable> routingError = new CompletableFuture<>();
        private final CompletableFuture<XtreamSessionEventListener.SessionCloseReason> closed = new CompletableFuture<>();
        private final CompletableFuture<XtreamRequest> dispatched = new CompletableFuture<>();
        private final DefaultXtreamSessionManager sessions =
                new DefaultXtreamSessionManager(false, null, new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator());
        private final LoopResources loops = LoopResources.create("annotation-test", 1, true);
        private final XtreamServer server;

        private TcpFixture() {
            this(Schedulers.immediate());
        }

        private TcpFixture(Scheduler nonBlockingScheduler) {
            final var mapping = new Mapping(this.handler, nonBlockingScheduler);
            this.sessions.addListener(new XtreamSessionEventListener<>() {
                @Override
                public void beforeSessionClose(XtreamSession session, SessionCloseReason reason) {
                    closed.complete(reason);
                }
            });
            this.server = XtreamServers.tcp().name("annotation-test").bind("127.0.0.1", 0)
                    .customize(tcp -> tcp.runOn(this.loops, false).doOnBound(bound -> this.address.set((InetSocketAddress) bound.address())))
                    .pipeline(pipeline -> pipeline.addFirst(new FixedLengthFrameDecoder(2)))
                    .dispatch(dispatch -> dispatch.sessionManager(this.sessions)
                            .addFilter((exchange, chain) -> {
                                this.dispatched.complete(exchange.request());
                                return chain.filter(exchange);
                            })
                            .addHandlerMapping(mapping).enableBuiltinHandlers(EntityCodec.DEFAULT)
                            .addHandlerResultHandler(new EmptyXtreamHandlerResultHandler())
                            .addExceptionHandler((exchange, error) -> {
                                this.routingError.complete(error);
                                return Mono.empty();
                            }))
                    .build();
            try {
                this.server.start();
            } catch (RuntimeException error) {
                close();
                throw error;
            }
        }

        private Socket connect() throws IOException {
            final var socket = new Socket();
            try {
                final var bound = this.address.get();
                assertNotNull(bound);
                socket.connect(bound, 2000);
                socket.setSoTimeout(2000);
                return socket;
            } catch (IOException error) {
                socket.close();
                throw error;
            }
        }

        @Override
        public void close() {
            try {
                this.server.stop();
            } finally {
                this.sessions.shutdown();
                this.loops.disposeLater(Duration.ZERO, Duration.ofSeconds(2)).block(Duration.ofSeconds(4));
            }
        }
    }

    private static final class Mapping extends AbstractSimpleXtreamRequestMappingHandlerMapping {
        private Mapping(Handler handler, Scheduler nonBlockingScheduler) {
            super(new DefaultXtreamSchedulerRegistry(nonBlockingScheduler, Schedulers.immediate(), Schedulers.immediate()),
                    new DefaultXtreamBlockingHandlerMethodPredicate(),
                    new String[]{Handler.class.getPackageName()},
                    type -> {
                        assertEquals(Handler.class, type);
                        return handler;
                    });
        }

        @Override
        public Mono<Object> getHandler(XtreamExchange exchange) {
            final int messageType = exchange.request().payload().getUnsignedByte(exchange.request().payload().readerIndex());
            return Flux.fromIterable(this.handlerMethods)
                    .filter(handler -> handler.getMethod().getAnnotation(Route.class).value() == messageType)
                    .next().cast(Object.class);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @XtreamRequestHandlerMapping
    public @interface Route {
        int value();
    }

    @XtreamRequestHandler
    public static final class Handler {
        private final List<Integer> observed = new CopyOnWriteArrayList<>();
        private final AtomicInteger monoInvocations = new AtomicInteger();

        @Route(1)
        @XtreamResponseBody
        public Reply reply(@XtreamRequestBody Request request) {
            return new Reply(request.value() + 1);
        }

        @Route(2)
        public Mono<Void> collectMono(@XtreamRequestBody Request request) {
            this.monoInvocations.incrementAndGet();
            return Mono.fromRunnable(() -> this.observed.add(request.value()));
        }

        @Route(3)
        public Flux<Void> collectFlux(@XtreamRequestBody Request request) {
            return Flux.defer(() -> {
                this.observed.add(request.value());
                return Flux.empty();
            });
        }

        public Reply ignored(@XtreamRequestBody Request request) {
            return new Reply(request.value());
        }
    }

    public record Request(@Preset.RustStyle.u8 int type, @Preset.RustStyle.u8 int value) {
    }

    public record Reply(@Preset.RustStyle.u8 int value) {
    }
}

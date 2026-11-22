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
import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchangeDecorator;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.ReactiveXtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.AbstractXtreamRequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DelegateXtreamHandlerMethodArgumentResolver;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class XtreamHandlerMethodTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private final XtreamHandlerMethodHandlerAdapter adapter = new XtreamHandlerMethodHandlerAdapter(
            DelegateXtreamHandlerMethodArgumentResolver.createDefault(EntityCodec.DEFAULT));

    @Test
    void decodesBodyWithoutConsumingTheRequestAndInvokesOnSelectedScheduler() throws Exception {
        final var buffer = Unpooled.buffer(2).writeByte(0).writeByte(42);
        buffer.skipBytes(1);
        final var exchange = ReactiveTestFixtures.exchange().mutate().request(builder -> builder.payload(buffer, false)).build();
        final var scheduler = Schedulers.newSingle("handler-selection-test");
        try {
            final Method method = Handlers.class.getMethod("decoded", XtreamExchange.class, XtreamRequest.class, XtreamResponse.class,
                    XtreamSession.class, Body.class);
            final var result = this.adapter.handle(exchange, handler(method, scheduler)).block(TIMEOUT);

            assertNotNull(result);
            final var invocation = assertInstanceOf(Invocation.class, result.getReturnValue());
            assertSame(exchange, invocation.exchange());
            assertSame(exchange.request(), invocation.request());
            assertSame(exchange.response(), invocation.response());
            assertSame(exchange.session().block(TIMEOUT), invocation.session());
            assertEquals(42, invocation.body().value());
            assertEquals(1, buffer.readerIndex());
            assertTrue(invocation.thread().startsWith("handler-selection-test"));
        } finally {
            scheduler.dispose();
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rawBodyUsesAViewOrOriginalBufferAsRequested(boolean slice) throws Exception {
        final var payload = Unpooled.buffer(2).writeByte(0).writeByte(42);
        payload.skipBytes(1);
        final var exchange = ReactiveTestFixtures.exchange().mutate().request(builder -> builder.payload(payload, false)).build();
        try {
            final Method method = Handlers.class.getMethod(slice ? "sliced" : "original", ByteBuf.class);
            final var result = this.adapter.handle(exchange, handler(method, Schedulers.immediate())).block(TIMEOUT);
            assertNotNull(result);
            final var body = assertInstanceOf(ByteBuf.class, result.getReturnValue());
            if (slice) {
                assertNotSame(payload, body);
                assertEquals(0, body.readerIndex());
            } else {
                assertSame(payload, body);
                assertEquals(1, body.readerIndex());
            }
            assertEquals(42, body.getUnsignedByte(body.readerIndex()));
            assertEquals(1, payload.refCnt());
        } finally {
            payload.release();
        }
    }

    @Test
    void emptyResolvedArgumentIsPassedAsNullAndUnsupportedArgumentIsReported() throws Exception {
        final var exchange = new XtreamExchangeDecorator(ReactiveTestFixtures.exchange(), null, null) {
            @Override
            public Mono<XtreamSession> session() {
                return Mono.empty();
            }
        };
        final Method optional = Handlers.class.getMethod("optional", XtreamSession.class);
        final var result = this.adapter.handle(exchange, handler(optional, Schedulers.immediate())).block(TIMEOUT);
        assertNotNull(result);
        assertEquals("无会话", result.getReturnValue());

        final Method unsupported = Handlers.class.getMethod("unsupported", InetSocketAddress.class);
        final var error = assertThrows(IllegalStateException.class,
                () -> this.adapter.handle(exchange, handler(unsupported, Schedulers.immediate())).block(TIMEOUT));
        assertTrue(error.getMessage().contains("Cannot resolve argument"));
    }

    @Test
    void defaultAndExplicitHandlerSchedulersFollowReturnTypeAndMapping() throws Exception {
        final var immediate = Schedulers.immediate();
        final var registry = new DefaultXtreamSchedulerRegistry(immediate, immediate, immediate);
        assertTrue(registry.registerScheduler("explicit", immediate));
        final var mapping = new TestMapping(registry);
        final Method sync = Handlers.class.getMethod("optional", XtreamSession.class);
        final Method reactive = Handlers.class.getMethod("reactive");

        final var blocking = mapping.method(sync, "");
        final var nonBlocking = mapping.method(reactive, "");
        assertFalse(blocking.isNonBlocking());
        assertEquals(XtreamSchedulerRegistry.SCHEDULER_NAME_BLOCKING, blocking.getSchedulerName());
        assertTrue(nonBlocking.isNonBlocking());
        assertEquals(XtreamSchedulerRegistry.SCHEDULER_NAME_NON_BLOCKING, nonBlocking.getSchedulerName());
        assertEquals("explicit", mapping.method(sync, "explicit").getSchedulerName());
        assertThrows(IllegalArgumentException.class, () -> mapping.method(sync, "unknown"));

        final var result = this.adapter.handle(ReactiveTestFixtures.exchange(), nonBlocking).block(TIMEOUT);
        assertNotNull(result);
        assertInstanceOf(Mono.class, result.getReturnValue());
    }

    @Test
    void dispatcherRunsTheDownstreamChainOnConfiguredScheduler() {
        final var scheduler = Schedulers.newSingle("dispatch-test");
        final var immediate = Schedulers.immediate();
        final var registry = new DefaultXtreamSchedulerRegistry(scheduler, immediate, immediate, immediate);
        final var filter = new RequestDispatcherSchedulerFilter(registry);
        final var exchange = ReactiveTestFixtures.exchange();
        final AtomicReference<String> thread = new AtomicReference<>();
        try {
            final var result = filter.filter(exchange, actual -> {
                assertSame(exchange, actual);
                thread.set(Thread.currentThread().getName());
                return Mono.empty();
            });

            assertNull(thread.get());
            result.block(TIMEOUT);
            assertNotNull(thread.get());
            assertTrue(thread.get().startsWith("dispatch-test"));
        } finally {
            scheduler.dispose();
        }
    }

    private static XtreamHandlerMethod handler(Method method, Scheduler scheduler) {
        final var config = XtreamSchedulerRegistry.SchedulerConfig.ofDefault("test");
        return new ReactiveXtreamHandlerMethod(Handlers.class, new Handlers(), method, method.getName(),
                ignored -> new XtreamHandlerMethod.HandlerMethodSchedulerInfo(true, scheduler, config));
    }

    private static final class TestMapping extends AbstractXtreamRequestMappingHandlerMapping {
        private TestMapping(XtreamSchedulerRegistry registry) {
            super(registry, new DefaultXtreamBlockingHandlerMethodPredicate());
        }

        private XtreamHandlerMethod method(Method method, String scheduler) {
            return new ReactiveXtreamHandlerMethod(Handlers.class, new Handlers(), method, method.getName(),
                    handler -> determineSchedulerInfo(handler, scheduler, XtreamSchedulerRegistry.SCHEDULER_NAME_BLOCKING,
                            XtreamSchedulerRegistry.SCHEDULER_NAME_NON_BLOCKING));
        }

        @Override
        public Mono<Object> getHandler(XtreamExchange exchange) {
            return Mono.empty();
        }
    }

    public static final class Handlers {
        public Invocation decoded(XtreamExchange exchange, XtreamRequest request, XtreamResponse response,
                                  XtreamSession session, @XtreamRequestBody Body body) {
            return new Invocation(exchange, request, response, session, body, Thread.currentThread().getName());
        }

        public ByteBuf sliced(@XtreamRequestBody ByteBuf body) {
            return body;
        }

        public ByteBuf original(@XtreamRequestBody(bufferAsSlice = false) ByteBuf body) {
            return body;
        }

        public String optional(@Nullable XtreamSession session) {
            return session == null ? "无会话" : session.id();
        }

        public Mono<Void> reactive() {
            return Mono.empty();
        }

        public void unsupported(InetSocketAddress address) {
            fail("参数无法解析时不应调用处理方法: " + address);
        }
    }

    public record Body(@Preset.RustStyle.u8 int value) {
    }

    public record Invocation(XtreamExchange exchange, XtreamRequest request, XtreamResponse response,
                             XtreamSession session, Body body, String thread) {
    }
}

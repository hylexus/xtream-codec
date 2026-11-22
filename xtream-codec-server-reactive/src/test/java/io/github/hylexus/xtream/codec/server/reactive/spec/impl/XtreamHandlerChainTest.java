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

import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamHandlerAdapterBuilder;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.hylexus.xtream.codec.server.reactive.spec.impl.ReactiveTestFixtures.exchange;
import static org.junit.jupiter.api.Assertions.*;

class XtreamHandlerChainTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Test
    void udpBuilderInvokesTheOverriddenHandlerFactoryWithDefaultSessions() {
        final var calls = new AtomicInteger();
        final var builder = new UdpXtreamHandlerAdapterBuilder(ByteBufAllocator.DEFAULT) {
            @Override
            protected XtreamHandler createRequestHandler() {
                calls.incrementAndGet();
                return super.createRequestHandler();
            }
        };
        builder.addHandlerMapping(ignored -> Mono.empty())
                .enableBuiltinHandlerAdapters(EntityCodec.DEFAULT)
                .build();

        assertEquals(1, calls.get());
    }

    @Test
    void filtersWrapEachSubscriptionAndCanShortCircuit() {
        final List<String> calls = new ArrayList<>();
        final var chain = new FilteringXtreamHandler(ignored -> fail("不应进入业务处理器"), List.of(
                (exchange, next) -> {
                    calls.add("进入外层");
                    return next.filter(exchange).then(Mono.fromRunnable(() -> calls.add("退出外层")));
                },
                (exchange, next) -> {
                    calls.add("短路");
                    return Mono.empty();
                },
                (exchange, next) -> fail("不应继续过滤")));

        final Mono<Void> invocation = chain.handle(exchange());
        assertTrue(calls.isEmpty());
        invocation.block(TIMEOUT);
        invocation.block(TIMEOUT);
        assertEquals(List.of("进入外层", "短路", "退出外层", "进入外层", "短路", "退出外层"), calls);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void recoversSynchronousAndAsynchronousErrorsUsingHandlerOrder(boolean synchronous) {
        final var failure = new IllegalArgumentException("原始异常");
        final var wrapped = new XtreamWrappedRuntimeException(failure);
        final var exchange = exchange();
        final List<String> calls = new ArrayList<>();
        final var handler = new ExceptionHandlingXtreamHandler(ignored -> {
            if (synchronous) {
                throw wrapped;
            }
            return Mono.error(wrapped);
        }, List.of((request, error) -> {
            assertSame(exchange, request);
            assertSame(failure, error);
            calls.add("恢复");
            return Mono.empty();
        }, new XtreamRequestExceptionHandler() {
            @Override
            public Mono<Void> handleRequestException(XtreamExchange request, Throwable error) {
                assertSame(exchange, request);
                assertSame(failure, error);
                calls.add("继续传播");
                return Mono.error(error);
            }

            @Override
            public int order() {
                return -10;
            }
        }, (ignored, error) -> fail("恢复后不应再调用后续异常处理器")));

        handler.handle(exchange).block(TIMEOUT);
        assertEquals(List.of("继续传播", "恢复"), calls);
    }

    @Test
    void unhandledFilterFailureKeepsTheOriginalCause() {
        final var failure = new IllegalStateException("过滤失败");
        final var filtering = new FilteringXtreamHandler(ignored -> fail("不应进入业务处理器"), List.of((exchange, next) -> {
            throw failure;
        }));
        final var handler = new ExceptionHandlingXtreamHandler(filtering, List.of());

        assertSame(failure, assertThrows(IllegalStateException.class, () -> handler.handle(exchange()).block(TIMEOUT)));
    }

}

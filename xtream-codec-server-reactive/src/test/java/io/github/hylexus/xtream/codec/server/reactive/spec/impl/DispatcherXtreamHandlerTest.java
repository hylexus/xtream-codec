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

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.RequestHandlerNotFoundException;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamDispatchExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherXtreamHandlerTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private final XtreamExchange exchange = ReactiveTestFixtures.exchange();

    @Test
    void dispatchesToTheFirstMatchingComponentsInOrder() {
        final List<String> calls = new ArrayList<>();
        final XtreamHandlerResult result = result("响应");
        final var dispatcher = new DispatcherXtreamHandler(
                List.of(new Mapping(30, () -> fail("不应继续查找")),
                        new Mapping(20, () -> {
                            calls.add("匹配映射");
                            return Mono.just("处理器");
                        }), new Mapping(10, () -> {
                            calls.add("空映射");
                            return Mono.empty();
                        })),
                List.of(new Adapter(30, ignored -> fail("不应检查后续适配器"), ignored -> fail()),
                        new Adapter(20, "处理器"::equals, handler -> {
                            calls.add("匹配适配器");
                            return Mono.just(result);
                        }), new Adapter(10, ignored -> {
                            calls.add("不支持的适配器");
                            return false;
                        }, ignored -> fail())),
                List.of(new ResultHandler(30, ignored -> fail("不应检查后续响应处理器"), ignored -> fail()),
                        new ResultHandler(20, candidate -> candidate == result, candidate -> {
                            calls.add("处理响应");
                            assertSame(result, candidate);
                            return Mono.empty();
                        }), new ResultHandler(10, ignored -> {
                            calls.add("不支持的响应处理器");
                            return false;
                        }, ignored -> fail())));

        final Mono<Void> dispatch = dispatcher.handle(this.exchange);
        assertTrue(calls.isEmpty());
        dispatch.block(TIMEOUT);
        assertEquals(List.of("空映射", "匹配映射", "不支持的适配器", "匹配适配器", "不支持的响应处理器", "处理响应"), calls);
    }

    @Test
    void reportsTheOriginalExchangeWhenNoMappingMatches() {
        final var dispatcher = new DispatcherXtreamHandler(List.of(ignored -> Mono.empty()), List.of(), List.of());

        final var error = assertThrows(RequestHandlerNotFoundException.class, () -> dispatcher.handle(this.exchange).block(TIMEOUT));
        assertSame(this.exchange, error.getExchange());
    }

    @Test
    void reportsAnUnsupportedHandlerAndAnUnsupportedResultSeparately() {
        final List<XtreamHandlerMapping> mappings = List.of(ignored -> Mono.just("缺少适配器"));
        final var withoutAdapter = new DispatcherXtreamHandler(mappings, List.of(), List.of());
        final var withoutResultHandler = new DispatcherXtreamHandler(mappings,
                List.of(new Adapter(0, ignored -> true, ignored -> Mono.just(result("缺少结果处理器")))), List.of());

        assertTrue(assertThrows(IllegalStateException.class, () -> withoutAdapter.handle(this.exchange).block(TIMEOUT))
                .getMessage().contains("缺少适配器"));
        assertTrue(assertThrows(IllegalStateException.class, () -> withoutResultHandler.handle(this.exchange).block(TIMEOUT))
                .getMessage().contains("缺少结果处理器"));
    }

    @Test
    void emptyAdapterResultSkipsResultProcessing() {
        final var dispatcher = new DispatcherXtreamHandler(List.of(ignored -> Mono.just("无应答")),
                List.of(new Adapter(0, ignored -> true, ignored -> Mono.empty())),
                List.of(new ResultHandler(0, ignored -> fail("没有结果可处理"), ignored -> fail())));

        assertNull(dispatcher.handle(this.exchange).block(TIMEOUT));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void adapterExceptionHandlerCanRecoverFromSynchronousAndReactiveFailures(boolean synchronous) {
        final var failure = new IllegalStateException("适配失败");
        final var wrapped = new XtreamWrappedRuntimeException(failure);
        final AtomicReference<Throwable> recovered = new AtomicReference<>();
        final var adapter = new RecoveringAdapter(ignored -> {
            if (synchronous) {
                throw wrapped;
            }
            return Mono.error(wrapped);
        }, (current, cause) -> {
            assertSame(this.exchange, current);
            recovered.set(cause);
            return Mono.just(result("已经恢复"));
        });
        final var dispatcher = new DispatcherXtreamHandler(List.of(ignored -> Mono.just("处理器")), List.of(adapter),
                List.of(new ResultHandler(0, ignored -> true, actual -> {
                    assertEquals("已经恢复", actual.getReturnValue());
                    return Mono.empty();
                })));

        dispatcher.handle(this.exchange).block(TIMEOUT);
        assertSame(failure, recovered.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void resultExceptionHandlerCanRecoverFromSynchronousAndReactiveFailures(boolean synchronous) {
        final var failure = new IllegalStateException("响应失败");
        final var wrapped = new XtreamWrappedRuntimeException(failure);
        final XtreamHandlerResult fallback = result("备用结果");
        final XtreamHandlerResult original = new XtreamHandlerResult("处理器", "原结果", returnType()) {
            @Override
            public XtreamDispatchExceptionHandler getExceptionHandler() {
                return (actual, cause) -> {
                    assertSame(DispatcherXtreamHandlerTest.this.exchange, actual);
                    assertSame(failure, cause);
                    return Mono.just(fallback);
                };
            }
        };
        final AtomicReference<XtreamHandlerResult> sent = new AtomicReference<>();
        final var dispatcher = new DispatcherXtreamHandler(List.of(ignored -> Mono.just("处理器")),
                List.of(new Adapter(0, ignored -> true, ignored -> Mono.just(original))),
                List.of(new ResultHandler(0, ignored -> true, actual -> {
                    if (actual == original) {
                        if (synchronous) {
                            throw wrapped;
                        }
                        return Mono.error(wrapped);
                    }
                    sent.set(actual);
                    return Mono.empty();
                })));

        dispatcher.handle(this.exchange).block(TIMEOUT);
        assertSame(fallback, sent.get());
    }

    private static XtreamHandlerResult result(String value) {
        return new XtreamHandlerResult("处理器", value, returnType());
    }

    private static XtreamMethodParameter returnType() {
        try {
            return new XtreamMethodParameter(-1, Object.class.getMethod("toString"));
        } catch (NoSuchMethodException error) {
            throw new AssertionError(error);
        }
    }

    private record Mapping(int order, Supplier<Mono<Object>> lookup) implements XtreamHandlerMapping {
        @Override
        public Mono<Object> getHandler(XtreamExchange exchange) {
            return lookup.get();
        }
    }

    private record Adapter(int order, Predicate<Object> matches, Function<Object, Mono<XtreamHandlerResult>> action) implements XtreamHandlerAdapter {
        @Override
        public boolean supports(Object handler) {
            return matches.test(handler);
        }

        @Override
        public Mono<XtreamHandlerResult> handle(XtreamExchange exchange, Object handler) {
            return action.apply(handler);
        }
    }

    private record ResultHandler(int order, Predicate<XtreamHandlerResult> matches,
                                 Function<XtreamHandlerResult, Mono<Void>> action) implements XtreamHandlerResultHandler {
        @Override
        public boolean supports(XtreamHandlerResult result) {
            return matches.test(result);
        }

        @Override
        public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
            return action.apply(result);
        }
    }

    private record RecoveringAdapter(Function<Object, Mono<XtreamHandlerResult>> action,
                                     XtreamDispatchExceptionHandler recovery) implements XtreamHandlerAdapter, XtreamDispatchExceptionHandler {
        @Override
        public boolean supports(Object handler) {
            return true;
        }

        @Override
        public Mono<XtreamHandlerResult> handle(XtreamExchange exchange, Object handler) {
            return action.apply(handler);
        }

        @Override
        public Mono<XtreamHandlerResult> handleError(XtreamExchange exchange, Throwable error) {
            return recovery.handleError(exchange, error);
        }
    }
}

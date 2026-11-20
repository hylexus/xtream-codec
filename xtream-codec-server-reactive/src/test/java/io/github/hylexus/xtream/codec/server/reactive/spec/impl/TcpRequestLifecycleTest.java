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

import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.DefaultTcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.ByteBufFlux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.util.context.Context;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TcpRequestLifecycleTest {
    @Test
    void messagesOnOneConnectionShouldWaitForPreviousCompletion() {
        final List<ByteBuf> buffers = buffers(1, 2);
        final List<Integer> started = new ArrayList<>();
        final Sinks.One<Void> first = Sinks.one();
        final TestAdapter adapter = new TestAdapter(buf -> {
            final int id = buf.getUnsignedByte(0);
            started.add(id);
            return id == 1 ? first.asMono() : Mono.empty();
        });
        final var subscription = Flux.from(adapter.apply(inbound(buffers), null)).subscribe();
        try {
            assertEquals(List.of(1), started);
            assertEquals(1, buffers.getFirst().refCnt());
            first.tryEmitEmpty();
            assertEquals(List.of(1, 2), started);
            assertReleased(buffers);
        } finally {
            subscription.dispose();
            releaseRemaining(buffers);
        }
    }

    @Test
    void independentConnectionsShouldRemainConcurrent() {
        final List<ByteBuf> first = buffers(1);
        final List<ByteBuf> second = buffers(2);
        final Sinks.One<Void> gate = Sinks.one();
        final List<Integer> started = new ArrayList<>();
        final TestAdapter adapter = new TestAdapter(buf -> {
            final int id = buf.getUnsignedByte(0);
            started.add(id);
            return id == 1 ? gate.asMono() : Mono.empty();
        });
        final var subscription = Flux.from(adapter.apply(inbound(first), null)).subscribe();
        try {
            Flux.from(adapter.apply(inbound(second), null)).then().block(Duration.ofSeconds(1));
            assertEquals(List.of(1, 2), started);
            assertReleased(second);
        } finally {
            subscription.dispose();
            releaseRemaining(first);
            releaseRemaining(second);
        }
    }

    @Test
    void cancellationShouldReleaseActiveAndDiscardedBuffers() {
        final List<ByteBuf> buffers = buffers(1, 2, 3);
        final List<Integer> started = new ArrayList<>();
        final TestAdapter adapter = new TestAdapter(buf -> {
            started.add((int) buf.getUnsignedByte(0));
            return Mono.never();
        });
        final var subscription = Flux.from(adapter.apply(inbound(buffers), null)).subscribe();
        try {
            subscription.dispose();
            assertEquals(List.of(1), started);
            assertReleased(buffers);
        } finally {
            releaseRemaining(buffers);
        }
    }

    @Test
    void synchronousDecodeFailureShouldReleaseAndContinue() {
        final List<ByteBuf> buffers = buffers(1, 2);
        final List<Integer> started = new ArrayList<>();
        final TestAdapter adapter = new TestAdapter(buf -> {
            final int id = buf.getUnsignedByte(0);
            started.add(id);
            if (id == 1) {
                throw new IllegalArgumentException("测试同步解码异常");
            }
            return Mono.empty();
        });
        try {
            Flux.from(adapter.apply(inbound(buffers), null)).then().block(Duration.ofSeconds(1));
            assertEquals(List.of(1, 2), started);
            assertReleased(buffers);
        } finally {
            releaseRemaining(buffers);
        }
    }

    @Test
    void asynchronousFailureAndEmptyPayloadShouldRelease() {
        final List<ByteBuf> buffers = new ArrayList<>(buffers(1, 2));
        buffers.add(Unpooled.buffer(1));
        final TestAdapter adapter = new TestAdapter(buf -> Mono.error(new IllegalArgumentException("测试异步业务异常")));
        try {
            Flux.from(adapter.apply(inbound(buffers), null)).then().block(Duration.ofSeconds(1));
            assertReleased(buffers);
        } finally {
            releaseRemaining(buffers);
        }
    }

    private static List<ByteBuf> buffers(int... values) {
        final List<ByteBuf> result = new ArrayList<>();
        for (int value : values) {
            result.add(Unpooled.buffer(1).writeByte(value));
        }
        return result;
    }

    private static NettyInbound inbound(List<ByteBuf> buffers) {
        // 模拟 Reactor Netty 在交付入站报文后释放其持有的引用。
        final ByteBufFlux source = ByteBufFlux.fromInbound(new Flux<ByteBuf>() {
            @Override
            public void subscribe(CoreSubscriber<? super ByteBuf> actual) {
                Flux.fromIterable(buffers).subscribe(new CoreSubscriber<>() {
                    @Override
                    public Context currentContext() {
                        return actual.currentContext();
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        actual.onSubscribe(subscription);
                    }

                    @Override
                    public void onNext(ByteBuf buffer) {
                        try {
                            actual.onNext(buffer);
                        } finally {
                            ReferenceCountUtil.release(buffer);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        actual.onError(error);
                    }

                    @Override
                    public void onComplete() {
                        actual.onComplete();
                    }
                });
            }
        });
        return (NettyInbound) Proxy.newProxyInstance(TcpRequestLifecycleTest.class.getClassLoader(), new Class<?>[]{NettyInbound.class},
                (proxy, method, args) -> method.getName().equals("receive") ? source : null);
    }

    private static void assertReleased(List<ByteBuf> buffers) {
        buffers.forEach(buffer -> assertEquals(0, buffer.refCnt()));
    }

    private static void releaseRemaining(List<ByteBuf> buffers) {
        buffers.forEach(buffer -> {
            if (buffer.refCnt() > 0) {
                buffer.release(buffer.refCnt());
            }
        });
    }

    @NullMarked
    private static final class TestAdapter extends DefaultTcpXtreamNettyHandlerAdapter {
        private final Function<ByteBuf, Mono<Void>> handler;

        private TestAdapter(Function<ByteBuf, Mono<Void>> handler) {
            super(ByteBufAllocator.DEFAULT, null, exchange -> Mono.empty());
            this.handler = handler;
        }

        @Override
        public InboundInfo initTcpInboundInfo(NettyInbound inbound) {
            return new InboundInfo(null, new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
        }

        @Override
        protected Mono<Void> handleSingleRequest(NettyInbound inbound, NettyOutbound outbound, ByteBuf payload, InboundInfo info) {
            return handler.apply(payload);
        }
    }
}

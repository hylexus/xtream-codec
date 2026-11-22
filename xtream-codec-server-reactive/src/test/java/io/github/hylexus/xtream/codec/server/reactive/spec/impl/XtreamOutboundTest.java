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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.hylexus.xtream.codec.server.reactive.spec.impl.ReactiveTestFixtures.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

class XtreamOutboundTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @ParameterizedTest
    @EnumSource(XtreamInbound.Type.class)
    void writesBuffersThroughTheAppropriateTransport(XtreamInbound.Type type) {
        final var sink = new CapturingOutbound(type);
        final var response = new DefaultXtreamResponse(ByteBufAllocator.DEFAULT, sink.delegate(), type, ADDRESS);
        final ByteBuf first = Unpooled.buffer(1).writeByte(19);
        final ByteBuf second = Unpooled.buffer(1).writeByte(25);

        try {
            response.writeWith(Flux.just(first, second)).block(TIMEOUT);
            assertEquals(List.of(type == XtreamInbound.Type.TCP ? "send" : "sendObject"), sink.operations);
            assertEquals(List.of(19, 25), sink.received);
            assertEquals(0, first.refCnt());
            assertEquals(0, second.refCnt());
        } finally {
            releaseIfNecessary(first);
            releaseIfNecessary(second);
        }
    }

    @ParameterizedTest
    @EnumSource(XtreamInbound.Type.class)
    void flushesTcpGroupsAndSendsIndividualUdpDatagrams(XtreamInbound.Type type) {
        final var sink = new CapturingOutbound(type);
        final var response = new DefaultXtreamResponse(ByteBufAllocator.DEFAULT, sink.delegate(), type, ADDRESS);
        final ByteBuf first = Unpooled.buffer(1).writeByte(19);
        final ByteBuf second = Unpooled.buffer(1).writeByte(25);

        try {
            response.writeAndFlushWith(Flux.just(Mono.just(first), Mono.<ByteBuf>empty(), Mono.just(second))).block(TIMEOUT);
            assertEquals(type == XtreamInbound.Type.TCP ? List.of("sendGroups") : List.of("sendObject", "sendObject"), sink.operations);
            assertEquals(List.of(19, 25), sink.received.stream().sorted().toList());
            assertEquals(0, first.refCnt());
            assertEquals(0, second.refCnt());
        } finally {
            releaseIfNecessary(first);
            releaseIfNecessary(second);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void udpWriteFailureOrCancellationReleasesRemainingBuffers(boolean failed) {
        final var counters = new AtomicInteger();
        final var error = new IllegalStateException("UDP 发送失败");
        final var sink = new CapturingOutbound(XtreamInbound.Type.UDP, source -> {
            final var sequence = counters.incrementAndGet();
            return Flux.from(source).doOnNext(ReferenceCountUtil::release).then(sequence == 2 && failed ? Mono.error(error) : Mono.never());
        });
        final var response = new DefaultXtreamResponse(ByteBufAllocator.DEFAULT, sink.delegate(), XtreamInbound.Type.UDP, ADDRESS);
        final List<ByteBuf> buffers = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            buffers.add(Unpooled.buffer(1).writeByte(i));
        }
        try {
            final var send = response.writeAndFlushWith(Mono.just(Flux.fromIterable(buffers)));
            if (failed) {
                assertSame(error, assertThrows(IllegalStateException.class, () -> send.block(TIMEOUT)));
                assertEquals(2, counters.get());
            } else {
                final var subscribed = send.subscribe();
                try {
                    assertTrue(counters.get() < buffers.size());
                } finally {
                    subscribed.dispose();
                }
            }
            assertTrue(buffers.stream().allMatch(buffer -> buffer.refCnt() == 0));
        } finally {
            buffers.forEach(XtreamOutboundTest::releaseIfNecessary);
        }
    }

    private static void releaseIfNecessary(ByteBuf buffer) {
        if (buffer.refCnt() > 0) {
            buffer.release();
        }
    }

    private static final class CapturingOutbound {
        private final XtreamInbound.Type type;
        private final List<String> operations = new ArrayList<>();
        private final List<Integer> received = new ArrayList<>();
        private final Function<Flux<?>, Mono<Void>> write;

        private CapturingOutbound(XtreamInbound.Type type) {
            this.type = type;
            this.write = messages -> messages.doOnNext(this::capture).then();
        }

        private CapturingOutbound(XtreamInbound.Type type, Function<Flux<?>, Mono<Void>> write) {
            this.type = type;
            this.write = write;
        }

        private NettyOutbound delegate() {
            return (NettyOutbound) Proxy.newProxyInstance(XtreamOutboundTest.class.getClassLoader(),
                    new Class<?>[]{NettyOutbound.class}, (proxy, method, args) -> {
                        final String operation = method.getName();
                        this.operations.add(operation);
                        return switch (operation) {
                            case "send" -> completedBy(Flux.from((Publisher<?>) args[0]));
                            case "sendGroups" -> completedBy(Flux.from((Publisher<?>) args[0]).concatMap(group -> Flux.from((Publisher<?>) group)));
                            case "sendObject" -> completedBy(args[0] instanceof Publisher<?> publisher ? Flux.from(publisher) : Flux.just(args[0]));
                            default -> throw new AssertionError(operation);
                        };
                    });
        }

        @SuppressWarnings("unchecked")
        private NettyOutbound completedBy(Flux<?> messages) {
            final Mono<Void> written = this.write.apply(messages);
            return (NettyOutbound) Proxy.newProxyInstance(XtreamOutboundTest.class.getClassLoader(),
                    new Class<?>[]{NettyOutbound.class}, (proxy, method, args) -> switch (method.getName()) {
                        case "then" -> written;
                        case "subscribe" -> {
                            written.subscribe((Subscriber<? super Void>) args[0]);
                            yield null;
                        }
                        default -> throw new AssertionError(method.getName());
                    });
        }

        private void capture(Object message) {
            try {
                final ByteBuf buffer;
                if (this.type == XtreamInbound.Type.UDP) {
                    final var datagram = assertInstanceOf(DatagramPacket.class, message);
                    assertEquals(ADDRESS, datagram.recipient());
                    buffer = datagram.content();
                } else {
                    buffer = assertInstanceOf(ByteBuf.class, message);
                }
                this.received.add((int) buffer.readUnsignedByte());
                assertFalse(buffer.isReadable());
            } finally {
                ReferenceCountUtil.release(message);
            }
        }
    }
}

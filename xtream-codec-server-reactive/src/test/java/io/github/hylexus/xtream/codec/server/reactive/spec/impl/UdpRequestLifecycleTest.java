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

import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.DefaultUdpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class UdpRequestLifecycleTest {
    @Test
    void synchronousDatagramFailureShouldNotTerminateReception() {
        final var first = packet(1);
        final var second = packet(2);
        final var received = new ArrayList<Integer>();
        final var adapter = new TestAdapter(packet -> {
            final int id = packet.content().getUnsignedByte(0);
            received.add(id);
            if (id == 1) {
                throw new IllegalArgumentException("测试同步 UDP 解码异常");
            }
            return Mono.empty();
        });
        try {
            Flux.from(adapter.apply(inbound(first, second), null)).then().block(Duration.ofSeconds(1));
            assertEquals(List.of(1, 2), received);
            // 此处仍保留测试替身持有的原始入站引用。
            assertEquals(1, first.refCnt());
            assertEquals(1, second.refCnt());
        } finally {
            first.release();
            second.release();
        }
    }

    @Test
    void asynchronousFailureReleasesItsReferenceAndKeepsProcessing() {
        final var first = packet(1);
        final var second = packet(2);
        final var received = new ArrayList<Integer>();
        final var adapter = new TestAdapter(packet -> Mono.defer(() -> {
            final int id = packet.content().getUnsignedByte(0);
            received.add(id);
            return id == 1 ? Mono.error(new IllegalStateException("测试异步 UDP 异常")) : Mono.empty();
        }));
        try {
            Flux.from(adapter.apply(inbound(first, second), null)).then().block(Duration.ofSeconds(1));
            assertEquals(List.of(1, 2), received);
            assertEquals(1, first.refCnt());
            assertEquals(1, second.refCnt());
        } finally {
            first.release();
            second.release();
        }
    }

    @Test
    void cancellationShouldReleaseRetainedDatagram() {
        final var packet = packet(1);
        final var adapter = new TestAdapter(ignored -> Mono.never());
        final var subscription = Flux.from(adapter.apply(inbound(packet), null)).subscribe();
        try {
            // 模拟 Reactor Netty 交付后释放原始入站引用。
            packet.release();
            assertEquals(1, packet.refCnt());
            subscription.dispose();
            assertEquals(0, packet.refCnt());
        } finally {
            subscription.dispose();
            if (packet.refCnt() > 0) {
                packet.release(packet.refCnt());
            }
        }
    }

    private static DatagramPacket packet(int value) {
        return new DatagramPacket(Unpooled.buffer(1).writeByte(value), new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
    }

    private static NettyInbound inbound(DatagramPacket... packets) {
        return (NettyInbound) Proxy.newProxyInstance(UdpRequestLifecycleTest.class.getClassLoader(), new Class<?>[]{NettyInbound.class},
                (proxy, method, args) -> method.getName().equals("receiveObject") ? Flux.fromArray(packets) : null);
    }

    @NullMarked
    private static final class TestAdapter extends DefaultUdpXtreamNettyHandlerAdapter {
        private final Function<DatagramPacket, Mono<Void>> handler;

        private TestAdapter(Function<DatagramPacket, Mono<Void>> handler) {
            super(ByteBufAllocator.DEFAULT, null, exchange -> Mono.empty());
            this.handler = handler;
        }

        @Override
        public InboundInfo initUdpInboundInfo(NettyInbound inbound, DatagramPacket packet) {
            return new InboundInfo(null, packet.recipient());
        }

        @Override
        protected Mono<Void> handleRequest(NettyInbound inbound, NettyOutbound outbound, DatagramPacket packet, InboundInfo info) {
            return handler.apply(packet);
        }
    }
}

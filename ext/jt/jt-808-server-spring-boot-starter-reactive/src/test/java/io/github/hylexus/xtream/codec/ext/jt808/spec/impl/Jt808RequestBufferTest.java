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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808AttachmentServerTcpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808AttachmentServerUdpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808InstructionServerTcpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808InstructionServerUdpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@NullMarked
class Jt808RequestBufferTest {
    @Test
    void udpSynchronousDecodeFailureShouldReleaseFrame() {
        final var decoder = mock(Jt808RequestDecoder.class);
        when(decoder.decode(any(), anyString(), any(), any(), any(), any(), any(), any())).thenThrow(new IllegalArgumentException("测试解码异常"));
        final var frame = Unpooled.buffer(1).writeByte(1);
        assertThrows(IllegalArgumentException.class, () -> new ExposedUdp(decoder, new Jt808RequestLifecycleListener.NoopJt808RequestLifecycleListener()).handle(frame).block());
        assertEquals(0, frame.refCnt());
    }

    @Test
    void throwingListenerShouldReleaseDecodedTcpAndUdpBody() {
        for (boolean udp : new boolean[]{false, true}) {
            final var decoder = mock(Jt808RequestDecoder.class);
            final var listener = mock(Jt808RequestLifecycleListener.class);
            final var request = mock(Jt808Request.class, CALLS_REAL_METHODS);
            final var body = Unpooled.buffer(1).writeByte(1);
            final var frame = Unpooled.buffer(1).writeByte(2);
            when(request.payload()).thenReturn(body);
            when(decoder.decode(any(), anyString(), any(), any(), any(), any(), any(), any())).thenReturn(request);
            doThrow(new IllegalStateException("测试生命周期通知异常")).when(listener).afterRequestDecoded(any(), any(), any());
            if (udp) {
                assertThrows(IllegalStateException.class, () -> new ExposedUdp(decoder, listener).handle(frame).block());
                assertEquals(0, frame.refCnt());
            } else {
                final var adapter = new ExposedTcp(decoder, listener);
                assertThrows(IllegalStateException.class, () -> adapter.decode(frame));
                assertEquals(1, frame.refCnt());
                frame.release();
            }
            assertEquals(0, body.refCnt());
        }
    }

    @Test
    void throwingListenerShouldReleaseDecodedAttachmentBody() {
        final var decoder = mock(Jt808RequestDecoder.class);
        final var listener = mock(Jt808RequestLifecycleListener.class);
        final var request = mock(Jt808Request.class, CALLS_REAL_METHODS);
        final var failure = new IllegalStateException("测试附件生命周期通知异常");
        final var frame = Unpooled.EMPTY_BUFFER;
        final var address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345);
        final var tcp = new Jt808AttachmentServerTcpHandlerAdapter(ByteBufAllocator.DEFAULT, null, null, decoder, listener, null) {
            void decode() {
                doCreateTcpRequest(ByteBufAllocator.DEFAULT, null, frame, new InboundInfo(null, address), XtreamInbound.Type.TCP);
            }
        };
        final var udp = new Jt808AttachmentServerUdpHandlerAdapter(ByteBufAllocator.DEFAULT, null, null, null, null, decoder, listener) {
            void decode() {
                doCreateUdpRequest(ByteBufAllocator.DEFAULT, null, frame, new InboundInfo(null, address));
            }
        };
        when(decoder.decode(any(), anyString(), any(), any(), any(), any(), any(), any())).thenReturn(request);
        doThrow(failure).when(listener).afterRequestDecoded(any(), any(), same(request));
        for (Executable decode : new Executable[]{tcp::decode, udp::decode}) {
            final var body = Unpooled.buffer(1).writeByte(1);
            when(request.payload()).thenReturn(body);
            try {
                assertSame(failure, assertThrows(IllegalStateException.class, decode));
                assertEquals(0, body.refCnt());
            } finally {
                if (body.refCnt() != 0) {
                    body.release();
                }
            }
        }
    }

    @Test
    void udpCancellationShouldReleaseDecodedBodyAndFrame() {
        final var frame = Unpooled.buffer(1).writeByte(1);
        final var body = Unpooled.buffer(1).writeByte(2);
        final var request = mock(Jt808Request.class, CALLS_REAL_METHODS);
        when(request.payload()).thenReturn(body);
        final var exchange = mock(XtreamExchange.class);
        when(exchange.request()).thenReturn(request);
        final var adapter = new ExposedUdp(null, null) {
            @Override
            protected XtreamExchange createUdpExchange(ByteBufAllocator allocator, NettyInbound inbound, NettyOutbound outbound, ByteBuf payload, InboundInfo info) {
                return exchange;
            }

            @Override
            protected Mono<Void> doUdpExchange(XtreamExchange ignored) {
                return Mono.never();
            }
        };
        final var subscription = adapter.handle(frame).subscribe();
        subscription.dispose();
        assertEquals(0, frame.refCnt());
        assertEquals(0, body.refCnt());
    }

    private static class ExposedUdp extends Jt808InstructionServerUdpHandlerAdapter {
        ExposedUdp(Jt808RequestDecoder decoder, Jt808RequestLifecycleListener listener) {
            super(ByteBufAllocator.DEFAULT, null, null, null, decoder, listener);
        }

        Mono<Void> handle(ByteBuf frame) {
            return handleSingleRequest(null, null, frame, new InboundInfo(null, new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345)));
        }
    }

    private static final class ExposedTcp extends Jt808InstructionServerTcpHandlerAdapter {
        ExposedTcp(Jt808RequestDecoder decoder, Jt808RequestLifecycleListener listener) {
            super(ByteBufAllocator.DEFAULT, null, null, decoder, listener);
        }

        XtreamRequest decode(ByteBuf frame) {
            return doCreateTcpRequest(ByteBufAllocator.DEFAULT, null, frame, new InboundInfo(null, new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345)), XtreamInbound.Type.TCP);
        }
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriber;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ResponseEntity;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.channel.AbortedException;
import reactor.netty.channel.ChannelOperations;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class Jt808ResponseBufferLifecycleTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final ByteBuf buffer = Unpooled.directBuffer(1).writeByte(0x7e);
    private final Jt808ResponseEncoder encoder = mock(Jt808ResponseEncoder.class);
    private final Jt808RequestLifecycleListener listener = mock(Jt808RequestLifecycleListener.class);
    private final Jt808Request request = mock(Jt808Request.class);

    @AfterEach
    void releaseTestResources() {
        channel.finishAndReleaseAll();
        if (buffer.refCnt() > 0) {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReleaseResponseWhenClosedChannelRejectsPublisher(boolean entity) {
        final var response = nettyResponse();
        channel.close().syncUninterruptibly();

        assertThrows(AbortedException.class, () -> handle(entity, response).block(Duration.ofSeconds(2)));
        verify(listener).beforeResponseSend(request, buffer);
        assertEquals(0, buffer.refCnt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReleaseResponseWhenSendIsCancelledBeforeHandoff(boolean entity) {
        final var response = mock(XtreamResponse.class);
        when(response.writeWith(any())).thenReturn(Mono.never());

        final var subscription = handle(entity, response).subscribe();
        verify(listener).beforeResponseSend(request, buffer);
        assertEquals(1, buffer.refCnt());
        subscription.dispose();
        assertEquals(0, buffer.refCnt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReleaseResponseWhenListenerFails(boolean entity) {
        final var error = new IllegalStateException("listener failed");
        doThrow(error).when(listener).beforeResponseSend(request, buffer);

        assertSame(error, assertThrows(IllegalStateException.class,
                () -> handle(entity, nettyResponse()).block(Duration.ofSeconds(2))));
        assertNull(channel.readOutbound());
        assertEquals(0, buffer.refCnt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldHandOffTheLiveBufferOnSuccessfulSend(boolean entity) {
        handle(entity, nettyResponse()).block(Duration.ofSeconds(2));

        verify(listener).beforeResponseSend(request, buffer);
        assertSame(buffer, channel.readOutbound());
        assertEquals(1, buffer.refCnt());
        assertEquals(0x7e, buffer.readUnsignedByte());
        assertTrue(buffer.release());
    }

    private DefaultXtreamResponse nettyResponse() {
        final var outbound = new ChannelOperations<>(Connection.from(channel), ConnectionObserver.emptyListener());
        return new DefaultXtreamResponse(channel.alloc(), outbound, XtreamInbound.Type.TCP, new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
    }

    private Mono<Void> handle(boolean entity, XtreamResponse response) {
        final var header = mock(Jt808RequestHeader.class);
        when(header.version()).thenReturn(Jt808ProtocolVersion.VERSION_2019);
        when(header.terminalId()).thenReturn("00000000000000000001");
        when(request.header()).thenReturn(header);
        when(request.castAs(Jt808Request.class)).thenReturn(request);
        final var exchange = mock(XtreamExchange.class);
        when(exchange.request()).thenReturn(request);
        when(exchange.response()).thenReturn(response);
        final var returnType = mock(XtreamMethodParameter.class);
        final var body = new Object();
        if (entity) {
            when(encoder.encode(same(body), any(Jt808MessageDescriber.class))).thenReturn(buffer);
            final var result = new XtreamHandlerResult(this, new Jt808ResponseEntity<>(0x8001, body), returnType);
            return new Jt808ResponseEntityHandlerResultHandler(encoder, listener).handleResult(exchange, result);
        }
        final var annotation = mock(Jt808ResponseBody.class);
        when(returnType.getMethodAnnotation(Jt808ResponseBody.class)).thenReturn(annotation);
        when(encoder.encode(body, Jt808ProtocolVersion.VERSION_2019, "00000000000000000001", annotation)).thenReturn(buffer);
        final var result = new XtreamHandlerResult(this, body, returnType);
        return new Jt808ResponseBodyHandlerResultHandler(encoder, listener).handleResult(exchange, result);
    }
}

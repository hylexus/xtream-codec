/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.time.Instant;

/**
 * @author hylexus
 */
public class DefaultUdpXtreamNettyHandlerAdapter implements UdpXtreamNettyHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefaultUdpXtreamNettyHandlerAdapter.class);
    protected final XtreamHandler xtreamHandler;
    protected final ByteBufAllocator allocator;
    protected final XtreamSessionManager<? extends XtreamSession> sessionManager;

    public DefaultUdpXtreamNettyHandlerAdapter(ByteBufAllocator allocator, XtreamSessionManager<? extends XtreamSession> sessionManager, XtreamHandler xtreamHandler) {
        this.xtreamHandler = xtreamHandler;
        this.allocator = allocator;
        this.sessionManager = sessionManager;
        log.info("DefaultUdpXtreamNettyHandlerAdapter initialized");
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound.receiveObject().flatMap(object -> {
            if (object instanceof DatagramPacket datagramPacket) {
                final InboundInfo inboundInfo = this.initUdpInboundInfo(nettyInbound, datagramPacket);
                return handleRequest(nettyInbound, nettyOutbound, datagramPacket, inboundInfo)
                        .onErrorResume(Throwable.class, throwable -> {
                            log.error("Unexpected Exception", throwable);
                            return Mono.empty();
                        });
            } else {
                return Mono.error(new IllegalStateException("Cannot handle message. type = [" + object.getClass() + "]"));
            }
        }).onErrorResume(throwable -> {
            log.error("Unexpected Error", throwable);
            return Mono.empty();
        });
    }

    protected Mono<Void> handleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, DatagramPacket datagramPacket, InboundInfo inboundInfo) {
        final XtreamExchange exchange = this.createUdpExchange(allocator, nettyInbound, nettyOutbound, datagramPacket.content(), inboundInfo);
        return this.doUdpExchange(exchange);
    }

    protected XtreamExchange createUdpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InboundInfo inboundInfo) {
        final XtreamRequest.Type type = XtreamRequest.Type.UDP;
        final XtreamRequest request = this.doCreateUdpRequest(allocator, nettyInbound, payload, inboundInfo);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, inboundInfo.remoteAddress());

        return new DefaultXtreamExchange(sessionManager, request, response);
    }

    protected XtreamRequest doCreateUdpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf payload, InboundInfo inboundInfo) {
        return new DefaultXtreamRequest(XtreamField.ALL_VERSION, this.generateRequestId(nettyInbound), allocator, nettyInbound, XtreamInbound.Type.UDP, payload, inboundInfo.channel(), inboundInfo.remoteAddress());
    }

    protected Mono<Void> doUdpExchange(XtreamExchange exchange) {
        return exchange.session().flatMap(session -> {
            session.lastCommunicateTime(Instant.now());
            return xtreamHandler.handle(exchange);
        });
    }
}

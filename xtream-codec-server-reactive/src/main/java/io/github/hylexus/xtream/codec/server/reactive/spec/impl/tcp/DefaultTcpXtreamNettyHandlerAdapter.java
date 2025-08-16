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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.time.Instant;

/**
 * @author hylexus
 */
public class DefaultTcpXtreamNettyHandlerAdapter implements TcpXtreamNettyHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefaultTcpXtreamNettyHandlerAdapter.class);
    protected final XtreamHandler xtreamHandler;
    protected final ByteBufAllocator allocator;
    protected final XtreamSessionManager<? extends XtreamSession> sessionManager;

    public DefaultTcpXtreamNettyHandlerAdapter(ByteBufAllocator allocator, XtreamSessionManager<? extends XtreamSession> sessionManager, XtreamHandler xtreamHandler) {
        this.xtreamHandler = xtreamHandler;
        this.allocator = allocator;
        this.sessionManager = sessionManager;
        log.info("DefaultTcpXtreamNettyHandlerAdapter initialized");
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound.receive().flatMap(byteBuf -> {
            if (byteBuf.readableBytes() <= 0) {
                return Mono.empty();
            }
            final InetSocketAddress remoteAddress = this.initTcpRemoteAddress(nettyInbound);
            byteBuf.retain();
            return this.handleSingleRequest(nettyInbound, nettyOutbound, byteBuf, remoteAddress)
                    .onErrorResume(Throwable.class, throwable -> {
                        log.error("Unexpected Exception", throwable);
                        return Mono.empty();
                    }).doFinally(signalType -> {
                        // ...
                        XtreamBytes.releaseBuf(byteBuf);
                    });
        }).onErrorResume(throwable -> {
            log.error("Unexpected Error", throwable);
            return Mono.empty();
        });
    }

    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, remoteAddress);
        return this.doTcpExchange(exchange);
    }

    public XtreamExchange createTcpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf byteBuf, InetSocketAddress remoteAddress) {
        final XtreamRequest.Type type = XtreamRequest.Type.TCP;
        final XtreamRequest request = this.doCreateTcpRequest(allocator, nettyInbound, byteBuf, remoteAddress, type);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, remoteAddress);

        return new DefaultXtreamExchange(sessionManager, request, response);
    }

    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InetSocketAddress remoteAddress, XtreamRequest.Type type) {
        return new DefaultXtreamRequest(this.generateRequestId(nettyInbound), allocator, nettyInbound, type, byteBuf, remoteAddress);
    }

    protected Mono<Void> doTcpExchange(XtreamExchange exchange) {
        return exchange.session().flatMap(session -> {
            session.lastCommunicateTime(Instant.now());
            return xtreamHandler.handle(exchange);
        });
    }

    protected InetSocketAddress initTcpRemoteAddress(NettyInbound nettyInbound) {
        final InetSocketAddress[] remoteAddress = new InetSocketAddress[1];
        nettyInbound.withConnection(connection -> remoteAddress[0] = (InetSocketAddress) connection.channel().remoteAddress());
        return remoteAddress[0];
    }

}

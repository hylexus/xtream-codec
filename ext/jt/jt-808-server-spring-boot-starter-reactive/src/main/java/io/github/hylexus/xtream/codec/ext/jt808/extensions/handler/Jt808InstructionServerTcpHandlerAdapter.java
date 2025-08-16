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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.DefaultTcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public class Jt808InstructionServerTcpHandlerAdapter extends DefaultTcpXtreamNettyHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Jt808InstructionServerTcpHandlerAdapter.class);
    protected final Jt808RequestDecoder requestDecoder;
    protected final Jt808RequestLifecycleListener requestLifecycleListener;

    public Jt808InstructionServerTcpHandlerAdapter(ByteBufAllocator allocator, XtreamSessionManager<? extends XtreamSession> sessionManager, XtreamHandler xtreamHandler, Jt808RequestDecoder requestDecoder, Jt808RequestLifecycleListener requestLifecycleListener) {
        super(allocator, sessionManager, xtreamHandler);
        this.requestDecoder = requestDecoder;
        this.requestLifecycleListener = requestLifecycleListener;
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound.receive().flatMap(byteBuf -> {
            if (byteBuf.readableBytes() <= 0) {
                return Mono.empty();
            }
            final InetSocketAddress remoteAddress = this.initTcpRemoteAddress(nettyInbound);
            return this.handleSingleRequest(nettyInbound, nettyOutbound, byteBuf, remoteAddress)
                    .onErrorResume(Throwable.class, throwable -> {
                        log.error("Unexpected Exception", throwable);
                        return Mono.empty();
                    });
        }).onErrorResume(throwable -> {
            log.error("Unexpected Error", throwable);
            return Mono.empty();
        });
    }

    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, remoteAddress);
        return this.doTcpExchange(exchange).doFinally(signalType -> {
            // ...
            exchange.request().release();
        });
    }

    public XtreamExchange createTcpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf byteBuf, InetSocketAddress remoteAddress) {
        final XtreamRequest.Type type = XtreamRequest.Type.TCP;
        final XtreamRequest request = this.doCreateTcpRequest(allocator, nettyInbound, byteBuf, remoteAddress, type);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, remoteAddress);

        return new DefaultXtreamExchange(sessionManager, request, response);
    }

    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InetSocketAddress remoteAddress, XtreamRequest.Type type) {
        final Jt808Request request = this.requestDecoder.decode(Jt808ServerType.INSTRUCTION_SERVER, this.generateRequestId(nettyInbound), allocator, nettyInbound, XtreamInbound.Type.TCP, byteBuf, remoteAddress);
        this.requestLifecycleListener.afterRequestDecoded(nettyInbound, byteBuf, request);
        return request;
    }

}

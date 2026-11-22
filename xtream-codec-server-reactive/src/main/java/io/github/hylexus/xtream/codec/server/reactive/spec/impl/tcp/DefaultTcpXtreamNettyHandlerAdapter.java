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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.FutureMono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.time.Instant;

/**
 * @author hylexus
 * @author Codex (AI)
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
        log.info("[{}] initialized", this.getClass().getSimpleName());
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        final InboundInfo inboundInfo = this.initTcpInboundInfo(nettyInbound);
        return this.handleRequests(nettyInbound, nettyOutbound, inboundInfo).onErrorResume(throwable -> {
            log.error("Unexpected Error", throwable);
            return Mono.empty();
        });
    }

    protected Flux<Void> handleRequests(NettyInbound inbound, NettyOutbound outbound, InboundInfo inboundInfo) {
        return inbound
                .receive()
                .concatMap(
                        (ByteBuf payload) -> {
                            // 仅保留交给请求链的入站引用，未交付的缓冲区由 Reactor Netty 管理。
                            payload.retain();
                            return Mono
                                    .defer(() -> payload.isReadable()
                                            ? this.handleSingleRequest(inbound, outbound, payload, inboundInfo)
                                            : Mono.empty()
                                    )
                                    .onErrorResume(throwable -> {
                                        log.error("Unexpected Exception", throwable);
                                        return Mono.empty();
                                    })
                                    .doFinally(signal -> XtreamBytes.releaseBuf(payload));
                        },
                        0
                )
                // 入站完成后 concatMap 仍会等待在途业务，通道关闭时应取消该业务。
                .takeUntilOther(FutureMono.from(inboundInfo.channel().closeFuture()));
    }

    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InboundInfo inboundInfo) {
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, inboundInfo);
        return this.doTcpExchange(exchange);
    }

    protected XtreamExchange createTcpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf byteBuf, InboundInfo inboundInfo) {
        final XtreamInbound.Type type = XtreamRequest.Type.TCP;
        final XtreamRequest request = this.doCreateTcpRequest(allocator, nettyInbound, byteBuf, inboundInfo, type);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, inboundInfo.remoteAddress());

        return new DefaultXtreamExchange(sessionManager, request, response);
    }

    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InboundInfo inboundInfo, XtreamInbound.Type type) {
        return new DefaultXtreamRequest(XtreamField.ALL_VERSION, this.generateRequestId(nettyInbound), allocator, nettyInbound, type, byteBuf, inboundInfo.channel(), inboundInfo.remoteAddress());
    }

    protected Mono<Void> doTcpExchange(XtreamExchange exchange) {
        return exchange.session().flatMap(session -> {
            session.lastCommunicateTime(Instant.now());
            return xtreamHandler.handle(exchange);
        });
    }

}

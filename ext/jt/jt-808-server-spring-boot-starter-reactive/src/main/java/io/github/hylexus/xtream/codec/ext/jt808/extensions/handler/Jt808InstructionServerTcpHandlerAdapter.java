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
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.DefaultTcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * @author <a href="https://gitee.com/qinglang1208">何事惊慌</a>
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
        final InboundInfo inboundInfo = this.initTcpInboundInfo(nettyInbound);
        return nettyInbound.receive().flatMap(byteBuf -> {
            if (byteBuf.readableBytes() <= 0) {
                return Mono.empty();
            }
            return this.handleSingleRequest(nettyInbound, nettyOutbound, byteBuf, inboundInfo)
                    .onErrorResume(Throwable.class, throwable -> {
                        log.error("Unexpected Exception", throwable);
                        return Mono.empty();
                    });
        }).doOnError(SocketException.class, throwable -> {
            this.doWithSessionManager(manager -> {
                boolean closed = false;
                Channel channel = inboundInfo.channel();
                if (channel == null) {
                    return;
                }
                try {
                    final String sessionId = sessionManager.sessionIdGenerator().generateTcpSessionId(channel);
                    closed = sessionManager.closeSessionById(sessionId, XtreamSessionEventListener.DefaultSessionCloseReason.CLOSED_BY_CLIENT);
                } finally {
                    if (!closed) {
                        closeAndIgnoreException(channel, "Close channel because of [Socket Exception]: {}");
                    }
                }
            });
        }).onErrorResume(throwable -> {
            log.error("Unexpected Error", throwable);
            return Mono.empty();
        });
    }

    protected void doWithSessionManager(Consumer<XtreamSessionManager<? extends XtreamSession>> consumer) {
        if (this.sessionManager != null) {
            consumer.accept(this.sessionManager);
        } else {
            throw new IllegalStateException("No session manager found");
        }
    }

    private static void closeAndIgnoreException(Channel channel, String message) {
        try {
            log.info(message, channel);
            channel.close();
        } catch (Exception ignored) {
            // ignored
        }
    }

    @Override
    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InboundInfo inboundInfo) {
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, inboundInfo);
        return this.doTcpExchange(exchange).doFinally(signalType -> {
            // ...
            exchange.request().release();
        });
    }

    @Override
    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InboundInfo inboundInfo, XtreamRequest.Type type) {
        final Channel channel = inboundInfo.channel();
        final InetSocketAddress remoteAddress = inboundInfo.remoteAddress();
        final String requestId = this.generateRequestId(nettyInbound);
        final Jt808Request request = this.requestDecoder.decode(Jt808ServerType.INSTRUCTION_SERVER, requestId, allocator, nettyInbound, XtreamInbound.Type.TCP, byteBuf, channel, remoteAddress);
        this.requestLifecycleListener.afterRequestDecoded(nettyInbound, byteBuf, request);
        return request;
    }

}

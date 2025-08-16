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

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808MessageBodyProps;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.DefaultTcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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
public class Jt808AttachmentServerTcpHandlerAdapter extends DefaultTcpXtreamNettyHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Jt808AttachmentServerTcpHandlerAdapter.class);
    protected final Jt808RequestDecoder requestDecoder;
    protected final Jt808RequestLifecycleListener requestLifecycleListener;
    protected final XtreamHandler attachmentHandler;

    public Jt808AttachmentServerTcpHandlerAdapter(ByteBufAllocator allocator, XtreamSessionManager<? extends XtreamSession> sessionManager, XtreamHandler xtreamHandler, Jt808RequestDecoder requestDecoder, Jt808RequestLifecycleListener requestLifecycleListener, XtreamHandler attachmentHandler) {
        super(allocator, sessionManager, xtreamHandler);
        this.requestDecoder = requestDecoder;
        this.requestLifecycleListener = requestLifecycleListener;
        this.attachmentHandler = attachmentHandler;
    }

    @Override
    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        // 码流消息
        if (JtProtocolUtils.isAttachmentRequest(payload)) {
            return this.handleStreamRequest(nettyInbound, nettyOutbound, payload, remoteAddress);
        }

        // 普通的指令消息
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, remoteAddress);
        return doTcpExchange(exchange).doFinally(signalType -> {
            // ...
            exchange.request().release();
        });
    }

    protected Mono<Void> handleStreamRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        return this.getTcpAttachmentSession(nettyInbound, remoteAddress).flatMap(session -> {
            session.lastCommunicateTime(Instant.now());
            final Jt808Request jt808Request = simulateJt808Request(allocator, nettyInbound, payload, session, remoteAddress);
            final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, XtreamInbound.Type.TCP, remoteAddress);
            final XtreamExchange simulatedExchange = new DefaultXtreamExchange(this.sessionManager, jt808Request, response);
            return this.attachmentHandler.handle(simulatedExchange);
        });
    }

    public Jt808Request simulateJt808Request(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf payload, Jt808Session session, InetSocketAddress remoteAddress) {
        final Jt808RequestHeader header = Jt808RequestHeader.newBuilder()
                .version(session.protocolVersion())
                .messageId(0x30316364)
                .messageBodyProps(new DefaultJt808MessageBodyProps(0))
                .terminalId(session.terminalId())
                .flowId(0)
                .build();

        return new DefaultJt808Request(
                Jt808ServerType.ATTACHMENT_SERVER,
                this.generateRequestId(nettyInbound),
                Jt808RequestCombiner.randomTraceId(),
                allocator,
                nettyInbound,
                XtreamInbound.Type.TCP,
                // 跳过 0x30316364 4字节
                payload.readerIndex(payload.readerIndex() + 4),
                remoteAddress,
                header,
                0,
                0
        );
    }

    Mono<Jt808Session> getTcpAttachmentSession(NettyInbound inbound, InetSocketAddress remoteAddress) {
        final String sessionId = generateSessionId(inbound);
        return this.sessionManager.getSessionById(sessionId)
                .map(session -> {
                    @SuppressWarnings("rawtype") Jt808Session jt808Session = (Jt808Session) session;
                    return jt808Session;
                })
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalStateException("Attachment session not found: " + remoteAddress))));
    }

    private String generateSessionId(NettyInbound inbound) {
        final String[] sessionIdHolder = new String[1];
        inbound.withConnection(connection -> sessionIdHolder[0] = this.sessionManager.sessionIdGenerator().generateTcpSessionId(connection.channel()));
        return sessionIdHolder[0];
    }

    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InetSocketAddress remoteAddress, XtreamRequest.Type type) {
        final Jt808Request request = this.requestDecoder.decode(Jt808ServerType.ATTACHMENT_SERVER, this.generateRequestId(nettyInbound), allocator, nettyInbound, XtreamInbound.Type.TCP, byteBuf, remoteAddress);
        this.requestLifecycleListener.afterRequestDecoded(nettyInbound, byteBuf, request);
        return request;
    }

    public XtreamExchange createTcpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf byteBuf, InetSocketAddress remoteAddress) {
        final XtreamRequest.Type type = XtreamRequest.Type.TCP;
        final XtreamRequest request = this.doCreateTcpRequest(allocator, nettyInbound, byteBuf, remoteAddress, type);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, remoteAddress);

        return new DefaultXtreamExchange(sessionManager, request, response);
    }
}

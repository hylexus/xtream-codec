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
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

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
    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InboundInfo inboundInfo) {
        // 码流消息
        if (JtProtocolUtils.isAttachmentRequest(payload)) {
            return this.handleStreamRequest(nettyInbound, nettyOutbound, payload, inboundInfo);
        }

        // 普通的指令消息
        final XtreamExchange exchange = this.createTcpExchange(allocator, nettyInbound, nettyOutbound, payload, inboundInfo);
        return doTcpExchange(exchange).doFinally(signalType -> {
            // ...
            exchange.request().release();
        });
    }

    protected Mono<Void> handleStreamRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InboundInfo inboundInfo) {
        return this.getTcpAttachmentSession(inboundInfo).flatMap(session -> {
            session.lastCommunicateTime(Instant.now());
            final Jt808Request jt808Request = simulateJt808Request(allocator, nettyInbound, payload, session, inboundInfo);
            final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, XtreamInbound.Type.TCP, inboundInfo.remoteAddress());
            final XtreamExchange simulatedExchange = new DefaultXtreamExchange(this.sessionManager, jt808Request, response);
            return this.attachmentHandler.handle(simulatedExchange);
        });
    }

    public Jt808Request simulateJt808Request(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf payload, Jt808Session session, InboundInfo inboundInfo) {
        final Jt808RequestHeader header = Jt808RequestHeader.newBuilder()
                .version(session.protocolVersion())
                .messageId(0x30316364)
                .messageBodyProps(new DefaultJt808MessageBodyProps(0))
                .terminalId(session.terminalId())
                .flowId(0)
                .build();

        return new DefaultJt808Request(
                session.protocolVersion().versionValue(),
                Jt808ServerType.ATTACHMENT_SERVER,
                this.generateRequestId(nettyInbound),
                Jt808RequestCombiner.randomTraceId(),
                allocator,
                nettyInbound,
                XtreamInbound.Type.TCP,
                // 跳过 0x30316364 4字节
                payload.readerIndex(payload.readerIndex() + 4),
                inboundInfo.channel(),
                inboundInfo.remoteAddress(),
                header,
                0,
                0
        );
    }

    Mono<Jt808Session> getTcpAttachmentSession(InboundInfo inboundInfo) {
        final String sessionId = this.sessionManager.sessionIdGenerator().generateTcpSessionId(inboundInfo.channel());
        return this.sessionManager.getSessionById(sessionId)
                .map(session -> {
                    @SuppressWarnings("rawtype") Jt808Session jt808Session = (Jt808Session) session;
                    return jt808Session;
                })
                .switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalStateException("Attachment session not found: " + inboundInfo.remoteAddress()))));
    }

    @Override
    protected XtreamRequest doCreateTcpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf byteBuf, InboundInfo inboundInfo, XtreamRequest.Type type) {
        final String requestId = this.generateRequestId(nettyInbound);
        final XtreamInbound.Type requestType = XtreamInbound.Type.TCP;
        final Channel channel = inboundInfo.channel();
        final Jt808Request request = this.requestDecoder.decode(Jt808ServerType.ATTACHMENT_SERVER, requestId, allocator, nettyInbound, requestType, byteBuf, channel, inboundInfo.remoteAddress());
        this.requestLifecycleListener.afterRequestDecoded(nettyInbound, byteBuf, request);
        return request;
    }

}

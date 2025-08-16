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

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808UdpDatagramPackageSplitter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.DefaultUdpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author hylexus
 */
public class Jt808InstructionServerUdpHandlerAdapter extends DefaultUdpXtreamNettyHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Jt808InstructionServerUdpHandlerAdapter.class);
    protected final Jt808UdpDatagramPackageSplitter splitter;
    protected final Jt808RequestDecoder requestDecoder;
    protected final Jt808RequestLifecycleListener requestLifecycleListener;

    public Jt808InstructionServerUdpHandlerAdapter(ByteBufAllocator allocator, XtreamSessionManager<? extends XtreamSession> sessionManager, XtreamHandler xtreamHandler, Jt808UdpDatagramPackageSplitter splitter, Jt808RequestDecoder requestDecoder, Jt808RequestLifecycleListener requestLifecycleListener) {
        super(allocator, sessionManager, xtreamHandler);
        this.splitter = splitter;
        this.requestDecoder = requestDecoder;
        this.requestLifecycleListener = requestLifecycleListener;
    }

    protected Mono<Void> handleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, DatagramPacket datagramPacket) {
        final InetSocketAddress remoteAddress = datagramPacket.sender();
        // 一个 UDP 包可能包含多个 JT808 (完整)报文
        final List<ByteBuf> byteBufList = this.splitter.split(datagramPacket.content());

        // 只有一个包(绝大多数场景)
        if (byteBufList.size() == 1) {
            return this.handleSingleRequest(nettyInbound, nettyOutbound, byteBufList.getFirst(), remoteAddress);
        }

        // 每个(完整)包都触发一次处理逻辑
        return Flux.fromIterable(byteBufList).flatMap(byteBuf -> {
            // ...
            return this.handleSingleRequest(nettyInbound, nettyOutbound, byteBuf, remoteAddress);
        }).then();
    }

    protected Mono<Void> handleSingleRequest(NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        final XtreamExchange exchange = this.createUdpExchange(allocator, nettyInbound, nettyOutbound, payload, remoteAddress);
        return this.doUdpExchange(exchange).doFinally(signalType -> {
            XtreamBytes.releaseBuf(payload);
            exchange.request().release();
        });
    }

    public XtreamExchange createUdpExchange(ByteBufAllocator allocator, NettyInbound nettyInbound, NettyOutbound nettyOutbound, ByteBuf payload, InetSocketAddress remoteAddress) {
        final XtreamRequest.Type type = XtreamRequest.Type.UDP;
        final XtreamRequest request = this.doCreateUdpRequest(allocator, nettyInbound, payload, remoteAddress, type);
        final DefaultXtreamResponse response = new DefaultXtreamResponse(allocator, nettyOutbound, type, remoteAddress);

        return new DefaultXtreamExchange(sessionManager, request, response);
    }

    protected XtreamRequest doCreateUdpRequest(ByteBufAllocator allocator, NettyInbound nettyInbound, ByteBuf payload, InetSocketAddress remoteAddress, XtreamRequest.Type type) {
        final Jt808Request request = this.requestDecoder.decode(Jt808ServerType.INSTRUCTION_SERVER, this.generateRequestId(nettyInbound), allocator, nettyInbound, XtreamInbound.Type.UDP, payload, remoteAddress);
        this.requestLifecycleListener.afterRequestDecoded(nettyInbound, payload, request);
        return request;
    }

}

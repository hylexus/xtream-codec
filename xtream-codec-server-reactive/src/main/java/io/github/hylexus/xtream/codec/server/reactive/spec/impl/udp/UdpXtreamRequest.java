/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamRequest;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public class UdpXtreamRequest extends AbstractXtreamRequest {

    private final InetSocketAddress remoteAddress;

    public UdpXtreamRequest(ByteBufAllocator allocator, NettyInbound delegate, Mono<XtreamSession> session, DatagramPacket datagramPacket) {
        super(allocator, delegate, session, datagramPacket.content());
        this.remoteAddress = datagramPacket.sender();
    }

    @Override
    public Type type() {
        return Type.UDP;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public XtreamRequestBuilder mutate() {
        return new DefaultUdpXtreamRequestBuilder(this);
    }

}

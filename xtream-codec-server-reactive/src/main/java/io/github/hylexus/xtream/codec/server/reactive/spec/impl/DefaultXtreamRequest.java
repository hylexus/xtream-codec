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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import reactor.netty.NettyInbound;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author hylexus
 */
public class DefaultXtreamRequest implements XtreamRequest {
    protected final ByteBufAllocator allocator;
    protected final NettyInbound delegate;
    protected final ByteBuf payload;
    protected String requestId;
    protected final Type type;
    protected InetSocketAddress remoteAddress;
    protected final Channel channel;

    public DefaultXtreamRequest(String requestId, ByteBufAllocator allocator, NettyInbound delegate, Type type, ByteBuf payload, Channel channel, InetSocketAddress remoteAddress) {
        this.requestId = requestId;
        this.allocator = allocator;
        this.delegate = delegate;
        this.payload = payload;
        this.type = type;
        this.remoteAddress = remoteAddress;
        this.channel = channel;
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public ByteBufAllocator bufferFactory() {
        return this.allocator;
    }

    @Override
    public NettyInbound underlyingInbound() {
        return this.delegate;
    }

    @Override
    public Channel underlyingChannel() {
        return this.channel;
    }

    @Override
    public String requestId() {
        return this.requestId;
    }

    @Override
    public ByteBuf payload() {
        return this.payload;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public Map<String, Object> attributes() {
        return Map.of();
    }

    @Override
    public XtreamRequestBuilder mutate() {
        return new DefaultXtreamRequestBuilder(this);
    }

    @Override
    public String toString() {
        return "DefaultXtreamRequest{"
               + "type=" + type()
               + ", requestId=" + requestId()
               + ", remoteAddress=" + remoteAddress()
               + ", payload=" + (payload.refCnt() > 0 ? FormatUtils.toHexString(payload) : "<FREED>")
               + '}';
    }

}

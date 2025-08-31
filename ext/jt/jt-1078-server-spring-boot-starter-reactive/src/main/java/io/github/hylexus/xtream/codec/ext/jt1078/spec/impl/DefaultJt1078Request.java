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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHeader;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import reactor.netty.NettyInbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public class DefaultJt1078Request extends DefaultXtreamRequest implements Jt1078Request {
    private final Jt1078RequestHeader header;

    public DefaultJt1078Request(
            String requestId, ByteBufAllocator allocator, NettyInbound nettyInbound, Type type, Channel channel, InetSocketAddress remoteAddress,
            Jt1078RequestHeader header, ByteBuf payload) {
        super(XtreamField.DEFAULT_VERSION, requestId, allocator, nettyInbound, type, payload, channel, remoteAddress);
        this.header = header;
    }

    @Override
    public Jt1078RequestHeader header() {
        return this.header;
    }

    /**
     * @deprecated JT/T 1078 中没必要实现这个方法
     */
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public Jt1078RequestBuilder mutate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "DefaultJt1078Request{" + "header=" + header + '}';
    }

}

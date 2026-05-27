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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import reactor.netty.NettyInbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public interface XtreamInbound {

    enum Type {
        TCP,
        UDP
    }

    Type type();

    ByteBufAllocator bufferFactory();

    NettyInbound underlyingInbound();

    Channel underlyingChannel();

    /**
     * 同一个 ”网络包“ 的请求和响应，该值应该确保一致（即使是在中途重新包装或修改了 {@link XtreamRequest} 对象）。
     */
    String requestId();

    InetSocketAddress remoteAddress();

    ByteBuf payload();

}

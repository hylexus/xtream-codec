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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamHandlerAdapterBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamHandlerAdapterBuilder;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.util.UUID;
import java.util.function.BiFunction;

/**
 * @author hylexus
 */
public interface XtreamNettyHandlerAdapter extends BiFunction<NettyInbound, NettyOutbound, Publisher<Void>> {
    static TcpXtreamHandlerAdapterBuilder newTcpBuilder() {
        return newTcpBuilder(ByteBufAllocator.DEFAULT);
    }

    static TcpXtreamHandlerAdapterBuilder newTcpBuilder(ByteBufAllocator allocator) {
        return new TcpXtreamHandlerAdapterBuilder(allocator);
    }

    static UdpXtreamHandlerAdapterBuilder newUdpBuilder() {
        return newUdpBuilder(ByteBufAllocator.DEFAULT);
    }

    static UdpXtreamHandlerAdapterBuilder newUdpBuilder(ByteBufAllocator allocator) {
        return new UdpXtreamHandlerAdapterBuilder(allocator);
    }

    @Override
    Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound);

    default void shutdown() {
    }

    default String generateRequestId(NettyInbound ignored) {
        return UUID.randomUUID().toString().replace("-", "");
    }

}

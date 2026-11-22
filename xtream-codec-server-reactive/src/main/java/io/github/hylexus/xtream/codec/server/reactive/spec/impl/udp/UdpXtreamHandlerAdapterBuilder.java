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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp;

import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamSessionManager;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * @author hylexus
 */
public class UdpXtreamHandlerAdapterBuilder extends AbstractXtreamHandlerAdapterBuilderUdp<UdpXtreamHandlerAdapterBuilder> {

    public UdpXtreamHandlerAdapterBuilder(ByteBufAllocator allocator) {
        super(allocator);
    }

    @Override
    public UdpXtreamNettyHandlerAdapter build() {
        final XtreamHandler handler = this.createRequestHandler();
        final var manager = this.sessionManager;
        if (manager != null) {
            return new DefaultUdpXtreamNettyHandlerAdapter(this.byteBufAllocator, manager, handler);
        }
        final ByteBufAllocator allocator = this.byteBufAllocator;
        // 每个绑定的通道有独立的默认会话管理器，处理订阅结束时关闭该管理器。
        return (NettyInbound inbound, NettyOutbound outbound) -> Flux.using(
                () -> newDefaultSessionManager(true),
                (DefaultXtreamSessionManager defaultManager) -> new DefaultUdpXtreamNettyHandlerAdapter(allocator, defaultManager, handler).apply(inbound, outbound),
                DefaultXtreamSessionManager::shutdown
        );
    }

    @Override
    protected XtreamHandler createRequestHandler() {
        // 构建阶段只装配处理链，默认管理器等通道开始接收数据时再创建。
        return this.createHandlerChain();
    }

}

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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamExchange;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * @author hylexus
 */
public class DefaultTcpXtreamNettyHandlerAdapter implements XtreamNettyHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefaultTcpXtreamNettyHandlerAdapter.class);
    private final XtreamHandler xtreamHandler;
    private final ByteBufAllocator allocator;

    public DefaultTcpXtreamNettyHandlerAdapter(XtreamHandler xtreamHandler, ByteBufAllocator allocator) {
        this.xtreamHandler = xtreamHandler;
        this.allocator = allocator;
        log.info("DefaultTcpXtreamNettyHandlerAdapter initialized");
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound.receive().flatMap(byteBuf -> {
            if (byteBuf.readableBytes() <= 0) {
                return Mono.empty();
            }

            final TcpXtreamSession session = new TcpXtreamSession();
            final XtreamExchange exchange = new DefaultXtreamExchange(
                    new TcpXtreamRequest(allocator, nettyInbound, Mono.just(session), byteBuf),
                    new TcpXtreamResponse(allocator, nettyOutbound),
                    session
            );
            return xtreamHandler
                    .handle(exchange)
                    .doOnError(Throwable.class, throwable -> {
                        // ...
                        log.error(throwable.getMessage(), throwable);
                    });
        });
    }
}

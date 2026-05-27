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

package io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.TcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

public class Jt1078ServerTcpHandlerAdapter implements TcpXtreamNettyHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Jt1078ServerTcpHandlerAdapter.class);
    protected final ByteBufAllocator allocator;
    protected final Scheduler scheduler;
    protected final Jt1078SessionManager sessionManager;
    protected final Jt1078RequestHandler jt1078RequestHandler;

    public Jt1078ServerTcpHandlerAdapter(ByteBufAllocator allocator, Scheduler scheduler, Jt1078SessionManager sessionManager, Jt1078RequestHandler jt1078RequestHandler) {
        this.allocator = allocator;
        this.scheduler = scheduler;
        this.sessionManager = sessionManager;
        this.jt1078RequestHandler = jt1078RequestHandler;
    }

    /**
     * @see io.github.hylexus.xtream.codec.ext.jt1078.codec.Jt1078ByteToMessageDecoder
     */
    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound.receiveObject()
                .map(Jt1078Request.class::cast)
                // .onBackpressureBuffer(1024,(buffer)->{
                //     log.error("Buffer Overflow");
                // }, BufferOverflowStrategy.DROP_OLDEST)
                // .doOnNext(it->it.body().retain())
                .publishOn(scheduler)
                .flatMap((Jt1078Request request) -> {
                    // ...
                    return this.jt1078RequestHandler.handleRequest(request)
                            .doFinally(s -> request.release());
                })
                .onErrorResume(throwable -> {
                    log.error("Unexpected Error", throwable);
                    return Mono.empty();
                });
    }

}

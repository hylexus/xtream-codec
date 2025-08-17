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

package io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler;

import io.github.hylexus.xtream.codec.ext.jt1078.codec.Jt1078ByteToMessageDecoderUdp;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

public class Jt1078ServerUdpHandlerAdapter implements UdpXtreamNettyHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Jt1078ServerUdpHandlerAdapter.class);
    private final Scheduler scheduler;
    private final Jt1078ByteToMessageDecoderUdp byteToMessageDecoder;
    private final Jt1078RequestHandler handler;

    public Jt1078ServerUdpHandlerAdapter(ByteBufAllocator allocator, Scheduler scheduler, Jt1078SimConverter simConverter, Jt1078SessionManager sessionManager, Jt1078RequestHandler handler) {
        this.scheduler = scheduler;
        this.handler = handler;
        this.byteToMessageDecoder = new Jt1078ByteToMessageDecoderUdp(
                allocator,
                simConverter,
                sessionManager
        );
    }

    @Override
    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        return nettyInbound
                .receiveObject()
                .map(obj -> {
                    final DatagramPacket datagramPacket = (DatagramPacket) obj;
                    datagramPacket.retain();
                    return datagramPacket;
                })
                .publishOn(scheduler)
                .flatMap(datagramPacket -> {
                    final Jt1078Request request;
                    try {
                        final InboundInfo inboundInfo = InboundInfo.forUdp(nettyInbound, datagramPacket);
                        request = this.byteToMessageDecoder.decode(nettyInbound, nettyOutbound, datagramPacket, inboundInfo);
                    } catch (Throwable throwable) {
                        log.error("Unexpected Error", throwable);
                        return Mono.error(throwable);
                    }
                    // log.info("{}", request);
                    return this.handler.handleRequest(request)
                            .doFinally(signalType -> {
                                // ...
                                request.release();
                            })
                            .onErrorResume(throwable -> {
                                log.error("Unexpected Error", throwable);
                                return Mono.empty();
                            });
                });
    }

}

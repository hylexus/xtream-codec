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

package io.github.hylexus.xtream.debug.codec.server.reactive.tcp;


import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamSessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.DefaultTcpXtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class XtreamServerReactiveTcpDebugApp01 {

    private static final Logger log = LoggerFactory.getLogger(XtreamServerReactiveTcpDebugApp01.class);

    /**
     * 测试报文:
     * <p>
     * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
     */
    public static void main(String[] args) {
        // FIXME 如果你不了解 ResourceLeakDetector 是做什么的, 请务必注释掉下面这行代码!!!
        io.netty.util.ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.PARANOID);
        TcpServer.create()
                .host("0.0.0.0")
                .port(8888)
                .doOnChannelInit((observer, channel, remoteAddress) -> {
                    channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, true, Unpooled.copiedBuffer(new byte[]{0x7e})));
                    log.info("doOnChannelInit [PipelineCreated] {}", channel);
                })
                .handle(
                        new DefaultTcpXtreamNettyHandlerAdapter(
                                ByteBufAllocator.DEFAULT,
                                new DefaultXtreamSessionManager(false, null, new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator()),
                                (XtreamExchange exchange) -> {
                                    final XtreamRequest request = exchange.request();
                                    final XtreamResponse response = exchange.response();
                                    log.info("===> RequestId: {}, payload: {}", request.requestId(), FormatUtils.toHexString(request.payload()));
                                    final String respMessage = "Hello! ServerTime: " + LocalDateTime.now();
                                    return response.writeWith(Mono.just(Unpooled.wrappedBuffer(respMessage.getBytes(StandardCharsets.UTF_8))));
                                }
                        )
                )
                .bindNow()
                .onDispose()
                .block();
    }
}

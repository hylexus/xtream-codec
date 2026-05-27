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

package io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handler;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.SimpleXtreamRequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class DemoTcpXtreamHandler implements SimpleXtreamRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoTcpXtreamHandler.class);

    @Override
    public Mono<Void> handle(XtreamExchange exchange) {
        log.info("{}", exchange.request().remoteAddress());
        final ByteBuf byteBuf = exchange.request().payload();
        final CompositeByteBuf compositeByteBuf = exchange.bufferFactory().compositeBuffer(2);
        compositeByteBuf.addComponents(true, byteBuf.copy());
        compositeByteBuf.addComponents(true, Unpooled.wrappedBuffer(new byte[]{1, 1, 1, 1}));
        return exchange.response()
                .writeWith(Mono.just(compositeByteBuf));
    }
}

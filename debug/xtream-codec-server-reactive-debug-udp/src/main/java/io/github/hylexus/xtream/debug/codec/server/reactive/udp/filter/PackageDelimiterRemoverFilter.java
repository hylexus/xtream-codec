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

package io.github.hylexus.xtream.debug.codec.server.reactive.udp.filter;

import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public class PackageDelimiterRemoverFilter implements UdpXtreamFilter {

    @Override
    public Mono<Void> filter(XtreamExchange exchange, XtreamFilterChain chain) {
        final ByteBuf originalPayload = exchange.request().payload();

        // 仅仅是个示例(808报文)
        // 以 0x7e 开头 && 以 0x7e 结尾 ==> 去掉开头和结尾的 0x7e(示例项目的解码器不需要分隔符)
        if (originalPayload.readableBytes() > 2
                && originalPayload.getByte(0) == 0x7e
                && originalPayload.getByte(originalPayload.readableBytes() - 1) == 0x7e) {

            final XtreamExchange mutatedExchange = exchange.mutate().request(requestBuilder -> {
                final ByteBuf newPayload = originalPayload.slice(1, originalPayload.readableBytes() - 2);
                requestBuilder.payload(newPayload, false);
            }).build();

            return chain.filter(mutatedExchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int order() {
        return -1;
    }
}

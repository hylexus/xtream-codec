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

package io.github.hylexus.xtream.debug.codec.server.reactive.udp.handler;

import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.github.hylexus.xtream.debug.codec.server.reactive.udp.message.DemoLocationMsg01;
import io.github.hylexus.xtream.debug.codec.server.reactive.udp.message.DemoLocationMsg02;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertNotSame;
import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertSame;

/**
 * @author hylexus
 */
@XtreamRequestHandler
// @XtreamResponseBody
public class DemoUdpXtreamHandler2 {

    private static final Logger log = LoggerFactory.getLogger(DemoUdpXtreamHandler2.class);

    public DemoUdpXtreamHandler2() {
    }

    @MyUdpRequestRouter
    @XtreamResponseBody
    public Flux<DemoLocationMsg01> handle(
            // public ByteBuf handle(
            XtreamExchange exchange,
            @XtreamRequestBody DemoLocationMsg01 msg01,
            @XtreamRequestBody DemoLocationMsg02 msg02,
            @XtreamRequestBody ByteBuf msg03,
            @XtreamRequestBody(bufferAsSlice = false) ByteBuf msg04,
            XtreamSession session,
            XtreamRequest request,
            DefaultXtreamRequest udpRequest,
            XtreamResponse response,
            DefaultXtreamResponse udpResponse) {

        assertNotSame(exchange.request().payload(), msg03);
        assertSame(exchange.request().payload(), msg04);
        assertSame(exchange.request(), request);
        assertSame(exchange.request(), udpRequest);
        assertSame(exchange.response(), response);
        assertSame(exchange.response(), udpResponse);

        final DemoLocationMsg01 msg = new DemoLocationMsg01();
        msg.setMsgId(0x01);
        msg.setMsgBodyProps(222);
        msg.setProtocolVersion((short) 1);
        msg.setTerminalId("6666");
        // return Flux.error(new RuntimeException("..."));
        // return ByteBufAllocator.DEFAULT.buffer().writeBytes(new byte[]{1,1,2,2});
        return Flux.just(msg).concatWith(Flux.just(msg));
    }
}

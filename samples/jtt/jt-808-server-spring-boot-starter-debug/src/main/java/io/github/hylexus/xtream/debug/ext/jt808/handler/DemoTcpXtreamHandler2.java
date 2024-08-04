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

package io.github.hylexus.xtream.debug.ext.jt808.handler;

import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DefaultXtreamResponse;
import io.github.hylexus.xtream.debug.ext.jt808.message.DemoLocationMsg01;
import io.github.hylexus.xtream.debug.ext.jt808.message.DemoLocationMsg02;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static io.github.hylexus.xtream.codec.common.utils.XtreamAssertions.assertSame;

/**
 * @author hylexus
 */
@Component
@Jt808RequestHandler
public class DemoTcpXtreamHandler2 {

    private static final Logger log = LoggerFactory.getLogger(DemoTcpXtreamHandler2.class);

    public DemoTcpXtreamHandler2() {
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
    public Flux<DemoLocationMsg01> handle(
            // public ByteBuf handle(
            XtreamExchange exchange,
            @XtreamRequestBody DemoLocationMsg01 msg01,
            @XtreamRequestBody DemoLocationMsg02 msg02,
            @XtreamRequestBody ByteBuf msg03,
            XtreamSession session,
            XtreamRequest request,
            DefaultXtreamRequest tcpRequest,
            XtreamResponse response,
            DefaultXtreamResponse tcpResponse) {

        assertSame(exchange.request().payload(), msg03);
        assertSame(exchange.request(), request);
        assertSame(exchange.request(), tcpRequest);
        assertSame(exchange.response(), response);
        assertSame(exchange.response(), tcpResponse);

        final DemoLocationMsg01 msg = new DemoLocationMsg01();
        msg.setMsgId(0x01);
        msg.setMsgBodyProps(222);
        msg.setProtocolVersion((byte) 1);
        msg.setTerminalId("6666");
        // return Flux.error(new RuntimeException("..."));
        // return ByteBufAllocator.DEFAULT.buffer().writeBytes(new byte[]{1,1,2,2});
        return Flux.just(msg).concatWith(Flux.just(msg));
    }
}

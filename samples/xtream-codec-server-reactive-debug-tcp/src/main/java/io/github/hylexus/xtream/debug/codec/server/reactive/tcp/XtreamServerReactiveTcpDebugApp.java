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

package io.github.hylexus.xtream.debug.codec.server.reactive.tcp;


import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.LoggingXtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.LoggingXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamServerBuilder;
import io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handlermapping.DemoTcpXtreamHandlerMapping;
import io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handlermapping.DemoTcpXtreamHandlerMapping2;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XtreamServerReactiveTcpDebugApp {

    private static final Logger log = LoggerFactory.getLogger(XtreamServerReactiveTcpDebugApp.class);

    /**
     * 测试报文:
     * <p>
     * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
     */
    public static void main(String[] args) {
        XtreamServerBuilder.newTcpServerBuilder()
                .addServerCustomizer(server -> server.host("localhost")
                        .port(8888)
                        .doOnConnection(conn -> log.info("doOnConnection {}", conn))
                        .doOnChannelInit((observer, channel, remoteAddress) -> {
                            log.info("doOnChannelInit {}", channel);
                            channel.pipeline().addFirst(
                                    // stripDelimiter=true
                                    new DelimiterBasedFrameDecoder(1024, true, Unpooled.copiedBuffer(new byte[]{0x7e}))
                            );
                        })
                )
                .addServerCustomizer(server -> server.handle(
                        XtreamNettyHandlerAdapter.newTcpBuilder()
                                .addHandlerMappings(new DemoTcpXtreamHandlerMapping(), new DemoTcpXtreamHandlerMapping2())
                                .enableBuiltinHandlerAdapters(EntityCodec.DEFAULT)
                                .enableBuiltinHandlerResultHandlers(EntityCodec.DEFAULT)
                                .addFilter(new LoggingXtreamFilter())
                                .addExceptionHandler(new LoggingXtreamRequestExceptionHandler())
                                .build()
                ))
                .build()
                .start();
    }
}

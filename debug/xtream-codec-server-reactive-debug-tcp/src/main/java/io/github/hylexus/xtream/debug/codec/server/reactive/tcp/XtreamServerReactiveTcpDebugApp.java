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
        // FIXME 如果你不了解 ResourceLeakDetector 是做什么的,请务必注释掉下面这行代码!!!
        io.netty.util.ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.PARANOID);
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
                .build("demo")
                .start();
    }
}

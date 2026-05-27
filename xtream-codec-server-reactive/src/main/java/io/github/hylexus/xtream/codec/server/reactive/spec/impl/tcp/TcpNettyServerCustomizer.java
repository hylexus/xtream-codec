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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.NettyServerCustomizer;
import io.netty.channel.ChannelOption;
import reactor.netty.tcp.TcpServer;
import reactor.netty.tcp.TcpServerConfig;

/**
 * @author hylexus
 */
public interface TcpNettyServerCustomizer extends NettyServerCustomizer<TcpServer, TcpServerConfig> {

    @Override
    TcpServer customize(TcpServer server);

    class Default implements TcpNettyServerCustomizer {

        @Override
        public TcpServer customize(TcpServer server) {
            return server
                    .host("0.0.0.0")
                    .port(3927)
                    .option(ChannelOption.SO_BACKLOG, 2048)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        @Override
        public int order() {
            return OrderedComponent.HIGHEST_PRECEDENCE;
        }
    }

}

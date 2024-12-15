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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.NettyServerCustomizer;
import reactor.netty.udp.UdpServer;
import reactor.netty.udp.UdpServerConfig;

/**
 * @author hylexus
 */
public interface UdpNettyServerCustomizer extends NettyServerCustomizer<UdpServer, UdpServerConfig> {

    @Override
    UdpServer customize(UdpServer server);

    class Default implements UdpNettyServerCustomizer {

        @Override
        public UdpServer customize(UdpServer server) {
            return server.host("0.0.0.0").port(3721);
        }

        @Override
        public int order() {
            return OrderedComponent.HIGHEST_PRECEDENCE;
        }
    }

}

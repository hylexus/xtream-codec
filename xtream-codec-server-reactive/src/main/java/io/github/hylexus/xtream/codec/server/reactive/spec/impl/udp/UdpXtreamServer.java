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
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamServer;
import reactor.netty.DisposableChannel;
import reactor.netty.udp.UdpServer;
import reactor.netty.udp.UdpServerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hylexus
 */
public class UdpXtreamServer extends AbstractXtreamServer<UdpServer, UdpServerConfig, UdpNettyServerCustomizer> {
    private final List<UdpNettyServerCustomizer> nettyServerCustomizers;

    public UdpXtreamServer() {
        this(List.of());
    }

    public UdpXtreamServer(List<UdpNettyServerCustomizer> nettyServerCustomizers) {
        final List<UdpNettyServerCustomizer> customizers = new ArrayList<>(nettyServerCustomizers);
        customizers.add(new UdpNettyServerCustomizer.Default());
        this.nettyServerCustomizers = OrderedComponent.sort(customizers);
    }

    @Override
    protected DisposableChannel initServer() {
        UdpServer udpServer = UdpServer.create();
        for (final UdpNettyServerCustomizer customizer : this.nettyServerCustomizers) {
            udpServer = customizer.customize(udpServer);
        }
        return udpServer.bindNow();
    }

    @Override
    protected List<UdpNettyServerCustomizer> getCustomizers() {
        return this.nettyServerCustomizers;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.UDP;
    }
}

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
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamServer;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactor.netty.tcp.TcpServerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hylexus
 */
public class TcpXtreamServer extends AbstractXtreamServer<TcpServer, TcpServerConfig, TcpNettyServerCustomizer> {

    private final List<TcpNettyServerCustomizer> nettyServerCustomizers;

    public TcpXtreamServer(String name, List<TcpNettyServerCustomizer> nettyServerCustomizers) {
        super(name);
        final List<TcpNettyServerCustomizer> customizers = new ArrayList<>(nettyServerCustomizers);
        customizers.add(new TcpNettyServerCustomizer.Default());
        this.nettyServerCustomizers = OrderedComponent.sort(customizers);
    }

    @Override
    protected DisposableServer initServer() {
        TcpServer tcpServer = TcpServer.create();
        for (final NettyServerCustomizer<TcpServer, TcpServerConfig> customizer : this.nettyServerCustomizers) {
            tcpServer = customizer.customize(tcpServer);
        }
        return tcpServer.bindNow();
    }

    @Override
    protected List<TcpNettyServerCustomizer> getCustomizers() {
        return this.nettyServerCustomizers;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.TCP;
    }
}

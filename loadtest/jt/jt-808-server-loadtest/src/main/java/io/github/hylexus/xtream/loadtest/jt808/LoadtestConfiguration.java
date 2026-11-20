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

package io.github.hylexus.xtream.loadtest.jt808;

import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpNettyServerCustomizer;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LoadtestConfiguration {
    private static final Logger log = LoggerFactory.getLogger(LoadtestConfiguration.class);

    @Bean
    TcpNettyServerCustomizer loadtestTcpCustomizer(LoadtestProperties properties, LoadtestMetrics metrics) {
        return server -> server
                .option(ChannelOption.SO_BACKLOG, properties.tcpBacklog())
                .doOnConnection(metrics::onTcpConnection)
                .doOnBound(bound -> {
                    // ...
                    log.info("TCP 压测监听: address={}, channel={}, requestedBacklog={}",
                            bound.address(), bound.channel().getClass().getSimpleName(), properties.tcpBacklog());
                });
    }

}

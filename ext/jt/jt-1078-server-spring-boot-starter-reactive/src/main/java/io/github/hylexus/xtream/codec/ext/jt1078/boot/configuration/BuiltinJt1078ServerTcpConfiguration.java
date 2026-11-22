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

package io.github.hylexus.xtream.codec.ext.jt1078.boot.configuration;

import io.github.hylexus.xtream.codec.common.utils.BufferFactoryHolder;
import io.github.hylexus.xtream.codec.ext.jt1078.boot.condition.ConditionalOnJt1078Server;
import io.github.hylexus.xtream.codec.ext.jt1078.boot.properties.XtreamJt1078ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler.Jt1078ServerTcpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Servers;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.resources.Jt1078XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.TcpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultTcpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.TcpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.XtreamNettyResourceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;

import static io.github.hylexus.xtream.codec.ext.jt1078.utils.Jt1078Constants.*;

@ConditionalOnJt1078Server(protocolType = ConditionalOnJt1078Server.ProtocolType.TCP)
public class BuiltinJt1078ServerTcpConfiguration {

    @Bean(value = BEAN_NAME_JT1078_TCP_XTREAM_NETTY_HANDLER_ADAPTER, destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_TCP_XTREAM_NETTY_HANDLER_ADAPTER)
    TcpXtreamNettyHandlerAdapter tcpXtreamNettyHandlerAdapter(
            BufferFactoryHolder bufferFactoryHolder,
            Jt1078XtreamSchedulerRegistry schedulerRegistry,
            Jt1078SessionManager sessionManager,
            Jt1078RequestPublisher requestPublisher) {

        final Scheduler scheduler = schedulerRegistry.audioVideoCodecScheduler();
        final Jt1078RequestHandler handler = new DefaultJt1078RequestHandler(requestPublisher);
        return new Jt1078ServerTcpHandlerAdapter(
                bufferFactoryHolder.getAllocator(),
                scheduler,
                sessionManager,
                handler
        );
    }

    @Bean(value = BEAN_NAME_JT1078_TCP_XTREAM_NETTY_RESOURCE_FACTORY, destroyMethod = "dispose")
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_TCP_XTREAM_NETTY_RESOURCE_FACTORY)
    TcpXtreamNettyResourceFactory tcpXtreamNettyResourceFactory(XtreamJt1078ServerProperties serverProperties) {
        final XtreamJt1078ServerProperties.TcpLoopResourcesProperty loopResources = serverProperties.getTcpServer().getLoopResources();
        return new DefaultTcpXtreamNettyResourceFactory(new XtreamNettyResourceFactory.LoopResourcesProperty(
                loopResources.getThreadNamePrefix(),
                loopResources.getSelectCount(),
                loopResources.getWorkerCount(),
                loopResources.isDaemon(),
                loopResources.isColocate(),
                loopResources.isPreferNative()
        ));
    }

    @Bean(value = BEAN_NAME_JT1078_TCP_XTREAM_SERVER, destroyMethod = "stop")
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_TCP_XTREAM_SERVER)
    TcpXtreamServer tcpXtreamServer(
            @Qualifier(BEAN_NAME_JT1078_TCP_XTREAM_NETTY_HANDLER_ADAPTER) TcpXtreamNettyHandlerAdapter tcpXtreamNettyHandlerAdapter,
            @Qualifier(BEAN_NAME_JT1078_TCP_XTREAM_NETTY_RESOURCE_FACTORY) TcpXtreamNettyResourceFactory resourceFactory,
            ObjectProvider<TcpNettyServerCustomizer> customizers,
            Jt1078SessionManager sessionManager,
            XtreamJt1078ServerProperties serverProperties,
            Jt1078SimConverter jt1078SimConverter) {

        final XtreamJt1078ServerProperties.TcpServerProps tcpServer = serverProperties.getTcpServer();
        return Jt1078Servers.tcp()
                .bind(tcpServer.getHost(), tcpServer.getPort())
                .handlerAdapter(tcpXtreamNettyHandlerAdapter)
                .sessionManager(sessionManager)
                .simConverter(jt1078SimConverter)
                .sessionIdleStateChecker(tcpServer.getSessionIdleStateChecker())
                .maxFrameLength(tcpServer.getMaxFrameLength())
                .resourceFactory(resourceFactory)
                .customizers(customizers.stream().toList())
                .build();
    }

}

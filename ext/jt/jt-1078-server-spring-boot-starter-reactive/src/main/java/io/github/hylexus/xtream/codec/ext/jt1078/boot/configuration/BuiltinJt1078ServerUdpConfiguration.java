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
import io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler.Jt1078ServerUdpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.resources.Jt1078XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamServerBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultUdpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.UdpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.XtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.utils.BuiltinConfigurationUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;

import static io.github.hylexus.xtream.codec.ext.jt1078.utils.Jt1078Constants.*;

@ConditionalOnJt1078Server(protocolType = ConditionalOnJt1078Server.ProtocolType.UDP)
public class BuiltinJt1078ServerUdpConfiguration {

    @Bean(value = BEAN_NAME_JT1078_UDP_XTREAM_NETTY_HANDLER_ADAPTER, destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_UDP_XTREAM_NETTY_HANDLER_ADAPTER)
    UdpXtreamNettyHandlerAdapter udpXtreamNettyHandlerAdapter(
            BufferFactoryHolder bufferFactoryHolder,
            Jt1078XtreamSchedulerRegistry schedulerRegistry,
            Jt1078SimConverter simConverter,
            Jt1078SessionManager sessionManager,
            Jt1078RequestPublisher requestPublisher) {

        final Scheduler scheduler = schedulerRegistry.audioVideoCodecScheduler();
        final Jt1078RequestHandler handler = new DefaultJt1078RequestHandler(requestPublisher);
        return new Jt1078ServerUdpHandlerAdapter(
                bufferFactoryHolder.getAllocator(),
                scheduler,
                simConverter,
                sessionManager,
                handler
        );
    }

    @Bean(BEAN_NAME_JT1078_UDP_XTREAM_NETTY_RESOURCE_FACTORY)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_UDP_XTREAM_NETTY_RESOURCE_FACTORY)
    UdpXtreamNettyResourceFactory udpXtreamNettyResourceFactory(XtreamJt1078ServerProperties serverProperties) {
        final XtreamJt1078ServerProperties.UdpLoopResourcesProperty loopResources = serverProperties.getUdpServer().getLoopResources();
        return new DefaultUdpXtreamNettyResourceFactory(new XtreamNettyResourceFactory.LoopResourcesProperty(
                loopResources.getThreadNamePrefix(),
                loopResources.getSelectCount(),
                loopResources.getWorkerCount(),
                loopResources.isDaemon(),
                loopResources.isColocate(),
                loopResources.isPreferNative()
        ));
    }

    @Bean(BEAN_NAME_JT1078_UDP_XTREAM_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT1078_UDP_XTREAM_SERVER)
    UdpXtreamServer tcpXtreamServer(
            @Qualifier(BEAN_NAME_JT1078_UDP_XTREAM_NETTY_HANDLER_ADAPTER) UdpXtreamNettyHandlerAdapter nettyHandlerAdapter,
            @Qualifier(BEAN_NAME_JT1078_UDP_XTREAM_NETTY_RESOURCE_FACTORY) UdpXtreamNettyResourceFactory resourceFactory,
            ObjectProvider<UdpNettyServerCustomizer> customizers,
            Jt1078SessionManager sessionManager,
            XtreamJt1078ServerProperties serverProperties) {
        final XtreamJt1078ServerProperties.UdpServerProps udpServer = serverProperties.getUdpServer();
        return XtreamServerBuilder.newUdpServerBuilder()
                // 默认 host和 port(用户自定义配置可以再次覆盖默认配置)
                .addServerCustomizer(BuiltinConfigurationUtils.defaultUdpBasicConfigurer(udpServer.getHost(), udpServer.getPort()))
                // handler
                .addServerCustomizer(server -> server.handle(nettyHandlerAdapter))
                // loopResources
                .addServerCustomizer(server -> server.runOn(resourceFactory.loopResources(), resourceFactory.preferNative()))
                // 用户自定义配置
                .addServerCustomizers(customizers.stream().toList())
                .build("JT/T-1078");

    }
}

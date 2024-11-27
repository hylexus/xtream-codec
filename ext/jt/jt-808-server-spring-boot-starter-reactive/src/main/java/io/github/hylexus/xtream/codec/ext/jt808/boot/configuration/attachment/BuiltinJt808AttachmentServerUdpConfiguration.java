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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.attachment;

import io.github.hylexus.xtream.codec.common.utils.BufferFactoryHolder;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808UdpDatagramPackageSplitter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808AttachmentServerExchangeCreator;
import io.github.hylexus.xtream.codec.ext.jt808.utils.BuiltinConfigurationUtils;
import io.github.hylexus.xtream.codec.ext.jt808.utils.Jt808AttachmentServerUdpHandlerAdapterBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.TcpXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.DispatcherXtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamServerBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultUdpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.UdpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.XtreamNettyResourceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.*;

/**
 * 附件服务器配置(UDP)
 */
@ConditionalOnProperty(prefix = "jt808-server.udp-attachment-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BuiltinJt808AttachmentServerUdpConfiguration {

    @Bean(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_ATTACHMENT_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_ATTACHMENT_SERVER)
    UdpXtreamNettyHandlerAdapter udpXtreamNettyHandlerAdapter(
            BufferFactoryHolder bufferFactoryHolder,
            Jt808AttachmentServerExchangeCreator exchangeCreator,
            Jt808UdpDatagramPackageSplitter udpDatagramPackageSplitter,
            List<XtreamHandlerMapping> handlerMappings,
            List<XtreamHandlerAdapter> handlerAdapters,
            List<XtreamHandlerResultHandler> handlerResultHandlers,
            List<XtreamFilter> xtreamFilters,
            List<XtreamRequestExceptionHandler> exceptionHandlers) {

        final DispatcherXtreamHandler dispatcherHandler = new DispatcherXtreamHandler(handlerMappings, handlerAdapters, handlerResultHandlers);

        return new Jt808AttachmentServerUdpHandlerAdapterBuilder(bufferFactoryHolder.getAllocator(), udpDatagramPackageSplitter)
                .setAttachmentDispatcherHandler(dispatcherHandler)
                .setXtreamExchangeCreator(exchangeCreator)
                .addHandlerMappings(handlerMappings)
                .addHandlerAdapters(handlerAdapters)
                .addHandlerResultHandlers(handlerResultHandlers)
                .addFilters(xtreamFilters.stream().filter(it -> !(it instanceof TcpXtreamFilter)).toList())
                .addExceptionHandlers(exceptionHandlers)
                .build();
    }

    @Bean(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_ATTACHMENT_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_ATTACHMENT_SERVER)
    UdpXtreamNettyResourceFactory udpXtreamNettyResourceFactory(XtreamJt808ServerProperties serverProperties) {
        final XtreamJt808ServerProperties.UdpLoopResourcesProperty loopResources = serverProperties.getAttachmentServer().getUdpServer().getLoopResources();
        return new DefaultUdpXtreamNettyResourceFactory(new XtreamNettyResourceFactory.LoopResourcesProperty(
                loopResources.getThreadNamePrefix(),
                loopResources.getSelectCount(),
                loopResources.getWorkerCount(),
                loopResources.isDaemon(),
                loopResources.isColocate(),
                loopResources.isPreferNative()
        ));
    }

    @Bean(BEAN_NAME_JT_808_UDP_XTREAM_SERVER_ATTACHMENT_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_SERVER_ATTACHMENT_SERVER)
    UdpXtreamServer udpXtreamServer(
            @Qualifier(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_ATTACHMENT_SERVER) UdpXtreamNettyHandlerAdapter udpXtreamNettyHandlerAdapter,
            @Qualifier(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_ATTACHMENT_SERVER) UdpXtreamNettyResourceFactory resourceFactory,
            ObjectProvider<UdpNettyServerCustomizer> customizers,
            XtreamJt808ServerProperties serverProperties) {
        final XtreamJt808ServerProperties.UdpAttachmentServerProps udpServer = serverProperties.getAttachmentServer().getUdpServer();
        return XtreamServerBuilder.newUdpServerBuilder()
                // 默认 host和 port(用户自定义配置可以再次覆盖默认配置)
                .addServerCustomizer(BuiltinConfigurationUtils.defaultUdpBasicConfigurer(udpServer.getHost(), udpServer.getPort()))
                // handler
                .addServerCustomizer(server -> server.handle(udpXtreamNettyHandlerAdapter))
                // loopResources
                .addServerCustomizer(server -> server.runOn(resourceFactory.loopResources(), resourceFactory.preferNative()))
                // 用户自定义配置
                .addServerCustomizers(customizers.stream().toList())
                .build("ATTACHMENT");
    }
}

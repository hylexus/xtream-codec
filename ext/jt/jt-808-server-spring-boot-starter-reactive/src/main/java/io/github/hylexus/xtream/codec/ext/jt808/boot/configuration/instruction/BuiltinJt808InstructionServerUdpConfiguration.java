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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.instruction;

import io.github.hylexus.xtream.codec.common.utils.BufferFactoryHolder;
import io.github.hylexus.xtream.codec.ext.jt808.boot.condition.ConditionalOnJt808Server;
import io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.utils.Jt808ConfigurationUtils;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808UdpDatagramPackageSplitter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.utils.Jt808InstructionServerUdpHandlerAdapterBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
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

import java.util.List;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.*;

/**
 * 指令服务器配置(UDP)
 */
@ConditionalOnJt808Server(serverType = ConditionalOnJt808Server.ServerType.INSTRUCTION_SERVER, protocolType = ConditionalOnJt808Server.ProtocolType.UDP)
public class BuiltinJt808InstructionServerUdpConfiguration {

    /**
     * @see Jt808ConfigurationUtils#jt808RequestFilterPredicateUdp(XtreamFilter)
     */
    @Bean(value = BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_INSTRUCTION_SERVER, destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_INSTRUCTION_SERVER)
    UdpXtreamNettyHandlerAdapter udpXtreamNettyHandlerAdapter(
            BufferFactoryHolder bufferFactoryHolder,
            Jt808RequestDecoder requestDecoder,
            Jt808RequestLifecycleListener requestLifecycleListener,
            Jt808SessionManager sessionManager,
            Jt808UdpDatagramPackageSplitter udpDatagramPackageSplitter,
            List<XtreamHandlerMapping> handlerMappings,
            List<XtreamHandlerAdapter> handlerAdapters,
            List<XtreamHandlerResultHandler> handlerResultHandlers,
            List<XtreamFilter> xtreamFilters,
            List<XtreamRequestExceptionHandler> exceptionHandlers) {

        return new Jt808InstructionServerUdpHandlerAdapterBuilder(bufferFactoryHolder.getAllocator(), udpDatagramPackageSplitter)
                .requestDecoder(requestDecoder)
                .requestLifecycleListener(requestLifecycleListener)
                .sessionManager(sessionManager)
                .addHandlerMappings(handlerMappings)
                .addHandlerAdapters(handlerAdapters)
                .addHandlerResultHandlers(handlerResultHandlers)
                .addFilters(xtreamFilters.stream().filter(Jt808ConfigurationUtils::jt808RequestFilterPredicateUdp).toList())
                .addExceptionHandlers(exceptionHandlers)
                .build();
    }

    @Bean(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER)
    UdpXtreamNettyResourceFactory udpXtreamNettyResourceFactory(XtreamJt808ServerProperties serverProperties) {
        final XtreamJt808ServerProperties.UdpLoopResourcesProperty loopResources = serverProperties.getInstructionServer().getUdpServer().getLoopResources();
        return new DefaultUdpXtreamNettyResourceFactory(new XtreamNettyResourceFactory.LoopResourcesProperty(
                loopResources.getThreadNamePrefix(),
                loopResources.getSelectCount(),
                loopResources.getWorkerCount(),
                loopResources.isDaemon(),
                loopResources.isColocate(),
                loopResources.isPreferNative()
        ));
    }

    @Bean(BEAN_NAME_JT_808_UDP_XTREAM_SERVER_INSTRUCTION_SERVER)
    @ConditionalOnMissingBean(name = BEAN_NAME_JT_808_UDP_XTREAM_SERVER_INSTRUCTION_SERVER)
    UdpXtreamServer udpXtreamServer(
            @Qualifier(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_INSTRUCTION_SERVER) UdpXtreamNettyHandlerAdapter udpXtreamNettyHandlerAdapter,
            @Qualifier(BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER) UdpXtreamNettyResourceFactory resourceFactory,
            ObjectProvider<UdpNettyServerCustomizer> customizers,
            XtreamJt808ServerProperties serverProperties) {
        final XtreamJt808ServerProperties.UdpServerProps udpServer = serverProperties.getInstructionServer().getUdpServer();
        return XtreamServerBuilder.newUdpServerBuilder()
                // 默认 host和 port(用户自定义配置可以再次覆盖默认配置)
                .addServerCustomizer(BuiltinConfigurationUtils.defaultUdpBasicConfigurer(udpServer.getHost(), udpServer.getPort()))
                // handler
                .addServerCustomizer(server -> server.handle(udpXtreamNettyHandlerAdapter))
                // loopResources
                .addServerCustomizer(server -> server.runOn(resourceFactory.loopResources(), resourceFactory.preferNative()))
                // 用户自定义配置
                .addServerCustomizers(customizers.stream().toList())
                .build("JT/T-808-INSTRUCTION");
    }

}

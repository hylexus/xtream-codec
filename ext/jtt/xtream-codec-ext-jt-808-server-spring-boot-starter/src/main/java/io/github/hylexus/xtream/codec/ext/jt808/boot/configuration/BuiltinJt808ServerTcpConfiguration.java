/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.server.reactive.spec.TcpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamServerBuilder;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpNettyServerCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultTcpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.TcpXtreamNettyResourceFactory;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.XtreamNettyResourceFactory;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import reactor.netty.tcp.TcpServer;

import java.util.List;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.*;

@ConditionalOnProperty(prefix = "jt808-server.tcp-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BuiltinJt808ServerTcpConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TcpXtreamNettyHandlerAdapter tcpXtreamNettyHandlerAdapter(
            List<XtreamHandlerMapping> handlerMappings,
            List<XtreamHandlerAdapter> handlerAdapters,
            List<XtreamHandlerResultHandler> handlerResultHandlers,
            List<XtreamFilter> xtreamFilters,
            List<XtreamRequestExceptionHandler> exceptionHandlers) {

        return XtreamNettyHandlerAdapter.newTcpBuilder()
                .addHandlerMappings(handlerMappings)
                .addHandlerAdapters(handlerAdapters)
                .addHandlerResultHandlers(handlerResultHandlers)
                .addFilters(xtreamFilters.stream().filter(it -> !(it instanceof UdpXtreamFilter)).toList())
                .addExceptionHandlers(exceptionHandlers)
                .build();
    }

    @Bean
    DefaultTcpServerCustomizer defaultTcpServerCustomizer(XtreamJt808ServerProperties serverProps) {
        return new DefaultTcpServerCustomizer(serverProps);
    }

    @Bean
    @ConditionalOnMissingBean
    TcpXtreamNettyResourceFactory tcpXtreamNettyResourceFactory(XtreamJt808ServerProperties serverProperties) {
        final XtreamJt808ServerProperties.TcpLoopResourcesProperty loopResources = serverProperties.getTcpServer().getLoopResources();
        return new DefaultTcpXtreamNettyResourceFactory(new XtreamNettyResourceFactory.LoopResourcesProperty(
                loopResources.getThreadNamePrefix(),
                loopResources.getSelectCount(),
                loopResources.getWorkerCount(),
                loopResources.isDaemon(),
                loopResources.isColocate(),
                loopResources.isPreferNative()
        ));
    }

    @Bean
    @ConditionalOnMissingBean
    TcpXtreamServer tcpXtreamServer(
            TcpXtreamNettyHandlerAdapter tcpXtreamNettyHandlerAdapter,
            TcpXtreamNettyResourceFactory resourceFactory,
            ObjectProvider<TcpNettyServerCustomizer> customizers) {

        return XtreamServerBuilder.newTcpServerBuilder()
                // handler
                .addServerCustomizer(server -> server.handle(tcpXtreamNettyHandlerAdapter))
                // 分包
                .addServerCustomizer(server -> server.doOnChannelInit((observer, channel, remoteAddress) -> {
                    // stripDelimiter=true
                    final DelimiterBasedFrameDecoder frameDecoder = new DelimiterBasedFrameDecoder(DEFAULT_MAX_PACKAGE_SIZE, true, Unpooled.copiedBuffer(new byte[]{PACKAGE_DELIMITER}));
                    channel.pipeline().addFirst(BEAN_NAME_CHANNEL_INBOUND_HANDLER_ADAPTER, frameDecoder);
                }))
                // loopResources
                .addServerCustomizer(server -> server.runOn(resourceFactory.loopResources(), resourceFactory.preferNative()))
                // 用户自定义配置
                .addServerCustomizers(customizers.stream().toList())
                .build();
    }

    public static class DefaultTcpServerCustomizer implements TcpNettyServerCustomizer {
        private final XtreamJt808ServerProperties serverProps;

        public DefaultTcpServerCustomizer(XtreamJt808ServerProperties serverProps) {
            this.serverProps = serverProps;
        }

        public int order() {
            return OrderedComponent.HIGHEST_PRECEDENCE + 1;
        }

        @Override
        public TcpServer customize(TcpServer server) {
            final XtreamJt808ServerProperties.TcpServerProps tcpServer = serverProps.getTcpServer();
            if (StringUtils.hasText(tcpServer.getHost())) {
                server = server.host(tcpServer.getHost());
            }
            return server.port(tcpServer.getPort());
        }
    }
}
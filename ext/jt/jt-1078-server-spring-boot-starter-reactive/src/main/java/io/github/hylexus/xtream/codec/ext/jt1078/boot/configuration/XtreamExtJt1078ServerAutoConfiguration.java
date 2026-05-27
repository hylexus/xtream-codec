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
import io.github.hylexus.xtream.codec.ext.jt1078.boot.listener.XtreamExtJt1078ServerStartupListener;
import io.github.hylexus.xtream.codec.ext.jt1078.boot.properties.XtreamJt1078ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.BuiltinJt1078SessionCloseListener;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionEventListener;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties({
        XtreamJt1078ServerProperties.class,
})
@Import({
        BuiltinJt1078ServerSchedulerConfiguration.class,
        BuiltinJt1078ServerTcpConfiguration.class,
        BuiltinJt1078ServerUdpConfiguration.class,
        BuiltinJt1078ServerHandlerConfiguration.class,
})
@ConditionalOnProperty(prefix = "jt1078-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XtreamExtJt1078ServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BufferFactoryHolder bufferFactoryHolder() {
        return new BufferFactoryHolder(ByteBufAllocator.DEFAULT);
    }

    @Bean
    @ConditionalOnMissingBean
    XtreamSessionIdGenerator xtreamSessionIdGenerator() {
        return new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    Jt1078SessionManager jt808SessionManager(XtreamSessionIdGenerator idGenerator, XtreamJt1078ServerProperties serverProperties) {
        final UdpSessionIdleStateCheckerProps idleStateChecker = serverProperties.getUdpServer().getSessionIdleStateChecker();
        return new DefaultJt1078SessionManager(
                serverProperties.getUdpServer().isEnabled(),
                idleStateChecker, idGenerator
        );
    }

    @Bean
    XtreamExtJt1078ServerStartupListener xtreamExtJt1078ServerStartupListener(XtreamJt1078ServerProperties serverProperties) {
        return new XtreamExtJt1078ServerStartupListener(serverProperties);
    }

    @Bean
    BuiltinJt1078SessionCloseListener builtinJt1078SessionCloseListener(Jt1078RequestPublisher publisher) {
        return new BuiltinJt1078SessionCloseListener(publisher);
    }

    @Bean
    CommandLineRunner jt1078SessionEventListenerRegister(Jt1078SessionManager sessionManager, ObjectProvider<Jt1078SessionEventListener> listeners) {
        return args -> listeners.orderedStream().forEach(sessionManager::addListener);
    }

}

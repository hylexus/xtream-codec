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
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.impl.DefaultJt808XtreamCommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808FlowIdGenerator;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionEventListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({
        BuiltinJt808InstructionServerTcpConfiguration.class,
        BuiltinJt808InstructionServerUdpConfiguration.class,
})
@ConditionalOnExpression("${jt808-server.instruction-server.tcp-server.enabled:true} || ${jt808-server.instruction-server.udp-server.enabled:true}")
public class BuiltinJt808InstructionServerConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    Jt808SessionManager jt808SessionManager(XtreamSessionIdGenerator idGenerator, XtreamJt808ServerProperties serverProperties) {
        final UdpSessionIdleStateCheckerProps idleStateChecker = serverProperties.getInstructionServer().getUdpServer().getSessionIdleStateChecker();
        return new DefaultJt808SessionManager(
                serverProperties.getInstructionServer().getUdpServer().isEnabled(),
                idleStateChecker, idGenerator
        );
    }

    @Bean
    CommandLineRunner xtreamSessionEventListenerRegister(Jt808SessionManager sessionManager, ObjectProvider<Jt808SessionEventListener> listeners) {
        return args -> listeners.orderedStream().forEach(sessionManager::addListener);
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808CommandSender jt808CommandSender(
            BufferFactoryHolder holder,
            Jt808SessionManager sessionManager,
            Jt808ResponseEncoder encoder,
            Jt808FlowIdGenerator flowIdGenerator,
            Jt808RequestLifecycleListener requestLifecycleListener) {
        return new DefaultJt808XtreamCommandSender(holder.getAllocator(), sessionManager, encoder, flowIdGenerator, requestLifecycleListener);
    }

}

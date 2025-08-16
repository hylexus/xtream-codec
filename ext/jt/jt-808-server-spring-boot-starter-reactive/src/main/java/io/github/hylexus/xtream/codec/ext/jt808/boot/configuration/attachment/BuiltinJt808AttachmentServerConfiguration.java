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

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionEventListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({
        BuiltinJt808AttachmentServerTcpConfiguration.class,
        BuiltinJt808AttachmentServerUdpConfiguration.class,
})
@ConditionalOnExpression("${jt808-server.attachment-server.tcp-server.enabled:true} || ${jt808-server.attachment-server.udp-server.enabled:true}")
public class BuiltinJt808AttachmentServerConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    Jt808AttachmentSessionManager jt808AttachmentSessionManager(XtreamSessionIdGenerator idGenerator, XtreamJt808ServerProperties serverProperties) {

        final UdpSessionIdleStateCheckerProps idleStateChecker = serverProperties.getAttachmentServer().getUdpServer().getSessionIdleStateChecker();
        return new DefaultJt808AttachmentSessionManager(
                serverProperties.getAttachmentServer().getUdpServer().isEnabled(),
                idleStateChecker, idGenerator
        );
    }

    @Bean
    CommandLineRunner xtreamAttachmentSessionEventListenerRegister(Jt808AttachmentSessionManager sessionManager, ObjectProvider<Jt808SessionEventListener> listeners) {
        return args -> listeners.orderedStream().forEach(sessionManager::addListener);
    }

}

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

import io.github.hylexus.xtream.codec.ext.jt1078.boot.condition.ConditionalOnJt1078Server;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.DefaultJt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.RemoveLeadingZerosJt1078SimConverter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.resources.Jt1078XtreamSchedulerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;

@ConditionalOnJt1078Server(protocolType = ConditionalOnJt1078Server.ProtocolType.ANY)
public class BuiltinJt1078ServerHandlerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Jt1078SimConverter jt1078SimConverter() {
        return new RemoveLeadingZerosJt1078SimConverter();
    }

    @Bean
    Jt1078RequestPublisher jt1078RequestPublisher(Jt1078SimConverter simConverter, Jt1078XtreamSchedulerRegistry schedulerRegistry) {
        final Scheduler scheduler = schedulerRegistry.audioVideoSubscriberScheduler();
        return new DefaultJt1078RequestPublisher(simConverter, scheduler);
    }

}

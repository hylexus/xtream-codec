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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.processor.BuiltinJt808ServerCustomSchedulerRegistrar;
import io.github.hylexus.xtream.codec.ext.jt808.processor.DefaultJt808XtreamScheduleRegistryCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamServerConstants;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin.DefaultXtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin.DisruptorBasedXtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import({
        BuiltinJt808ServerCustomSchedulerRegistrar.class,
        BuiltinJt808ServerSchedulerConfigurationWithMicroMeter.class,
})
public class BuiltinJt808ServerSchedulerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    XtreamBlockingHandlerMethodPredicate xtreamBlockingHandlerMethodPredicate() {
        return new DefaultXtreamBlockingHandlerMethodPredicate();
    }

    @Bean
    @ConditionalOnMissingBean
    XtreamSchedulerRegistry xtreamSchedulerRegistry(List<XtreamSchedulerRegistryCustomizer> customizers) {
        return new DefaultXtreamSchedulerRegistry(customizers, (config, scheduler) -> scheduler);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    XtreamEventPublisher xtreamEventPublisher(XtreamJt808ServerProperties serverProperties, XtreamSchedulerRegistry schedulerRegistry) {
        final XtreamJt808ServerProperties.XtreamEventPublisherProps publisherProps = serverProperties.getEventPublisher();
        if (publisherProps.getPublisherType() == XtreamJt808ServerProperties.XtreamEventPublisherProps.PublisherType.REACTOR) {
            return new DefaultXtreamEventPublisher(schedulerRegistry, publisherProps.getReactor().getThreadName());
        }
        final XtreamJt808ServerProperties.EventPublisherDisruptorProps disruptorProps = publisherProps.getDisruptor();
        final DefaultThreadFactory threadFactory = new DefaultThreadFactory(disruptorProps.getThreadName(), disruptorProps.isDaemon());
        return new DisruptorBasedXtreamEventPublisher(schedulerRegistry, disruptorProps.getRingBufferSize(), threadFactory);
    }

    @Bean(name = XtreamServerConstants.BEAN_NAME_EVENT_PUBLISHER_SCHEDULER)
    @ConditionalOnMissingBean(name = XtreamServerConstants.BEAN_NAME_EVENT_PUBLISHER_SCHEDULER)
    XtreamSchedulerRegistryCustomizer eventPublisherSchedulerCustomizer(XtreamJt808ServerProperties serverProperties) {
        return new DefaultJt808XtreamScheduleRegistryCustomizer(XtreamServerConstants.BEAN_NAME_EVENT_PUBLISHER_SCHEDULER, serverProperties.getSchedulers().getEventPublisher());
    }

    @Bean(name = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER)
    @ConditionalOnMissingBean(name = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER)
    XtreamSchedulerRegistryCustomizer nonBlockingSchedulerCustomizer(XtreamJt808ServerProperties serverProperties) {
        return new DefaultJt808XtreamScheduleRegistryCustomizer(XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER, serverProperties.getSchedulers().getNonBlockingHandler());
    }

    @Bean(name = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_BLOCKING_SCHEDULER)
    @ConditionalOnMissingBean(name = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_BLOCKING_SCHEDULER)
    XtreamSchedulerRegistryCustomizer blockingSchedulerCustomizer(XtreamJt808ServerProperties serverProperties) {
        return new DefaultJt808XtreamScheduleRegistryCustomizer(XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_BLOCKING_SCHEDULER, serverProperties.getSchedulers().getBlockingHandler());
    }

    @ConditionalOnProperty(prefix = "jt808-server.features.request-dispatcher-scheduler", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = XtreamServerConstants.BEAN_NAME_REQUEST_DISPATCHER_SCHEDULER)
    @Bean(name = XtreamServerConstants.BEAN_NAME_REQUEST_DISPATCHER_SCHEDULER)
    XtreamSchedulerRegistryCustomizer requestDispatcherSchedulerCustomizer(XtreamJt808ServerProperties serverProperties) {
        return new DefaultJt808XtreamScheduleRegistryCustomizer(XtreamServerConstants.BEAN_NAME_REQUEST_DISPATCHER_SCHEDULER, serverProperties.getSchedulers().getRequestDispatcher());
    }

}

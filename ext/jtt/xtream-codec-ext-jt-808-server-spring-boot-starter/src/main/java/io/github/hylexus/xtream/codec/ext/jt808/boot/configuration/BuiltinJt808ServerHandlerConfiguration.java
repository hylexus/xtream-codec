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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.attachment.BuiltinJt808AttachmentServerConfiguration;
import io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.instruction.BuiltinJt808InstructionServerConfiguration;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.Jt808RequestLifecycleListenerComposite;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.filter.Jt808RequestCombinerFilter;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBodyHandlerResultHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.listener.Jt808RequestLoggerListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionIdGenerator;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodArgumentResolver;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DelegateXtreamHandlerMethodArgumentResolver;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.LoggingXtreamRequestExceptionHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Import({
        BuiltinJt808InstructionServerConfiguration.class,
        BuiltinJt808AttachmentServerConfiguration.class,
        BuiltinJt808ServerHandlerConfiguration.BuiltinJt808DashboardAndActuatorConfiguration.class,
        BuiltinJt808ServerActuatorConfiguration.class,
        BuiltinJt808DashboardConfiguration.class,
})
public class BuiltinJt808ServerHandlerConfiguration {

    // region exceptionHandlers
    @Bean
    LoggingXtreamRequestExceptionHandler loggingXtreamRequestExceptionHandler() {
        return new LoggingXtreamRequestExceptionHandler();
    }
    // endregion exceptionHandlers

    // region filters
    @Bean
    @ConditionalOnProperty(prefix = "jt808-server.features.request-dispatcher-scheduler", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    RequestDispatcherSchedulerFilter requestDispatcherSchedulerFilter(XtreamSchedulerRegistry schedulerRegistry) {
        return new RequestDispatcherSchedulerFilter(schedulerRegistry);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jt808-server.features.request-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
    Jt808RequestLoggerListener loggingXtreamFilter() {
        return new Jt808RequestLoggerListener();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jt808-server.features.request-combiner", name = "enabled", havingValue = "true", matchIfMissing = true)
    Jt808RequestCombinerFilter jt808RequestCombinerFilter(
            Jt808RequestCombiner jt808RequestCombiner,
            Jt808RequestLifecycleListener lifecycleListener) {
        return new Jt808RequestCombinerFilter(jt808RequestCombiner, lifecycleListener);
    }
    // endregion filters

    // region handlerMappings
    @Bean
    Jt808RequestMappingHandlerMapping jt808RequestMappingHandlerMapping(
            XtreamSchedulerRegistry schedulerRegistry,
            XtreamBlockingHandlerMethodPredicate blockingHandlerMethodPredicate) {

        return new Jt808RequestMappingHandlerMapping(schedulerRegistry, blockingHandlerMethodPredicate);
    }
    // endregion handlerMappings

    // region handlerAdapters
    @Bean
    @Primary
    XtreamHandlerMethodArgumentResolver xtreamHandlerMethodArgumentResolver(
            List<XtreamHandlerMethodArgumentResolver> resolvers,
            EntityCodec entityCodec) {

        final DelegateXtreamHandlerMethodArgumentResolver resolver = new DelegateXtreamHandlerMethodArgumentResolver();
        resolvers.forEach(resolver::addArgumentResolver);
        resolver.addDefault(entityCodec);
        return resolver;
    }

    @Bean
    XtreamHandlerMethodHandlerAdapter xtreamHandlerMethodHandlerAdapter(XtreamHandlerMethodArgumentResolver argumentResolver) {

        return new XtreamHandlerMethodHandlerAdapter(argumentResolver);
    }

    @Bean
    SimpleXtreamRequestHandlerHandlerAdapter simpleXtreamRequestHandlerHandlerAdapter() {
        return new SimpleXtreamRequestHandlerHandlerAdapter();
    }
    // endregion handlerAdapters

    // region handlerResultHandlers
    @Bean
    LoggingXtreamHandlerResultHandler loggingXtreamHandlerResultHandler() {
        return new LoggingXtreamHandlerResultHandler();
    }

    @Bean
    EmptyXtreamHandlerResultHandler emptyXtreamHandlerResultHandler() {
        return new EmptyXtreamHandlerResultHandler();
    }

    @Bean
    @Primary
    Jt808RequestLifecycleListener jt808RequestLifecycleListenerComposite(List<Jt808RequestLifecycleListener> listeners) {
        return new Jt808RequestLifecycleListenerComposite(listeners);
    }

    @Bean
    Jt808ResponseBodyHandlerResultHandler jt808ResponseBodyHandlerResultHandler(Jt808ResponseEncoder jt808ResponseEncoder, Jt808RequestLifecycleListener lifecycleListener) {
        return new Jt808ResponseBodyHandlerResultHandler(jt808ResponseEncoder, lifecycleListener);
    }

    @Bean
    XtreamResponseBodyHandlerResultHandler xtreamResponseBodyHandlerResultHandler(EntityCodec entityCodec) {
        return new XtreamResponseBodyHandlerResultHandler(entityCodec);
    }
    // endregion handlerResultHandlers

    // region session
    @Bean
    @ConditionalOnMissingBean
    XtreamSessionIdGenerator xtreamSessionIdGenerator() {
        return new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator();
    }
    // endregion session

    @ConditionalOnExpression("${jt808-server.features.dashboard.enabled:true} || ${management.endpoint.jt808.enabled:false}")
    static class BuiltinJt808DashboardAndActuatorConfiguration {
        @Bean
        Jt808ServerSimpleMetricsHolder metricsHolder() {
            return new Jt808ServerSimpleMetricsHolder(
                    new SimpleTypes.SessionInfo(),
                    new SimpleTypes.SessionInfo(),
                    new SimpleTypes.SessionInfo(),
                    new SimpleTypes.SessionInfo(),
                    new SimpleTypes.RequestInfo(new LongAdder(), new ConcurrentHashMap<>()),
                    new SimpleTypes.RequestInfo(new LongAdder(), new ConcurrentHashMap<>()),
                    new SimpleTypes.RequestInfo(new LongAdder(), new ConcurrentHashMap<>()),
                    new SimpleTypes.RequestInfo(new LongAdder(), new ConcurrentHashMap<>())
            );
        }
    }
}

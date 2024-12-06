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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.mapping.DispatcherHandlerXtreamMappingDescriptionProvider;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.mapping.XtreamMappingDescriptionProvider;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.*;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.Jt808DashboardRequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.RequestInfoCollector;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.SessionInfoCollector;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMappingService;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsService;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl.DefaultJt808DashboardMappingService;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl.DefaultJt808DashboardMetricsService;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author hylexus
 */
public class BuiltinJt808DashboardConfiguration {

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

    @Bean
    @ConditionalOnBean(Jt808SessionManager.class)
    SessionInfoCollector sessionInfoCollector(
            Jt808ServerSimpleMetricsHolder serverSimpleMetricsHolder,
            XtreamEventPublisher eventPublisher,
            @Autowired(required = false) Jt808SessionManager sessionManager,
            @Autowired(required = false) Jt808AttachmentSessionManager attachmentSessionManager) {
        return new SessionInfoCollector(serverSimpleMetricsHolder, sessionManager, attachmentSessionManager, eventPublisher);
    }

    @Bean
    RequestInfoCollector requestInfoCollector(Jt808ServerSimpleMetricsHolder metricsHolder, Jt808MessageDescriptionRegistry descriptor) {
        return new RequestInfoCollector(metricsHolder, descriptor);
    }

    @Bean
    BuiltinJt808DashboardEventController builtinJt808DashboardEventController(XtreamEventPublisher eventPublisher) {
        return new BuiltinJt808DashboardEventController(eventPublisher);
    }

    @Bean
    Jt808DashboardMetricsService jt808DashboardMetricsService(
            XtreamJt808ServerProperties serverProperties,
            XtreamEventPublisher eventPublisher,
            Jt808ServerSimpleMetricsHolder metricsHolder) {
        return new DefaultJt808DashboardMetricsService(serverProperties, eventPublisher, metricsHolder);
    }

    @Bean
    BuiltinJt808DashboardMetricsController builtinJt808DashboardMetricsController(Jt808DashboardMetricsService metricsService) {
        return new BuiltinJt808DashboardMetricsController(metricsService);
    }

    @Bean
    XtreamMappingDescriptionProvider dispatcherHandlerXtreamMappingDescriptionProvider(
            Jt808MessageDescriptionRegistry descriptionRegistry,
            List<XtreamHandlerMapping> handlerMappingList) {
        return new DispatcherHandlerXtreamMappingDescriptionProvider(handlerMappingList, descriptionRegistry);
    }

    @Bean
    Jt808DashboardMappingService jt808DashboardMappingService(ObjectProvider<XtreamMappingDescriptionProvider> mappingDescriptionProviders) {
        return new DefaultJt808DashboardMappingService(mappingDescriptionProviders.orderedStream().toList());
    }

    @Bean
    BuiltinJt808DashboardCommonController builtinJt808DashboardCommonController(XtreamJt808ServerProperties serverProperties, Jt808DashboardMappingService dashboardMappingService) {
        return new BuiltinJt808DashboardCommonController(serverProperties, dashboardMappingService);
    }

    @Bean
    Jt808DashboardRequestLifecycleListener jt808DashboardRequestLifecycleListener(XtreamEventPublisher eventPublisher) {
        return new Jt808DashboardRequestLifecycleListener(eventPublisher);
    }

    @Bean
    BuiltinJt808DashboardSessionController builtinJt808DashboardSessionController(
            @Autowired(required = false) Jt808SessionManager sessionManager,
            @Autowired(required = false) Jt808AttachmentSessionManager attachmentSessionManager) {
        return new BuiltinJt808DashboardSessionController(sessionManager, attachmentSessionManager);
    }

    @Bean
    BuiltinJt808DashboardPublisherController builtinJt808DashboardPublisherController(XtreamEventPublisher eventPublisher) {
        return new BuiltinJt808DashboardPublisherController(eventPublisher);
    }

}

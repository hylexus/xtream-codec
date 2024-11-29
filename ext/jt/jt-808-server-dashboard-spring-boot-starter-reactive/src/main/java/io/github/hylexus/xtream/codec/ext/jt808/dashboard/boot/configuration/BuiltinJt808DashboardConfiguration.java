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
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.*;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.Jt808DashboardRequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.RequestInfoCollector;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.SessionInfoCollector;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808AttachmentSessionManager;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

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
    BuiltinJt808DashboardMetricsController builtinJt808DashboardMetricsController(
            XtreamEventPublisher eventPublisher,
            Jt808ServerSimpleMetricsHolder metricsHolder) {
        return new BuiltinJt808DashboardMetricsController(eventPublisher, metricsHolder);
    }

    @Bean
    BuiltinJt808DashboardCommonController builtinJt808DashboardCommonController(XtreamJt808ServerProperties serverProperties) {
        return new BuiltinJt808DashboardCommonController(serverProperties);
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
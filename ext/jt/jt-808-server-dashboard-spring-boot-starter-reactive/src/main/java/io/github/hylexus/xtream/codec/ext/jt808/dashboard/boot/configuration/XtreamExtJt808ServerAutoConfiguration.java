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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request.DefaultJt808DashboardErrorReporter;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request.DefaultJt808DashboardErrorStore;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request.Jt808DashboardErrorReporter;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request.Jt808DashboardErrorStore;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration.reactive.XtreamExtJt808ServerAutoConfigurationReactive;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration.servlet.XtreamExtJt808ServerAutoConfigurationServlet;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl.DefaultJt808DashboardEventService;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        BuiltinJt808DashboardConfiguration.class,
        XtreamExtJt808ServerAutoConfigurationReactive.class,
        XtreamExtJt808ServerAutoConfigurationServlet.class,
})
@EnableConfigurationProperties({
        XtreamJt808ServerProperties.class,
        XtreamJt808ServerDashboardProperties.class,
})
@ConditionalOnProperty(prefix = "jt808-server.dashboard", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XtreamExtJt808ServerAutoConfiguration {

    public XtreamExtJt808ServerAutoConfiguration() {
    }

    // region error-reporter
    @Bean
    @ConditionalOnMissingBean
    Jt808DashboardErrorStore jt808DashboardErrorStore() {
        return new DefaultJt808DashboardErrorStore();
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808DashboardErrorReporter jt808DashboardErrorReporter(Jt808DashboardErrorStore errorStore) {
        return new DefaultJt808DashboardErrorReporter(errorStore);
    }
    // endregion error-reporter

    @Bean
    @ConditionalOnMissingBean
    Jt808DashboardEventService jt808DashboardEventService(XtreamEventPublisher eventPublisher) {
        return new DefaultJt808DashboardEventService(eventPublisher);
    }

}

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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt1078.boot.properties.XtreamJt1078ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.reactive.XtreamExtJt1078ServerAutoConfigurationReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.servlet.XtreamExtJt1078ServerAutoConfigurationServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        BuiltinJt1078DashboardConfiguration.class,
        XtreamExtJt1078ServerAutoConfigurationReactive.class,
        XtreamExtJt1078ServerAutoConfigurationServlet.class,
})
@EnableConfigurationProperties({
        XtreamJt1078ServerProperties.class,
        XtreamJt808ServerDashboardProperties.class,
})
@ConditionalOnProperty(prefix = "jt1078-server.dashboard", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XtreamExtJt1078ServerAutoConfiguration {

    public XtreamExtJt1078ServerAutoConfiguration() {
    }


}

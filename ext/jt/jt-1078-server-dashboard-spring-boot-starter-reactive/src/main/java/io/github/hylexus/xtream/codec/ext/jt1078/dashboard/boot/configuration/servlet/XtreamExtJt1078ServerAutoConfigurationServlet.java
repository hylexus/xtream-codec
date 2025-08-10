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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.servlet;

import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.servlet.BuiltinJt1078DashboardProxyControllerServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.servlet.BuiltinJt1078SubscriptionHttpHandlerServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.servlet.BuiltinJt1078SubscriptionWebSocketHandlerServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.Jt808ProxyServiceServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.impl.DefaultJt808ProxyServiceServlet;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;

import java.util.List;

import static io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.reactive.XtreamExtJt1078ServerAutoConfigurationReactive.filteredNonNullList;

@EnableWebSocket
@Import({
        XtreamExtJt1078WebMvcAutoConfiguration.class,
        XtreamJt1078Rfc7807StyleWebExceptionHandlerServlet.class,
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class XtreamExtJt1078ServerAutoConfigurationServlet {

    @Bean
    Jt808ProxyServiceServlet jt808ProxyService(XtreamJt808ServerDashboardProperties properties) {
        return new DefaultJt808ProxyServiceServlet(properties);
    }

    @Bean
    BuiltinJt1078DashboardProxyControllerServlet builtinJt1078DashboardProxyControllerServlet(Jt808ProxyServiceServlet proxyServiceServlet) {
        return new BuiltinJt1078DashboardProxyControllerServlet(proxyServiceServlet);
    }

    @Bean
    BuiltinJt1078SubscriptionHttpHandlerServlet builtinJt1078SubscriptionHttpHandlerServlet(Jt1078RequestPublisher publisher) {
        return new BuiltinJt1078SubscriptionHttpHandlerServlet(publisher);
    }

    @Bean
    public BuiltinJt1078SubscriptionWebSocketHandlerServlet builtinJt1078SubscriptionWebSocketHandlerServlet(Jt1078RequestPublisher publisher) {
        return new BuiltinJt1078SubscriptionWebSocketHandlerServlet(publisher);
    }

    @Bean
    WebSocketConfigurer builtinJt1078SubscriptionWebSocketConfigurer(
            XtreamJt808ServerDashboardProperties dashboardProperties,
            BuiltinJt1078SubscriptionWebSocketHandlerServlet webSocketHandler) {

        final XtreamJt808ServerDashboardProperties.CorsProperties corsProperties = dashboardProperties.getDashboardApiCorsFilter();

        return registry -> {
            final WebSocketHandlerRegistration registration = registry.addHandler(webSocketHandler, BuiltinJt1078SubscriptionWebSocketHandlerServlet.PATH_PATTERN);
            if (!corsProperties.isEnabled()) {
                return;
            }

            final List<String> allowedOriginPatterns = filteredNonNullList(corsProperties.getAllowedOriginPatterns());
            if (allowedOriginPatterns.isEmpty()) {
                final List<String> allowedOrigins = filteredNonNullList(corsProperties.getAllowedOrigins());
                if (!allowedOrigins.isEmpty()) {
                    registration.setAllowedOrigins(allowedOrigins.toArray(String[]::new));
                }
            } else {
                registration.setAllowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new));
            }

        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "jt1078-server.features.dashboard.dashboard-api-cors-filter", name = "enabled", havingValue = "true")
    public WebMvcConfigurer jt1078DashboardApiCorsFilter(XtreamJt808ServerDashboardProperties dashboardProperties) {
        final XtreamJt808ServerDashboardProperties.CorsProperties corsProperties = dashboardProperties.getDashboardApiCorsFilter();

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(@Nonnull CorsRegistry registry) {
                // 1. path-pattern
                final CorsRegistration corsRegistration = registry.addMapping(corsProperties.getPathPattern());

                // 2. allow-credentials
                if (corsProperties.getAllowCredentials() != null) {
                    corsRegistration.allowCredentials(corsProperties.getAllowCredentials());
                }

                // 3. allowed-origins or allowed-origin-patterns
                final List<String> allowedOriginPatterns = filteredNonNullList(corsProperties.getAllowedOriginPatterns());
                if (allowedOriginPatterns.isEmpty()) {
                    final List<String> allowedOrigins = filteredNonNullList(corsProperties.getAllowedOrigins());
                    if (!allowedOrigins.isEmpty()) {
                        corsRegistration.allowedOrigins(allowedOrigins.toArray(String[]::new));
                    }
                } else {
                    corsRegistration.allowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new));
                }

                // 4. allowed-headers
                final List<String> allowedHeaders = filteredNonNullList(corsProperties.getAllowedHeaders());
                if (!allowedHeaders.isEmpty()) {
                    corsRegistration.allowedHeaders(allowedHeaders.toArray(String[]::new));
                }

                // 5. allowed-methods
                final List<String> allowedMethods = filteredNonNullList(corsProperties.getAllowedMethods());
                if (!allowedMethods.isEmpty()) {
                    corsRegistration.allowedMethods(allowedMethods.toArray(String[]::new));
                }

                // 6. exposed-headers
                final List<String> exposedHeaders = filteredNonNullList(corsProperties.getExposedHeaders());
                if (!exposedHeaders.isEmpty()) {
                    corsRegistration.exposedHeaders(exposedHeaders.toArray(String[]::new));
                }

                // 7. max-age
                if (corsProperties.getMaxAge() != null) {
                    corsRegistration.maxAge(corsProperties.getMaxAge().toSeconds());
                }

            }
        };
    }

}

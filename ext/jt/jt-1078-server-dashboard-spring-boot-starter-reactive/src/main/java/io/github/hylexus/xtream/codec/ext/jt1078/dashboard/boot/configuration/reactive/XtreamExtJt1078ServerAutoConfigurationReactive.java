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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.reactive;

import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive.BuiltinJt1078DashboardProxyControllerReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive.BuiltinJt1078SubscriptionHttpHandlerReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive.BuiltinJt1078SubscriptionWebSocketHandlerReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.Jt808ProxyServiceReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.impl.DefaultJt808ProxyServiceReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Import({
        XtreamExtJt1078WebFluxAutoConfiguration.class,
        XtreamJt1078Rfc7807StyleWebExceptionHandlerReactive.class,
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class XtreamExtJt1078ServerAutoConfigurationReactive {

    @Bean
    Jt808ProxyServiceReactive jt808ProxyService(XtreamJt808ServerDashboardProperties properties) {
        return new DefaultJt808ProxyServiceReactive(properties);
    }

    @Bean
    BuiltinJt1078DashboardProxyControllerReactive builtinJt1078DashboardProxyControllerReactive(Jt808ProxyServiceReactive proxyService) {
        return new BuiltinJt1078DashboardProxyControllerReactive(proxyService);
    }

    @Bean
    BuiltinJt1078SubscriptionHttpHandlerReactive builtinJt1078SubscriptionHttpHandlerReactive(Jt1078RequestPublisher publisher) {
        return new BuiltinJt1078SubscriptionHttpHandlerReactive(publisher);
    }

    @Bean
    BuiltinJt1078SubscriptionWebSocketHandlerReactive builtinJt1078SubscriptionWebSocketHandlerReactive(Jt1078RequestPublisher publisher) {
        return new BuiltinJt1078SubscriptionWebSocketHandlerReactive(publisher);
    }

    @Bean
    public HandlerMapping builtinJt1078SubscriptionWebSocketHandlerMapping(BuiltinJt1078SubscriptionWebSocketHandlerReactive webSocketHandler) {
        final SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        final Map<String, WebSocketHandler> handlerMap = new LinkedHashMap<>();
        handlerMap.put(BuiltinJt1078SubscriptionWebSocketHandlerReactive.PATH_PATTERN, webSocketHandler);
        simpleUrlHandlerMapping.setUrlMap(handlerMap);
        simpleUrlHandlerMapping.setOrder(-1);
        return simpleUrlHandlerMapping;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jt1078-server.features.dashboard.dashboard-api-cors-filter", name = "enabled", havingValue = "true")
    public CorsWebFilter jt1078DashboardApiCorsFilter(XtreamJt808ServerDashboardProperties dashboardProperties) {
        final XtreamJt808ServerDashboardProperties.CorsProperties corsProperties = dashboardProperties.getDashboardApiCorsFilter();

        final CorsConfiguration config = new CorsConfiguration();

        // 1. path-pattern
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPathPattern(), config);

        // 2. allow-credentials
        if (corsProperties.getAllowCredentials() != null) {
            config.setAllowCredentials(corsProperties.getAllowCredentials());
        }

        // 3. allowed-origins or allowed-origin-patterns
        final List<String> allowedOriginPatterns = filteredNonNullList(corsProperties.getAllowedOriginPatterns());
        if (allowedOriginPatterns.isEmpty()) {
            final List<String> allowedOrigins = filteredNonNullList(corsProperties.getAllowedOrigins());
            if (!allowedOrigins.isEmpty()) {
                config.setAllowedOrigins(allowedOrigins);
            }
        } else {
            config.setAllowedOriginPatterns(allowedOriginPatterns);
        }

        // 4. allowed-headers
        final List<String> allowedHeaders = filteredNonNullList(corsProperties.getAllowedHeaders());
        if (!allowedHeaders.isEmpty()) {
            config.setAllowedHeaders(allowedHeaders);
        }

        // 5. allowed-methods
        final List<String> allowedMethods = filteredNonNullList(corsProperties.getAllowedMethods());
        if (!allowedMethods.isEmpty()) {
            config.setAllowedMethods(allowedMethods);
        }

        // 6. exposed-headers
        final List<String> exposedHeaders = filteredNonNullList(corsProperties.getExposedHeaders());
        if (!exposedHeaders.isEmpty()) {
            config.setExposedHeaders(exposedHeaders);
        }
        // 7. max-age
        if (corsProperties.getMaxAge() != null) {
            config.setMaxAge(corsProperties.getMaxAge());
        }

        return new CorsWebFilter(source);
    }

    public static List<String> filteredNonNullList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(StringUtils::hasText).toList();
    }

}

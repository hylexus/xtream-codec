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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.PathResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author hylexus
 */
public class Jt808ServerDashboardWebFluxConfigurer implements WebFluxConfigurer {
    public static final String DASHBOARD_STATIC_RESOURCE_DIR = "/static/dashboard/808/";
    private final String basePth;
    private final XtreamJt808ServerDashboardProperties dashboardProperties;

    public Jt808ServerDashboardWebFluxConfigurer(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
        this.basePth = dashboardProperties.getBasePath();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(this.basePth + "**")
                .addResourceLocations("classpath:" + DASHBOARD_STATIC_RESOURCE_DIR)
                .resourceChain(true)
                .addResolver(new JtDashboardStaticResourceResolver(this.dashboardProperties.isForwardNotFoundToIndex()));
    }

    private static class JtDashboardStaticResourceResolver implements ResourceResolver {
        private final PathResourceResolver defaultResolver = new PathResourceResolver();
        private final boolean forward404ToIndex;

        JtDashboardStaticResourceResolver(boolean forward404ToIndex) {
            this.forward404ToIndex = forward404ToIndex;
        }

        @Override
        public Mono<Resource> resolveResource(@Nullable ServerWebExchange exchange, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            final Mono<Resource> resource = defaultResolver.resolveResource(exchange, requestPath, locations, chain);
            if (this.forward404ToIndex) {
                // 404 ==> index.html
                return resource.switchIfEmpty(Mono.just(new ClassPathResource(DASHBOARD_STATIC_RESOURCE_DIR + "index.html")));
            }
            return resource;
        }

        @Override
        public Mono<String> resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
            return defaultResolver.resolveUrlPath(resourcePath, locations, chain);
        }
    }
}

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

package io.github.hylexus.xtream.quickstart.ext.jt1078.nonblocking.config;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.PathResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DemoWebFluxConfiguration {
    public static final String QUICK_START_UI_BASE_PATH = "/jt1078-quick-start-ui/";
    public static final String QUICK_START_STATIC_RESOURCE_DIR = "/static/quickstart-ui/";

    @Bean
    RouterFunction<ServerResponse> dashboardRoutes() {
        final String basePath = QUICK_START_UI_BASE_PATH;
        final Mono<ServerResponse> dashboardIndex = ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(new ClassPathResource(QUICK_START_STATIC_RESOURCE_DIR + "index.html"));

        // `/jt1078-quick-start-ui/` ==> `/static/quickstart-ui/index.html`
        return route(GET(basePath), request -> dashboardIndex)
                // `/jt1078-quick-start-ui` ==> `/static/quickstart-ui/index.html`
                .and(route(GET(basePath.substring(0, basePath.length() - 1)), request -> dashboardIndex))
                // `/` ==> redirect to `/jt1078-quick-start-ui/`
                .and(route(GET("/"), request -> ServerResponse.temporaryRedirect(URI.create(basePath)).build()));
    }

    @Bean
    WebFluxConfigurer demoWebFluxConfigure() {
        return new WebFluxConfigurer() {
            @Override
            public void addResourceHandlers(@Nonnull ResourceHandlerRegistry registry) {
                registry.addResourceHandler(QUICK_START_UI_BASE_PATH + "**")
                        .addResourceLocations("classpath:" + QUICK_START_STATIC_RESOURCE_DIR)
                        .resourceChain(true)
                        .addResolver(new Jt1078QuickStartStaticResourceResolver(true));
            }
        };
    }

    private static class Jt1078QuickStartStaticResourceResolver implements ResourceResolver {
        private final PathResourceResolver defaultResolver = new PathResourceResolver();
        private final boolean forward404ToIndex;

        public Jt1078QuickStartStaticResourceResolver(boolean forward404ToIndex) {
            this.forward404ToIndex = forward404ToIndex;
        }

        @Override
        @Nonnull
        public Mono<Resource> resolveResource(ServerWebExchange exchange, @Nonnull String requestPath, @Nonnull List<? extends Resource> locations, @Nonnull ResourceResolverChain chain) {
            final Mono<Resource> resource = defaultResolver.resolveResource(exchange, requestPath, locations, chain);
            if (this.forward404ToIndex) {
                // 404 ==> index.html
                return resource.switchIfEmpty(Mono.just(new ClassPathResource(QUICK_START_STATIC_RESOURCE_DIR + "index.html")));
            }
            return resource;
        }

        @Override
        @Nonnull
        public Mono<String> resolveUrlPath(@Nonnull String resourcePath, @Nonnull List<? extends Resource> locations, @Nonnull ResourceResolverChain chain) {
            return defaultResolver.resolveUrlPath(resourcePath, locations, chain);
        }
    }
}

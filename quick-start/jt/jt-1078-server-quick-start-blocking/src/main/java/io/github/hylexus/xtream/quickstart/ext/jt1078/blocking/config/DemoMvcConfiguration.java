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

package io.github.hylexus.xtream.quickstart.ext.jt1078.blocking.config;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.net.URI;

@Configuration
public class DemoMvcConfiguration {

    public static final String QUICK_START_UI_BASE_PATH = "/jt1078-quick-start-ui/";
    public static final String QUICK_START_STATIC_RESOURCE_DIR = "/static/quickstart-ui/";

    @Bean
    WebMvcConfigurer demoMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(@Nonnull ResourceHandlerRegistry registry) {
                registry.addResourceHandler(QUICK_START_UI_BASE_PATH + "**")
                        .addResourceLocations("classpath:" + QUICK_START_STATIC_RESOURCE_DIR)
                        .resourceChain(true)
                        .addResolver(new Jt1078QuickStartStaticResourceResolver(true));
            }
        };
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        final String basePath = QUICK_START_UI_BASE_PATH;
        final ServerResponse dashboardIndex = ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource(QUICK_START_STATIC_RESOURCE_DIR + "index.html"));

        final RouterFunctions.Builder builder = RouterFunctions.route();
        // `/` ==> redirect to `/jt1078-quick-start-ui/`
        builder.GET("/", request -> ServerResponse.temporaryRedirect(URI.create(basePath)).build());
        // `/jt1078-quick-start-ui/` ==> `/static/quick-start-ui/index.html`
        builder.GET(basePath, request -> dashboardIndex);
        // `/jt1078-quick-start-ui` ==> `/static/quick-start-ui/index.html`
        builder.GET(basePath.substring(0, basePath.length() - 1), request -> dashboardIndex);
        return builder.build();
    }

    private static class Jt1078QuickStartStaticResourceResolver extends PathResourceResolver {
        private final boolean forward404ToIndex;

        public Jt1078QuickStartStaticResourceResolver(boolean forward404ToIndex) {
            this.forward404ToIndex = forward404ToIndex;
        }

        @Override
        protected Resource getResource(@Nonnull String resourcePath, @Nonnull Resource location) throws IOException {
            final Resource requestedResource = super.getResource(resourcePath, location);
            if (requestedResource == null || !requestedResource.exists()) {
                if (forward404ToIndex) {
                    // 如果资源不存在，并且配置了转发404到index.html，则返回index.html
                    return new ClassPathResource(QUICK_START_STATIC_RESOURCE_DIR + "index.html");
                }
                // 返回null表示资源未找到
                return null;
            }
            return requestedResource;
        }

    }
}

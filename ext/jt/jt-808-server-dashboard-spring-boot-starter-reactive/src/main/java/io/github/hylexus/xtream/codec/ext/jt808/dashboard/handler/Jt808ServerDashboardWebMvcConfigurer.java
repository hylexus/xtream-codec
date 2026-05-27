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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * @author hylexus
 */
public class Jt808ServerDashboardWebMvcConfigurer implements WebMvcConfigurer {
    public static final String DASHBOARD_STATIC_RESOURCE_DIR = "/static/dashboard/808/";
    private final String basePath;
    private final XtreamJt808ServerDashboardProperties dashboardProperties;

    public Jt808ServerDashboardWebMvcConfigurer(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
        this.basePath = dashboardProperties.getBasePath();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(basePath + "**")
                .addResourceLocations("classpath:" + DASHBOARD_STATIC_RESOURCE_DIR)
                .resourceChain(true)
                .addResolver(new JtDashboardStaticResourceResolver(dashboardProperties.isForwardNotFoundToIndex()));
    }

    private static class JtDashboardStaticResourceResolver extends PathResourceResolver {
        private final boolean forward404ToIndex;

        JtDashboardStaticResourceResolver(boolean forward404ToIndex) {
            this.forward404ToIndex = forward404ToIndex;
        }

        @Override
        protected @Nullable Resource getResource(String resourcePath, Resource location) throws IOException {
            final Resource requestedResource = super.getResource(resourcePath, location);
            if (requestedResource == null || !requestedResource.exists()) {
                if (forward404ToIndex) {
                    // 如果资源不存在，并且配置了转发404到index.html，则返回index.html
                    return new ClassPathResource(Jt808ServerDashboardWebMvcConfigurer.DASHBOARD_STATIC_RESOURCE_DIR + "index.html");
                }
                // 返回null表示资源未找到
                return null;
            }
            return requestedResource;
        }

    }
}

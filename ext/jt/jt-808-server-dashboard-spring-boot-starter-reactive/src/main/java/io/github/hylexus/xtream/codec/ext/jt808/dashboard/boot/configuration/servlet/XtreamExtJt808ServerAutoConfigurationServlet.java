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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration.servlet;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.servlet.BuiltinJt808DashboardEventControllerServlet;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.Jt808ServerDashboardWebFluxConfigurer;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.Jt808ServerDashboardWebMvcConfigurer;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Import({
        XtreamExtJt808WebMvcAutoConfiguration.class,
        XtreamJt808Rfc7807StyleWebExceptionHandlerServlet.class,
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class XtreamExtJt808ServerAutoConfigurationServlet {

    private final XtreamJt808ServerDashboardProperties dashboardProperties;

    public XtreamExtJt808ServerAutoConfigurationServlet(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
    }

    @Bean
    Jt808ServerDashboardWebMvcConfigurer jt808ServerDashboardWebMvcConfigurer() {
        return new Jt808ServerDashboardWebMvcConfigurer(this.dashboardProperties);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        final String basePath = this.dashboardProperties.getFormatedBasePath();
        final ServerResponse dashboardIndex = ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource(Jt808ServerDashboardWebFluxConfigurer.DASHBOARD_STATIC_RESOURCE_DIR + "index.html"));
        // `/dashboard-ui/` ==> index.html
        final RouterFunctions.Builder builder = RouterFunctions.route()
                .GET(basePath, request -> dashboardIndex);
        if ("/".equals(basePath)) {
            return builder.build();
        }
        // `/dashboard-ui` ==> index.html
        builder.GET(basePath.substring(0, basePath.length() - 1), request -> dashboardIndex);
        if (this.dashboardProperties.isRedirectRootToBasePath()) {
            // `/` ==> redirect to `/dashboard-ui/`
            builder.GET("/", request -> ServerResponse.temporaryRedirect(URI.create(basePath)).build());
        }
        return builder.build();
    }

    @Bean
    BuiltinJt808DashboardEventControllerServlet builtinJt808DashboardEventControllerServlet(Jt808DashboardEventService eventService) {
        return new BuiltinJt808DashboardEventControllerServlet(eventService);
    }

}

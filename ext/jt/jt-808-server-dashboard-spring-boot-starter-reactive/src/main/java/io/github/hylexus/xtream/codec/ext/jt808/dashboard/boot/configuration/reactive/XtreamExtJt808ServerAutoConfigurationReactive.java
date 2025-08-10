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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration.reactive;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.reactive.BuiltinJt808DashboardEventControllerReactive;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.Jt808ServerDashboardWebFluxConfigurer;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Import({
        XtreamExtJt808WebFluxAutoConfiguration.class,
        XtreamJt808Rfc7807StyleWebExceptionHandlerReactive.class,
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class XtreamExtJt808ServerAutoConfigurationReactive {

    private final XtreamJt808ServerDashboardProperties dashboardProperties;

    public XtreamExtJt808ServerAutoConfigurationReactive(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.dashboardProperties = dashboardProperties;
    }

    @Bean
    Jt808ServerDashboardWebFluxConfigurer jt808ServerDashboardWebFluxConfigurer() {
        return new Jt808ServerDashboardWebFluxConfigurer(this.dashboardProperties);
    }

    @Bean
    public RouterFunction<ServerResponse> dashboardRoutes() {
        final String basePath = this.dashboardProperties.getFormatedBasePath();
        final Mono<ServerResponse> dashboardIndex = ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(new ClassPathResource(Jt808ServerDashboardWebFluxConfigurer.DASHBOARD_STATIC_RESOURCE_DIR + "index.html"));
        // `/dashboard-ui/` ==> index.html
        RouterFunction<ServerResponse> routerFunction = route(GET(basePath), request -> dashboardIndex);
        if ("/".equals(basePath)) {
            return routerFunction;
        }
        // `/dashboard-ui` ==> index.html
        routerFunction = routerFunction.and(route(GET(basePath.substring(0, basePath.length() - 1)), request -> dashboardIndex));
        if (this.dashboardProperties.isRedirectRootToBasePath()) {
            // `/` ==> redirect to `/dashboard-ui/`
            routerFunction = routerFunction.and(route(GET("/"), request ->
                    ServerResponse.temporaryRedirect(URI.create(basePath)).build()
            ));
        }

        return routerFunction;
    }

    @Bean
    BuiltinJt808DashboardEventControllerReactive builtinJt808DashboardEventControllerReactive(Jt808DashboardEventService eventService) {
        return new BuiltinJt808DashboardEventControllerReactive(eventService);
    }

}

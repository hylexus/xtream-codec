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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.impl;

import io.github.hylexus.xtream.codec.base.web.proxy.XtreamWebProxy;
import io.github.hylexus.xtream.codec.base.web.proxy.XtreamWebProxyBackend;
import io.github.hylexus.xtream.codec.base.web.proxy.XtreamWebProxyExchangeFilter;
import io.github.hylexus.xtream.codec.base.web.proxy.XtreamWebProxyUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.Jt808ProxyServiceReactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

public class DefaultJt808ProxyServiceReactive implements Jt808ProxyServiceReactive {

    private static final Logger log = LoggerFactory.getLogger(DefaultJt808ProxyServiceReactive.class);
    private final XtreamWebProxy jt808DashboardWebProxy;
    private final XtreamWebProxyBackend jt808DashboardBackend;

    public DefaultJt808ProxyServiceReactive(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.jt808DashboardWebProxy = XtreamWebProxy.newBuilder()
                .webClient(WebClient.builder().build())
                .filterFunction(new Jt1078ProxyRewritePathFilter())
                .build();
        this.jt808DashboardBackend = new XtreamWebProxyBackend(dashboardProperties.getJt808DashboardProxy().getBaseUrl());
    }

    @Override
    public Mono<Void> forwardToJt808DashboardApi(ServerWebExchange exchange) {
        return XtreamWebProxyUtils.proxyReactiveRequest(
                this.jt808DashboardWebProxy,
                this.jt808DashboardBackend,
                exchange
        );
    }


    static class Jt1078ProxyRewritePathFilter implements XtreamWebProxyExchangeFilter {

        public Jt1078ProxyRewritePathFilter() {
        }

        @Override
        public Mono<ClientResponse> filter(XtreamWebProxyBackend backend, ClientRequest request, ExchangeFunction next) {
            final String originalPath = request.url().getPath();
            // /dashboard-api/jt1078/808-dashboard-proxy/v1/session/instruction-sessions ====> /dashboard-api/jt808/v1/session/instruction-sessions
            final String newPath = originalPath.replaceFirst("dashboard-api/jt1078/808-dashboard-proxy/", "dashboard-api/jt808/");
            log.info("PathRewriteFilter({}): {} ====> {}", backend.getBaseUrl(), originalPath, newPath);
            final URI newUri = UriComponentsBuilder.fromUri(request.url())
                    .replacePath(newPath)
                    .build()
                    .toUri();
            final ClientRequest newRequest = ClientRequest.from(request).url(newUri).build();
            return next.exchange(newRequest);
        }
    }

}

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
import io.github.hylexus.xtream.codec.base.web.proxy.XtreamWebProxyUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties.XtreamJt808ServerDashboardProperties;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.Jt808ProxyServiceServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;

public class DefaultJt808ProxyServiceServlet implements Jt808ProxyServiceServlet {
    private final XtreamWebProxy jt808DashboardWebProxy;
    private final XtreamWebProxyBackend jt808DashboardBackend;

    public DefaultJt808ProxyServiceServlet(XtreamJt808ServerDashboardProperties dashboardProperties) {
        this.jt808DashboardWebProxy = XtreamWebProxy.newBuilder()
                .webClient(WebClient.builder().build())
                .filterFunction(new DefaultJt808ProxyServiceReactive.Jt1078ProxyRewritePathFilter())
                .build();
        this.jt808DashboardBackend = new XtreamWebProxyBackend(dashboardProperties.getJt808DashboardProxy().getBaseUrl());
    }

    @Override
    public void forwardToJt808DashboardApi(HttpServletRequest request) {
        XtreamWebProxyUtils.proxyServletRequest(
                this.jt808DashboardWebProxy,
                this.jt808DashboardBackend,
                request
        );
    }

}

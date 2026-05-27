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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.reactive;

import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.service.Jt808ProxyServiceReactive;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class BuiltinJt1078DashboardProxyControllerReactive {

    private final Jt808ProxyServiceReactive jt808ProxyServiceReactive;

    public BuiltinJt1078DashboardProxyControllerReactive(Jt808ProxyServiceReactive jt808ProxyServiceReactive) {
        this.jt808ProxyServiceReactive = jt808ProxyServiceReactive;
    }

    // forward `{{currentDomain}}/dashboard-api/jt1078/808-dashboard-proxy/v1/**` to  `{{808ServerBackend}}/dashboard-api/jt808/v1/**`
    @RequestMapping("/dashboard-api/jt1078/808-dashboard-proxy/v1/**")
    public Mono<Void> proxyToJt808DashboardApi(ServerWebExchange exchange) {
        return this.jt808ProxyServiceReactive.forwardToJt808DashboardApi(exchange);
    }

}

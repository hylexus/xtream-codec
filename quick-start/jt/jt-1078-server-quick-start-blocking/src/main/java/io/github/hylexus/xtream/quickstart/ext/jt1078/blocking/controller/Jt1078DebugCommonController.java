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

package io.github.hylexus.xtream.quickstart.ext.jt1078.blocking.controller;

import io.github.hylexus.xtream.codec.ext.jt1078.boot.properties.XtreamJt1078ServerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/jt-1078-server-quick-start-api/v1/")
public class Jt1078DebugCommonController {
    private final WebApplicationType webApplicationType;
    private final String jt1078ServerHost;
    private final XtreamJt1078ServerProperties jt1078ServerProperties;
    private final int webServerPort;

    public Jt1078DebugCommonController(
            ApplicationContext applicationContext,
            XtreamJt1078ServerProperties jt1078ServerProperties,
            @Value("${quick-start-app-config.jt1078-server-host}") String jt1078ServerHost,
            @Value("${server.port}") int webServerPort) {
        this.webApplicationType = applicationContext instanceof ReactiveWebApplicationContext ? WebApplicationType.REACTIVE : WebApplicationType.SERVLET;
        this.jt1078ServerHost = jt1078ServerHost;
        this.jt1078ServerProperties = jt1078ServerProperties;
        this.webServerPort = webServerPort;
    }

    @GetMapping("/server-config")
    public Map<String, Object> serverConfig() {
        return Map.of(
                "webApplicationType", webApplicationType,
                "jt1078ServerHost", jt1078ServerHost,
                "jt1078ServerTcpPort", jt1078ServerProperties.getTcpServer().getPort(),
                "jt1078ServerUdpPort", jt1078ServerProperties.getUdpServer().getPort(),
                "jt1078ServerWebPort", webServerPort
        );
    }

}

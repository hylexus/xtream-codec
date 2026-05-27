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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.ServerInfoVo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMappingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt808/v1")
public class BuiltinJt808DashboardCommonController {

    private final Instant serverStartupTime = Instant.now();

    private final XtreamJt808ServerProperties serverProperties;
    private final Jt808DashboardMappingService dashboardMappingService;

    public BuiltinJt808DashboardCommonController(XtreamJt808ServerProperties serverProperties, Jt808DashboardMappingService dashboardMappingService) {
        this.serverProperties = serverProperties;
        this.dashboardMappingService = dashboardMappingService;
    }

    @GetMapping("/server-info")
    public ServerInfoVo serverInfo() {
        return new ServerInfoVo(this.serverStartupTime, this.serverProperties);
    }

    @GetMapping("/actuator/mappings")
    public Map<String, Object> mappings() {
        return this.dashboardMappingService.getJt808HandlerMappings();
    }

}

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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.servlet;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.LinkDataDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtWebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Optional;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/v1/event")
public class BuiltinJt808DashboardEventControllerServlet {
    private static final Logger log = LoggerFactory.getLogger(BuiltinJt808DashboardEventControllerServlet.class);
    private final Jt808DashboardEventService dashboardEventService;

    public BuiltinJt808DashboardEventControllerServlet(Jt808DashboardEventService dashboardEventService) {
        this.dashboardEventService = dashboardEventService;
    }

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events</a>
     */
    @GetMapping("/link-data")
    public Flux<ServerSentEvent<Object>> getLinkData(HttpServletRequest request, @Validated LinkDataDto dto) {
        final String clientIp = JtWebUtils.getClientIp(request::getHeader)
                .or(() -> Optional.ofNullable(request.getRemoteAddr()))
                .orElse("unknown");
        return this.dashboardEventService.linkData(clientIp, dto);
    }

    @PostMapping("/link-data")
    public Flux<ServerSentEvent<Object>> linkData(HttpServletRequest request, @Validated @RequestBody LinkDataDto dto) {
        final String clientIp = JtWebUtils.getClientIp(request::getHeader)
                .or(() -> Optional.ofNullable(request.getRemoteAddr()))
                .orElse("unknown");
        return this.dashboardEventService.linkData(clientIp, dto);
    }

}

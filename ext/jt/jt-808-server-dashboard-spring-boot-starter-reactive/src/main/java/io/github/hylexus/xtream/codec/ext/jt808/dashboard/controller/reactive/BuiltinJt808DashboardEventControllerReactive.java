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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.reactive;

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.LinkDataDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt808/v1/event")
public class BuiltinJt808DashboardEventControllerReactive {
    // private static final Logger log = LoggerFactory.getLogger(BuiltinJt808DashboardEventControllerReactive.class);
    private final Jt808DashboardEventService dashboardEventService;

    public BuiltinJt808DashboardEventControllerReactive(Jt808DashboardEventService dashboardEventService) {
        this.dashboardEventService = dashboardEventService;
    }

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events</a>
     */
    @GetMapping("/link-data")
    public Flux<ServerSentEvent<Object>> getLinkData(@Validated LinkDataDto dto, @ClientIp String clientIp) {
        return this.dashboardEventService.linkData(clientIp, dto);
    }

    @PostMapping("/link-data")
    public Flux<ServerSentEvent<Object>> linkData(@Validated @RequestBody LinkDataDto dto, @ClientIp String clientIp) {
        return this.dashboardEventService.linkData(clientIp, dto);
    }

}

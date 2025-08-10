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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsService;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsServiceWithMicroMeter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/dashboard-api/jt808/v1/metrics")
public class BuiltinJt808DashboardMetricsController {
    private static final Logger log = LoggerFactory.getLogger(BuiltinJt808DashboardMetricsController.class);

    private final Jt808DashboardMetricsService jt808DashboardMetricsService;
    private final Jt808DashboardMetricsServiceWithMicroMeter jt808DashboardMetricsServiceWithMicroMeter;

    public BuiltinJt808DashboardMetricsController(Jt808DashboardMetricsService jt808DashboardMetricsService, Jt808DashboardMetricsServiceWithMicroMeter jt808DashboardMetricsServiceWithMicroMeter) {
        this.jt808DashboardMetricsService = jt808DashboardMetricsService;
        this.jt808DashboardMetricsServiceWithMicroMeter = jt808DashboardMetricsServiceWithMicroMeter;
    }

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events</a>
     */
    @RequestMapping(value = "/basic", method = {RequestMethod.GET, RequestMethod.POST})
    public Flux<ServerSentEvent<Object>> metrics(
            @Validated
            @Min(message = "`duration` must be greater than or equal to 5", value = 5)
            @Max(message = "`duration` must be letter than or equal to 60", value = 60)
            @RequestParam(value = "duration", required = false, defaultValue = "5") long duration) {

        return this.jt808DashboardMetricsService.getBasicMetrics(Duration.ofSeconds(duration));
    }

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events</a>
     */
    @RequestMapping(value = "/thread-dump", method = {RequestMethod.GET, RequestMethod.POST})
    public Flux<ServerSentEvent<Object>> threadDump(
            @Validated
            @Min(message = "`duration` must be greater than or equal to 5", value = 5)
            @Max(message = "`duration` must be letter than or equal to 60", value = 60)
            @RequestParam(value = "duration", required = false, defaultValue = "5") long duration) {

        return this.jt808DashboardMetricsService.getThreadDumpMetrics(Duration.ofSeconds(duration));
    }

    /**
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events</a>
     */
    @RequestMapping(value = "/schedulers", method = {RequestMethod.GET, RequestMethod.POST})
    public Flux<ServerSentEvent<Object>> schedulers(
            @Validated
            @Min(message = "`duration` must be greater than or equal to 5", value = 5)
            @Max(message = "`duration` must be letter than or equal to 60", value = 60)
            @RequestParam(value = "duration", required = false, defaultValue = "5") long duration) {

        if (this.jt808DashboardMetricsServiceWithMicroMeter == null) {
            return Flux.empty();
        }
        return this.jt808DashboardMetricsServiceWithMicroMeter.getSchedulerMetrics(Duration.ofSeconds(duration));
    }

}

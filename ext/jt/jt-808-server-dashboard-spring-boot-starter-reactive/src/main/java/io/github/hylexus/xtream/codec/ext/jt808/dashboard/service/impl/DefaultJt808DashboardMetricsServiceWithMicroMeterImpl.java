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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SchedulerMetrics;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsServiceWithMicroMeter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Stream;

public class DefaultJt808DashboardMetricsServiceWithMicroMeterImpl implements Jt808DashboardMetricsServiceWithMicroMeter {
    private final XtreamSchedulerRegistry schedulerRegistry;
    private final MeterRegistry meterRegistry;

    public DefaultJt808DashboardMetricsServiceWithMicroMeterImpl(XtreamSchedulerRegistry schedulerRegistry, MeterRegistry meterRegistry) {
        this.schedulerRegistry = schedulerRegistry;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Flux<ServerSentEvent<Object>> getSchedulerMetrics(Duration duration) {
        return this.schedulerMetrics()
                .concatWith(Flux.interval(duration)
                        .flatMap(ignored -> this.schedulerMetrics())
                );
    }

    private Flux<ServerSentEvent<Object>> schedulerMetrics() {
        final LocalDateTime now = LocalDateTime.now();
        final Stream<Object> stream = this.schedulerRegistry.schedulerConfigAsMapView().values().stream()
                .filter(XtreamSchedulerRegistry.SchedulerConfig::metricsEnabled)
                .map(it -> {
                    final LongTaskTimer activeMeter = this.meterRegistry.get(it.metricsPrefix() + ".scheduler.tasks.active").longTaskTimer();
                    final Timer completedMeter = this.meterRegistry.get(it.metricsPrefix() + ".scheduler.tasks.completed").timer();
                    final LongTaskTimer pendingMeter = this.meterRegistry.get(it.metricsPrefix() + ".scheduler.tasks.pending").longTaskTimer();
                    final SchedulerMetrics.Submitted submitted = this.createSubmittedMetrics(it);
                    final SchedulerMetrics metrics = new SchedulerMetrics(it, activeMeter, completedMeter, pendingMeter, submitted);
                    return new SimpleTypes.TimeSeries<>(now, metrics);
                });
        return Flux.fromStream(stream)
                .map(it -> ServerSentEvent.builder(it).event("schedulerMetrics").build());
    }

    private SchedulerMetrics.Submitted createSubmittedMetrics(XtreamSchedulerRegistry.SchedulerConfig it) {
        long direct = 0;
        long delayed = 0;
        long periodicIteration = 0;
        long periodicInitial = 0;
        final Collection<Counter> meters = this.meterRegistry.get(it.metricsPrefix() + ".scheduler.tasks.submitted").counters();
        for (Counter counter : meters) {
            final String tag = counter.getId().getTag("submission.type");
            switch (tag) {
                case "direct" -> direct = (long) counter.count();
                case "delayed" -> delayed = (long) counter.count();
                case "periodic_iteration" -> periodicIteration = (long) counter.count();
                case "periodic_initial" -> periodicInitial = (long) counter.count();
                case null, default -> {
                }
            }
        }
        return new SchedulerMetrics.Submitted(direct, delayed, periodicIteration, periodicInitial);
    }

}

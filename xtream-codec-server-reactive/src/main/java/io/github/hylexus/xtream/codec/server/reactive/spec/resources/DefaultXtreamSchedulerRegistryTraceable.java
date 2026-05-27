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

package io.github.hylexus.xtream.codec.server.reactive.spec.resources;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.observability.micrometer.Micrometer;

import java.util.List;

public class DefaultXtreamSchedulerRegistryTraceable extends DefaultXtreamSchedulerRegistry {
    protected final MeterRegistry meterRegistry;

    public DefaultXtreamSchedulerRegistryTraceable(List<XtreamSchedulerRegistryCustomizer> customizers, MeterRegistry meterRegistry) {
        super(customizers, ((config, scheduler) -> {
            if (config.metricsEnabled()) {
                return Micrometer.timedScheduler(scheduler, meterRegistry, config.metricsPrefix());
            }
            return scheduler;
        }));
        this.meterRegistry = meterRegistry;
    }

}

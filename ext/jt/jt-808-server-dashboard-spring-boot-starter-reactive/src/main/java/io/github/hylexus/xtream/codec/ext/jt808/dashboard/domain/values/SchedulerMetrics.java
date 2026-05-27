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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Timer;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public record SchedulerMetrics(
        String name,
        String remark,
        Map<String, Serializable> metadata,
        Value value) {

    public SchedulerMetrics(
            XtreamSchedulerRegistry.SchedulerConfig config,
            LongTaskTimer activeMeter,
            Timer completedMeter,
            LongTaskTimer pendingMeter,
            Submitted submitted) {
        this(
                config.name(), config.remark(), config.metadata(),
                new Value(
                        new Active(activeMeter.activeTasks(), activeMeter.duration(TimeUnit.SECONDS)),
                        new Completed(completedMeter.count(), completedMeter.totalTime(TimeUnit.SECONDS), (int) completedMeter.max(TimeUnit.SECONDS)),
                        new Pending(pendingMeter.activeTasks(), pendingMeter.duration(TimeUnit.SECONDS)),
                        submitted
                )
        );
    }

    public record Value(Active active, Completed completed, Pending pending, Submitted submitted) {
    }

    public record Active(int tasks, double duration) {
    }

    public record Completed(long count, double total, int max) {
    }

    public record Pending(int active, double duration) {
    }

    public record Submitted(long direct, long delayed, long periodicIteration, long periodicInitial) {
    }
}

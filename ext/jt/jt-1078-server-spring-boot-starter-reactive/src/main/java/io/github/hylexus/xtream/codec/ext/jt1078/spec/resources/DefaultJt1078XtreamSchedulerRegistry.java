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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.resources;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.BiFunction;

public class DefaultJt1078XtreamSchedulerRegistry
        extends DefaultXtreamSchedulerRegistry
        implements Jt1078XtreamSchedulerRegistry {

    protected final Scheduler avCodecScheduler;
    protected final Scheduler avSubscriberScheduler;

    public DefaultJt1078XtreamSchedulerRegistry(List<XtreamSchedulerRegistryCustomizer> customizers, BiFunction<SchedulerConfig, Scheduler, Scheduler> wrapper) {
        super(customizers, wrapper);
        this.avCodecScheduler = this.getScheduler(SCHEDULER_NAME_AV_CODEC)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine default av-codec scheduler"));
        this.avSubscriberScheduler = this.getScheduler(SCHEDULER_NAME_AV_SUBSCRIBER)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine default av-subscriber scheduler"));
    }

    @Override
    protected void afterInit() {
        // 不做任何事
        // jt1078 不需要有 requestDispatcherScheduler 等调度器
    }

    @Override
    public Scheduler audioVideoCodecScheduler() {
        return this.avCodecScheduler;
    }

    @Override
    public Scheduler audioVideoSubscriberScheduler() {
        return this.avSubscriberScheduler;
    }
}

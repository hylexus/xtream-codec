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

package io.github.hylexus.xtream.codec.ext.jt808.processor;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamServerSchedulerProperties;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.scheduler.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.XtreamReactorThreadFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DefaultJt808XtreamScheduleRegistryCustomizer implements XtreamSchedulerRegistryCustomizer {
    protected final XtreamServerSchedulerProperties schedulerProperties;
    protected final String schedulerName;

    public DefaultJt808XtreamScheduleRegistryCustomizer(String schedulerName, XtreamServerSchedulerProperties schedulerProperties) {
        this.schedulerProperties = schedulerProperties;
        this.schedulerName = schedulerName;
    }

    @Override
    public void customize(XtreamSchedulerRegistry registry) {
        final Scheduler scheduler = createScheduler(this.schedulerProperties);
        registry.registerScheduler(this.schedulerProperties.toSchedulerConfig(this.schedulerName), scheduler);
    }

    @SuppressWarnings("deprecation")
    public static Scheduler createScheduler(XtreamServerSchedulerProperties property) {
        final SchedulerType schedulerType = property.getType();
        return switch (schedulerType) {
            case BOUNDED_ELASTIC -> newBoundedElastic(property.getBoundedElastic());
            case PARALLEL -> newParallel(property.getParallel());
            case VIRTUAL -> newVirtualThreadPerTaskExecutor(property.getVirtual());
            case SINGLE -> newSingle(property.getSingle());
            case IMMEDIATE -> Schedulers.immediate();
            default -> throw new IllegalArgumentException("Unsupported SchedulerType: " + schedulerType);
        };
    }

    /**
     * @see Executors#newVirtualThreadPerTaskExecutor()
     */
    public static Scheduler newVirtualThreadPerTaskExecutor(VirtualThreadProperties properties) {
        final ThreadFactory factory = Thread.ofVirtual()
                .name(properties.getPrefix().endsWith("-") ? properties.getPrefix() : properties.getPrefix() + "-", properties.getStart())
                .inheritInheritableThreadLocals(properties.isInheritInheritableThreadLocals())
                .factory();
        final ExecutorService executorService = Executors.newThreadPerTaskExecutor(factory);
        return Schedulers.fromExecutorService(executorService);
    }

    public static Scheduler newSingle(SingleProperties single) {
        return Schedulers.newSingle(
                new XtreamReactorThreadFactory(
                        single.getThreadNamePrefix(),
                        single.isDaemon(),
                        single.isRejectBlockingTask()
                )
        );
    }

    public static Scheduler newParallel(ParallelProperties properties) {
        return Schedulers.newParallel(
                properties.getParallelism(),
                new XtreamReactorThreadFactory(
                        properties.getThreadNamePrefix(),
                        properties.isDaemon(),
                        properties.isRejectBlockingTask()
                )
        );
    }

    public static Scheduler newBoundedElastic(BoundedElasticProperties properties) {
        return Schedulers.newBoundedElastic(
                properties.getThreadCapacity(),
                properties.getQueuedTaskCapacity(),
                new XtreamReactorThreadFactory(
                        properties.getThreadNamePrefix(),
                        properties.isDaemon(),
                        properties.isRejectBlockingTask()
                ),
                (int) properties.getTtl().toSeconds()
        );
    }
}

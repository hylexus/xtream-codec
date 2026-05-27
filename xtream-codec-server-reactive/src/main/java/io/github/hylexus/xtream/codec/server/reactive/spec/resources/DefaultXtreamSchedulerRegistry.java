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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import org.jspecify.annotations.Nullable;
import reactor.core.scheduler.Scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author hylexus
 */
@SuppressWarnings("NullAway")
public class DefaultXtreamSchedulerRegistry implements XtreamSchedulerRegistry {

    protected final @Nullable Scheduler requestDispatcherScheduler;
    protected final Scheduler defaultNonBlockingScheduler;
    protected final Scheduler defaultBlockingScheduler;
    protected final Scheduler eventPublisherScheduler;
    protected final ConcurrentHashMap<String, Scheduler> schedulerMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, SchedulerConfig> schedulerConfigMap = new ConcurrentHashMap<>();
    protected final BiFunction<SchedulerConfig, Scheduler, Scheduler> wrapper;

    public DefaultXtreamSchedulerRegistry(List<XtreamSchedulerRegistryCustomizer> customizers, BiFunction<SchedulerConfig, Scheduler, Scheduler> wrapper) {
        this.wrapper = wrapper;
        for (final XtreamSchedulerRegistryCustomizer customizer : customizers) {
            customizer.customize(this);
        }
        this.requestDispatcherScheduler = this.getScheduler(SCHEDULER_NAME_REQUEST_DISPATCHER).orElse(null);
        this.defaultNonBlockingScheduler = this.getScheduler(SCHEDULER_NAME_NON_BLOCKING).orElse(null);
        this.defaultBlockingScheduler = this.getScheduler(SCHEDULER_NAME_BLOCKING).orElse(null);
        this.eventPublisherScheduler = this.getScheduler(SCHEDULER_NAME_EVENT_PUBLISHER).orElse(null);
        this.afterInit();
    }

    public DefaultXtreamSchedulerRegistry(Scheduler defaultNonBlockingScheduler, Scheduler defaultBlockingScheduler, Scheduler eventPublisherScheduler) {
        this(null, defaultNonBlockingScheduler, defaultBlockingScheduler, eventPublisherScheduler);
    }

    public DefaultXtreamSchedulerRegistry(@Nullable Scheduler requestDispatcherScheduler, Scheduler defaultNonBlockingScheduler, Scheduler defaultBlockingScheduler, Scheduler eventPublisherScheduler) {
        this.wrapper = (config, scheduler) -> scheduler;
        this.requestDispatcherScheduler = requestDispatcherScheduler;
        this.defaultNonBlockingScheduler = defaultNonBlockingScheduler;
        this.defaultBlockingScheduler = defaultBlockingScheduler;
        this.eventPublisherScheduler = eventPublisherScheduler;

        if (requestDispatcherScheduler != null) {
            this.registerScheduler(SCHEDULER_NAME_REQUEST_DISPATCHER, requestDispatcherScheduler);
        }
        this.registerScheduler(SCHEDULER_NAME_BLOCKING, defaultBlockingScheduler);
        this.registerScheduler(SCHEDULER_NAME_NON_BLOCKING, defaultNonBlockingScheduler);
        this.registerScheduler(SCHEDULER_NAME_EVENT_PUBLISHER, eventPublisherScheduler);

        this.afterInit();
    }

    @SuppressWarnings("ReturnValueIgnored")
    protected void afterInit() {
        this.getScheduler(SCHEDULER_NAME_NON_BLOCKING)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine default non-blocking scheduler"));
        this.getScheduler(SCHEDULER_NAME_BLOCKING)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine default blocking scheduler"));
        this.getScheduler(SCHEDULER_NAME_EVENT_PUBLISHER)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine default eventPublisher scheduler"));
    }

    @Override
    public @Nullable Scheduler requestDispatcherScheduler() {
        return this.requestDispatcherScheduler;
    }

    @Override
    public Scheduler defaultNonBlockingScheduler() {
        return this.defaultNonBlockingScheduler;
    }

    @Override
    public Scheduler defaultBlockingScheduler() {
        return this.defaultBlockingScheduler;
    }

    @Override
    public Scheduler eventPublisherScheduler() {
        return this.eventPublisherScheduler;
    }

    @Override
    public Optional<Scheduler> getScheduler(String name) {
        return Optional.ofNullable(this.schedulerMap.get(name));
    }

    @Override
    public synchronized boolean registerScheduler(SchedulerConfig config, Scheduler scheduler) {
        final String name = config.name();
        final Scheduler v = this.schedulerMap.putIfAbsent(name, this.wrapper.apply(config, scheduler));
        final boolean registered = v == null;
        if (registered) {
            this.schedulerConfigMap.put(name, config);
        }
        return registered;
    }

    @Override
    public boolean removeScheduler(String name) {
        if (SCHEDULER_NAME_REQUEST_DISPATCHER.equals(name) || SCHEDULER_NAME_BLOCKING.equals(name) || SCHEDULER_NAME_NON_BLOCKING.equals(name) || SCHEDULER_NAME_EVENT_PUBLISHER.equals(name)) {
            throw new UnsupportedOperationException("Cannot remove default scheduler");
        }

        final Scheduler v = this.schedulerMap.remove(name);
        this.schedulerConfigMap.remove(name);
        return v != null;
    }

    @Override
    public Map<String, Scheduler> asMapView() {
        return Collections.unmodifiableMap(this.schedulerMap);
    }

    @Override
    public Map<String, SchedulerConfig> schedulerConfigAsMapView() {
        return Collections.unmodifiableMap(this.schedulerConfigMap);
    }

    @Override
    public Optional<SchedulerConfig> getSchedulerConfig(String name) {
        final SchedulerConfig config = this.schedulerConfigMap.get(name);
        return Optional.ofNullable(config);
    }

}

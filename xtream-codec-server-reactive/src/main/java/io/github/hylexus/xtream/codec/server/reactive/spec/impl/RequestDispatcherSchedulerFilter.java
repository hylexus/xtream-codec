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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * @author hylexus
 */
public class RequestDispatcherSchedulerFilter implements XtreamFilter {

    protected final Scheduler scheduler;

    public RequestDispatcherSchedulerFilter(XtreamSchedulerRegistry schedulerRegistry) {
        this.scheduler = Objects.requireNonNull(schedulerRegistry.requestDispatcherScheduler());
    }

    @Override
    public Mono<Void> filter(XtreamExchange exchange, XtreamFilterChain chain) {
        return Mono.just(exchange)
                .publishOn(scheduler)
                .flatMap(chain::filter);
    }

    @Override
    public int order() {
        return OrderedComponent.HIGHEST_PRECEDENCE;
    }
}

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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import org.springframework.util.StringUtils;
import reactor.core.scheduler.Scheduler;

public abstract class AbstractXtreamRequestMappingHandlerMapping implements XtreamHandlerMapping {
    protected final XtreamSchedulerRegistry schedulerRegistry;
    protected final XtreamBlockingHandlerMethodPredicate blockingHandlerMethodPredicate;

    protected AbstractXtreamRequestMappingHandlerMapping(XtreamSchedulerRegistry schedulerRegistry, XtreamBlockingHandlerMethodPredicate blockingHandlerMethodPredicate) {
        this.schedulerRegistry = schedulerRegistry;
        this.blockingHandlerMethodPredicate = blockingHandlerMethodPredicate;
    }

    protected XtreamHandlerMethod.HandlerMethodSchedulerInfo determineSchedulerInfo(XtreamHandlerMethod handlerMethod, String methodLevelScheduler, String blockingScheduler, String nonBlockingScheduler) {
        final boolean isBlockingHandler = this.blockingHandlerMethodPredicate.isBlockingHandlerMethod(handlerMethod);
        if (StringUtils.hasText(methodLevelScheduler)) {
            return this.getSchedulerOrThrow(methodLevelScheduler, isBlockingHandler);
        }
        if (isBlockingHandler) {
            if (StringUtils.hasText(blockingScheduler)) {
                return this.getSchedulerOrThrow(blockingScheduler, true);
            }
            throw new IllegalArgumentException("Cannot determine `blockingScheduler`. Because `schedulerName` is EMPTY");
        }
        if (StringUtils.hasText(nonBlockingScheduler)) {
            return this.getSchedulerOrThrow(nonBlockingScheduler, false);
        }

        throw new IllegalArgumentException("Cannot determine `nonBlockingScheduler`. Because `schedulerName` is EMPTY");
    }

    protected XtreamHandlerMethod.HandlerMethodSchedulerInfo getSchedulerOrThrow(String schedulerName, boolean isBlocking) {
        final Scheduler scheduler = this.schedulerRegistry.getScheduler(schedulerName)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine `Scheduler` with name `" + schedulerName + "`"));
        final XtreamSchedulerRegistry.SchedulerConfig config = this.schedulerRegistry.getSchedulerConfig(schedulerName)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine `SchedulerConfig` with name `" + schedulerName + "`"));
        return new XtreamHandlerMethod.HandlerMethodSchedulerInfo(isBlocking, scheduler, config);
    }

}

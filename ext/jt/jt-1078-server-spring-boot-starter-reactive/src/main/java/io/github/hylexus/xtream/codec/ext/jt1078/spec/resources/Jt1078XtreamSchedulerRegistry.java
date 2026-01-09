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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import org.jspecify.annotations.Nullable;
import reactor.core.scheduler.Scheduler;

public interface Jt1078XtreamSchedulerRegistry extends XtreamSchedulerRegistry {

    String SCHEDULER_NAME_AV_CODEC = "x78-av-codec";
    String SCHEDULER_NAME_AV_SUBSCRIBER = "x78-av-subscriber";

    Scheduler audioVideoCodecScheduler();

    Scheduler audioVideoSubscriberScheduler();

    @Override
    @Deprecated
    default @Nullable Scheduler requestDispatcherScheduler() {
        throw new UnsupportedOperationException("Jt1078XtreamSchedulerRegistry does not support requestDispatcherScheduler()");
    }

    @Override
    @Deprecated
    default Scheduler defaultBlockingScheduler() {
        throw new UnsupportedOperationException("Jt1078XtreamSchedulerRegistry does not support defaultBlockingScheduler()");
    }

    @Override
    @Deprecated
    default Scheduler defaultNonBlockingScheduler() {
        throw new UnsupportedOperationException("Jt1078XtreamSchedulerRegistry does not support defaultNonBlockingScheduler()");
    }

    @Override
    @Deprecated
    default Scheduler eventPublisherScheduler() {
        throw new UnsupportedOperationException("Jt1078XtreamSchedulerRegistry does not support eventPublisherScheduler()");
    }

}

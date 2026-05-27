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

package io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.scheduler;

import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public enum SchedulerType {
    /**
     * @see reactor.core.scheduler.Schedulers#newBoundedElastic(int, int, ThreadFactory, int)
     */
    BOUNDED_ELASTIC,
    /**
     * @see reactor.core.scheduler.Schedulers#newParallel(int, ThreadFactory)
     */
    PARALLEL,
    /**
     * 基于虚拟线程
     *
     * @see Executors#newVirtualThreadPerTaskExecutor()
     */
    VIRTUAL,
    /**
     * 用户自定义
     */
    CUSTOMIZED,
    /**
     * 仅仅用于测试
     *
     * @see reactor.core.scheduler.Schedulers#newSingle(ThreadFactory)
     */
    @Deprecated
    SINGLE,
    /**
     * 仅仅用于测试
     *
     * @see Schedulers#immediate()
     */
    @Deprecated
    IMMEDIATE,
}

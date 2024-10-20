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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamServerConstants;
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.Optional;

/**
 * @author hylexus
 */
public interface XtreamSchedulerRegistry {
    String SCHEDULER_NAME_NON_BLOCKING = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER;
    String SCHEDULER_NAME_BLOCKING = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_BLOCKING_SCHEDULER;

    default Scheduler defaultNonBlockingScheduler() {
        return this.getScheduler(SCHEDULER_NAME_NON_BLOCKING).orElseThrow();
    }

    default Scheduler defaultBlockingScheduler() {
        return this.getScheduler(SCHEDULER_NAME_BLOCKING).orElseThrow();
    }

    /**
     * @param name 调度器名称
     */
    Optional<Scheduler> getScheduler(String name);

    /**
     * @param name      要注册的调度器的名称
     * @param scheduler 要注册的调度器
     * @return {@code true}: 成功注册; {@code false}: 已经存在同名调度器
     */
    boolean registerScheduler(String name, Scheduler scheduler);

    /**
     * @param name 要移除的调度器名称
     * @return {@code true}: 成功移除; {@code false}: 要移除的调度器不存在
     * @throws UnsupportedOperationException 尝试移除默认调度器
     */
    boolean removeScheduler(String name) throws UnsupportedOperationException;

    /**
     * @return 只读视图
     */
    Map<String, Scheduler> asMapView();

}

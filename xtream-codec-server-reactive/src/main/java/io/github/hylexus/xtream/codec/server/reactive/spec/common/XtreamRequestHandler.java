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

package io.github.hylexus.xtream.codec.server.reactive.spec.common;


import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;

import java.lang.annotation.*;


/**
 * @author hylexus
 * @see XtreamRequestHandlerMapping#scheduler()
 * @see XtreamSchedulerRegistry#defaultNonBlockingScheduler()
 * @see XtreamSchedulerRegistry#defaultBlockingScheduler()
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XtreamRequestHandler {

    /**
     * 默认的 <strong style="color:red;">非阻塞</strong> 处理器的调度器。
     * <p>
     * 可以被 {@link XtreamRequestHandlerMapping#scheduler()} 覆盖
     *
     * @see XtreamRequestHandlerMapping#scheduler()
     */
    String nonBlockingScheduler() default XtreamSchedulerRegistry.SCHEDULER_NAME_NON_BLOCKING;

    /**
     * 默认的 <strong style="color:red;">阻塞</strong> 处理器的调度器。
     * <p>
     * 可以被 {@link XtreamRequestHandlerMapping#scheduler()} 覆盖
     *
     * @see XtreamRequestHandlerMapping#scheduler()
     */
    String blockingScheduler() default XtreamSchedulerRegistry.SCHEDULER_NAME_BLOCKING;

}

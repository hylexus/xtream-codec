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
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XtreamRequestHandlerMapping {

    /**
     * 当前处理器方法使用的 {@link reactor.core.scheduler.Scheduler Scheduler} 。
     * <p>
     * 默认逻辑:
     * <ol>
     *     <li>如果用户指定了自定义的调度器名称 {@code scheduler}，就使用 {@link XtreamSchedulerRegistry#getScheduler(String scheduler)} 返回的调度器；
     *     用户未指定自定义调度器时通过后面步骤确定当前处理器方法使用的调度器:</li>
     *     <li>如果当前处理器方法是 <strong style="color:red;">阻塞的</strong>，就使用 {@link XtreamRequestHandler#blockingScheduler()} 或 {@link XtreamSchedulerRegistry#defaultBlockingScheduler()}</li>
     *     <li>如果当前处理器方法是 <strong style="color:red;">非阻塞的</strong>，就使用 {@link XtreamRequestHandler#nonBlockingScheduler()}  或  {@link XtreamSchedulerRegistry#defaultNonBlockingScheduler()} </li>
     * </ol>
     *
     * @apiNote 通过 {@link io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate} 判断处理器方法是否阻塞
     * @see reactor.core.scheduler.Scheduler
     * @see XtreamRequestHandler#nonBlockingScheduler()
     * @see XtreamRequestHandler#blockingScheduler()
     * @see io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry#getScheduler(String)
     * @see io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate
     */
    String scheduler() default "";

}

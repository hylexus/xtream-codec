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

package io.github.hylexus.xtream.quickstart.custom.annotation.annotation;

import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandlerMapping;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/// 标记方法能处理的 X-IoT Demo 消息类型。
///
/// 被 `@XtreamRequestHandlerMapping` 标记后，`@DemoMessageHandler` 能自动识别该方法。
///
/// @see DemoMessageHandler
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamRequestHandlerMapping
public @interface DemoMessageMapping {

    /// @return 当前处理器方法能处理的消息类型（对应 X-IoT Demo 协议的 `msgType` 字段）
    int[] msgType();

    /// `scheduler()` 别名 —— 对应 `@XtreamRequestHandlerMapping.scheduler()`。
    ///
    /// 指定当前方法运行在哪个 `Scheduler` 上。
    ///
    /// - 默认空字符串：使用框架默认的调度器（非阻塞）
    /// - 指定名称：从 `XtreamSchedulerRegistry` 中查找
    ///
    /// @see io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate
    @AliasFor(annotation = XtreamRequestHandlerMapping.class, attribute = "scheduler")
    String scheduler() default "";
}

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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;


import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandler;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * @author hylexus
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamRequestHandler
public @interface Jt808RequestHandler {

    @AliasFor(annotation = XtreamRequestHandler.class, attribute = "nonBlockingScheduler")
    String nonBlockingScheduler() default XtreamSchedulerRegistry.SCHEDULER_NAME_NON_BLOCKING;

    @AliasFor(annotation = XtreamRequestHandler.class, attribute = "blockingScheduler")
    String blockingScheduler() default XtreamSchedulerRegistry.SCHEDULER_NAME_BLOCKING;

}

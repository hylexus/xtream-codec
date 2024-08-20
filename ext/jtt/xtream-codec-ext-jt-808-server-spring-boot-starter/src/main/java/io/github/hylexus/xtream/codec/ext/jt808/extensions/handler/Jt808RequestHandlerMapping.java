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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;


import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandlerMapping;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * @author hylexus
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@XtreamRequestHandlerMapping
public @interface Jt808RequestHandlerMapping {

    @AliasFor(annotation = XtreamRequestHandlerMapping.class, attribute = "scheduler")
    String scheduler() default "";

    /**
     * @return 当前处理器方法能处理的消息类型
     */
    int[] messageIds();

    /**
     * @return 当前处理器能处理的消息对应的版本
     */
    Jt808ProtocolVersion[] versions() default {Jt808ProtocolVersion.AUTO_DETECTION};

    String desc() default "";

}

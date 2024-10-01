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

package io.github.hylexus.xtream.codec.core.annotation;

import java.lang.annotation.*;

/**
 * 默认解析顺序:
 * <ol>
 *     <li>处理器类上的注解</li>
 *     <li>处理器方法上的注解</li>
 *     <li>处理器方法返回类型上的注解</li>
 * </ol>
 *
 * @author hylexus
 * @see "io.github.hylexus.xtream.codec.server.reactive.spec.impl.XtreamResponseBodyHandlerResultHandler"
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XtreamResponseBody {
}

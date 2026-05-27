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

package io.github.hylexus.xtream.codec.core.annotation;

import io.github.hylexus.xtream.codec.common.bean.PropertyAccessStrategy;

import java.lang.annotation.*;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.common.bean.PropertyAccessorFactory
 * @since 0.3.0
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XtreamEntity {

    /**
     * 当前类的属性访问策略。
     * <p>
     * 可以被 {@link XtreamField#propertyAccessStrategy()} 覆盖。
     *
     * @see XtreamField#propertyAccessStrategy()
     */
    PropertyAccessStrategy propertyAccessStrategy() default PropertyAccessStrategy.AUTO;

}

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

import io.netty.buffer.ByteBuf;

import java.lang.annotation.*;

/**
 * @author hylexus
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XtreamRequestBody {

    /**
     * 该属性仅仅用于 {@link ByteBuf} 类型
     *
     * @return {@code true}: 如果目标对象是 {@link ByteBuf} 类型; 返回 {@link ByteBuf#slice()} 而不是 {@link ByteBuf} 本身
     */
    boolean bufferAsSlice() default true;

}

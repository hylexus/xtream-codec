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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import org.jspecify.annotations.Nullable;

/**
 * 当前类是从 `org.springframework.web.reactive.HandlerResult` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.reactive.HandlerResult`.
 *
 * @author hylexus
 */
public class XtreamHandlerResult {

    private final Object handler;

    private final @Nullable Object returnValue;
    private final XtreamMethodParameter returnType;

    private @Nullable XtreamDispatchExceptionHandler exceptionHandler;

    public XtreamHandlerResult(Object handler, @Nullable Object returnValue, XtreamMethodParameter returnType) {
        this.handler = handler;
        this.returnValue = returnValue;
        this.returnType = returnType;
    }

    public Object getHandler() {
        return this.handler;
    }

    public @Nullable Object getReturnValue() {
        return returnValue;
    }

    public XtreamMethodParameter getReturnType() {
        return returnType;
    }

    public @Nullable XtreamDispatchExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public String toString() {
        return "XtreamHandlerResult{"
               + "handler=" + handler
               + ", returnValue=" + returnValue
               + ", returnType=" + returnType
               + '}';
    }
}

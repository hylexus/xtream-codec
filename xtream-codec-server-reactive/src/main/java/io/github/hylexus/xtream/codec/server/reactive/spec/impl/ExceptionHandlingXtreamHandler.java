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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前类是从 `org.springframework.web.server.handler.ExceptionHandlingWebHandler` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.handler.ExceptionHandlingWebHandler`.
 *
 * @author hylexus
 */
public class ExceptionHandlingXtreamHandler implements XtreamHandler {
    // private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingXtreamHandler.class);
    private final XtreamHandler delegateHandler;
    private final List<XtreamRequestExceptionHandler> exceptionHandlers;

    public ExceptionHandlingXtreamHandler(XtreamHandler delegateHandler, List<XtreamRequestExceptionHandler> exceptionHandlers) {
        this.delegateHandler = delegateHandler;

        final List<XtreamRequestExceptionHandler> list = new ArrayList<>();
        list.add(new XtreamRequestExceptionHandler.CheckpointInsertingXtreamRequestExceptionHandler());
        list.addAll(exceptionHandlers);
        this.exceptionHandlers = OrderedComponent.sort(list);
    }

    @Override
    public Mono<Void> handle(XtreamExchange exchange) {
        Mono<Void> completion;
        try {
            completion = this.delegateHandler.handle(exchange);
        } catch (Throwable ex) {
            completion = Mono.error(ex);
        }

        for (XtreamRequestExceptionHandler handler : this.exceptionHandlers) {
            completion = completion.onErrorResume(ex -> {
                final Throwable cause = XtreamWrappedRuntimeException.unwrapIfNecessary(ex);
                return handler.handleRequestException(exchange, cause);
            });
        }

        return completion;
    }
}

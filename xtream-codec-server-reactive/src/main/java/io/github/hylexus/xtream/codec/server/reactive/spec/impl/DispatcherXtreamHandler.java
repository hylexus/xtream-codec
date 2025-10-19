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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;


import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.RequestHandlerNotFoundException;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 当前类是从 `org.springframework.web.reactive.DispatcherHandler` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.reactive.DispatcherHandler`.
 *
 * @author hylexus
 */
public class DispatcherXtreamHandler implements XtreamHandler {

    private final List<XtreamHandlerMapping> handlerMappings;

    private final List<XtreamHandlerAdapter> handlerAdapters;

    private final List<XtreamHandlerResultHandler> resultHandlers;

    public DispatcherXtreamHandler(List<XtreamHandlerMapping> handlerMappings, List<XtreamHandlerAdapter> handlerAdapters, List<XtreamHandlerResultHandler> resultHandlers) {
        this.handlerMappings = OrderedComponent.sort(handlerMappings);
        this.handlerAdapters = OrderedComponent.sort(handlerAdapters);
        this.resultHandlers = OrderedComponent.sort(resultHandlers);
    }

    @Override
    public Mono<Void> handle(XtreamExchange exchange) {
        return Flux.fromIterable(this.handlerMappings)
                .concatMap(mapping -> mapping.getHandler(exchange))
                .next()
                .switchIfEmpty(createNotFoundError(exchange))
                // .onErrorResume(ex -> handleResultMono(exchange, Mono.error(ex)))
                .flatMap(handler -> handleRequestWith(exchange, handler));
    }

    private Mono<Void> handleRequestWith(XtreamExchange exchange, Object handler) {

        if (this.handlerAdapters != null) {
            for (XtreamHandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    Mono<XtreamHandlerResult> resultMono = adapter.handle(exchange, handler);
                    return handleResultMono(exchange, resultMono);
                }
            }
        }
        return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
    }

    private Mono<Void> handleResultMono(XtreamExchange exchange, Mono<XtreamHandlerResult> resultMono) {
        if (this.handlerAdapters != null) {
            for (XtreamHandlerAdapter adapter : this.handlerAdapters) {
                if (adapter instanceof XtreamDispatchExceptionHandler exceptionHandler) {
                    resultMono = resultMono.onErrorResume(ex2 -> {
                        final Throwable cause = XtreamWrappedRuntimeException.unwrapIfNecessary(ex2);
                        return exceptionHandler.handleError(exchange, cause);
                    });
                }
            }
        }
        return resultMono.flatMap(result -> {
            Mono<Void> voidMono = handleResult(exchange, result, "Handler " + result.getHandler());
            final XtreamDispatchExceptionHandler exceptionHandler = result.getExceptionHandler();
            if (exceptionHandler != null) {
                voidMono = voidMono.onErrorResume(ex -> {
                    final Throwable cause = XtreamWrappedRuntimeException.unwrapIfNecessary(ex);
                    return exceptionHandler.handleError(exchange, cause).flatMap(result2 ->
                            handleResult(exchange, result2, "Exception handler "
                                                            + result2.getHandler() + ", error=\"" + cause.getMessage() + "\""));
                });
            }
            return voidMono;
        });
    }

    private Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult handlerResult, String description) {

        if (this.resultHandlers != null) {
            for (XtreamHandlerResultHandler resultHandler : this.resultHandlers) {
                if (resultHandler.supports(handlerResult)) {
                    description += " [DispatcherHandler]";
                    return resultHandler.handleResult(exchange, handlerResult).checkpoint(description);
                }
            }
        }
        return Mono.error(new IllegalStateException(
                "No HandlerResultHandler for " + handlerResult.getReturnValue()));
    }

    private <R> Mono<R> createNotFoundError(XtreamExchange exchange) {
        return Mono.defer(() -> {
            Exception ex = new RequestHandlerNotFoundException("No handler found. requestInfo : " + exchange.request(), exchange);
            return Mono.error(ex);
        });
    }

}

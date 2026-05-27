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
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author hylexus
 */
public class XtreamHandlerMethodHandlerAdapter implements XtreamHandlerAdapter {

    protected static final Mono<Object[]> EMPTY_ARGS = Mono.just(new Object[0]);
    protected static final Object NO_ARG_VALUE = new Object();
    protected final XtreamHandlerMethodArgumentResolver argumentResolver;

    public XtreamHandlerMethodHandlerAdapter(XtreamHandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolver = argumentResolver;
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof XtreamHandlerMethod;
    }

    @Override
    public Mono<XtreamHandlerResult> handle(XtreamExchange exchange, Object handler) {
        final XtreamHandlerMethod handlerMethod = (XtreamHandlerMethod) handler;

        return this.resolveArguments(exchange, handlerMethod)
                // 参数解析结束之后 调度到指定的调度器中执行
                .publishOn(handlerMethod.getScheduler())
                .flatMap(args -> {
                    // ...
                    return handlerMethod.invoke(handlerMethod.getContainerInstance(), args);
                });
    }

    private Mono<Object[]> resolveArguments(XtreamExchange exchange, XtreamHandlerMethod handlerMethod) {
        final XtreamMethodParameter[] parameters = handlerMethod.getParameters();
        if (parameters.length == 0) {
            return EMPTY_ARGS;
        }

        final List<Mono<Object>> args = new ArrayList<>(parameters.length);
        for (final XtreamMethodParameter parameter : parameters) {
            if (!this.argumentResolver.supportsParameter(parameter)) {
                return Mono.error(new IllegalStateException("Cannot resolve argument: " + parameter));
            }
            final Mono<Object> mono = this.argumentResolver.resolveArgument(parameter, exchange)
                    .defaultIfEmpty(NO_ARG_VALUE);
            args.add(mono);
        }
        return Mono.zip(args, objects -> {
            // ...
            return Stream.of(objects).map(it -> it == NO_ARG_VALUE ? null : it).toArray();
        });
    }
}

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

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * 不需要回复数据给客户端的场景
 *
 * @author hylexus
 */
public class EmptyXtreamHandlerResultHandler implements XtreamHandlerResultHandler {
    // private static final Logger log = LoggerFactory.getLogger(EmptyXtreamHandlerResultHandler.class);

    public EmptyXtreamHandlerResultHandler() {
    }

    @Override
    public boolean supports(XtreamHandlerResult result) {
        final XtreamMethodParameter returnType = result.getReturnType();
        return isVoid(returnType.getParameterType())
               || (!returnType.getGenericType().isEmpty() && isVoid(returnType.getGenericType().getFirst()));
    }

    protected boolean isVoid(Type cls) {
        return Void.class.equals(cls) || void.class.equals(cls);
    }

    @Override
    public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
        return switch (result.getReturnValue()) {
            case null -> Mono.empty();
            case Mono<?> mono -> mono.then();
            case Flux<?> flux -> flux.then();
            case Object ignored -> Mono.empty();
        };
    }

    @Override
    public int order() {
        return BUILTIN_COMPONENT_PRECEDENCE + 100;
    }

}

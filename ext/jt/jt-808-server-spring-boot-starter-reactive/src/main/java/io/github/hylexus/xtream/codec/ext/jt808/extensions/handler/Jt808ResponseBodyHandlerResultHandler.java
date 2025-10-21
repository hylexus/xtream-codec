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

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author hylexus
 */
public class Jt808ResponseBodyHandlerResultHandler implements XtreamHandlerResultHandler {
    protected final Jt808ResponseEncoder jt808ResponseEncoder;
    protected final Jt808RequestLifecycleListener lifecycleListener;

    public Jt808ResponseBodyHandlerResultHandler(Jt808ResponseEncoder jt808ResponseEncoder, Jt808RequestLifecycleListener lifecycleListener) {
        this.jt808ResponseEncoder = jt808ResponseEncoder;
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public boolean supports(XtreamHandlerResult handlerResult) {
        return this.getJt808ResponseBody(handlerResult) != null;
    }

    @Override
    public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
        final Jt808ResponseBody annotation = Objects.requireNonNull(this.getJt808ResponseBody(result));
        final Jt808Request jt808Request = (Jt808Request) exchange.request();
        final Jt808RequestHeader requestHeader = jt808Request.header();

        return this.adaptReturnValue(result.getReturnValue()).flatMap(body -> {
            final ByteBuf responseBuf = this.jt808ResponseEncoder.encode(body, requestHeader.version(), requestHeader.terminalId(), annotation);
            this.lifecycleListener.beforeResponseSend(jt808Request, responseBuf);
            return exchange.response().writeWith(Mono.just(responseBuf));
        });
    }

    @Override
    public int order() {
        return OrderedComponent.BUILTIN_COMPONENT_PRECEDENCE - 100;
    }

    protected Mono<?> adaptReturnValue(@Nullable Object returnValue) {
        return switch (returnValue) {
            case null -> Mono.error(new IllegalArgumentException("message is null"));
            case Mono<?> mono -> mono;
            case Flux<?> ignored -> Mono.error(() -> new IllegalArgumentException("Flux is not supported"));
            case Object object -> Mono.just(object);
        };
    }

    protected @Nullable Jt808ResponseBody getJt808ResponseBody(XtreamHandlerResult handlerResult) {
        final XtreamMethodParameter returnType = handlerResult.getReturnType();
        // 优先获取方法上的注解
        final Jt808ResponseBody methodAnnotation = returnType.getMethodAnnotation(Jt808ResponseBody.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 泛型类型？
        if (returnType.hasGenericType()) {
            // 808这里暂时只支持 Mono，不支持 Flux
            if (Mono.class.isAssignableFrom(returnType.getParameterType())) {
                final Jt808ResponseBody genericTypeAnnotation = returnType.getGenericTypeAnnotation(0, Jt808ResponseBody.class);
                if (genericTypeAnnotation != null) {
                    return genericTypeAnnotation;
                }
            }
        }
        // 非泛型类型 ==> 直接标记在类上的注解
        return returnType.getTypeAnnotation(Jt808ResponseBody.class);
    }
}

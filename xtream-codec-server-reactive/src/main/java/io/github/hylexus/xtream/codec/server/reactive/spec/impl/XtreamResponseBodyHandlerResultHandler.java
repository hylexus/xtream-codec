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
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 * @see EntityCodec
 */
public class XtreamResponseBodyHandlerResultHandler implements XtreamHandlerResultHandler {

    private final EntityCodec entityCodec;

    public XtreamResponseBodyHandlerResultHandler(EntityCodec entityCodec) {
        this.entityCodec = entityCodec;
    }

    @Override
    public boolean supports(XtreamHandlerResult result) {
        return this.getXtreamResponseBodyAnnotation(result) != null;
    }

    @Override
    public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
        final int version = exchange.request().version();
        final Flux<ByteBuf> byteBufFlux = this.encode(exchange.response().bufferFactory(), version, result.getReturnValue());
        return exchange.response().writeWith(byteBufFlux);
    }

    @Override
    public int order() {
        return BUILTIN_COMPONENT_PRECEDENCE;
    }

    public Flux<ByteBuf> encode(ByteBufAllocator allocator, int version, Object data) {
        return switch (data) {
            case null -> Flux.error(new IllegalArgumentException("message is null"));
            case Mono<?> mono -> mono.map(msg -> this.doEncode(version, msg, allocator)).flux();
            case Flux<?> flux -> flux.map(msg -> this.doEncode(version, msg, allocator));
            case Object obj -> Flux.just(this.doEncode(version, obj, allocator));
        };
    }

    protected ByteBuf doEncode(int version, Object message, ByteBufAllocator allocator) {
        if (message instanceof ByteBuf byteBuf) {
            return byteBuf;
        }
        final ByteBuf buffer = allocator.buffer();
        try {
            this.entityCodec.encode(version, message, buffer);
        } catch (Throwable e) {
            buffer.release();
            throw new RuntimeException(e);
        }
        return buffer;
    }

    protected XtreamResponseBody getXtreamResponseBodyAnnotation(XtreamHandlerResult handlerResult) {
        // 处理器方法所在类上的注解
        final XtreamResponseBody classLevelAnnotation = handlerResult.getReturnType().getContainerClass().getAnnotation(XtreamResponseBody.class);
        if (classLevelAnnotation != null) {
            return classLevelAnnotation;
        }
        final XtreamMethodParameter returnType = handlerResult.getReturnType();
        // 处理器方法上的注解
        final XtreamResponseBody methodAnnotation = returnType.getMethodAnnotation(XtreamResponseBody.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 处理器方法的返回类型
        // 有泛型类型？
        if (returnType.hasGenericType()) {
            // Mono || Flux
            if (Mono.class.isAssignableFrom(returnType.getParameterType()) || Flux.class.isAssignableFrom(returnType.getParameterType())) {
                final XtreamResponseBody genericTypeAnnotation = returnType.getGenericTypeAnnotation(0, XtreamResponseBody.class);
                if (genericTypeAnnotation != null) {
                    return genericTypeAnnotation;
                }
            }
        }
        // 非泛型类型 ==> 直接标记在类上的注解
        return returnType.getTypeAnnotation(XtreamResponseBody.class);
    }
}

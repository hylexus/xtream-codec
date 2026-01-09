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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodArgumentResolver;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class XtreamRequestBodyArgumentResolver implements XtreamHandlerMethodArgumentResolver {
    private final EntityCodec messageCodec;

    public XtreamRequestBodyArgumentResolver(EntityCodec entityCodec) {
        this.messageCodec = entityCodec;
    }

    @Override
    public boolean supportsParameter(XtreamMethodParameter parameter) {
        return parameter.getParameterAnnotation(XtreamRequestBody.class).isPresent();
    }

    @Override
    public Mono<Object> resolveArgument(XtreamMethodParameter parameter, XtreamExchange exchange) {
        if (ByteBuf.class.isAssignableFrom(parameter.getParameterType())) {
            final XtreamRequestBody annotation = parameter.getParameterAnnotation(XtreamRequestBody.class).orElseThrow();
            if (annotation.bufferAsSlice()) {
                return Mono.justOrEmpty(exchange.request().payload().slice());
            }
            return Mono.justOrEmpty(exchange.request().payload());
        }
        final int version = exchange.request().version();
        final Object instance = this.messageCodec.decode(version, parameter.getParameterType(), exchange.request().payload().slice());
        return Mono.justOrEmpty(instance);
    }
}

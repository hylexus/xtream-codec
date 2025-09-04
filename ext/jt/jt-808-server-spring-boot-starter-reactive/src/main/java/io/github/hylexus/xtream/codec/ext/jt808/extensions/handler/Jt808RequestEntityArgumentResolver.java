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
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodArgumentResolver;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class Jt808RequestEntityArgumentResolver implements XtreamHandlerMethodArgumentResolver {
    private final EntityCodec messageCodec;

    public Jt808RequestEntityArgumentResolver(EntityCodec messageCodec) {
        this.messageCodec = messageCodec;
    }

    @Override
    public boolean supportsParameter(XtreamMethodParameter parameter) {
        return Jt808RequestEntity.class == parameter.getParameterType();
    }

    @Override
    public Mono<Object> resolveArgument(XtreamMethodParameter parameter, XtreamExchange exchange) {
        final Class<?> parameterType = parameter.getParameterType();
        if (ByteBuf.class.isAssignableFrom(parameterType)) {
            return this.createEntity(exchange, exchange.request().payload().slice());
        }
        @SuppressWarnings("unchecked") final Class<Object> genericType = (Class<Object>) parameter.getGenericType().getFirst();
        final int version = exchange.request().version();
        final Object instance = this.messageCodec.decode(version, genericType, exchange.request().payload().slice());
        return this.createEntity(exchange, instance);
    }

    protected Mono<Object> createEntity(XtreamExchange exchange, Object instance) {
        final Jt808Request request = exchange.request().castAs(Jt808Request.class);
        final Jt808RequestEntity<Object> entity = new Jt808RequestEntity<>(request, instance);
        return Mono.just(entity);
    }

}

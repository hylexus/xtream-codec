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
import io.github.hylexus.xtream.codec.ext.jt808.spec.AbstractJt808Message;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodArgumentResolver;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author hylexus
 */
public class Jt808MessageArgumentResolver implements XtreamHandlerMethodArgumentResolver {
    private final EntityCodec messageCodec;

    public Jt808MessageArgumentResolver(EntityCodec messageCodec) {
        this.messageCodec = messageCodec;
    }

    @Override
    public boolean supportsParameter(XtreamMethodParameter parameter) {
        return AbstractJt808Message.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Mono<Object> resolveArgument(XtreamMethodParameter parameter, XtreamExchange exchange) {
        final Object message = createMessageInstance(parameter, exchange);

        final int version = exchange.request().version();
        final Object instance = this.messageCodec.decode(version, message, exchange.request().payload().slice());
        return Mono.just(instance);
    }

    private Object createMessageInstance(XtreamMethodParameter parameter, XtreamExchange exchange) {
        try {
            final Constructor<?> constructor = parameter.getParameterType().getConstructor(Jt808Request.class);
            return constructor.newInstance(exchange.request().castAs(Jt808Request.class));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}

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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMethodArgumentResolver;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hylexus
 */
public class DelegateXtreamHandlerMethodArgumentResolver implements XtreamHandlerMethodArgumentResolver {

    public static XtreamHandlerMethodArgumentResolver createDefault(EntityCodec entityCodec) {
        return new DelegateXtreamHandlerMethodArgumentResolver().addDefault(entityCodec);
    }

    private final List<XtreamHandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
    private final Map<XtreamMethodParameter, XtreamHandlerMethodArgumentResolver> argumentResolverCache;

    public DelegateXtreamHandlerMethodArgumentResolver() {
        this(List.of());
    }

    public DelegateXtreamHandlerMethodArgumentResolver(List<XtreamHandlerMethodArgumentResolver> resolvers) {
        resolvers.stream().filter(Objects::nonNull).forEach(this::addArgumentResolver);
        this.argumentResolverCache = new ConcurrentHashMap<>();
    }

    public DelegateXtreamHandlerMethodArgumentResolver addDefault(EntityCodec messageCodec) {
        this.addArgumentResolver(new XtreamExchangeArgumentResolver());
        this.addArgumentResolver(new XtreamRequestBodyArgumentResolver(messageCodec));
        this.addArgumentResolver(new XtreamRequestArgumentResolver());
        this.addArgumentResolver(new XtreamResponseArgumentResolver());
        this.addArgumentResolver(new XtreamSessionArgumentResolver());
        return this;
    }

    public DelegateXtreamHandlerMethodArgumentResolver addArgumentResolver(XtreamHandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        return this;
    }

    @Override
    public boolean supportsParameter(XtreamMethodParameter parameter) {
        return this.getArgumentResolver(parameter) != null;
    }

    @Override
    public Mono<Object> resolveArgument(XtreamMethodParameter parameter, XtreamExchange exchange) {
        final XtreamHandlerMethodArgumentResolver resolver = this.getArgumentResolver(parameter);
        if (resolver == null) {
            return Mono.error(new IllegalArgumentException("Unsupported argument type: " + parameter));
        }
        return resolver.resolveArgument(parameter, exchange);
    }

    @Nullable XtreamHandlerMethodArgumentResolver getArgumentResolver(XtreamMethodParameter parameter) {
        final XtreamHandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result != null) {
            return result;
        }

        for (final XtreamHandlerMethodArgumentResolver resolver : this.argumentResolvers) {
            if (resolver.supportsParameter(parameter)) {
                this.argumentResolverCache.put(parameter, resolver);
                return resolver;
            }
        }
        return null;
    }
}

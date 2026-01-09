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

package io.github.hylexus.xtream.codec.base.web.handler.reactive;


import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author hylexus
 */
@NullMarked
public class ClientIpArgumentResolverReactive implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientIp.class);
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        final ClientIp annotation = Objects.requireNonNull(parameter.getParameterAnnotation(ClientIp.class));
        final String clientIp = XtreamWebUtils.getClientIp(exchange.getRequest().getHeaders()::getFirst, exchange.getRequest().getRemoteAddress())
                .map(ip -> XtreamWebUtils.filterClientIp(ip, annotation.defaultValue(), annotation.ignoreLocalhost(), annotation.localhostValue()))
                .orElse(null);
        return Mono.justOrEmpty(clientIp);
    }

}

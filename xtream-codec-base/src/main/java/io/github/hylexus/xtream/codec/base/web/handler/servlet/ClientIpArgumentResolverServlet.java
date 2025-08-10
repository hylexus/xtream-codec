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

package io.github.hylexus.xtream.codec.base.web.handler.servlet;


import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;
import java.util.Optional;

/**
 * @author hylexus
 */
public class ClientIpArgumentResolverServlet implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientIp.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final ClientIp annotation = Objects.requireNonNull(parameter.getParameterAnnotation(ClientIp.class));

        final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            return XtreamWebUtils.filterNullIp(annotation.defaultValue());
        }

        return getClientIp(servletRequest)
                .map(ip -> XtreamWebUtils.filterClientIp(ip, annotation.defaultValue(), annotation.ignoreLocalhost(), annotation.localhostValue()))
                .orElse(null);
    }

    public static Optional<String> getClientIp(HttpServletRequest request) {
        return XtreamWebUtils.getClientIp(request::getHeader)
                .or(() -> Optional.ofNullable(request.getRemoteAddr()));
    }
}

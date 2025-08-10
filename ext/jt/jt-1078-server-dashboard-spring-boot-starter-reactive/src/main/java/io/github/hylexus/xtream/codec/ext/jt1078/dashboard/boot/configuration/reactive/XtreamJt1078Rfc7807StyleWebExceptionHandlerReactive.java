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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration.reactive;

import io.github.hylexus.xtream.codec.base.web.handler.reactive.BaseXtreamInternalRfc7807StyleWebExceptionHandlerReactive;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.BuiltinJt1078ControllerMarkerInterface;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice(basePackageClasses = {
        BuiltinJt1078ControllerMarkerInterface.class,
})
@Order(-200)
public class XtreamJt1078Rfc7807StyleWebExceptionHandlerReactive
        extends BaseXtreamInternalRfc7807StyleWebExceptionHandlerReactive {

    public XtreamJt1078Rfc7807StyleWebExceptionHandlerReactive() {
    }

    @Override
    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<?>> handleThrowable(ServerWebExchange exchange, Throwable error) throws Throwable {
        return super.handleThrowable(exchange, error);
    }

}

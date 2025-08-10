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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration.servlet;

import io.github.hylexus.xtream.codec.base.web.handler.servlet.BaseXtreamInternalRfc7807StyleWebExceptionHandlerServlet;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller.BuiltinJt808ControllerMarkerInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {
        BuiltinJt808ControllerMarkerInterface.class,
})
@Order(-200)
public class XtreamJt808Rfc7807StyleWebExceptionHandlerServlet
        extends BaseXtreamInternalRfc7807StyleWebExceptionHandlerServlet {

    public XtreamJt808Rfc7807StyleWebExceptionHandlerServlet() {
    }

    @Override
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleThrowable(HttpServletRequest request, Throwable error) throws Throwable {
        return super.handleThrowable(request, error);
    }

}

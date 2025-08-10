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

import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiErrorCode;
import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiProblemDetails;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpErrorDetails;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpTransparentException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * @author hylexus
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC-7807</a>
 */
public class BaseXtreamInternalRfc7807StyleWebExceptionHandlerServlet {

    private static final Logger log = LoggerFactory.getLogger(BaseXtreamInternalRfc7807StyleWebExceptionHandlerServlet.class);

    public BaseXtreamInternalRfc7807StyleWebExceptionHandlerServlet() {
    }

    public ResponseEntity<?> handleThrowable(HttpServletRequest request, Throwable error) throws Throwable {
        return switch (error) {
            case XtreamHttpTransparentException transparentException -> {
                final Object body = transparentException.error();
                yield render(transparentException.status(), body);
            }
            case HandlerMethodValidationException ex -> {
                final String message = Optional.of(ex.getValueResults())
                        .filter(r -> !r.isEmpty())
                        .map(List::getFirst)
                        .map(ParameterValidationResult::getResolvableErrors)
                        .filter(it -> !it.isEmpty())
                        .map(List::getFirst)
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .orElse("ParameterError");
                yield render(XtreamApiProblemDetails.badRequest(XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList(message)));
            }
            case WebExchangeBindException ex -> {
                final String message = Optional.of(ex.getAllErrors()).filter(it -> !it.isEmpty())
                        .map(List::getFirst)
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .orElse(ex.getMessage());
                yield render(XtreamApiProblemDetails.badRequest(XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList(message)));
            }
            case ResponseStatusException statusException -> {
                final HttpStatusCode statusCode = statusException.getStatusCode();
                final XtreamApiProblemDetails body = XtreamApiProblemDetails.of(statusCode, "000:" + statusCode.value(), statusException.getReason(), null);
                yield render(body);
            }
            case XtreamApiProblemDetails problemDetails -> render(problemDetails);
            default -> {
                log.error("Internal Server Error", error);
                final XtreamApiProblemDetails problemDetails = XtreamApiProblemDetails.newBuilderFrom(XtreamApiErrorCode.SERVER_ERROR)
                        .detail(error.getMessage()).build();
                yield render(problemDetails);
            }
        };
    }

    private static ResponseEntity<?> render(XtreamApiProblemDetails problemDetails) {
        return problemDetails.toResponseEntity();
    }

    private static ResponseEntity<?> render(int httpStatus, Object body) {
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

}

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

package io.github.hylexus.xtream.debug.ext.jt808;

import io.github.hylexus.xtream.codec.ext.jt808.domain.Resp;
import io.github.hylexus.xtream.codec.ext.jt808.exception.XtreamHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RestControllerAdvice
public class GlobalWebServerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWebServerExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleThrowable(Throwable ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(Map.of("status", 500, "message", "服务端异常"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(XtreamHttpException.class)
    public ResponseEntity<Resp<Object>> processXtreamHttpException(XtreamHttpException e) {
        return new ResponseEntity<>(Resp.badRequest(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Resp<Object>> processResponseStatusException(ResponseStatusException e) {
        return new ResponseEntity<>(Resp.failure(e.getStatusCode().value(), e.getReason()), e.getStatusCode());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Resp<Object>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        final String message = Optional.of(ex.getValueResults())
                .filter(r -> !r.isEmpty())
                .map(List::getFirst)
                .map(ParameterValidationResult::getResolvableErrors)
                .filter(it -> !it.isEmpty())
                .map(List::getFirst)
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse("参数异常");
        return new ResponseEntity<>(Resp.badRequest(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Resp<Object>> handleWebExchangeBindException(WebExchangeBindException ex) {
        final String message = Optional.of(ex.getAllErrors()).filter(it -> !it.isEmpty())
                .map(List::getFirst)
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(ex.getMessage());
        return new ResponseEntity<>(Resp.badRequest(message), HttpStatus.BAD_REQUEST);
    }

}

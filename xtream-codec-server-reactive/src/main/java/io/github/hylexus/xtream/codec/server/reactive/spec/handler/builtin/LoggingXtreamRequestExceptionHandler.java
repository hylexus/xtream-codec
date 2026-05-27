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

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.exception.RequestHandlerNotFoundException;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamRequestExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class LoggingXtreamRequestExceptionHandler implements XtreamRequestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingXtreamRequestExceptionHandler.class);

    public LoggingXtreamRequestExceptionHandler() {
    }

    @Override
    public Mono<Void> handleRequestException(XtreamExchange exchange, Throwable ex) {
        if (ex instanceof RequestHandlerNotFoundException notFoundException) {
            log.error("[LoggingXtreamRequestExceptionHandler] RequestHandlerNotFoundException: {}", notFoundException.getExchange());
            return Mono.empty();
        }
        return Mono.error(ex);
    }

    @Override
    public int order() {
        return OrderedComponent.LOWEST_PRECEDENCE - 1;
    }
}

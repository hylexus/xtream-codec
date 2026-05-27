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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class LoggingXtreamHandlerResultHandler implements XtreamHandlerResultHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggingXtreamHandlerResultHandler.class);

    @Override
    public boolean supports(XtreamHandlerResult result) {
        return true;
    }

    @Override
    public Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result) {
        log.error("LoggingXtreamHandlerResultHandler#handleResult: {}", result);
        return Mono.empty();
    }

    @Override
    public int order() {
        return OrderedComponent.LOWEST_PRECEDENCE - 1;
    }
}

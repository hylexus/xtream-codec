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

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class LoggingXtreamFilter implements XtreamFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingXtreamFilter.class);

    public LoggingXtreamFilter() {
    }

    @Override
    public Mono<Void> filter(XtreamExchange exchange, XtreamFilterChain chain) {
        log.info("==> Receive [{}] message: requestId = {}, remoteAddr = {}, payload = {}",
                exchange.request().type(),
                exchange.request().requestId(),
                exchange.request().remoteAddress(),
                FormatUtils.toHexString(exchange.request().payload())
        );
        return chain.filter(exchange);
    }

    @Override
    public int order() {
        return OrderedComponent.HIGHEST_PRECEDENCE + 1000;
    }
}

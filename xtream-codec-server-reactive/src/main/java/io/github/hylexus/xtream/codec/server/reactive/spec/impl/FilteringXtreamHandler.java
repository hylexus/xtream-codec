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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 当前类是从 `org.springframework.web.server.handler.FilteringWebHandler` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.handler.FilteringWebHandler`.
 *
 * @author hylexus
 */
public class FilteringXtreamHandler implements XtreamHandler {
    private final XtreamFilterChain chain;

    public FilteringXtreamHandler(XtreamHandler handler) {
        this(handler, Collections.emptyList());
    }

    public FilteringXtreamHandler(XtreamHandler handler, List<XtreamFilter> filters) {
        this.chain = new DefaultXtreamFilterChain(handler, filters);
    }

    @Override
    public Mono<Void> handle(XtreamExchange exchange) {
        return this.chain.filter(exchange);
    }
}

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
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.ListIterator;

/**
 * 当前类是从 `org.springframework.web.server.handler.DefaultWebFilterChain` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.handler.DefaultWebFilterChain`.
 *
 * @author hylexus
 */
public class DefaultXtreamFilterChain implements XtreamFilterChain {
    @SuppressWarnings("unused")
    private final List<XtreamFilter> allFilters;
    private final XtreamHandler handler;

    private final @Nullable XtreamFilter currentFilter;

    private final @Nullable DefaultXtreamFilterChain chain;

    public DefaultXtreamFilterChain(XtreamHandler handler, List<XtreamFilter> filters) {
        this.allFilters = filters;
        this.handler = handler;

        final DefaultXtreamFilterChain chain = this.initChain(filters, handler);
        this.currentFilter = chain.currentFilter;
        this.chain = chain.chain;
    }

    public DefaultXtreamFilterChain(List<XtreamFilter> filters, XtreamHandler handler, @Nullable XtreamFilter currentFilter, @Nullable DefaultXtreamFilterChain chain) {
        this.allFilters = filters;
        this.handler = handler;
        this.currentFilter = currentFilter;
        this.chain = chain;
    }

    private DefaultXtreamFilterChain initChain(List<XtreamFilter> filters, XtreamHandler handler) {
        DefaultXtreamFilterChain chain = new DefaultXtreamFilterChain(filters, handler, null, null);
        final ListIterator<? extends XtreamFilter> iterator = filters.listIterator(filters.size());
        while (iterator.hasPrevious()) {
            chain = new DefaultXtreamFilterChain(filters, handler, iterator.previous(), chain);
        }
        return chain;
    }

    @Override
    public Mono<Void> filter(XtreamExchange exchange) {
        return Mono.defer(() -> {
                    // ...
                    return this.currentFilter != null && this.chain != null
                            ? invokeFilter(this.currentFilter, this.chain, exchange)
                            : this.handler.handle(exchange);
                }
        );
    }

    private Mono<Void> invokeFilter(XtreamFilter current, DefaultXtreamFilterChain chain, XtreamExchange exchange) {
        final String currentName = current.getClass().getName();
        return current.filter(exchange, chain).checkpoint(currentName + " [DefaultXtreamFilterChain]");
    }
}

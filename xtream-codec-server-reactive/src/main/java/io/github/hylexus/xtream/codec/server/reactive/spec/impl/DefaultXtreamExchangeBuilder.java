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
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchangeDecorator;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamResponse;

import java.util.function.Consumer;

public class DefaultXtreamExchangeBuilder implements XtreamExchange.XtreamExchangeBuilder {
    protected final XtreamExchange delegate;
    protected XtreamRequest request;
    protected XtreamResponse response;

    public DefaultXtreamExchangeBuilder(XtreamExchange delegate) {
        this.delegate = delegate;
        this.request = delegate.request();
        this.response = delegate.response();
    }

    @Override
    public XtreamExchange.XtreamExchangeBuilder request(XtreamRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public XtreamExchange.XtreamExchangeBuilder request(Consumer<XtreamRequest.XtreamRequestBuilder> requestBuilder) {
        final XtreamRequest.XtreamRequestBuilder builder = this.delegate.request().mutate();
        requestBuilder.accept(builder);
        final XtreamRequest newRequest = builder.build();
        return this.request(newRequest);
    }

    @Override
    public XtreamExchange.XtreamExchangeBuilder response(XtreamResponse response) {
        this.response = response;
        return this;
    }

    @Override
    public XtreamExchange build() {
        return new XtreamExchangeDecorator(this.delegate, this.request, this.response);
    }

}

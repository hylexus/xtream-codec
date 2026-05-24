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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 当前类是从 `org.springframework.web.server.ServerWebExchange` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.ServerWebExchange`.
 *
 * @author hylexus
 */
public class XtreamExchangeDecorator implements XtreamExchange {

    protected final XtreamExchange delegate;
    protected @Nullable XtreamRequest request;
    protected @Nullable XtreamResponse response;

    public XtreamExchangeDecorator(XtreamExchange delegate, @Nullable XtreamRequest request, @Nullable XtreamResponse response) {
        this.delegate = delegate;
        this.request = request;
        this.response = response;
    }

    @Override
    public ByteBufAllocator bufferFactory() {
        return delegate.bufferFactory();
    }

    @Override
    public XtreamRequest request() {
        return this.request != null ? this.request : delegate.request();
    }

    @Override
    public XtreamResponse response() {
        return this.response != null ? this.response : delegate.response();
    }

    @Override
    public Mono<XtreamSession> session() {
        return delegate.session();
    }

    @Override
    public Map<String, Object> attributes() {
        return delegate.attributes();
    }
}

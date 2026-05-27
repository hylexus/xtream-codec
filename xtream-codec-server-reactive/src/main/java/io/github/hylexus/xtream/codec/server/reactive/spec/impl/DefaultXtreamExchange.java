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


import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author hylexus
 */
public class DefaultXtreamExchange implements XtreamExchange {
    protected final XtreamRequest request;
    protected final XtreamResponse response;
    protected final Mono<XtreamSession> sessionMono;

    public DefaultXtreamExchange(XtreamSession session, XtreamRequest request, XtreamResponse response) {
        this.request = request;
        this.response = response;
        this.sessionMono = Mono.just(session);
    }

    public DefaultXtreamExchange(@SuppressWarnings("rawtypes") XtreamSessionManager sessionManager, XtreamRequest request, XtreamResponse response) {
        this.request = request;
        this.response = response;
        @SuppressWarnings("unchecked") final Mono<XtreamSession> session = sessionManager.getSession(this, true);
        // this.sessionMono = session.cache();
        this.sessionMono = session;
    }

    @Override
    public XtreamRequest request() {
        return this.request;
    }

    @Override
    public XtreamResponse response() {
        return this.response;
    }

    @Override
    public Mono<XtreamSession> session() {
        return this.sessionMono;
    }

    @Override
    public Map<String, Object> attributes() {
        return new HashMap<>();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultXtreamExchange.class.getSimpleName() + "[", "]")
                .add("request=" + request)
                .toString();
    }

}

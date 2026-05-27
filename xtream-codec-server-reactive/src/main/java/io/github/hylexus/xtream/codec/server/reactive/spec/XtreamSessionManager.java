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

package io.github.hylexus.xtream.codec.server.reactive.spec;


import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 当前类是从 `org.springframework.web.server.session.WebSessionManager` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.session.WebSessionManager`.
 *
 * @author hylexus
 */
public interface XtreamSessionManager<S extends XtreamSession> {

    XtreamSessionIdGenerator sessionIdGenerator();

    default Mono<S> getSession(XtreamExchange exchange, boolean createNewIfMissing) {
        return Mono.defer(() -> {
            if (createNewIfMissing) {
                final String sessionId = this.sessionIdGenerator().generateSessionId(exchange);
                return this.getSessionById(sessionId).switchIfEmpty(Mono.defer(() -> {
                    // ...
                    return this.createSession(sessionId, exchange);
                }));
            }
            return this.getSession(exchange);
        });
    }

    Mono<S> getSession(XtreamExchange exchange);

    Mono<S> createSession(String sessionId, XtreamExchange exchange);

    default Mono<S> createSession(XtreamExchange exchange) {
        final String sessionId = this.sessionIdGenerator().generateSessionId(exchange);
        return this.createSession(sessionId, exchange);
    }

    Mono<S> getSessionById(String sessionId);

    boolean closeSessionById(String sessionId, XtreamSessionEventListener.SessionCloseReason reason);

    /**
     * @see XtreamSession#invalidate(XtreamSessionEventListener.SessionCloseReason)
     */
    void closeSession(S session, XtreamSessionEventListener.SessionCloseReason reason);

    void addListener(XtreamSessionEventListener<S> listener);

    void shutdown();

    long count();

    default long count(Predicate<S> filter) {
        return this.list().filter(filter).count();
    }

    long countTcp();

    long countUdp();

    Stream<S> list();

    default Stream<S> list(int page, int pageSize, Predicate<S> filter, Comparator<S> sorter) {
        return this.list().filter(filter).sorted(sorter).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    default Stream<S> list(int page, int pageSize, Predicate<S> filter) {
        return this.list().filter(filter).sorted(Comparator.comparing(S::id)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    default Stream<S> list(int page, int pageSize) {
        return this.list().sorted(Comparator.comparing(S::id)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    default <T> Stream<T> list(int page, int pageSize, Predicate<S> filter, Comparator<S> sorter, Function<S, T> converter) {
        return this.list().filter(filter).sorted(sorter).skip((long) (page - 1) * pageSize).limit(pageSize).map(converter);
    }

    default <T> Stream<T> list(int page, int pageSize, Predicate<S> filter, Function<S, T> converter) {
        return this.list().filter(filter).sorted(Comparator.comparing(S::id)).skip((long) (page - 1) * pageSize).limit(pageSize).map(converter);
    }

    default <T> Stream<T> list(int page, int pageSize, Function<S, T> converter) {
        return this.list().sorted(Comparator.comparing(S::id)).skip((long) (page - 1) * pageSize).map(converter).limit(pageSize);
    }
}

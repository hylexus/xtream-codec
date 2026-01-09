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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Jt1078SessionManager extends XtreamSessionManager<Jt1078Session> {

    /**
     * @deprecated JT/T 1078 没有实现 {@link XtreamExchange}; 请使用 {{@link #getOrCreateSession(String, Supplier)}}
     */
    @Override
    @Deprecated
    default Mono<Jt1078Session> getSession(XtreamExchange exchange) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    @Deprecated
    default Mono<Jt1078Session> getSession(XtreamExchange exchange, boolean createNewIfMissing) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Nullable Jt1078Session getUdpSession(InetSocketAddress remoteAddress);

    @Override
    @Deprecated
    default Mono<Jt1078Session> createSession(XtreamExchange exchange) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    @Deprecated
    default Mono<Jt1078Session> createSession(String sessionId, XtreamExchange exchange) {
        return Mono.error(new UnsupportedOperationException());
    }

    Mono<Jt1078Session> createSession(String sessionId, Mono<Jt1078Session> sessionLoader);

    void createSession(Jt1078Session session);

    default Mono<Jt1078Session> getOrCreateSession(String sessionId, Supplier<Jt1078Session> creator) {
        return Mono.defer(() -> {
            // ...
            return this.getSessionById(sessionId).switchIfEmpty(Mono.defer(() -> {
                final Jt1078Session newSession = creator.get();
                return this.createSession(sessionId, Mono.just(newSession));
            }));
        });
    }

    @Override
    default Stream<Jt1078Session> list(int page, int pageSize, Predicate<Jt1078Session> filter) {
        return this.list().filter(filter).sorted(Comparator.comparing(Jt1078Session::rawSim)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    @Override
    default Stream<Jt1078Session> list(int page, int pageSize) {
        return this.list().sorted(Comparator.comparing(Jt1078Session::rawSim)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    @Override
    default <T> Stream<T> list(int page, int pageSize, Predicate<Jt1078Session> filter, Function<Jt1078Session, T> converter) {
        return this.list().filter(filter).sorted(Comparator.comparing(Jt1078Session::rawSim)).skip((long) (page - 1) * pageSize).limit(pageSize).map(converter);
    }

    @Override
    default <T> Stream<T> list(int page, int pageSize, Function<Jt1078Session, T> converter) {
        return this.list().sorted(Comparator.comparing(Jt1078Session::rawSim)).skip((long) (page - 1) * pageSize).map(converter).limit(pageSize);
    }

}

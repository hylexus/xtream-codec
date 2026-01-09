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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSessionManager;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Jt808SessionManager extends XtreamSessionManager<Jt808Session> {

    default Optional<Jt808Session> findByTerminalId(String terminalId) {
        return this.list()
                .filter(session -> session.terminalId().equals(terminalId))
                .min(Comparator.comparing(XtreamSession::lastCommunicateTime));
    }

    @Override
    default Stream<Jt808Session> list(int page, int pageSize, Predicate<Jt808Session> filter) {
        return this.list().filter(filter).sorted(Comparator.comparing(Jt808Session::terminalId)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    @Override
    default Stream<Jt808Session> list(int page, int pageSize) {
        return this.list().sorted(Comparator.comparing(Jt808Session::terminalId)).skip((long) (page - 1) * pageSize).limit(pageSize);
    }

    @Override
    default <T> Stream<T> list(int page, int pageSize, Predicate<Jt808Session> filter, Function<Jt808Session, T> converter) {
        return this.list().filter(filter).sorted(Comparator.comparing(Jt808Session::terminalId)).skip((long) (page - 1) * pageSize).limit(pageSize).map(converter);
    }

    @Override
    default <T> Stream<T> list(int page, int pageSize, Function<Jt808Session, T> converter) {
        return this.list().sorted(Comparator.comparing(Jt808Session::terminalId)).skip((long) (page - 1) * pageSize).map(converter).limit(pageSize);
    }

}

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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Jt1078SubscriberManager {

    Jt1078SimConverter simConverter();

    long count(Predicate<Jt1078SubscriberDescriptor> predicate);

    Stream<Jt1078SubscriberDescriptor> list();

    default Stream<Jt1078SubscriberDescriptor> list(String sim) {
        final String converted = simConverter().convert(sim);
        return list().filter(it -> it.convertedSim().equals(converted));
    }

    default Stream<Jt1078SubscriberDescriptor> list(String sim, short channel) {
        return this.list(sim).filter(it -> it.channel() == channel);
    }

    void closeSubscriber(String id);
}

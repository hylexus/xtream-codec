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

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import org.jspecify.annotations.Nullable;
import reactor.core.scheduler.Scheduler;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Jt1078ChannelCollector {

    Scheduler scheduler();

    void collect(Jt1078Request request);

    Jt1078Subscriber doSubscribe(Jt1078SubscriberCreator creator);

    default void unsubscribe(String id) {
        this.unsubscribe(id, null);
    }

    void unsubscribe(String id, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason);

    void close(Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason);

    long count(Predicate<Jt1078SubscriberDescriptor> predicate);

    Stream<Jt1078SubscriberDescriptor> list();

}

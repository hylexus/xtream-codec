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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.collector.H264ToFlvJt1078ChannelCollector;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public interface Jt1078RequestPublisher extends Jt1078SubscriberManager {

    Scheduler scheduler();

    @Override
    Jt1078SimConverter simConverter();

    Mono<Void> publish(Jt1078Request request);

    default Jt1078Subscriber subscribeH264ToFlv(H264Jt1078SubscriberCreator creator) {
        return this.subscribe(H264ToFlvJt1078ChannelCollector.class, creator);
    }

    default Flux<Jt1078Subscription> subscribeH264ToFlvStream(H264Jt1078SubscriberCreator creator) {
        return this.subscribeH264ToFlv(creator).dataStream();
    }

    Jt1078Subscriber subscribe(Class<? extends Jt1078ChannelCollector> cls, Jt1078SubscriberCreator creator);

    default void unsubscribe(String id) {
        this.unsubscribe(id, null);
    }

    void unsubscribe(String id, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason);

    void unsubscribeWithSim(String sim, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason);

    default void unsubscribeWithSim(String sim) {
        this.unsubscribeWithSim(simConverter().convert(sim), null);
    }

    void unsubscribeWithSimAndChannelNumber(String sim, short channelNumber, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason);

    default void unsubscribeWithSimAndChannelNumber(String sim, short channelNumber) {
        this.unsubscribeWithSimAndChannelNumber(simConverter().convert(sim), channelNumber, null);
    }

    @Override
    default void closeSubscriber(String id) {
        this.unsubscribe(id, null);
    }

}

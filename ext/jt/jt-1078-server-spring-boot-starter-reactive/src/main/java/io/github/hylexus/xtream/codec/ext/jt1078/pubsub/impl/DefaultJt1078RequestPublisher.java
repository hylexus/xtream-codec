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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.*;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultJt1078RequestPublisher implements Jt1078RequestPublisher {
    // private static final Logger log = LoggerFactory.getLogger(DefaultJt1078RequestPublisher.class);
    private final Jt1078SimConverter jt1078SimConverter;
    private final ConcurrentMap<Jt1078Channel.ChannelKey, Jt1078Channel> channels = new ConcurrentHashMap<>();
    private final Scheduler scheduler;

    public DefaultJt1078RequestPublisher(Jt1078SimConverter jt1078SimConverter, Scheduler scheduler) {
        this.jt1078SimConverter = jt1078SimConverter;
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public Jt1078SimConverter simConverter() {
        return this.jt1078SimConverter;
    }

    @Override
    public long count(Predicate<Jt1078SubscriberDescriptor> predicate) {
        return this.channels.values().stream()
                .flatMap(it -> it.listSubscribers().filter(predicate))
                .count();
    }

    @Override
    public Stream<Jt1078SubscriberDescriptor> list() {
        return this.channels.values().stream().flatMap(Jt1078Channel::listSubscribers);
    }

    @Override
    public Mono<Void> publish(Jt1078Request request) {
        // log.info("publish request: {}", request);
        final Jt1078Channel.ChannelKey channelKey = Jt1078Channel.ChannelKey.from(request);
        final Jt1078Channel jt1078Channel = this.channels.computeIfAbsent(channelKey, this::createChannel);
        jt1078Channel.publish(request);
        return Mono.empty();
    }

    @Override
    public Jt1078Subscriber subscribe(Class<? extends Jt1078ChannelCollector> cls, Jt1078SubscriberCreator creator) {
        creator.convertedSim(this.jt1078SimConverter.convert(creator.rawSim()));
        final String shorterSim = creator.convertedSim();
        final Jt1078Channel.ChannelKey channelKey = Jt1078Channel.ChannelKey.of(shorterSim, creator.channelNumber());
        final Jt1078Channel jt1078Channel = this.channels.computeIfAbsent(channelKey, this::createChannel);
        return jt1078Channel.doSubscribe(cls, creator);
    }

    @Override
    public void unsubscribe(String id, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        for (final Map.Entry<Jt1078Channel.ChannelKey, Jt1078Channel> entry : this.channels.entrySet()) {
            final Jt1078Channel.ChannelKey channelKey = entry.getKey();
            final String prefix = Jt1078Channel.ChannelKey.prefix(channelKey.sim(), channelKey.channelNumber());
            if (id.startsWith(prefix)) {
                final Jt1078Channel jt1078Channel = entry.getValue();
                jt1078Channel.unsubscribe(id);
            }
        }
    }

    @Override
    public void unsubscribeWithSim(String sim, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        this.doUnsubscribe(channelKey -> channelKey.sim().equals(sim), reason);
    }

    @Override
    public void unsubscribeWithSimAndChannelNumber(String sim, short channelNumber, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        this.doUnsubscribe(channelKey -> channelKey.sim().equals(sim) && channelKey.channelNumber() == channelNumber, reason);
    }

    private void doUnsubscribe(Predicate<Jt1078Channel.ChannelKey> predicate, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        for (final Iterator<Map.Entry<Jt1078Channel.ChannelKey, Jt1078Channel>> iterator = this.channels.entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<Jt1078Channel.ChannelKey, Jt1078Channel> entry = iterator.next();
            final Jt1078Channel.ChannelKey channelKey = entry.getKey();
            if (predicate.test(channelKey)) {
                iterator.remove();
                final Jt1078Channel jt1078Channel = entry.getValue();
                jt1078Channel.close(reason);
            }
        }
    }

    protected Jt1078Channel createChannel(Jt1078Channel.ChannelKey channelKey) {
        return new DefaultJt1078Channel(channelKey, this.jt1078SimConverter, this.scheduler);
    }

}

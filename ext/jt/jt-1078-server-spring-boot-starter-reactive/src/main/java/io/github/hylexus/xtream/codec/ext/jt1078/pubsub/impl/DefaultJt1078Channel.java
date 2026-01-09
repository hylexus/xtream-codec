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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.*;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.collector.H264ToFlvJt1078ChannelCollector;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SimConverter;
import org.jspecify.annotations.Nullable;
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultJt1078Channel implements Jt1078Channel {

    // private static final Logger log = LoggerFactory.getLogger(DefaultJt1078Channel.class);
    private final ChannelKey key;
    private final Jt1078SimConverter jt1078SimConverter;
    // <TypeOfJt1078ChannelCollector, Jt1078ChannelCollector>
    // 同一个Channel上 同一种类型的Jt1078ChannelCollector 只有一个实例
    private final ConcurrentMap<Class<? extends Jt1078ChannelCollector>, Jt1078ChannelCollector> collectors = new ConcurrentHashMap<>();
    private final Scheduler scheduler;

    public DefaultJt1078Channel(ChannelKey key, Jt1078SimConverter jt1078SimConverter, Scheduler scheduler) {
        this.key = key;
        this.jt1078SimConverter = jt1078SimConverter;
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public ChannelKey key() {
        return this.key;
    }

    @Override
    public Jt1078SimConverter simConverter() {
        return this.jt1078SimConverter;
    }

    @Override
    public void publish(Jt1078Request request) {
        for (final Jt1078ChannelCollector collector : this.collectors.values()) {
            collector.collect(request);
        }
    }

    @Override
    public Jt1078Subscriber doSubscribe(Class<? extends Jt1078ChannelCollector> cls, Jt1078SubscriberCreator creator) {
        creator.convertedSim(this.jt1078SimConverter.convert(creator.rawSim()));
        final Jt1078ChannelCollector channelCollector = this.getOrCreate(cls, creator);
        return channelCollector.doSubscribe(creator);
    }

    @Override
    public void unsubscribe(String id, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        for (Map.Entry<Class<? extends Jt1078ChannelCollector>, Jt1078ChannelCollector> entry : this.collectors.entrySet()) {
            final Jt1078ChannelCollector collector = entry.getValue();
            collector.unsubscribe(id, reason);
        }
    }

    @Override
    public synchronized void close(Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        final var iterator = this.collectors.entrySet().iterator();
        while (iterator.hasNext()) {
            final var entry = iterator.next();
            iterator.remove();
            final Jt1078ChannelCollector collector = entry.getValue();
            collector.close(reason);
        }
    }

    @Override
    public long countSubscribers(Predicate<Jt1078SubscriberDescriptor> predicate) {
        return this.collectors.values().stream()
                .flatMap(it -> it.list().filter(predicate))
                .count();
    }

    @Override
    public Stream<Jt1078SubscriberDescriptor> listSubscribers() {
        return this.collectors.values().stream()
                .flatMap(Jt1078ChannelCollector::list);
    }

    protected synchronized Jt1078ChannelCollector getOrCreate(Class<? extends Jt1078ChannelCollector> cls, Jt1078SubscriberCreator creator) {
        return this.collectors.computeIfAbsent(cls, k -> new H264ToFlvJt1078ChannelCollector(this.key, this.scheduler, creator));
    }
}

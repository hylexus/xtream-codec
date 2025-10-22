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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.collector;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscription;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.FluxSink;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class AbstractInternalSubscriber
        implements InternalSubscriber {

    protected final String id;

    protected final Jt1078SubscriberCreator creator;

    protected final LocalDateTime createdAt;

    protected final FluxSink<Jt1078Subscription> sink;

    protected @Nullable Jt1078PayloadType rawAudioType;
    protected @Nullable Jt1078PayloadType rawVideoType;

    protected @Nullable String convertedAudioType;

    public AbstractInternalSubscriber(
            String id,
            Jt1078SubscriberCreator creator,
            LocalDateTime createdAt,
            FluxSink<Jt1078Subscription> sink) {
        this.creator = creator;
        this.id = id;
        this.createdAt = createdAt;
        this.sink = sink;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String convertedSim() {
        return this.creator.convertedSim();
    }

    @Override
    public String rawSim() {
        return this.creator.rawSim();
    }

    @Override
    public short channel() {
        return this.creator.channelNumber();
    }

    @Override
    public @Nullable Jt1078PayloadType rawAudioType() {
        return this.rawAudioType;
    }

    @Override
    public @Nullable Jt1078PayloadType rawVideoType() {
        return this.rawVideoType;
    }

    @Override
    public @Nullable String convertedAudioType() {
        return this.convertedAudioType;
    }

    @Override
    public String desc() {
        return this.creator.desc();
    }

    @Override
    public LocalDateTime createdAt() {
        return this.createdAt;
    }

    @Override
    public Map<String, Object> metadata() {
        return this.creator.metadata();
    }

    @Override
    public FluxSink<Jt1078Subscription> sink() {
        return this.sink;
    }

}

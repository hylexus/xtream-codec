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

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscription;
import reactor.core.publisher.Flux;

public class DefaultJt1078Subscriber implements Jt1078Subscriber {
    private final String id;
    private final Flux<Jt1078Subscription> dataStream;

    public DefaultJt1078Subscriber(String id, Flux<Jt1078Subscription> dataStream) {
        this.id = id;
        this.dataStream = dataStream;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Flux<Jt1078Subscription> dataStream() {
        return this.dataStream;
    }

}

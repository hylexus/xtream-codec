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

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscription;

public class ByteArrayJt1078Subscription implements Jt1078Subscription {
    private final Jt1078SubscriptionType type;
    private final byte[] payload;

    public ByteArrayJt1078Subscription(Jt1078SubscriptionType type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    @Override
    public Jt1078SubscriptionType type() {
        return this.type;
    }

    @Override
    public byte[] payload() {
        return this.payload;
    }
}

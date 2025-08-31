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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.core.EntityDecoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DefaultDeserializeContext implements FieldCodec.DeserializeContext {
    private final CodecTracker codecTracker;
    private final EntityDecoder entityDecoder;
    private final Object containerInstance;
    private final EvaluationContext evaluationContext;
    private final int version;

    public DefaultDeserializeContext(FieldCodec.DeserializeContext another, Object containerInstance) {
        this(another.entityDecoder(), containerInstance, another.version(), another.codecTracker());
    }

    public DefaultDeserializeContext(EntityDecoder entityDecoder, Object containerInstance, int version, CodecTracker tracker) {
        this.entityDecoder = entityDecoder;
        this.containerInstance = containerInstance;
        this.evaluationContext = new StandardEvaluationContext(containerInstance);
        this.version = version;
        this.codecTracker = tracker;
    }

    @Override
    public Object containerInstance() {
        return this.containerInstance;
    }

    @Override
    public EvaluationContext evaluationContext() {
        return this.evaluationContext;
    }

    @Override
    public int version() {
        return this.version;
    }

    @Override
    public EntityDecoder entityDecoder() {
        return this.entityDecoder;
    }

    @Override
    public CodecTracker codecTracker() {
        return this.codecTracker;
    }

}

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

import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.EntityDecoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DefaultDeserializeContext implements FieldCodec.DeserializeContext {
    private final ByteBufAllocator bufferFactory;
    private final EntityDecoder entityDecoder;
    private final Object containerInstance;
    private final EvaluationContext evaluationContext;
    private final FieldCodecRegistry fieldCodecRegistry;
    private final BeanMetadataRegistry beanMetadataRegistry;
    private final int version;
    private final CodecTracker codecTracker;

    public DefaultDeserializeContext(FieldCodec.DeserializeContext another, Object containerInstance) {
        this(another.bufferFactory(), another.entityDecoder(), containerInstance, another.version(), another.beanMetadataRegistry(), another.codecTracker());
    }

    public DefaultDeserializeContext(ByteBufAllocator bufferFactory, EntityDecoder entityDecoder, Object containerInstance, int version, BeanMetadataRegistry beanMetadataRegistry, CodecTracker tracker) {
        this.bufferFactory = bufferFactory;
        this.entityDecoder = entityDecoder;
        this.containerInstance = containerInstance;
        this.evaluationContext = new StandardEvaluationContext(containerInstance);
        this.version = version;
        this.codecTracker = tracker;
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = beanMetadataRegistry.getFieldCodecRegistry();
    }

    @Override
    public ByteBufAllocator bufferFactory() {
        return this.bufferFactory;
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
    public FieldCodecRegistry fieldCodecRegistry() {
        return this.fieldCodecRegistry;
    }

    @Override
    public BeanMetadataRegistry beanMetadataRegistry() {
        return this.beanMetadataRegistry;
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

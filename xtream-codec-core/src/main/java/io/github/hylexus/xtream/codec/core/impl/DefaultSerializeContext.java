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

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.EntityEncoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

public class DefaultSerializeContext implements FieldCodec.SerializeContext {
    private final ByteBufAllocator bufferFactory;
    private final EntityEncoder entityEncoder;
    private final Object containerInstance;
    private final XtreamEvaluationContext evaluationContext;
    private final FieldCodecRegistry fieldCodecRegistry;
    private final BeanMetadataRegistry beanMetadataRegistry;
    private final int version;
    private final @Nullable CodecTracker codecTracker;

    public DefaultSerializeContext(FieldCodec.SerializeContext another, Object containerInstance) {
        this(another.bufferFactory(), another.entityEncoder(), containerInstance, another.version(), another.beanMetadataRegistry(), another.codecTracker());
    }

    public DefaultSerializeContext(ByteBufAllocator bufferFactory, EntityEncoder entityEncoder, Object containerInstance, int version, BeanMetadataRegistry beanMetadataRegistry, @Nullable CodecTracker codecTracker) {
        this.bufferFactory = bufferFactory;
        this.entityEncoder = entityEncoder;
        this.containerInstance = containerInstance;
        this.evaluationContext = entityEncoder.expressionEngine().createEvaluationContext(containerInstance);
        this.version = version;
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = this.beanMetadataRegistry.getFieldCodecRegistry();
        this.codecTracker = codecTracker;
    }

    @Override
    public EntityEncoder entityEncoder() {
        return this.entityEncoder;
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
    public XtreamEvaluationContext evaluationContext() {
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
    public @Nullable CodecTracker codecTracker() {
        return this.codecTracker;
    }

}

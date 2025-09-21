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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class EntityEncoder {
    protected final ByteBufAllocator bufferFactory = ByteBufAllocator.DEFAULT;
    private final BeanMetadataRegistry beanMetadataRegistry;
    private final FieldCodecRegistry fieldCodecRegistry;

    public EntityEncoder(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = beanMetadataRegistry.getFieldCodecRegistry();
    }

    public void encode(Object instance, ByteBuf target) {
        this.encode(XtreamField.ALL_VERSION, instance, target);
    }

    public void encode(int version, Object instance, ByteBuf target) {
        if (instance == null) {
            return;
        }
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(instance.getClass(), version);
        this.encode(version, beanMetadata, instance, target);
    }

    public void encode(BeanMetadata beanMetadata, Object instance, ByteBuf target) {
        this.encode(XtreamField.ALL_VERSION, beanMetadata, instance, target);
    }

    public void encode(int version, BeanMetadata beanMetadata, Object instance, ByteBuf target) {
        if (instance == null) {
            return;
        }
        final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, null);
        for (final BeanPropertyMetadata propertyMetadata : beanMetadata.getPropertyMetadataList()) {
            if (propertyMetadata.xtreamFieldAnnotation().codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
                continue;
            }
            final Object value = propertyMetadata.getProperty(instance);
            if (value == null) {
                continue;
            }

            if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                propertyMetadata.encodePropertyValue(context, target, value);
            }
        }
    }

    // region withTracker
    @SuppressWarnings("unused")
    public void encodeWithTracker(Object instance, ByteBuf target, CodecTracker tracker) {
        this.encodeWithTracker(XtreamField.ALL_VERSION, instance, target, tracker);
    }

    public void encodeWithTracker(int version, Object instance, ByteBuf target, CodecTracker tracker) {
        if (instance == null) {
            return;
        }
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(instance.getClass(), version);
        this.encodeWithTracker(version, beanMetadata, instance, target, tracker);
    }

    public void encodeWithTracker(BeanMetadata beanMetadata, Object instance, ByteBuf target, CodecTracker tracker) {
        this.encodeWithTracker(XtreamField.ALL_VERSION, beanMetadata, instance, target, tracker);
    }

    public void encodeWithTracker(int version, BeanMetadata beanMetadata, Object instance, ByteBuf target, CodecTracker tracker) {
        if (instance == null) {
            return;
        }
        final FieldCodec.SerializeContext context = new DefaultSerializeContext(this.bufferFactory, this, instance, version, this.beanMetadataRegistry, tracker);
        final int indexBeforeWrite = target.writerIndex();
        if (tracker.getRootSpan().getEntityClass() == null) {
            tracker.getRootSpan().setEntityClass(beanMetadata.getRawType().getName());
        }
        for (final BeanPropertyMetadata propertyMetadata : beanMetadata.getPropertyMetadataList()) {
            if (propertyMetadata.xtreamFieldAnnotation().codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
                continue;
            }
            final Object value = propertyMetadata.getProperty(instance);
            if (value == null) {
                continue;
            }

            if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                propertyMetadata.encodePropertyValueWithTracker(context, target, value);
            }
        }
        tracker.getRootSpan().setHexString(FormatUtils.toHexString(target, indexBeforeWrite, target.writerIndex() - indexBeforeWrite));
    }
    // endregion withTracker

    @SuppressWarnings("redundent")
    public BeanMetadataRegistry getBeanMetadataRegistry() {
        return beanMetadataRegistry;
    }

    @SuppressWarnings("redundent")
    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
    }
}

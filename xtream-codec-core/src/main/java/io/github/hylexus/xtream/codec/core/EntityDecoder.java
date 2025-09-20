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
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.List;
import java.util.Objects;

public class EntityDecoder {
    protected final ByteBufAllocator bufferFactory = ByteBufAllocator.DEFAULT;
    protected final BeanMetadataRegistry beanMetadataRegistry;
    private final FieldCodecRegistry fieldCodecRegistry;

    public EntityDecoder(BeanMetadataRegistry beanMetadataRegistry) {
        this.beanMetadataRegistry = beanMetadataRegistry;
        this.fieldCodecRegistry = beanMetadataRegistry.getFieldCodecRegistry();
    }

    public <T> T decode(Class<T> entityClass, ByteBuf source) {
        return this.decode(XtreamField.ALL_VERSION, entityClass, source);
    }

    public <T> T decode(int version, Class<T> entityClass, ByteBuf source) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(entityClass, version);
        final Object containerInstance = beanMetadata.createNewInstance();
        return this.decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(BeanMetadata beanMetadata, ByteBuf source) {
        return this.decode(XtreamField.ALL_VERSION, beanMetadata, source);
    }

    public <T> T decode(int version, BeanMetadata beanMetadata, ByteBuf source) {
        final Object containerInstance = beanMetadata.createNewInstance();
        return decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(ByteBuf source, Object containerInstance) {
        return this.decode(XtreamField.ALL_VERSION, source, containerInstance);
    }

    public <T> T decode(int version, ByteBuf source, Object containerInstance) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(containerInstance.getClass(), version);
        return decode(version, source, beanMetadata, containerInstance);
    }

    public <T> T decode(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance) {
        return decode(XtreamField.ALL_VERSION, source, beanMetadata, containerInstance);
    }

    public <T> T decode(int version, ByteBuf source, BeanMetadata beanMetadata, Object containerInstance) {
        final FieldCodec.DeserializeContext context = new DefaultDeserializeContext(this.bufferFactory, this, containerInstance, version, this.beanMetadataRegistry, null);
        if (beanMetadata.getRawType().isRecord()) {
            final Object[] filedValues = new Object[beanMetadata.getPropertyMetadataList().size()];
            final List<BeanPropertyMetadata> propertyMetadataList = beanMetadata.getPropertyMetadataList();
            for (int i = 0; i < propertyMetadataList.size(); i++) {
                final BeanPropertyMetadata propertyMetadata = propertyMetadataList.get(i);
                if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                    final Object fieldValue = propertyMetadata.decodePropertyValue(context, source);
                    filedValues[i] = fieldValue;
                } else {
                    final XtreamField fieldAnnotation = propertyMetadata.xtreamFieldAnnotation();
                    if (fieldAnnotation instanceof XtreamFieldUtils.XtreamTransientFieldProxy proxy) {
                        final Object defaultValue = proxy.defaultValueForNulls();
                        filedValues[i] = defaultValue;
                    } else {
                        filedValues[i] = XtreamFieldUtils.createDefaultValueForNulls(fieldAnnotation.nulls(), propertyMetadata.rawClass());
                    }
                }
            }
            return beanMetadata.createNewRecordInstance(filedValues);
        } else {
            for (final BeanPropertyMetadata propertyMetadata : beanMetadata.getPropertyMetadataList()) {
                if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                    final Object fieldValue = propertyMetadata.decodePropertyValue(context, source);
                    propertyMetadata.setProperty(containerInstance, fieldValue);
                }
            }
            @SuppressWarnings("unchecked") final T casted = (T) containerInstance;
            return casted;
        }
    }

    // with tracker
    // with tracer
    public <T> T decodeWithTracker(Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, entityClass, source, tracker);
    }

    public <T> T decodeWithTracker(int version, Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(entityClass, version);
        final Object containerInstance = beanMetadata.createNewInstance();
        return this.decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(BeanMetadata beanMetadata, ByteBuf source, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, beanMetadata, source, tracker);
    }

    public <T> T decodeWithTracker(int version, BeanMetadata beanMetadata, ByteBuf source, CodecTracker tracker) {
        final Object containerInstance = beanMetadata.createNewInstance();
        return decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(ByteBuf source, Object containerInstance, CodecTracker tracker) {
        return this.decodeWithTracker(XtreamField.ALL_VERSION, source, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(int version, ByteBuf source, Object containerInstance, CodecTracker tracker) {
        final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(containerInstance.getClass(), version);
        return decodeWithTracker(version, source, beanMetadata, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(ByteBuf source, BeanMetadata beanMetadata, Object containerInstance, CodecTracker tracker) {
        return decodeWithTracker(XtreamField.ALL_VERSION, source, beanMetadata, containerInstance, tracker);
    }

    public <T> T decodeWithTracker(int version, ByteBuf source, BeanMetadata beanMetadata, Object containerInstance, CodecTracker tracker) {
        Objects.requireNonNull(tracker);
        final int indexBeforeRead = source.readerIndex();
        if (tracker.getRootSpan().getEntityClass() == null) {
            tracker.getRootSpan().setEntityClass(beanMetadata.getRawType().getName());
        }
        final FieldCodec.DeserializeContext context = new DefaultDeserializeContext(this.bufferFactory, this, containerInstance, version, this.beanMetadataRegistry, tracker);
        for (final BeanPropertyMetadata propertyMetadata : beanMetadata.getPropertyMetadataList()) {
            if (propertyMetadata.conditionEvaluator().evaluate(context)) {
                final Object fieldValue = propertyMetadata.decodePropertyValueWithTracker(context, source);
                propertyMetadata.setProperty(containerInstance, fieldValue);
            }
        }
        tracker.getRootSpan().setHexString(FormatUtils.toHexString(source, indexBeforeRead, source.readerIndex() - indexBeforeRead));
        @SuppressWarnings("unchecked") final T instance1 = (T) containerInstance;
        return instance1;
    }

    public BeanMetadataRegistry getBeanMetadataRegistry() {
        return beanMetadataRegistry;
    }

    public FieldCodecRegistry getFieldCodecRegistry() {
        return fieldCodecRegistry;
    }
}

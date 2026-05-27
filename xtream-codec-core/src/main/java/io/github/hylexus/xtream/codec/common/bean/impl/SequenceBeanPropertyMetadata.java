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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.CollectionFieldSpan;
import io.github.hylexus.xtream.codec.core.tracker.CollectionItemSpan;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class SequenceBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    // todo nestedBeanPropertyMetadata 配置
    private final NestedBeanPropertyMetadata nestedBeanPropertyMetadata;
    private final BeanPropertyMetadata delegate;
    private final ContainerInstanceFactory containerInstanceFactory;

    public SequenceBeanPropertyMetadata(BeanMetadataRegistry beanMetadataRegistry, BeanPropertyMetadata delegate, NestedBeanPropertyMetadata metadata) {
        super(beanMetadataRegistry, delegate.name(), delegate.rawClass(), delegate.version(), delegate.xtreamFieldAnnotation(), delegate.field(), delegate.propertyGetter(), delegate.propertySetter());
        this.nestedBeanPropertyMetadata = metadata;
        this.delegate = delegate;
        this.containerInstanceFactory = this.xtreamField.containerInstanceFactory() == ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class
                ? BeanUtils.createNewInstance(ContainerInstanceFactory.ArrayListContainerInstanceFactory.class, (Object[]) null)
                : BeanUtils.createNewInstance(this.xtreamField.containerInstanceFactory(), (Object[]) null);
    }

    @Override
    public ContainerInstanceFactory containerInstanceFactory() {
        return this.containerInstanceFactory;
    }

    @Override
    public void doEncode(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final Collection<@Nullable Object> collection = (Collection<Object>) value;
        int sequence = 0;
        for (final Object object : collection) {
            sequence++;
            if (object == null) {
                continue;
            }
            final Class<?> entityClass = object.getClass();
            if (object instanceof FieldCodecRegistry.AtomicDataType) {
                final int finalSequence = sequence;
                final FieldCodec<Object> fieldCodec = this.beanMetadataRegistry.getFieldCodecRegistry().getFieldCodecAndCastToObject(-1, xtreamField.signedness(), null, xtreamField.littleEndian(), object.getClass())
                        .orElseThrow(() -> new IllegalStateException("""
                                No FieldCode found
                                ==> field: %s
                                    elementOffset: %d
                                    elementType: %s
                                """.strip().formatted(this.nestedBeanPropertyMetadata.field().toGenericString(), finalSequence, object.getClass().getName())));
                fieldCodec.serialize(this.delegate, context, output, object);
            } else {
                final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(entityClass, context.version());
                context.entityEncoder().encode(context.version(), beanMetadata, object, output);
            }
        }
    }

    @Override
    protected void doEncodeWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final Collection<@Nullable Object> collection = (Collection<Object>) value;
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(this);
        final int parentIndexBeforeWrite = output.writerIndex();
        int sequence = 0;
        for (final Object object : collection) {
            if (object == null) {
                continue;
            }
            if (object instanceof FieldCodecRegistry.AtomicDataType) {
                final int finalSequence = sequence;
                final FieldCodec<Object> fieldCodec = this.beanMetadataRegistry.getFieldCodecRegistry().getFieldCodecAndCastToObject(-1, xtreamField.signedness(), null, xtreamField.littleEndian(), object.getClass())
                        .orElseThrow(() -> new IllegalStateException("""
                                No FieldCode found
                                ==> field: %s
                                    elementOffset: %d
                                    elementType: %s
                                """.strip().formatted(this.nestedBeanPropertyMetadata.field().toGenericString(), finalSequence, object.getClass().getName())));
                fieldCodec.serializeWithTracker(this.delegate, context, output, object);
            } else {
                final Class<?> entityClass = object.getClass();
                final BeanMetadata beanMetadata = this.beanMetadataRegistry.getBeanMetadata(entityClass, context.version());

                final int indexBeforeWrite = output.writerIndex();
                final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);

                context.entityEncoder().encodeWithTracker(context.version(), beanMetadata, object, output, codecTracker);

                collectionItemSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }
        }
        collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
        codecTracker.finishCurrentSpan();
    }

    @Override
    public @Nullable Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        int iterationTimes = this.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
        @SuppressWarnings("unchecked") final Collection<@Nullable Object> list = (Collection<Object>) this.containerInstanceFactory().create();
        while (slice.isReadable() && iterationTimes-- > 0) {
            final Object value = nestedBeanPropertyMetadata.decodePropertyValue(context, slice);
            list.add(value);
        }
        return list;
    }

    @Override
    public @Nullable Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        int iterationTimes = this.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(this);
        @SuppressWarnings("unchecked") final Collection<@Nullable Object> list = (Collection<Object>) this.containerInstanceFactory().create();
        int sequence = 0;
        final int parentIndexBeforeRead = input.readerIndex();
        while (slice.isReadable() && iterationTimes-- > 0) {
            final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
            final int indexBeforeRead = input.readerIndex();
            final Object value = nestedBeanPropertyMetadata.decodePropertyValueWithTracker(context, slice);
            collectionItemSpan.setHexString(FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead));
            list.add(value);
            codecTracker.finishCurrentSpan();
        }
        collectionFieldSpan.setHexString(FormatUtils.toHexString(input, parentIndexBeforeRead, input.readerIndex() - parentIndexBeforeRead));
        codecTracker.finishCurrentSpan();
        return list;
    }
}

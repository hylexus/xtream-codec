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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.IterationTimesExtractor;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CollectionFieldSpan;
import io.github.hylexus.xtream.codec.core.tracker.CollectionItemSpan;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import org.springframework.util.StringUtils;

import java.util.Collection;

public class SequenceBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    private final NestedBeanPropertyMetadata nestedBeanPropertyMetadata;
    private final BeanPropertyMetadata delegate;
    private final ContainerInstanceFactory containerInstanceFactory;
    private final IterationTimesExtractor iterationTimesExtractor;

    public SequenceBeanPropertyMetadata(BeanMetadataRegistry beanMetadataRegistry, BeanPropertyMetadata delegate, NestedBeanPropertyMetadata metadata) {
        super(beanMetadataRegistry, delegate.name(), delegate.rawClass(), delegate.version(), delegate.xtreamFieldAnnotation(), delegate.field(), delegate.propertyGetter(), delegate.propertySetter());
        this.nestedBeanPropertyMetadata = metadata;
        this.delegate = delegate;
        this.containerInstanceFactory = this.xtreamField.containerInstanceFactory() == ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class
                ? BeanUtils.createNewInstance(ContainerInstanceFactory.ArrayListContainerInstanceFactory.class, (Object[]) null)
                : BeanUtils.createNewInstance(this.xtreamField.containerInstanceFactory(), (Object[]) null);
        this.iterationTimesExtractor = this.initIterationTimesExtractor(this.xtreamField);
    }

    private IterationTimesExtractor initIterationTimesExtractor(XtreamField xtreamField) {
        if (xtreamField.iterationTimes() > 0) {
            return new IterationTimesExtractor.ConstantIterationTimesExtractor(xtreamField.iterationTimes());
        }
        if (StringUtils.hasText(xtreamField.iterationTimesExpression())) {
            return new IterationTimesExtractor.ExpressionIterationTimesExtractor(xtreamField);
        }
        return IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
    }

    @Override
    public ContainerInstanceFactory containerInstanceFactory() {
        return this.containerInstanceFactory;
    }

    @Override
    public IterationTimesExtractor iterationTimesExtractor() {
        return this.iterationTimesExtractor;
    }

    @Override
    public void doEncode(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final Collection<Object> collection = (Collection<Object>) value;
        for (final Object object : collection) {
            nestedBeanPropertyMetadata.encodePropertyValue(context, output, object);
        }
    }

    @Override
    protected void doEncodeWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final Collection<Object> collection = (Collection<Object>) value;
        final CollectionFieldSpan collectionFieldSpan = context.codecTracker().startNewCollectionFieldSpan(this);
        final int parentIndexBeforeWrite = output.writerIndex();
        int sequence = 0;
        for (final Object object : collection) {
            final int indexBeforeWrite = output.writerIndex();
            final CollectionItemSpan collectionItemSpan = context.codecTracker().startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
            nestedBeanPropertyMetadata.encodePropertyValueWithTracker(context, output, object);
            collectionItemSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
            context.codecTracker().finishCurrentSpan();
        }
        collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
        context.codecTracker().finishCurrentSpan();
    }

    @Override
    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        int iterationTimes = this.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
        @SuppressWarnings("unchecked") final Collection<Object> list = (Collection<Object>) this.containerInstanceFactory().create();
        while (slice.isReadable() && iterationTimes-- > 0) {
            final Object value = nestedBeanPropertyMetadata.decodePropertyValue(context, slice);
            list.add(value);
        }
        return list;
    }

    @Override
    public Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        int iterationTimes = this.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
        final CollectionFieldSpan collectionFieldSpan = context.codecTracker().startNewCollectionFieldSpan(this);
        @SuppressWarnings("unchecked") final Collection<Object> list = (Collection<Object>) this.containerInstanceFactory().create();
        int sequence = 0;
        final int parentIndexBeforeRead = input.readerIndex();
        while (slice.isReadable() && iterationTimes-- > 0) {
            final CollectionItemSpan collectionItemSpan = context.codecTracker().startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
            final int indexBeforeRead = input.readerIndex();
            final Object value = nestedBeanPropertyMetadata.decodePropertyValueWithTracker(context, slice);
            collectionItemSpan.setHexString(FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead));
            list.add(value);
            context.codecTracker().finishCurrentSpan();
        }
        collectionFieldSpan.setHexString(FormatUtils.toHexString(input, parentIndexBeforeRead, input.readerIndex() - parentIndexBeforeRead));
        context.codecTracker().finishCurrentSpan();
        return list;
    }
}

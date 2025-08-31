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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;

public class NestedBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    final BeanPropertyMetadata delegate;
    final BeanMetadata nestedBeanMetadata;
    private final FieldLengthExtractor fieldLengthExtractor;
    private final ContainerInstanceFactory containerInstanceFactory;

    public NestedBeanPropertyMetadata(BeanMetadataRegistry beanMetadataRegistry, BeanMetadata nestedBeanMetadata, BeanPropertyMetadata pm, FieldLengthExtractor fieldLengthExtractor) {
        super(beanMetadataRegistry, pm.name(), pm.rawClass(), pm.version(), pm.xtreamFieldAnnotation(), pm.field(), pm.propertyGetter(), pm.propertySetter());
        this.nestedBeanMetadata = nestedBeanMetadata;
        this.delegate = pm;
        this.fieldLengthExtractor = fieldLengthExtractor == null
                ? delegate.fieldLengthExtractor()
                : fieldLengthExtractor;
        this.containerInstanceFactory = this.delegate.containerInstanceFactory().getClass() == ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class
                ? () -> BeanUtils.createNewInstance(nestedBeanMetadata.getConstructor(), (Object[]) null)
                : BeanUtils.createNewInstance(this.xtreamField.containerInstanceFactory(), (Object[]) null);
    }

    @Override
    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final Object instance = BeanUtils.createNewInstance(nestedBeanMetadata.getConstructor());
        // final Object instance = this.containerInstanceFactory().create();
        final int length = this.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);

        final FieldCodec.DeserializeContext deserializeContext = new DefaultDeserializeContext(context, instance);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (!pm.conditionEvaluator().evaluate(deserializeContext)) {
                continue;
            }
            Object value = pm.decodePropertyValue(deserializeContext, slice);
            pm.setProperty(instance, value);
        }
        return instance;
    }

    @Override
    public Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        final Object instance = BeanUtils.createNewInstance(nestedBeanMetadata.getConstructor());
        // final Object instance = this.containerInstanceFactory().create();
        final int length = this.fieldLengthExtractor().extractFieldLengthWithTracker(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(this, this.getClass().getSimpleName(), null);
        final int indexBeforeRead = slice.readerIndex();
        final FieldCodec.DeserializeContext deserializeContext = new DefaultDeserializeContext(context, instance);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (!pm.conditionEvaluator().evaluate(deserializeContext)) {
                continue;
            }
            Object value = pm.decodePropertyValueWithTracker(deserializeContext, slice);
            pm.setProperty(instance, value);
        }
        nestedFieldSpan.setHexString(FormatUtils.toHexString(slice, indexBeforeRead, slice.readerIndex() - indexBeforeRead));
        context.codecTracker().finishCurrentSpan();
        return instance;
    }

    @Override
    public void doEncode(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        final DefaultSerializeContext serializeContext = new DefaultSerializeContext(context, value);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (!pm.conditionEvaluator().evaluate(serializeContext)) {
                continue;
            }
            final Object nestedValue = pm.getProperty(value);
            pm.encodePropertyValue(serializeContext, output, nestedValue);
        }
    }

    @Override
    protected void doEncodeWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        final DefaultSerializeContext serializeContext = new DefaultSerializeContext(context, value);
        final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(this, this.getClass().getSimpleName(), null);
        final int indexBeforeWrite = output.writerIndex();
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (!pm.conditionEvaluator().evaluate(serializeContext)) {
                continue;
            }
            final Object nestedValue = pm.getProperty(value);
            pm.encodePropertyValueWithTracker(serializeContext, output, nestedValue);
        }
        nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
        context.codecTracker().finishCurrentSpan();
    }

    @Override
    public ContainerInstanceFactory containerInstanceFactory() {
        return this.containerInstanceFactory;
    }

    @Override
    public FieldLengthExtractor fieldLengthExtractor() {
        return this.fieldLengthExtractor;
    }
}

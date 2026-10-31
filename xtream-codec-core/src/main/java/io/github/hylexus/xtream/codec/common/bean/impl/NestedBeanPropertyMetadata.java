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
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author hylexus
 * @author opencode (AI)
 * @author Codex (AI)
 */
public class NestedBeanPropertyMetadata extends BasicBeanPropertyMetadata {

    final BeanPropertyMetadata delegate;
    final BeanMetadata nestedBeanMetadata;
    private final FieldLengthExtractor fieldLengthExtractor;
    private final ContainerInstanceFactory containerInstanceFactory;

    public NestedBeanPropertyMetadata(BeanMetadataRegistry beanMetadataRegistry, BeanMetadata nestedBeanMetadata, BeanPropertyMetadata pm, FieldLengthExtractor fieldLengthExtractor) {
        super(beanMetadataRegistry, pm.name(), pm.rawClass(), pm.version(), pm.xtreamFieldAnnotation(), pm.field(), pm.propertyGetter(), pm.propertySetter());
        this.nestedBeanMetadata = nestedBeanMetadata;
        this.delegate = pm;
        this.fieldLengthExtractor = fieldLengthExtractor;
        this.containerInstanceFactory = this.delegate.containerInstanceFactory().getClass() == ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class
                ? nestedBeanMetadata::createNewInstanceForDecoding
                : BeanUtils.createNewInstance(this.xtreamField.containerInstanceFactory(), (Object[]) null);
    }

    @Override
    public @Nullable Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final Object instance = nestedBeanMetadata.createNewInstanceForDecoding();
        final int length = this.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);

        final FieldCodec.DeserializeContext newContext = new DefaultDeserializeContext(context, instance);
        if (!nestedBeanMetadata.getRawType().isRecord()) {
            return this.decodePojoFields(newContext, slice, instance, false);
        }
        return this.decodeRecordFields(newContext, slice, instance, false);
    }

    @Override
    public @Nullable Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        final Object instance = nestedBeanMetadata.createNewInstanceForDecoding();
        final int length = this.fieldLengthExtractor().extractFieldLengthWithTracker(context, context.evaluationContext(), input);

        final int inputReaderIndexBeforeSlice = input.readerIndex();
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        try (final CodecTracker.TraceScope scope = codecTracker.enterNestedField(this, this.getClass(), inputReaderIndexBeforeSlice)) {
            final FieldCodec.DeserializeContext newContext = new DefaultDeserializeContext(context, instance);
            final Object result;
            if (length < 0) {
                if (!nestedBeanMetadata.getRawType().isRecord()) {
                    result = this.decodePojoFields(newContext, slice, instance, true);
                } else {
                    result = this.decodeRecordFields(newContext, slice, instance, true);
                }
            } else {
                try (final CodecTracker.CoordinateScope ignored = codecTracker.openCoordinateBase(inputReaderIndexBeforeSlice)) {
                    if (!nestedBeanMetadata.getRawType().isRecord()) {
                        result = this.decodePojoFields(newContext, slice, instance, true);
                    } else {
                        result = this.decodeRecordFields(newContext, slice, instance, true);
                    }
                }
            }
            scope.complete(result, input.readerIndex());
            return result;
        }
    }


    private Object decodeRecordFields(FieldCodec.DeserializeContext newContext, ByteBuf slice, Object instance, boolean useTracker) {
        return useTracker
                ? newContext.entityDecoder().decodeWithTracker(newContext.version(), slice, this.nestedBeanMetadata, instance, Objects.requireNonNull(newContext.codecTracker()))
                : newContext.entityDecoder().decode(newContext.version(), slice, this.nestedBeanMetadata, instance);
    }

    private Object decodePojoFields(FieldCodec.DeserializeContext newContext, ByteBuf slice, Object instance, boolean useTracker) {
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (pm.isDerived() || !pm.conditionEvaluator().evaluate(newContext)) {
                newContext.evaluationContext().setVariable(pm.name(), null);
                continue;
            }
            final Object value = useTracker
                    ? pm.decodePropertyValueWithTracker(newContext, slice)
                    : pm.decodePropertyValue(newContext, slice);
            newContext.evaluationContext().setVariable(pm.name(), value);
            pm.setProperty(instance, value);

            // 内联求值：嵌套 Bean 的源字段解码后立即计算依赖它的派生字段值
            if (this.nestedBeanMetadata.hasDerivedFields()) {
                XtreamFieldUtils.applyDerivedFieldsInline(value, pm.name(), nestedBeanMetadata, (derived, derivedVal) -> {
                    derived.setProperty(instance, derivedVal);
                    newContext.evaluationContext().setVariable(derived.name(), derivedVal);
                });
            }
        }
        return instance;
    }

    @Override
    public void doEncode(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        if (this.nestedBeanMetadata.hasEncodedLengthField()) {
            context.entityEncoder().encode(context.version(), this.nestedBeanMetadata, value, output);
            return;
        }
        final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            if (pm.isDerived() || !pm.conditionEvaluator().evaluate(newContext)) {
                newContext.evaluationContext().setVariable(pm.name(), null);
                continue;
            }
            final Object nestedValue = XtreamFieldUtils.resolveEncodingValue(pm, value, this.nestedBeanMetadata);
            pm.encodePropertyValue(newContext, output, nestedValue);
            newContext.evaluationContext().setVariable(pm.name(), nestedValue);
        }
    }

    @Override
    protected void doEncodeWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final int indexBeforeWrite = output.writerIndex();
        try (final CodecTracker.TraceScope scope = codecTracker.enterNestedField(this, this.getClass(), indexBeforeWrite)) {
            if (this.nestedBeanMetadata.hasEncodedLengthField()) {
                context.entityEncoder().encodeWithTracker(context.version(), this.nestedBeanMetadata, value, output, codecTracker);
                scope.complete(value, output.writerIndex());
                return;
            }
            for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
                if (pm.isDerived() || !pm.conditionEvaluator().evaluate(newContext)) {
                    newContext.evaluationContext().setVariable(pm.name(), null);
                    continue;
                }
                final Object nestedValue = XtreamFieldUtils.resolveEncodingValue(pm, value, this.nestedBeanMetadata);
                pm.encodePropertyValueWithTracker(newContext, output, nestedValue);
                newContext.evaluationContext().setVariable(pm.name(), nestedValue);
            }
            scope.complete(value, output.writerIndex());
        }
    }

    @Override
    public ContainerInstanceFactory containerInstanceFactory() {
        return this.containerInstanceFactory;
    }

    @Override
    public FieldLengthExtractor fieldLengthExtractor() {
        return this.fieldLengthExtractor;
    }

    @Override
    public final boolean isEncodedLength() {
        return false;
    }

}

/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;

public class NestedBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    final BeanPropertyMetadata delegate;
    final BeanMetadata nestedBeanMetadata;
    private final FieldLengthExtractor fieldLengthExtractor;
    private final ContainerInstanceFactory containerInstanceFactory;

    public NestedBeanPropertyMetadata(BeanMetadata nestedBeanMetadata, BeanPropertyMetadata pm, FieldLengthExtractor fieldLengthExtractor) {
        super(pm.name(), pm.rawClass(), pm.field(), pm.propertyGetter(), pm.propertySetter());
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
        final int length = this.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext());

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
    public void encodePropertyValue(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        final DefaultSerializeContext serializeContext = new DefaultSerializeContext(context.entityEncoder(), value);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
            final Object nestedValue = pm.getProperty(value);
            pm.encodePropertyValue(serializeContext, output, nestedValue);
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
}

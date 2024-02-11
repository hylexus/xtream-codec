package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultDeserializeContext;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;

public class NestedBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    final BeanPropertyMetadata delegate;
    final BeanMetadata nestedBeanMetadata;

    public NestedBeanPropertyMetadata(BeanMetadata nestedBeanMetadata, BeanPropertyMetadata pm) {
        super(pm.name(), pm.rawClass(), pm.field(), pm.propertyGetter(), pm.propertySetter());
        this.nestedBeanMetadata = nestedBeanMetadata;
        this.delegate = pm;
    }

    @Override
    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final Object instance = BeanUtils.createNewInstance(nestedBeanMetadata.getConstructor());
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext());

        final ByteBuf slice = length < 0
                ? input
                : input.readSlice(length);

        final DefaultDeserializeContext deserializeContext = new DefaultDeserializeContext(instance);
        for (final BeanPropertyMetadata pm : this.nestedBeanMetadata.getPropertyMetadataList()) {
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
}

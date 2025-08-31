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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.MapEntryItemSpan;
import io.github.hylexus.xtream.codec.core.tracker.MapEntrySpan;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.netty.buffer.ByteBuf;

/**
 * 将编解码逻辑委托给上下文中的 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 * <p>
 * 实际上是递归调用 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.core.EntityDecoder
 * @see io.github.hylexus.xtream.codec.core.EntityEncoder
 * @since 0.0.1
 */
public class EntityFieldCodec<E> implements FieldCodec<Object> {
    protected final BeanMetadata beanMetadata;

    public EntityFieldCodec(int version, BeanMetadataRegistry registry, Class<E> entityClass) {
        this.beanMetadata = registry.getBeanMetadata(entityClass, version);
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        return context.entityDecoder().decode(beanMetadata, input);
    }

    @Override
    public Object deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final int indexBeforeRead = input.readerIndex();

        final Object value;
        if (context.codecTracker().getCurrentSpan() instanceof MapEntrySpan) {
            final MapEntryItemSpan mapEntryItemSpan = context.codecTracker().startNewMapEntryItemSpan(context.codecTracker().getCurrentSpan(), MapEntryItemSpan.Type.VALUE, this);
            value = context.entityDecoder().decodeWithTracker(beanMetadata, input, context.codecTracker());
            mapEntryItemSpan.setValue(value);
            mapEntryItemSpan.setHexString(FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead));
        } else {
            final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(propertyMetadata, this, beanMetadata.getRawType().getSimpleName());
            value = context.entityDecoder().decodeWithTracker(beanMetadata, input, context.codecTracker());
            nestedFieldSpan.setHexString(FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead));
        }

        context.codecTracker().finishCurrentSpan();
        return value;
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object instance) {
        context.entityEncoder().encode(context.version(), this.beanMetadata, instance, output);
    }

    @Override
    public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object instance) {
        final int indexBeforeWrite = output.writerIndex();
        if (context.codecTracker().getCurrentSpan() instanceof MapEntrySpan) {
            final MapEntryItemSpan mapEntryItemSpan = context.codecTracker().startNewMapEntryItemSpan(context.codecTracker().getCurrentSpan(), MapEntryItemSpan.Type.VALUE, this);
            context.entityEncoder().encodeWithTracker(this.beanMetadata, instance, output, context.codecTracker());
            mapEntryItemSpan.setValue(instance);
            mapEntryItemSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
        } else {
            final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(propertyMetadata, this, beanMetadata.getRawType().getSimpleName());
            context.entityEncoder().encodeWithTracker(this.beanMetadata, instance, output, context.codecTracker());
            nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
        }
        context.codecTracker().finishCurrentSpan();
    }

}

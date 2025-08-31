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
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.netty.buffer.ByteBuf;

/**
 * 将编解码逻辑委托给上下文中的 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 * <p>
 * 实际上是递归调用 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.core.EntityDecoder
 * @see io.github.hylexus.xtream.codec.core.EntityEncoder
 * @see io.github.hylexus.xtream.codec.common.bean.impl.MapBeanPropertyMetadata
 * @since 0.0.1
 */
public class DelegateBeanMetadataFieldCodec implements FieldCodec<Object> {
    private final BeanMetadata beanMetadata;

    public DelegateBeanMetadataFieldCodec(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        return context.entityDecoder().decode(context.version(), beanMetadata, input);
    }

    @Override
    public Object deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        return context.entityDecoder().decodeWithTracker(context.version(), beanMetadata, input, context.codecTracker());
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object instance) {
        context.entityEncoder().encode(context.version(), this.beanMetadata, instance, output);
    }

    @Override
    public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object instance) {
        context.entityEncoder().encodeWithTracker(context.version(), this.beanMetadata, instance, output, context.codecTracker());
    }

}

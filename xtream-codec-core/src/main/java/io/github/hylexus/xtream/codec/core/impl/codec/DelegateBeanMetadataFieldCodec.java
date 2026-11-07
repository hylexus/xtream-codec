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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 将编解码逻辑委托给上下文中的 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 * <p>
 * 实际上是递归调用 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder} 和 {@link io.github.hylexus.xtream.codec.core.EntityDecoder EntityDecoder}
 * 有界字段先隔离对应字节区域，避免嵌套字段读取外层的后续数据。
 *
 * @author hylexus
 * @author Codex (AI)
 * @see io.github.hylexus.xtream.codec.core.EntityDecoder
 * @see io.github.hylexus.xtream.codec.core.EntityEncoder
 * @see io.github.hylexus.xtream.codec.common.bean.impl.MapBeanPropertyMetadata
 * @since 0.0.1
 */
public class DelegateBeanMetadataFieldCodec implements FieldCodec<Object> {
    private final Class<?> targetEntityClass;

    public DelegateBeanMetadataFieldCodec(Class<?> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final ByteBuf slice = length < 0 ? input : input.readSlice(length);
        return context.entityDecoder().decode(context.version(), this.targetEntityClass, slice);
    }

    @Override
    public Object deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final int indexBeforeRead = input.readerIndex();
        final ByteBuf slice = length < 0 ? input : input.readSlice(length);
        if (XtreamTypes.isBasicType(propertyMetadata.rawClass())) {
            try (final CodecTracker.CoordinateScope ignored = length >= 0 ? codecTracker.openCoordinateBase(indexBeforeRead) : null) {
                return context.entityDecoder().decodeWithTracker(context.version(), this.targetEntityClass, slice, codecTracker);
            }
        } else {
            try (final CodecTracker.TraceScope scope = codecTracker.enterNestedField(propertyMetadata, this.targetEntityClass, this.getClass(), indexBeforeRead)) {
                final Object instance;
                try (final CodecTracker.CoordinateScope ignored = length >= 0 ? codecTracker.openCoordinateBase(indexBeforeRead) : null) {
                    instance = context.entityDecoder().decodeWithTracker(context.version(), this.targetEntityClass, slice, codecTracker);
                }
                scope.complete(instance, input, input.readerIndex());
                return instance;
            }
        }
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object instance) {
        context.entityEncoder().encode(context.version(), instance, output);
    }

    @Override
    public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object instance) {
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        if (XtreamTypes.isBasicType(propertyMetadata.rawClass())) {
            context.entityEncoder().encodeWithTracker(context.version(), instance, output, codecTracker);
        } else {
            final int indexBeforeWrite = output.writerIndex();
            try (final CodecTracker.TraceScope scope = codecTracker.enterNestedField(propertyMetadata, this.targetEntityClass, this.getClass(), indexBeforeWrite)) {
                context.entityEncoder().encodeWithTracker(context.version(), instance, output, codecTracker);
                scope.complete(instance, output, output.writerIndex());
            }
        }
    }

}

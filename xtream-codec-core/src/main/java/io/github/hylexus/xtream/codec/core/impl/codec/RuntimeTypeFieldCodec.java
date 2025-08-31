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
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.core.EntityDecoder;
import io.github.hylexus.xtream.codec.core.EntityEncoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.RuntimeTypeSupplier;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.netty.buffer.ByteBuf;
import org.springframework.util.ClassUtils;

import java.util.Optional;

/**
 * 该实现类针对于 具体类型只有在运行时才能确定的场景
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.FiledDataType#dynamic
 * @see io.github.hylexus.xtream.codec.core.type.Preset.RustStyle.dyn
 * @see io.github.hylexus.xtream.codec.core.type.Preset.JtStyle.RuntimeType
 */
public class RuntimeTypeFieldCodec extends AbstractFieldCodec<Object> {
    public static final RuntimeTypeFieldCodec INSTANCE = new RuntimeTypeFieldCodec();

    private RuntimeTypeFieldCodec() {
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        if (!(context.containerInstance() instanceof RuntimeTypeSupplier supplier)) {
            throw new UnsupportedOperationException();
        }
        final XtreamField xtreamField = propertyMetadata.findAnnotation(XtreamField.class)
                .orElseThrow();
        final Class<?> targetClass = supplier.getRuntimeType(propertyMetadata.name());
        final EntityDecoder entityDecoder = context.entityDecoder();
        final int sizeInBytes = xtreamField.length() > 0 ? xtreamField.length() : XtreamTypes.getDefaultSizeInBytes(targetClass).orElse(-1);
        final Optional<FieldCodec<?>> fieldCodec = entityDecoder.getFieldCodecRegistry().getFieldCodec(targetClass, sizeInBytes, xtreamField.charset(), xtreamField.littleEndian());
        if (fieldCodec.isPresent()) {
            @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec.get();
            return codec.deserialize(propertyMetadata, context, input, length);
        } else {
            final ByteBuf slice = length < 0
                    ? input // all remaining
                    : input.readSlice(length);
            final BeanMetadata beanMetadata = entityDecoder.getBeanMetadataRegistry().getBeanMetadata(targetClass, context.version());
            return entityDecoder.decode(beanMetadata, slice);
        }
    }

    @Override
    public Object deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        if (!(context.containerInstance() instanceof RuntimeTypeSupplier supplier)) {
            throw new UnsupportedOperationException();
        }
        final XtreamField xtreamField = propertyMetadata.findAnnotation(XtreamField.class)
                .orElseThrow();
        final Class<?> targetClass = supplier.getRuntimeType(propertyMetadata.name());
        final EntityDecoder entityDecoder = context.entityDecoder();
        final int sizeInBytes = xtreamField.length() > 0 ? xtreamField.length() : XtreamTypes.getDefaultSizeInBytes(targetClass).orElse(-1);
        final Optional<FieldCodec<?>> fieldCodec = entityDecoder.getFieldCodecRegistry().getFieldCodec(targetClass, sizeInBytes, xtreamField.charset(), xtreamField.littleEndian());

        if (fieldCodec.isPresent()) {
            @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec.get();
            return codec.deserializeWithTracker(propertyMetadata, context, input, length);
        } else {
            final ByteBuf slice = length < 0
                    ? input // all remaining
                    : input.readSlice(length);
            final BeanMetadata beanMetadata = entityDecoder.getBeanMetadataRegistry().getBeanMetadata(targetClass, context.version());
            final int parentIndexBeforeRead = slice.readerIndex();
            final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(propertyMetadata, this, targetClass.getTypeName());

            final Object value = entityDecoder.decodeWithTracker(beanMetadata, slice, context.codecTracker());

            nestedFieldSpan.setHexString(FormatUtils.toHexString(slice, parentIndexBeforeRead, slice.readerIndex() - parentIndexBeforeRead));
            context.codecTracker().finishCurrentSpan();

            return value;
        }
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
        final XtreamField xtreamField = propertyMetadata.findAnnotation(XtreamField.class)
                .orElseThrow();
        final Class<?> targetClass = getTargetClass(value);

        final EntityEncoder entityEncoder = context.entityEncoder();
        final int sizeInBytes = xtreamField.length() > 0 ? xtreamField.length() : XtreamTypes.getDefaultSizeInBytes(targetClass).orElse(-1);
        final Optional<FieldCodec<?>> fieldCodec = entityEncoder.getFieldCodecRegistry().getFieldCodec(targetClass, sizeInBytes, xtreamField.charset(), xtreamField.littleEndian());

        if (fieldCodec.isPresent()) {
            @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec.get();
            codec.serialize(propertyMetadata, context, output, value);
        } else {
            final BeanMetadata beanMetadata = entityEncoder.getBeanMetadataRegistry().getBeanMetadata(targetClass, context.version());
            entityEncoder.encode(context.version(), beanMetadata, value, output);
        }
    }

    @Override
    public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
        if (value == null) {
            return;
        }

        final XtreamField xtreamField = propertyMetadata.findAnnotation(XtreamField.class)
                .orElseThrow();
        final Class<?> targetClass = getTargetClass(value);

        final EntityEncoder entityEncoder = context.entityEncoder();
        final int sizeInBytes = xtreamField.length() > 0 ? xtreamField.length() : XtreamTypes.getDefaultSizeInBytes(targetClass).orElse(-1);
        final Optional<FieldCodec<?>> fieldCodec = entityEncoder.getFieldCodecRegistry().getFieldCodec(targetClass, sizeInBytes, xtreamField.charset(), xtreamField.littleEndian());
        final int parentIndexBeforeWrite = output.writerIndex();

        if (fieldCodec.isPresent()) {
            @SuppressWarnings("unchecked") final FieldCodec<Object> codec = (FieldCodec<Object>) fieldCodec.get();
            codec.serializeWithTracker(propertyMetadata, context, output, value);
        } else {
            final NestedFieldSpan nestedFieldSpan = context.codecTracker().startNewNestedFieldSpan(propertyMetadata, this, targetClass.getTypeName());

            final BeanMetadata beanMetadata = entityEncoder.getBeanMetadataRegistry().getBeanMetadata(targetClass, context.version());
            entityEncoder.encodeWithTracker(beanMetadata, value, output, context.codecTracker());

            nestedFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
            context.codecTracker().finishCurrentSpan();
        }
    }

    protected Class<?> getTargetClass(Object value) {
        final Class<?> cls = value.getClass();
        if (ClassUtils.isLambdaClass(cls)) {
            return cls.getInterfaces()[0];
        }
        return cls;
    }

}

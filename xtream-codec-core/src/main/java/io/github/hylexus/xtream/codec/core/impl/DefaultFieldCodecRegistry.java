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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistryAware;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.core.impl.codec.wrapper.*;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.ByteBufContainer;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultFieldCodecRegistry implements FieldCodecRegistry {

    private final Map<String, FieldCodec<?>> mapping = new LinkedHashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends FieldCodec>, FieldCodec<?>> instanceMapping = new LinkedHashMap<>();

    public DefaultFieldCodecRegistry() {
        this(true);
    }

    public DefaultFieldCodecRegistry(boolean registerDefault) {
        if (registerDefault) {
            init(this);
        }
    }

    static void init(DefaultFieldCodecRegistry registry) {
        registry.register(I8FieldCodec.INSTANCE, byte.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodec.INSTANCE, Byte.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(U8FieldCodec.INSTANCE, short.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodec.INSTANCE, Short.class, XtreamDataType.u8.sizeInBytes(), "", false);

        registry.register(I16FieldCodec.INSTANCE, short.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodec.INSTANCE, Short.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(U16FieldCodec.INSTANCE, int.class, XtreamDataType.u16.sizeInBytes(), "", false);
        registry.register(U16FieldCodec.INSTANCE, Integer.class, XtreamDataType.u16.sizeInBytes(), "", false);

        registry.register(I16FieldCodecLittleEndian.INSTANCE, short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecLittleEndian.INSTANCE, Short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecLittleEndian.INSTANCE, int.class, XtreamDataType.u16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecLittleEndian.INSTANCE, Integer.class, XtreamDataType.u16_le.sizeInBytes(), "", true);

        registry.register(I32FieldCodec.INSTANCE, int.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(I32FieldCodec.INSTANCE, Integer.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(U32FieldCodec.INSTANCE, long.class, XtreamDataType.u32.sizeInBytes(), "", false);
        registry.register(U32FieldCodec.INSTANCE, Long.class, XtreamDataType.u32.sizeInBytes(), "", false);

        registry.register(I32FieldCodecLittleEndian.INSTANCE, int.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(I32FieldCodecLittleEndian.INSTANCE, Integer.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(U32FieldCodecLittleEndian.INSTANCE, long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);
        registry.register(U32FieldCodecLittleEndian.INSTANCE, Long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);

        registry.register(I64FieldCodec.INSTANCE, long.class, XtreamDataType.i64.sizeInBytes(), "", false);
        registry.register(I64FieldCodec.INSTANCE, Long.class, XtreamDataType.i64.sizeInBytes(), "", false);

        registry.register(I64FieldCodecLittleEndian.INSTANCE, long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);
        registry.register(I64FieldCodecLittleEndian.INSTANCE, Long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);

        registry.register(F32FieldCodec.INSTANCE, float.class, XtreamDataType.f32.sizeInBytes(), "", false);
        registry.register(F32FieldCodec.INSTANCE, Float.class, XtreamDataType.f32.sizeInBytes(), "", false);

        registry.register(F32FieldCodecLittleEndian.INSTANCE, float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);
        registry.register(F32FieldCodecLittleEndian.INSTANCE, Float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);

        registry.register(F64FieldCodec.INSTANCE, double.class, XtreamDataType.f64.sizeInBytes(), "", false);
        registry.register(F64FieldCodec.INSTANCE, Double.class, XtreamDataType.f64.sizeInBytes(), "", false);

        registry.register(F64FieldCodecLittleEndian.INSTANCE, double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);
        registry.register(F64FieldCodecLittleEndian.INSTANCE, Double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);

        registry.register(ByteBufFieldCodec.INSTANCE, ByteBuf.class, -1, "", false);
        registry.register(ByteArrayFieldCodec.INSTANCE, byte[].class, -1, "", false);
        registry.register(ByteBoxArrayFieldCodec.INSTANCE, Byte[].class, -1, "", false);

        registry.register(ByteArrayContainerFieldCodec.INSTANCE, ByteArrayContainer.class, -1, "", false);
        registry.register(ByteBufContainerFieldCodec.INSTANCE, ByteBufContainer.class, -1, "", false);

        registry.register(DataWrapperFieldCodec.INSTANCE, DataWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodec.INSTANCE, BytesDataWrapper.class, -1, "", false);

        registry.register(I8WrapperFieldCodec.INSTANCE, I8Wrapper.class, -1, "", false);
        registry.register(U8WrapperFieldCodec.INSTANCE, U8Wrapper.class, -1, "", false);
        registry.register(I16WrapperFieldCodec.INSTANCE, I16Wrapper.class, -1, "", false);
        registry.register(U16WrapperFieldCodec.INSTANCE, U16Wrapper.class, -1, "", false);
        registry.register(I32WrapperFieldCodec.INSTANCE, I32Wrapper.class, -1, "", false);
        registry.register(U32WrapperFieldCodec.INSTANCE, U32Wrapper.class, -1, "", false);
        registry.register(DwordWrapperFieldCodec.INSTANCE, DwordWrapper.class, -1, "", false);
        registry.register(WordWrapperFieldCodec.INSTANCE, WordWrapper.class, -1, "", false);
        registry.register(StringWrapperUtf8FieldCodec.INSTANCE, StringWrapperUtf8.class, -1, "", false);
        registry.register(StringWrapperGbkFieldCodec.INSTANCE, StringWrapperGbk.class, -1, "", false);
        registry.register(StringWrapperBcdFieldCodec.INSTANCE, StringWrapperBcd.class, -1, "", false);

        registerDefaultStringCodec(registry);
    }

    private static void registerDefaultStringCodec(DefaultFieldCodecRegistry registry) {
        Arrays.asList(
                XtreamConstants.CHARSET_GBK,
                XtreamConstants.CHARSET_GB_2312,
                StandardCharsets.UTF_8,
                StandardCharsets.US_ASCII,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16
        ).forEach(charset -> {
            // ...
            registry.register(new StringFieldCodec(charset.name()), String.class, XtreamDataType.string.sizeInBytes(), charset.name(), false);
        });

        registry.register(StringFieldCodec.INSTANCE_BCD_8421, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_BCD_8421, false);
        registry.register(StringFieldCodec.INSTANCE_HEX, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_HEX, false);
    }

    @Override
    public void register(FieldCodec<?> fieldCodec, Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        if (fieldCodec instanceof FieldCodecRegistryAware registryAware) {
            registryAware.setFieldCodecRegistry(this);
        }
        String key = this.generateKey(targetType, sizeInBytes, charset, littleEndian);
        mapping.put(key, fieldCodec);
        instanceMapping.put(fieldCodec.getClass(), fieldCodec);
    }

    protected String generateKey(Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        if (String.class == targetType) {
            return key(targetType, -1, charset, false);
        } else if (byte[].class == targetType || Byte[].class == targetType) {
            return key(targetType, -1, "", false);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return key(targetType, sizeInBytes, "", false);
        } else if (ByteBuf.class.isAssignableFrom(targetType)) {
            return "ByteBuf <--> byte[n]";
        } else {
            return key(targetType, sizeInBytes, "", littleEndian);
        }
    }

    @Override
    public <T extends FieldCodec<?>> Optional<T> getFieldCodecInstanceByType(Class<T> targetType) {
        @SuppressWarnings("unchecked") final T fieldCodec = (T) this.instanceMapping.get(targetType);
        return Optional.ofNullable(fieldCodec);
    }

    @Override
    public Optional<FieldCodec<?>> getFieldCodec(Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        final String key = generateKey(targetType, sizeInBytes, charset, littleEndian);
        return Optional.ofNullable(mapping.get(key));
    }

    @Override
    public Optional<FieldCodec<?>> getFieldCodec(BeanPropertyMetadata metadata) {

        final XtreamField xtreamField = metadata.xtreamFieldAnnotation();

        if (xtreamField.fieldCodec() != FieldCodec.Placeholder.class) {
            @SuppressWarnings({"rawtypes"}) final Class<? extends FieldCodec> aClass = xtreamField.fieldCodec();
            final FieldCodec<?> newInstance = BeanUtils.createNewInstance(aClass);
            if (newInstance instanceof BeanMetadataRegistryAware registryAware) {
                registryAware.setBeanMetadataRegistry(metadata.version(), metadata.beanMetadataRegistry());
            }
            return Optional.of(newInstance);
        }

        final Class<?> rawClassType = metadata.rawClass();
        final int length = this.getDefaultSizeInBytes(xtreamField, rawClassType);

        return this.getFieldCodec(rawClassType, length, xtreamField.charset(), xtreamField.littleEndian());
    }

    int getDefaultSizeInBytes(XtreamField xtreamField, Class<?> rawClassType) {
        if (xtreamField.length() > 0) {
            return xtreamField.length();
        }

        return XtreamTypes.getDefaultSizeInBytes(rawClassType).orElseGet(xtreamField::length);
    }

    String key(Class<?> targetType, int length, String charset, boolean littleEndian) {
        final String numberTips = (targetType != byte.class && targetType != Byte.class && XtreamTypes.isNumberType(targetType))
                ? (littleEndian ? "(LE)" : "(BE)")
                : "";
        if (XtreamUtils.hasElement(charset)) {
            return targetType.getName() + numberTips + " <--> byte[" + (length <= 0 ? "n" : length) + "] (" + (charset.toLowerCase()) + ")";
        }
        return targetType.getName() + numberTips + " <--> byte[" + (length <= 0 ? "n" : length) + "]";
    }

}

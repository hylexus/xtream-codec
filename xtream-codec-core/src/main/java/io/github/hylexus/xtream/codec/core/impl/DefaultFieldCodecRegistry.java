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

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistryAware;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.core.impl.codec.wrapper.*;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.ByteBufContainer;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

@NullMarked
public class DefaultFieldCodecRegistry implements FieldCodecRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultFieldCodecRegistry.class);
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

    @SuppressWarnings({"deprecation", "removal"})
    static void init(DefaultFieldCodecRegistry registry) {
        registry.register(I8FieldCodec.INSTANCE, byte.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodec.INSTANCE, Byte.class, XtreamDataType.i8.sizeInBytes(), "", false);

        registry.register(I8FieldCodecs.BYTE_INSTANCE, byte.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.BYTE_INSTANCE, Byte.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.SHORT_INSTANCE, short.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.SHORT_INSTANCE, Short.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.INTEGER_INSTANCE, int.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.INTEGER_INSTANCE, Integer.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.i8.sizeInBytes(), "", false);
        registry.register(I8FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.i8.sizeInBytes(), "", false);

        registry.register(U8FieldCodec.INSTANCE, short.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodec.INSTANCE, Short.class, XtreamDataType.u8.sizeInBytes(), "", false);

        registry.register(U8FieldCodecs.SHORT_INSTANCE, short.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodecs.SHORT_INSTANCE, Short.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodecs.INTEGER_INSTANCE, int.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodecs.INTEGER_INSTANCE, Integer.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.u8.sizeInBytes(), "", false);
        registry.register(U8FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.u8.sizeInBytes(), "", false);

        registry.register(I16FieldCodec.INSTANCE, short.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodec.INSTANCE, Short.class, XtreamDataType.i16.sizeInBytes(), "", false);

        registry.register(I16FieldCodecs.SHORT_INSTANCE, short.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodecs.SHORT_INSTANCE, Short.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodecs.INTEGER_INSTANCE, int.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodecs.INTEGER_INSTANCE, Integer.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.i16.sizeInBytes(), "", false);
        registry.register(I16FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.i16.sizeInBytes(), "", false);

        registry.register(U16FieldCodec.INSTANCE, int.class, XtreamDataType.u16.sizeInBytes(), "", false);
        registry.register(U16FieldCodec.INSTANCE, Integer.class, XtreamDataType.u16.sizeInBytes(), "", false);

        registry.register(U16FieldCodecs.INTEGER_INSTANCE, int.class, XtreamDataType.u16.sizeInBytes(), "", false);
        registry.register(U16FieldCodecs.INTEGER_INSTANCE, Integer.class, XtreamDataType.u16.sizeInBytes(), "", false);
        registry.register(U16FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.u16.sizeInBytes(), "", false);
        registry.register(U16FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.u16.sizeInBytes(), "", false);

        registry.register(I16FieldCodecLittleEndian.INSTANCE, short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecLittleEndian.INSTANCE, Short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);

        registry.register(I16FieldCodecs.SHORT_INSTANCE_LE, short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecs.SHORT_INSTANCE_LE, Short.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecs.INTEGER_INSTANCE_LE, int.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecs.INTEGER_INSTANCE_LE, Integer.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecs.LONG_INSTANCE_LE, long.class, XtreamDataType.i16_le.sizeInBytes(), "", true);
        registry.register(I16FieldCodecs.LONG_INSTANCE_LE, Long.class, XtreamDataType.i16_le.sizeInBytes(), "", true);

        registry.register(U16FieldCodecLittleEndian.INSTANCE, int.class, XtreamDataType.u16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecLittleEndian.INSTANCE, Integer.class, XtreamDataType.u16_le.sizeInBytes(), "", true);

        registry.register(U16FieldCodecs.INTEGER_INSTANCE_LE, int.class, XtreamDataType.u16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecs.INTEGER_INSTANCE_LE, Integer.class, XtreamDataType.u16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecs.LONG_INSTANCE_LE, long.class, XtreamDataType.u16_le.sizeInBytes(), "", true);
        registry.register(U16FieldCodecs.LONG_INSTANCE_LE, Long.class, XtreamDataType.u16_le.sizeInBytes(), "", true);

        registry.register(I32FieldCodec.INSTANCE, int.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(I32FieldCodec.INSTANCE, Integer.class, XtreamDataType.i32.sizeInBytes(), "", false);

        registry.register(I32FieldCodecs.INTEGER_INSTANCE, int.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(I32FieldCodecs.INTEGER_INSTANCE, Integer.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(I32FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.i32.sizeInBytes(), "", false);
        registry.register(I32FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.i32.sizeInBytes(), "", false);

        registry.register(U32FieldCodec.INSTANCE, long.class, XtreamDataType.u32.sizeInBytes(), "", false);
        registry.register(U32FieldCodec.INSTANCE, Long.class, XtreamDataType.u32.sizeInBytes(), "", false);

        registry.register(U32FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.u32.sizeInBytes(), "", false);
        registry.register(U32FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.u32.sizeInBytes(), "", false);

        registry.register(I32FieldCodecLittleEndian.INSTANCE, int.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(I32FieldCodecLittleEndian.INSTANCE, Integer.class, XtreamDataType.i32_le.sizeInBytes(), "", true);

        registry.register(I32FieldCodecs.INTEGER_INSTANCE_LE, int.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(I32FieldCodecs.INTEGER_INSTANCE_LE, Integer.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(I32FieldCodecs.LONG_INSTANCE_LE, long.class, XtreamDataType.i32_le.sizeInBytes(), "", true);
        registry.register(I32FieldCodecs.LONG_INSTANCE_LE, Long.class, XtreamDataType.i32_le.sizeInBytes(), "", true);

        registry.register(U32FieldCodecLittleEndian.INSTANCE, long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);
        registry.register(U32FieldCodecLittleEndian.INSTANCE, Long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);

        registry.register(U32FieldCodecs.LONG_INSTANCE_LE, long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);
        registry.register(U32FieldCodecs.LONG_INSTANCE_LE, Long.class, XtreamDataType.u32_le.sizeInBytes(), "", true);

        registry.register(I64FieldCodec.INSTANCE, long.class, XtreamDataType.i64.sizeInBytes(), "", false);
        registry.register(I64FieldCodec.INSTANCE, Long.class, XtreamDataType.i64.sizeInBytes(), "", false);

        registry.register(I64FieldCodecs.LONG_INSTANCE, long.class, XtreamDataType.i64.sizeInBytes(), "", false);
        registry.register(I64FieldCodecs.LONG_INSTANCE, Long.class, XtreamDataType.i64.sizeInBytes(), "", false);

        registry.register(I64FieldCodecLittleEndian.INSTANCE, long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);
        registry.register(I64FieldCodecLittleEndian.INSTANCE, Long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);

        registry.register(I64FieldCodecs.LONG_INSTANCE_LE, long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);
        registry.register(I64FieldCodecs.LONG_INSTANCE_LE, Long.class, XtreamDataType.i64_le.sizeInBytes(), "", true);

        registry.register(F32FieldCodec.INSTANCE, float.class, XtreamDataType.f32.sizeInBytes(), "", false);
        registry.register(F32FieldCodec.INSTANCE, Float.class, XtreamDataType.f32.sizeInBytes(), "", false);

        registry.register(F32FieldCodecs.FLOAT_INSTANCE, float.class, XtreamDataType.f32.sizeInBytes(), "", false);
        registry.register(F32FieldCodecs.FLOAT_INSTANCE, Float.class, XtreamDataType.f32.sizeInBytes(), "", false);

        registry.register(F32FieldCodecLittleEndian.INSTANCE, float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);
        registry.register(F32FieldCodecLittleEndian.INSTANCE, Float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);

        registry.register(F32FieldCodecs.FLOAT_INSTANCE_LE, float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);
        registry.register(F32FieldCodecs.FLOAT_INSTANCE_LE, Float.class, XtreamDataType.f32_le.sizeInBytes(), "", true);

        registry.register(F64FieldCodec.INSTANCE, double.class, XtreamDataType.f64.sizeInBytes(), "", false);
        registry.register(F64FieldCodec.INSTANCE, Double.class, XtreamDataType.f64.sizeInBytes(), "", false);

        registry.register(F64FieldCodecs.DOUBLE_INSTANCE, double.class, XtreamDataType.f64.sizeInBytes(), "", false);
        registry.register(F64FieldCodecs.DOUBLE_INSTANCE, Double.class, XtreamDataType.f64.sizeInBytes(), "", false);

        registry.register(F64FieldCodecLittleEndian.INSTANCE, double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);
        registry.register(F64FieldCodecLittleEndian.INSTANCE, Double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);

        registry.register(F64FieldCodecs.DOUBLE_INSTANCE_LE, double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);
        registry.register(F64FieldCodecs.DOUBLE_INSTANCE_LE, Double.class, XtreamDataType.f64_le.sizeInBytes(), "", true);

        registry.register(ByteBufFieldCodec.INSTANCE, ByteBuf.class, -1, "", false);
        registry.register(BytesFieldCodecs.INSTANCE_BYTE_BUF, ByteBuf.class, -1, "", false);
        registry.register(ByteArrayFieldCodec.INSTANCE, byte[].class, -1, "", false);
        registry.register(BytesFieldCodecs.INSTANCE_BYTE_ARRAY, byte[].class, -1, "", false);
        registry.register(ByteBoxArrayFieldCodec.INSTANCE, Byte[].class, -1, "", false);
        registry.register(BytesFieldCodecs.INSTANCE_BYTE_ARRAY_BOXED, Byte[].class, -1, "", false);

        registry.register(ByteArrayContainerFieldCodec.INSTANCE, ByteArrayContainer.class, -1, "", false);
        registry.register(BytesFieldCodecs.INSTANCE_BYTE_ARRAY_CONTAINER, ByteArrayContainer.class, -1, "", false);
        registry.register(ByteBufContainerFieldCodec.INSTANCE, ByteBufContainer.class, -1, "", false);
        registry.register(BytesFieldCodecs.INSTANCE_BYTE_BUF_CONTAINER, ByteBufContainer.class, -1, "", false);

        registry.register(DataWrapperFieldCodec.INSTANCE, DataWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodec.INSTANCE, BytesDataWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE, DataWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE, BytesDataWrapper.class, -1, "", false);

        registry.register(I8WrapperFieldCodec.INSTANCE, I8Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_I8, I8Wrapper.class, -1, "", false);
        registry.register(U8WrapperFieldCodec.INSTANCE, U8Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_U8, U8Wrapper.class, -1, "", false);
        registry.register(I16WrapperFieldCodec.INSTANCE, I16Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_I16, I16Wrapper.class, -1, "", false);
        registry.register(U16WrapperFieldCodec.INSTANCE, U16Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_U16, U16Wrapper.class, -1, "", false);
        registry.register(I32WrapperFieldCodec.INSTANCE, I32Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_I32, I32Wrapper.class, -1, "", false);
        registry.register(U32WrapperFieldCodec.INSTANCE, U32Wrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_U32, U32Wrapper.class, -1, "", false);
        registry.register(DwordWrapperFieldCodec.INSTANCE, DwordWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_DWORD, DwordWrapper.class, -1, "", false);
        registry.register(WordWrapperFieldCodec.INSTANCE, WordWrapper.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_WORD, WordWrapper.class, -1, "", false);
        registry.register(StringWrapperUtf8FieldCodec.INSTANCE, StringWrapperUtf8.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_STRING_UTF_8, StringWrapperUtf8.class, -1, "", false);
        registry.register(StringWrapperGbkFieldCodec.INSTANCE, StringWrapperGbk.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_STRING_GBK, StringWrapperGbk.class, -1, "", false);
        registry.register(StringWrapperBcdFieldCodec.INSTANCE, StringWrapperBcd.class, -1, "", false);
        registry.register(DataWrapperFieldCodes.INSTANCE_STRING_BCD_8421, StringWrapperBcd.class, -1, "", false);

        registerDefaultStringCodec(registry);

        registry.register(StringFieldCodecs.INSTANCE_UTF8, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_UTF8, false);
        registry.register(StringFieldCodecs.INSTANCE_GBK, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_GBK, false);
        registry.register(StringFieldCodecs.INSTANCE_GB_2312, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_GB_2312, false);
        registry.register(StringFieldCodecs.INSTANCE_BCD_8421, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_BCD_8421, false);
        registry.register(StringFieldCodecs.INSTANCE_HEX, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_HEX, false);

    }

    @SuppressWarnings("removal")
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

        registry.register(StringFieldCodecs.INSTANCE_BCD_8421, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_BCD_8421, false);
        registry.register(StringFieldCodecs.INSTANCE_HEX, String.class, XtreamDataType.string.sizeInBytes(), XtreamConstants.CHARSET_NAME_HEX, false);
    }

    @Override
    public void register(FieldCodec<?> fieldCodec, Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        if (fieldCodec instanceof FieldCodecRegistryAware registryAware) {
            registryAware.setFieldCodecRegistry(this);
        }
        final NumberSignedness signedness = fieldCodec.signedness();
        final String key = this.generateKey(signedness, targetType, sizeInBytes, charset, littleEndian);
        this.mapping.put(key, fieldCodec);
        this.instanceMapping.put(fieldCodec.getClass(), fieldCodec);
    }

    protected String generateKey(NumberSignedness signedness, Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        if (String.class == targetType) {
            return key(NumberSignedness.NONE, targetType, -1, charset, false);
        } else if (byte[].class == targetType || Byte[].class == targetType) {
            return key(NumberSignedness.NONE, targetType, -1, "", false);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return key(signedness, targetType, sizeInBytes, "", false);
        } else if (ByteBuf.class.isAssignableFrom(targetType)) {
            return "byte[n] <--> ByteBuf";
        } else {
            if (XtreamTypes.isNumberType(targetType)) {
                return key(signedness, targetType, sizeInBytes, "", littleEndian);
            }
            return key(NumberSignedness.NONE, targetType, sizeInBytes, "", littleEndian);
        }
    }

    @Override
    public Optional<FieldCodec<?>> getFieldCodec(BeanPropertyMetadata metadata) {

        final XtreamField xtreamField = metadata.xtreamFieldAnnotation();
        if (xtreamField.codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
            return Optional.of(FieldCodec.TransientRecordComponentFieldCodec.INSTANCE);
        }
        if (metadata.isRecordClass()) {
            final BeanMetadata beanMetadata = metadata.beanMetadataRegistry().getBeanMetadata(metadata.rawClass(), metadata.version());
            return Optional.of(new DelegateBeanMetadataFieldCodec(beanMetadata));
        }
        if (xtreamField.fieldCodec() != FieldCodec.Placeholder.class) {
            final Class<? extends FieldCodec<?>> aClass = xtreamField.fieldCodec();
            final FieldCodec<?> newInstance = this.getOrCreateFieldCodec(metadata.version(), metadata.beanMetadataRegistry(), null, aClass, xtreamField.charset(), null);
            return Optional.ofNullable(newInstance);
        }

        final Class<?> rawClassType = metadata.rawClass();
        final int length = this.getDefaultSizeInBytes(xtreamField, rawClassType);

        return this.getFieldCodec(length, xtreamField.signedness(), xtreamField.charset(), xtreamField.littleEndian(), rawClassType);
    }

    @Override
    public Optional<FieldCodec<?>> getFieldCodec(int sizeInBytes, NumberSignedness signedness, String charset, boolean littleEndian, Class<?> targetType) {
        final String key = generateKey(signedness, targetType, sizeInBytes, charset, littleEndian);
        final FieldCodec<?> value = mapping.get(key);
        // log.info("getFieldCodec: targetType={}, key={}, value={}", targetType, key, value);
        return Optional.ofNullable(value);
    }

    @Nullable
    @Override
    public FieldCodec<?> getOrCreateFieldCodec(
            int version,
            @Nullable BeanMetadataRegistry beanMetadataRegistry,
            @Nullable XtreamDataType targetType,
            @Nullable Class<? extends FieldCodec<?>> codecClass,
            @Nullable String charset,
            @Nullable Class<?> targetEntityClass) {

        if (targetType != null && !targetType.isPlaceholder()) {
            if (targetType == XtreamDataType.string) {
                Objects.requireNonNull(charset, "StringFieldCodec.charset is null");
                return StringFieldCodecs.createStringCodecAndCastToObject(charset);
            }
            return Objects.requireNonNull(targetType.codec());
        }

        if (codecClass != null && !Objects.equals(FieldCodec.Placeholder.class, codecClass)) {
            final FieldCodec<?> instance = this.instanceMapping.get(codecClass);
            if (instance != null) {
                return instance;
            }

            if (CharSequenceFieldCodec.class.isAssignableFrom(codecClass)) {
                Objects.requireNonNull(charset, "StringFieldCodec.charset is null");
                return StringFieldCodecs.createStringCodecAndCastToObject(charset);
            }

            Objects.requireNonNull(beanMetadataRegistry);
            final FieldCodec<?> newInstance = BeanUtils.createFieldCodecInstance(codecClass, beanMetadataRegistry, version);
            if (newInstance instanceof BeanMetadataRegistryAware registryAware) {
                registryAware.setBeanMetadataRegistry(version, beanMetadataRegistry);
            }
            if (newInstance instanceof FieldCodecRegistryAware aware) {
                aware.setFieldCodecRegistry(this);
            }
            return newInstance;
        }

        if (targetEntityClass != null && !(Objects.equals(Object.class, targetEntityClass))) {
            Objects.requireNonNull(beanMetadataRegistry);
            final BeanMetadata beanMetadata = beanMetadataRegistry.getBeanMetadata(targetEntityClass);
            return new DelegateBeanMetadataFieldCodec(beanMetadata);
        }

        return null;
    }

    int getDefaultSizeInBytes(XtreamField xtreamField, Class<?> rawClassType) {
        if (xtreamField.length() > 0) {
            return xtreamField.length();
        }

        return XtreamTypes.getDefaultSizeInBytes(rawClassType).orElseGet(xtreamField::length);
    }

    String key(NumberSignedness signedness, Class<?> targetType, int length, String charset, boolean littleEndian) {
        final String numberTips = (targetType != byte.class && targetType != Byte.class && XtreamTypes.isNumberType(targetType))
                ? (littleEndian ? "(LE)" : "(BE)")
                : "";
        final String signednessTips = signedness == NumberSignedness.NONE
                ? ""
                : "[" + signedness.value() + "]";
        if (XtreamUtils.hasElement(charset)) {
            return "byte[" + (length <= 0 ? "n" : length) + "] (" + (charset.toLowerCase()) + ") <--> " + targetType.getName() + numberTips + signednessTips;
        }
        return "byte[" + (length <= 0 ? "n" : length) + "] <--> " + targetType.getName() + numberTips + signednessTips;
    }

}

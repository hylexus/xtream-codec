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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.BeanIntrospectionException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ContainerInstanceFactory;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamFieldMapDescriptor;
import io.github.hylexus.xtream.codec.core.impl.codec.DelegateBeanMetadataFieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.BaseSpan;
import io.github.hylexus.xtream.codec.core.tracker.MapEntryItemSpan;
import io.github.hylexus.xtream.codec.core.tracker.MapEntrySpan;
import io.github.hylexus.xtream.codec.core.tracker.MapFieldSpan;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author hylexus
 * @see XtreamFieldMapDescriptor
 * @see DelegateBeanMetadataFieldCodec
 * @since 0.0.1
 */
public class MapBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    private static final Logger logger = LoggerFactory.getLogger(MapBeanPropertyMetadata.class);

    final BeanPropertyMetadata delegate;
    final XtreamFieldMapDescriptor xtreamFieldMapDescriptor;
    final FieldCodecRegistry fieldCodecRegistry;

    private final FieldCodec<Object> keyFieldCodec;
    private final FieldCodec<Object> defaultValueLengthSizeFieldCodec;
    private final Map<Object, FieldCodec<Object>> valueLengthSizeFieldDecoder;
    private final Map<Object, FieldCodec<Object>> valueLengthSizeFieldEncoder;
    private final ContainerInstanceFactory containerInstanceFactory;
    private final Map<Object, FieldCodec<Object>> valueEncoders;
    private final Map<Object, FieldCodec<Object>> valueDecoders;

    public MapBeanPropertyMetadata(BeanPropertyMetadata delegate, FieldCodecRegistry fieldCodecRegistry, BeanMetadataRegistry beanMetadataRegistry) {
        super(beanMetadataRegistry, delegate.name(), delegate.rawClass(), delegate.version(), delegate.xtreamFieldAnnotation(), delegate.field(), delegate.propertyGetter(), delegate.propertySetter());
        this.delegate = delegate;
        this.fieldCodecRegistry = fieldCodecRegistry;
        this.xtreamFieldMapDescriptor = this.findAnnotation(XtreamFieldMapDescriptor.class)
                .orElseThrow(() -> new BeanIntrospectionException("Map filed : [" + this.field() + "] should be marked by @" + XtreamFieldMapDescriptor.class.getSimpleName()));

        final ParsedMapCodecMetadata parsedMapItemMetadata = this.parseMapItemMetadata(xtreamFieldMapDescriptor);
        this.keyFieldCodec = this.detectKeyFieldCodec(xtreamFieldMapDescriptor, parsedMapItemMetadata);
        this.valueLengthSizeFieldDecoder = this.initValueLengthSizeFieldCodec(parsedMapItemMetadata.lengthFiledSizeDecodeMetadata());
        this.valueLengthSizeFieldEncoder = this.initValueLengthSizeFieldCodec(parsedMapItemMetadata.lengthFiledSizeEncodeMetadata());
        this.defaultValueLengthSizeFieldCodec = this.detectDefaultValueLengthSizeFieldCodec(xtreamFieldMapDescriptor);
        this.valueEncoders = this.initValueEncoders(this.xtreamFieldMapDescriptor);
        this.valueDecoders = this.initValueDecoders(this.xtreamFieldMapDescriptor);
        this.containerInstanceFactory = super.xtreamField.containerInstanceFactory() == ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class
                ? BeanUtils.createNewInstance(ContainerInstanceFactory.LinkedHashMapContainerInstanceFactory.class, (Object[]) null)
                : BeanUtils.createNewInstance(this.xtreamField.containerInstanceFactory(), (Object[]) null);
    }

    @Override
    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        @SuppressWarnings({"unchecked"}) final Map<Object, Object> map = (Map<Object, Object>) this.containerInstanceFactory().create();
        while (slice.isReadable()) {
            // 1. key(i8,u8,i16,u16,i32,u32,i64,string)
            final Object key = this.keyFieldCodec.deserialize(this, context, slice, this.xtreamFieldMapDescriptor.keyDescriptor().length());
            if (logger.isDebugEnabled()) {
                logger.debug("MapKeyDecoder: key={}, keyFieldCodec={}", key, keyFieldCodec);
            }

            // 2. valueLength(int)
            final FieldCodec<?> valueLengthFieldCodec = this.getValueLengthFieldDecoder(key);
            final int valueLength = ((Number) valueLengthFieldCodec.deserialize(this, context, slice, -1)).intValue();
            if (logger.isDebugEnabled()) {
                logger.debug("MapValueLengthDecoder: key={}, keyFieldCodec={}, length={}", key, keyFieldCodec, valueLength);
            }
            // 3. value(dynamic)
            final ByteBuf byteBuf = slice.readSlice(valueLength);
            final FieldCodec<?> valueFieldCodec = this.getValueDecoder(key);
            if (logger.isDebugEnabled()) {
                logger.debug("MapValueDecoder: key={}, valueFieldCodec={}", key, valueFieldCodec);
            }
            final Object value = valueFieldCodec.deserialize(this, context, byteBuf, valueLength);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public Object decodePropertyValueWithTracker(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext(), input);
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        final int parentIndexBeforeRead = input.readerIndex();
        final MapFieldSpan mapFieldSpan = context.codecTracker().startNewMapFieldSpan(this, this.getClass().getSimpleName());
        @SuppressWarnings({"unchecked"}) final Map<Object, Object> map = (Map<Object, Object>) this.containerInstanceFactory().create();
        int sequence = 0;
        while (slice.isReadable()) {
            final int indexBeforeRead = input.readerIndex();
            final MapEntrySpan mapEntrySpan = context.codecTracker().startNewMapEntrySpan(mapFieldSpan, this.name(), sequence++);
            // 1. key(i8,u8,i16,u16,i32,u32,i64,string)
            context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.KEY);
            final Object key = this.keyFieldCodec.deserializeWithTracker(this, context, slice, this.xtreamFieldMapDescriptor.keyDescriptor().length());

            // 2. valueLength(int)
            final FieldCodec<?> valueLengthFieldCodec = this.getValueLengthFieldDecoder(key);
            context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.VALUE_LENGTH);
            final int valueLength = ((Number) valueLengthFieldCodec.deserializeWithTracker(this, context, slice, -1)).intValue();

            // 3. value(dynamic)
            final ByteBuf byteBuf = slice.readSlice(valueLength);
            final FieldCodec<?> valueFieldCodec = this.getValueDecoder(key);

            context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.VALUE);
            final Object value = valueFieldCodec.deserializeWithTracker(this, context, byteBuf, valueLength);
            mapEntrySpan.setHexString(FormatUtils.toHexString(input, indexBeforeRead, input.readerIndex() - indexBeforeRead));
            map.put(key, value);
            context.codecTracker().finishCurrentSpan();
        }
        mapFieldSpan.setHexString(FormatUtils.toHexString(input, parentIndexBeforeRead, input.readerIndex() - parentIndexBeforeRead));
        context.codecTracker().finishCurrentSpan();
        return map;
    }

    @Override
    public void doEncode(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        if (value == null) {
            return;
        }
        final ByteBuf temp = ByteBufAllocator.DEFAULT.buffer();
        try {
            @SuppressWarnings("unchecked") final Map<Object, Object> map = (Map<Object, Object>) value;
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                // 1. key(i8,u8,i16,u16,i32,u32,i64,string)
                final Object key = entry.getKey();
                this.keyFieldCodec.serialize(this, context, output, key);

                if (logger.isDebugEnabled()) {
                    logger.debug("MapKeyEncoder: key={}, keyFieldCodec={}, key={}", key, keyFieldCodec, key);
                }

                // 3. value(dynamic)(tmp)
                final Object data = entry.getValue();
                final FieldCodec<Object> valueFieldCodec = this.getValueEncoder(context.version(), key, data);
                if (logger.isDebugEnabled()) {
                    logger.debug("MapValueEncoder: key={}, valueFieldCodec={}, data={}", key, valueFieldCodec, data);
                }
                valueFieldCodec.serialize(this, context, temp, data);

                // 2. valueLength(int)
                final int valueLength = temp.readableBytes();
                final FieldCodec<Object> valueLengthFieldCodec = this.getValueLengthFieldEncoder(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("MapValueLengthEncoder: key={}, LengthFieldCodec={}, length={}", key, valueLengthFieldCodec, valueLength);
                }
                valueLengthFieldCodec.serialize(this, context, output, castType(valueLengthFieldCodec.underlyingJavaType(), valueLength));
                // 3. value(dynamic)
                output.writeBytes(temp);
                temp.clear();
            }
        } finally {
            temp.release();
        }
    }

    @Override
    protected void doEncodeWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        if (value == null) {
            return;
        }
        final ByteBuf temp = ByteBufAllocator.DEFAULT.buffer();
        try {
            int sequence = 0;
            @SuppressWarnings("unchecked") final Map<Object, Object> map = (Map<Object, Object>) value;
            final MapFieldSpan mapFieldSpan = context.codecTracker().startNewMapFieldSpan(this, this.getClass().getSimpleName());
            final int parenIndexBeforeWrite = output.writerIndex();
            final BaseSpan parent = context.codecTracker().getCurrentSpan();
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                final MapEntrySpan mapEntrySpan = context.codecTracker().startNewMapEntrySpan(parent, this.name(), sequence++);
                final int writerIndex = output.writerIndex();
                // 1. key(i8,u8,i16,u16,i32,u32,i64,string)
                final Object key = entry.getKey();
                context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.KEY);
                this.keyFieldCodec.serializeWithTracker(this, context, output, key);

                // 3. value(dynamic)(tmp)
                final Object data = entry.getValue();
                final FieldCodec<Object> valueFieldCodec = this.getValueEncoder(context.version(), key, data);
                context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.VALUE);
                valueFieldCodec.serializeWithTracker(this, context, temp, data);

                // 2. valueLength(int)
                final int valueLength = temp.readableBytes();
                final FieldCodec<Object> valueLengthFieldCodec = this.getValueLengthFieldEncoder(key);
                context.codecTracker().updateTrackerHints(MapEntryItemSpan.Type.VALUE_LENGTH);
                valueLengthFieldCodec.serializeWithTracker(this, context, output, castType(valueLengthFieldCodec.underlyingJavaType(), valueLength));
                // 3. value(dynamic)
                output.writeBytes(temp);
                mapEntrySpan.setHexString(FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex));
                temp.clear();
                context.codecTracker().finishCurrentSpan();
            }
            mapFieldSpan.setHexString(FormatUtils.toHexString(output, parenIndexBeforeWrite, output.writerIndex() - parenIndexBeforeWrite));
            context.codecTracker().finishCurrentSpan();
        } finally {
            temp.release();
        }
    }

    private Object castType(Class<?> cls, int valueLength) {
        if (cls == Short.class || cls == short.class) {
            return (short) valueLength;
        } else if (cls == Byte.class || cls == byte.class) {
            return (byte) valueLength;
        } else if (cls == Integer.class || cls == int.class) {
            return valueLength;
        } else if (cls == Long.class || cls == long.class) {
            return (long) valueLength;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ContainerInstanceFactory containerInstanceFactory() {
        return this.containerInstanceFactory;
    }

    Class<?> getJavaTypeForFieldLengthField(int lengthFieldSize) {
        return switch (lengthFieldSize) {
            case 1 -> short.class;
            case 2 -> int.class;
            case 4 -> long.class;
            default -> throw new IllegalArgumentException("Unsupported length: " + lengthFieldSize);
        };
    }

    private Map<Object, FieldCodec<Object>> initValueLengthSizeFieldCodec(Map<Object, ValueLengthFieldConfigMetadata> metadataMap) {
        Map<Object, FieldCodec<Object>> map = new HashMap<>();
        metadataMap.forEach((key, value) -> {
            final Class<?> javaType = this.getJavaTypeForFieldLengthField(value.length());
            final FieldCodec<Object> fieldCodec = this.fieldCodecRegistry.getFieldCodecAndCastToObject(value.length, NumberSignedness.UNSIGNED, null, value.littleEndian, javaType)
                    .orElseThrow(() -> new IllegalArgumentException("Can not determine [FieldCodec] for " + value));
            map.put(key, fieldCodec);
        });
        return map;
    }

    FieldCodec<Object> getValueEncoder(int version, Object key, Object data) {
        final FieldCodec<Object> fieldCodec = valueEncoders.get(key);
        if (fieldCodec != null) {
            return fieldCodec;
        }

        final Class<?> javaType = data.getClass();
        final XtreamField defaultConfig = this.xtreamFieldMapDescriptor.valueEncoderDescriptors().defaultValueEncoderDescriptor().config();
        if (XtreamTypes.isBasicType(javaType)) {
            return this.fieldCodecRegistry.getFieldCodecAndCastToObject(
                            XtreamTypes.getDefaultSizeInBytes(javaType).orElse(defaultConfig.length()), defaultConfig.signedness(), defaultConfig.charset(), defaultConfig.littleEndian(), javaType
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Can not determine [FieldCodec] for " + javaType));
        } else {
            return new DelegateBeanMetadataFieldCodec(this.beanMetadataRegistry.getBeanMetadata(javaType, version));
        }
    }

    FieldCodec<Object> getValueDecoder(Object key) {
        final FieldCodec<Object> fieldCodec = this.valueDecoders.get(key);
        if (fieldCodec != null) {
            return fieldCodec;
        }

        final XtreamFieldMapDescriptor.ValueDecoderDescriptor defaultConfig = this.xtreamFieldMapDescriptor.valueDecoderDescriptors().defaultValueDecoderDescriptor();
        final Class<?> javaType = defaultConfig.javaType();
        final XtreamField config = defaultConfig.config();
        final Integer length = Optional.of(config.length())
                .filter(it -> it > 0)
                .or(() -> XtreamTypes.getDefaultSizeInBytes(javaType))
                .orElse(-1);
        if (XtreamTypes.isBasicType(javaType)) {
            return this.fieldCodecRegistry.getFieldCodecAndCastToObject(
                            length, config.signedness(), config.charset(), config.littleEndian(), javaType
                            // XtreamTypes.getDefaultSizeInBytes(javaType).orElse(config.length()),
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Can not determine [FieldCodec] for " + javaType));
        } else {
            return new DelegateBeanMetadataFieldCodec(this.beanMetadataRegistry.getBeanMetadata(javaType, this.version()));
        }
    }

    private Map<Object, FieldCodec<Object>> initValueCodec(XtreamFieldMapDescriptor xtreamFieldMapDescriptor, XtreamFieldMapDescriptor.ValueCodecConfig[] valueCodecConfigs) {
        final Map<Object, FieldCodec<Object>> map = new HashMap<>();
        for (final XtreamFieldMapDescriptor.ValueCodecConfig valueEncoderConfig : valueCodecConfigs) {
            final Object mapKey = this.getMapKey(xtreamFieldMapDescriptor, valueEncoderConfig);
            final XtreamField config = valueEncoderConfig.config();
            if (config.fieldCodec() != FieldCodec.Placeholder.class) {
                @SuppressWarnings("unchecked") final Class<FieldCodec<Object>> cls = (Class<FieldCodec<Object>>) config.fieldCodec();
                final FieldCodec<Object> newInstance = BeanUtils.createNewInstance(cls);
                map.put(mapKey, newInstance);
            } else {
                if (XtreamTypes.isBasicType(valueEncoderConfig.javaType())) {
                    final FieldCodec<Object> fieldCodec = this.fieldCodecRegistry.getFieldCodecAndCastToObject(config.length(), config.signedness(), config.charset(), config.littleEndian(), valueEncoderConfig.javaType())
                            .orElseThrow(() -> new UnsupportedOperationException("Can not determine fieldCodec for Map field: " + valueEncoderConfig));
                    map.put(mapKey, fieldCodec);
                } else {
                    map.put(mapKey, new DelegateBeanMetadataFieldCodec(this.beanMetadataRegistry.getBeanMetadata(valueEncoderConfig.javaType(), this.version())));
                }
            }
        }
        return map;
    }

    private Map<Object, FieldCodec<Object>> initValueEncoders(XtreamFieldMapDescriptor xtreamFieldMapDescriptor) {
        return this.initValueCodec(xtreamFieldMapDescriptor, xtreamFieldMapDescriptor.valueEncoderDescriptors().valueEncoderDescriptors());
    }

    private Map<Object, FieldCodec<Object>> initValueDecoders(XtreamFieldMapDescriptor xtreamFieldMapDescriptor) {
        return this.initValueCodec(xtreamFieldMapDescriptor, xtreamFieldMapDescriptor.valueDecoderDescriptors().valueDecoderDescriptors());
    }

    private FieldCodec<Object> getValueLengthFieldDecoder(Object key) {
        return this.valueLengthSizeFieldDecoder.getOrDefault(key, this.defaultValueLengthSizeFieldCodec);
    }

    private FieldCodec<Object> getValueLengthFieldEncoder(Object key) {
        return this.valueLengthSizeFieldEncoder.getOrDefault(key, this.defaultValueLengthSizeFieldCodec);
    }

    private Object getMapKey(XtreamFieldMapDescriptor xtreamFieldMapDescriptor, XtreamFieldMapDescriptor.ValueCodecConfig config) {
        return switch (xtreamFieldMapDescriptor.keyDescriptor().type()) {
            case i8 -> config.whenKeyIsI8();
            case u8 -> config.whenKeyIsU8();
            case i16 -> config.whenKeyIsI16();
            case u16 -> config.whenKeyIsU16();
            case i32 -> config.whenKeyIsI32();
            case u32 -> config.whenKeyIsU32();
            case str -> config.whenKeyIsStr();
        };
    }

    FieldCodec<Object> detectKeyFieldCodec(XtreamFieldMapDescriptor xtreamFieldMapDescriptor, ParsedMapCodecMetadata parsedMapItemMetadata) {
        final XtreamFieldMapDescriptor.KeyDescriptor keyDescriptor = xtreamFieldMapDescriptor.keyDescriptor();
        final MapItemMetadata keyMetadata = parsedMapItemMetadata.keyCodecMetadata();
        return fieldCodecRegistry.getFieldCodecAndCastToObject(keyMetadata.length(), keyDescriptor.type().getSignedness(), keyMetadata.charset(), keyMetadata.littleEndian(), keyMetadata.javaType())
                .orElseThrow(() -> new RuntimeException("Can not determine [FieldCodec] for Key : " + keyDescriptor));
    }

    FieldCodec<Object> detectDefaultValueLengthSizeFieldCodec(XtreamFieldMapDescriptor xtreamFieldMapDescriptor) {
        final XtreamFieldMapDescriptor.ValueLengthFieldDescriptor config = xtreamFieldMapDescriptor.valueLengthFieldDescriptor();

        final Class<?> javaType = this.getJavaTypeForFieldLengthField(config.length());
        return fieldCodecRegistry.getFieldCodecAndCastToObject(config.length(), NumberSignedness.UNSIGNED, null, config.littleEndian(), javaType)
                .orElseThrow(() -> new RuntimeException("Can not determine [FieldCodec] for [defaultValueLengthSize] : " + xtreamFieldMapDescriptor));
    }

    private ParsedMapCodecMetadata parseMapItemMetadata(XtreamFieldMapDescriptor mapDescriptor) {
        final XtreamFieldMapDescriptor.ValueLengthFieldDescriptor valueLengthFieldDescriptor = mapDescriptor.valueLengthFieldDescriptor();

        final MapItemMetadata keyMetadata = this.getKeyMetadata(mapDescriptor);

        final ParsedMapCodecMetadata metadata = new ParsedMapCodecMetadata(keyMetadata, new HashMap<>(), new HashMap<>());

        for (final XtreamFieldMapDescriptor.ValueCodecConfig valueCodecConfig : mapDescriptor.valueEncoderDescriptors().valueEncoderDescriptors()) {
            final Object key = getMapKey(mapDescriptor, valueCodecConfig);
            final int vls = valueCodecConfig.valueLengthFieldSize() > 0 ? valueCodecConfig.valueLengthFieldSize() : valueLengthFieldDescriptor.length();
            final boolean vll = valueCodecConfig.valueLengthFieldSizeIsLittleEndian() || valueLengthFieldDescriptor.littleEndian();
            // metadata.valueEncodeMetadata.put(key, new MapItemCodecMetadata(valueCodecConfig.javaType(), valueCodecConfig.config(), vls, vll));

            if (vls != valueLengthFieldDescriptor.length() || vll) {
                metadata.lengthFiledSizeEncodeMetadata.put(key, new ValueLengthFieldConfigMetadata(vls, vll));
            }
        }

        for (final XtreamFieldMapDescriptor.ValueCodecConfig valueCodecConfig : mapDescriptor.valueDecoderDescriptors().valueDecoderDescriptors()) {
            final Object key = getMapKey(mapDescriptor, valueCodecConfig);
            final int vls = valueCodecConfig.valueLengthFieldSize() > 0 ? valueCodecConfig.valueLengthFieldSize() : valueLengthFieldDescriptor.length();
            final boolean vll = valueCodecConfig.valueLengthFieldSizeIsLittleEndian() || valueLengthFieldDescriptor.littleEndian();
            // metadata.valueDecodeMetadata.put(key, new MapItemCodecMetadata(valueCodecConfig.javaType(), valueCodecConfig.config(), vls, vll));

            if (vls != valueLengthFieldDescriptor.length() || vll) {
                metadata.lengthFiledSizeDecodeMetadata.put(key, new ValueLengthFieldConfigMetadata(vls, vll));
            }
        }

        return metadata;
    }

    private MapItemMetadata getKeyMetadata(XtreamFieldMapDescriptor mapDescriptor) {
        final XtreamFieldMapDescriptor.KeyDescriptor keyDescriptor = mapDescriptor.keyDescriptor();
        return new MapItemMetadata(
                keyDescriptor.type().getJavaType(),
                keyDescriptor.type().getSizeInBytes(),
                keyDescriptor.littleEndian(),
                keyDescriptor.charset()
        );
    }

    record ValueLengthFieldConfigMetadata(int length, boolean littleEndian) {
    }

    record MapItemMetadata(
            Class<?> javaType,
            int length,
            boolean littleEndian,
            String charset) {
    }

    record ParsedMapCodecMetadata(
            MapItemMetadata keyCodecMetadata,
            Map<Object, ValueLengthFieldConfigMetadata> lengthFiledSizeDecodeMetadata,
            Map<Object, ValueLengthFieldConfigMetadata> lengthFiledSizeEncodeMetadata) {
    }

}

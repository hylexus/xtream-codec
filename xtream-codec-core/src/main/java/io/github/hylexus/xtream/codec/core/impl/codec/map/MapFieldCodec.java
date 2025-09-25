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

package io.github.hylexus.xtream.codec.core.impl.codec.map;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.XtreamAssertions;
import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.impl.codec.AbstractFieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.DelegateBeanMetadataFieldCodec;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.impl.codec.map.SimpleMapMetadataRegistry.*;
import static java.util.Objects.requireNonNull;

public class MapFieldCodec extends AbstractFieldCodec<Object> {
    private static final Logger log = LoggerFactory.getLogger(MapFieldCodec.class);

    @SuppressWarnings("all")
    private final BeanMetadataRegistry beanMetadataRegistry;

    @SuppressWarnings("unused")
    private MapFieldCodec() {
        throw new UnsupportedOperationException("Use MapFieldCodec(BeanMetadataRegistry) to inject BeanMetadataRegistry instance");
    }

    /**
     * @see BeanUtils#createFieldCodecInstance(Class, BeanMetadataRegistry, int)
     */
    @SuppressWarnings("unused")
    @FieldCodecCreator
    public MapFieldCodec(BeanMetadataRegistry beanMetadataRegistry, FieldCodecRegistry fieldCodecRegistry) {
        this.beanMetadataRegistry = requireNonNull(beanMetadataRegistry);
        XtreamAssertions.assertSame(fieldCodecRegistry, beanMetadataRegistry.getFieldCodecRegistry());
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Object value) {
        if (value == null) {
            return;
        }
        final SimpleMapMetadataRegistry.MapMeta mapMeta = getOrCreateMapMetadata(context, propertyMetadata);
        @SuppressWarnings("unchecked") final Map<Object, Object> map = (Map<Object, Object>) value;

        final FieldCodec<Object> keyCodec = mapMeta.keyMeta().codec();
        final FieldCodec<Object> valueLengthCodec = mapMeta.valueLengthMeta().codec();
        final SimpleMapMetadataRegistry.EncoderValueMeta encoder = mapMeta.valueMeta().encoder();
        final ByteBuf temp = context.bufferFactory().buffer();
        try {
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                final Object mapKey = entry.getKey();
                final Object mapValue = entry.getValue();
                // 1. key
                keyCodec.serialize(propertyMetadata, context, output, mapKey);

                // 3. value
                final FieldCodec<Object> valueCodec = this.getValueEncoder(context, encoder, mapKey, mapValue);
                valueCodec.serialize(propertyMetadata, context, temp, mapValue);

                // 2. value-length
                final int valueLength = temp.writerIndex();
                valueLengthCodec.serialize(propertyMetadata, context, output, valueLength);
                output.writeBytes(temp);
                temp.clear();
            }
        } finally {
            temp.release();
        }
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        @SuppressWarnings({"unchecked"}) final Map<Object, Object> map = (Map<Object, Object>) propertyMetadata.containerInstanceFactory().create();
        final SimpleMapMetadataRegistry.MapMeta mapMeta = getOrCreateMapMetadata(context,propertyMetadata);
        final KeyMeta keyMeta = mapMeta.keyMeta();
        final int keyLength = mapMeta.keyMeta().sizeInBytes();
        final FieldCodec<Object> keyCodec = keyMeta.codec();
        final FieldCodec<Object> valueLengthCodec = mapMeta.valueLengthMeta().codec();
        while (slice.isReadable()) {
            // 1. key(i8,u8,i16,u16,i32,u32,i64,string)
            final Object key = keyCodec.deserialize(propertyMetadata, context, slice, keyLength);

            // 2. valueLength(int)
            final int valueLength = ((Number) valueLengthCodec.deserialize(propertyMetadata, context, slice, length)).intValue();

            // 3. value(dynamic)
            final ByteBuf byteBuf = slice.readSlice(valueLength);
            final FieldCodec<?> valueFieldCodec = this.getValueDecoder(mapMeta, key);
            if (log.isDebugEnabled()) {
                log.debug("MapValueDecoder: key={}, valueFieldCodec={}", key, valueFieldCodec);
            }
            final Object value = valueFieldCodec.deserialize(propertyMetadata, context, byteBuf, valueLength);
            map.put(key, value);
        }
        return map;
    }

    private FieldCodec<Object> getValueEncoder(SerializeContext context, EncoderValueMeta encoder, Object mapKey, Object mapValue) {
        final Map<Object, ValueMatcherMeta> valueMatchersByKey = encoder.valueMatchersByKey();
        final ValueMatcherMeta valueMatcher = valueMatchersByKey.get(mapKey);
        if (valueMatcher != null) {
            return valueMatcher.valueCodec();
        }
        final Class<?> javaType = mapValue.getClass();
        if (Map.class.isAssignableFrom(javaType)) {
            throw new NotYetImplementedException("暂不支持内嵌 Map");
        }
        if (Collection.class.isAssignableFrom(javaType)) {
            throw new NotYetImplementedException("暂不支持内嵌 Collection");
        }
        if (XtreamTypes.isBasicType(javaType)) {
            return context.fieldCodecRegistry().getFieldCodecAndCastToObject(
                            XtreamTypes.getDefaultSizeInBytes(javaType).orElse(-1),
                            NumberSignedness.SIGNED,
                            encoder.commonParam().stringTypeCharset(),
                            encoder.commonParam().numberTypeAsLittleEndian(),
                            javaType
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Can not determine [FieldCodec] for " + javaType));
        }

        return new DelegateBeanMetadataFieldCodec(context.beanMetadataRegistry().getBeanMetadata(javaType, context.version()));
    }

    private FieldCodec<?> getValueDecoder(MapMeta mapMeta, Object key) {
        final DecoderValueMeta decoder = mapMeta.valueMeta().decoder();
        final ValueMatcherMeta valueMatcherMeta = decoder.valueMatchersByKey().get(key);
        if (valueMatcherMeta != null) {
            return valueMatcherMeta.valueCodec();
        }
        return decoder.fallbackValueMatcher().codec();
    }

}

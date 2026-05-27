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

package io.github.hylexus.xtream.codec.core.impl.codec.tlv;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.impl.domain.*;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.CollectionFieldSpan;
import io.github.hylexus.xtream.codec.core.tracker.CollectionItemSpan;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.github.hylexus.xtream.codec.core.type.FieldLength;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TLVCodecs {
    public static final TLVCodec INSTANCE = new TLVCodec();

    public static class TLVCodec implements FieldCodec<@Nullable TLV> {

        private TLVCodec() {
        }

        @Override
        public @Nullable TLV deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException("TLV codec does not support deserialization");
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable TLV value) {
            if (value == null) {
                return;
            }
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                encodeTlv(context, output, value, temp);
            } finally {
                temp.release();
            }
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable TLV value) {
            if (value == null) {
                return;
            }
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
                encodeTlvWithTracker(context, output, value, codecTracker, temp, this.getClass().getSimpleName());
            } finally {
                temp.release();
            }
        }
    }

    public static class TLVSequenceCodec implements FieldCodec<Iterable<@Nullable TLV>> {

        private final ExtendMetaRegistry extendMetaRegistry;

        @FieldCodecCreator
        public TLVSequenceCodec(BeanMetadataRegistry beanMetadataRegistry) {
            this.extendMetaRegistry = new DefaultExtendMetaRegistry(beanMetadataRegistry);
        }

        @Override
        public @Nullable Iterable<TLV> deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final XtreamTLVFieldSequenceMeta meta = this.extendMetaRegistry.getXtreamTlvFieldSequenceMeta(context.version(), propertyMetadata.field());
            final KeyMeta keyMeta = meta.decoder().key();
            final Map<Object, ValueMatcherMeta> valueMatchersByKey = meta.decoder().valueMatchers().valueMatchersByKey();
            final FallbackValueMatcherMeta fallbackValueMatcherMeta = meta.decoder().fallbackValueMatcher();
            final ValueLengthMeta lengthMeta = meta.decoder().length();

            final List<TLV> results = new ArrayList<>();
            final ByteBuf slice = length > 0
                    ? input.readSlice(length)
                    : input; // all remaining
            int iterationTimes = propertyMetadata.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
            while (slice.isReadable() && iterationTimes-- > 0) {
                final TLV tlv = decodeTlv(propertyMetadata, context, slice, keyMeta, lengthMeta, valueMatchersByKey, fallbackValueMatcherMeta);
                results.add(tlv);
            }
            return results;
        }

        @Override
        public @Nullable Iterable<@Nullable TLV> deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final XtreamTLVFieldSequenceMeta meta = this.extendMetaRegistry.getXtreamTlvFieldSequenceMeta(context.version(), propertyMetadata.field());
            final KeyMeta keyMeta = meta.decoder().key();
            final Map<Object, ValueMatcherMeta> valueMatchersByKey = meta.decoder().valueMatchers().valueMatchersByKey();
            final FallbackValueMatcherMeta fallbackValueMatcherMeta = meta.decoder().fallbackValueMatcher();
            final ValueLengthMeta lengthMeta = meta.decoder().length();

            final List<TLV> results = new ArrayList<>();
            final ByteBuf slice = length > 0
                    ? input.readSlice(length)
                    : input; // all remaining

            int iterationTimes = propertyMetadata.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(propertyMetadata);
            int sequence = 0;
            final int parentIndexBeforeRead = slice.readerIndex();

            while (slice.isReadable() && iterationTimes-- > 0) {
                final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
                final int indexBeforeRead = slice.readerIndex();

                final TLV tlv = decodeTlvWithTracker(codecTracker, propertyMetadata, context, slice, keyMeta, lengthMeta, valueMatchersByKey, fallbackValueMatcherMeta);
                results.add(tlv);

                collectionItemSpan.setHexString(FormatUtils.toHexString(slice, indexBeforeRead, slice.readerIndex() - indexBeforeRead));
                codecTracker.finishCurrentSpan();
            }

            collectionFieldSpan.setHexString(FormatUtils.toHexString(slice, parentIndexBeforeRead, slice.readerIndex() - parentIndexBeforeRead));
            codecTracker.finishCurrentSpan();

            return results;
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Iterable<@Nullable TLV> tlvIterable) {
            if (tlvIterable == null) {
                return;
            }
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                for (TLV value : tlvIterable) {
                    if (value == null) {
                        continue;
                    }
                    encodeTlv(context, output, value, temp);
                }
            } finally {
                temp.release();
            }

        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Iterable<@Nullable TLV> tlvIterable) {
            if (tlvIterable == null) {
                return;
            }
            final CodecTracker codecTracker = requireNonNull(context.codecTracker());
            final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(propertyMetadata);
            final int parentIndexBeforeWrite = output.writerIndex();
            int sequence = 0;
            final ByteBuf temp = context.bufferFactory().buffer();
            try {
                for (TLV tlv : tlvIterable) {
                    if (tlv == null) {
                        continue;
                    }

                    final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
                    final int indexBeforeWrite = output.writerIndex();

                    encodeTlvWithTracker(context, output, tlv, codecTracker, temp, this.getClass().getSimpleName());

                    collectionItemSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                    codecTracker.finishCurrentSpan();
                }
            } finally {
                temp.release();
            }

            collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
            codecTracker.finishCurrentSpan();
        }

    }

    private static void encodeTlv(FieldCodec.SerializeContext context, ByteBuf output, TLV value, ByteBuf temp) {
        temp.clear();
        final DataField.DictKey tag = value.tag();

        // 1. 编码 tag
        tag.encode(output);

        // Length 是 sealed interface 并且只有一个实现类，所以强转是安全的
        final FieldLength.DefaultFieldLength valueLength = (FieldLength.DefaultFieldLength) value.length();

        // 2. 编码 value
        context.entityEncoder().encode(value.value(), temp);
        valueLength.setValue(temp.readableBytes());

        // 3. 编码 value 长度
        valueLength.type().writeTo(output, valueLength.value());

        // 2. 编码 value
        output.writeBytes(temp);
    }

    private static void encodeTlvWithTracker(FieldCodec.SerializeContext context, ByteBuf output, TLV tlv, CodecTracker codecTracker, ByteBuf temp, String fieldCodec) {
        temp.clear();
        final DataField.DictKey tag = tlv.tag();

        // 1. 编码 tag
        tag.encodeWithTracker(output, codecTracker, "tag");

        // Length 是 sealed interface 并且只有一个实现类，所以强转是安全的
        final FieldLength.DefaultFieldLength valueLength = (FieldLength.DefaultFieldLength) tlv.length();
        final boolean flattedSpan = tlv.value() instanceof CodecTracker.FlattedSpan;

        // 2. 编码 value
        if (flattedSpan) {
            codecTracker.updateTempFieldName("value");
            context.entityEncoder().encodeWithTracker(tlv.value(), temp, codecTracker);
            valueLength.setValue(temp.readableBytes());
        } else {
            final NestedFieldSpan valueTracker = codecTracker.startNewNestedFieldSpan("value", "", tlv.value().getClass().getSimpleName(), fieldCodec);

            context.entityEncoder().encodeWithTracker(tlv.value(), temp, codecTracker);
            valueLength.setValue(temp.readableBytes());

            valueTracker.setHexString(FormatUtils.toHexString(temp, 0, temp.writerIndex()));
            codecTracker.finishCurrentSpan();
        }

        // 3. 编码 value 长度
        valueLength.type().writeToWithTracker(output, valueLength.value(), codecTracker, "length");

        // 2. 编码 value
        output.writeBytes(temp);
    }

    static TLV decodeTlv(BeanPropertyMetadata propertyMetadata, FieldCodec.DeserializeContext context, ByteBuf slice, KeyMeta keyMeta, ValueLengthMeta lengthMeta, Map<Object, ValueMatcherMeta> valueMatchersByKey, FallbackValueMatcherMeta fallbackValueMatcherMeta) {
        // 1. tag
        final DataField.DictKey keyWrapper = keyMeta.readFrom(slice);
        final Object key = keyWrapper.value();
        requireNonNull(key);

        // 2. length
        final Number valueLengthNumber = lengthMeta.type().readFrom(slice);
        final int valueLength = valueLengthNumber.intValue();
        final ValueMatcherMeta valueMatcherMeta = valueMatchersByKey.get(key);

        // 3. value
        final FieldCodec<Object> valueCodec;
        if (valueMatcherMeta != null) {
            valueCodec = valueMatcherMeta.valueCodec();
        } else {
            valueCodec = fallbackValueMatcherMeta.codec();
        }
        final Object value = valueCodec.deserialize(propertyMetadata, context, slice, valueLength);
        return new TLV(keyWrapper, new FieldLength.DefaultFieldLength(lengthMeta.type()).setValue(valueLength), requireNonNull(value));
    }


    static TLV decodeTlvWithTracker(CodecTracker codecTracker, BeanPropertyMetadata propertyMetadata, FieldCodec.DeserializeContext context, ByteBuf slice, KeyMeta keyMeta, ValueLengthMeta lengthMeta, Map<Object, ValueMatcherMeta> valueMatchersByKey, FallbackValueMatcherMeta fallbackValueMatcherMeta) {
        // 1. tag
        final DataField.DictKey keyWrapper = keyMeta.readFromWithTracker(slice, codecTracker, "tag");
        final Object key = keyWrapper.value();
        requireNonNull(key);

        // 2. length
        final Number valueLengthNumber = lengthMeta.type().readFromWithTracker(slice, codecTracker, "length");
        final int valueLength = valueLengthNumber.intValue();
        final ValueMatcherMeta valueMatcherMeta = valueMatchersByKey.get(key);

        // 3. value
        codecTracker.updateTempFieldName("value");
        final FieldCodec<Object> valueCodec;
        if (valueMatcherMeta != null) {
            valueCodec = valueMatcherMeta.valueCodec();
        } else {
            valueCodec = fallbackValueMatcherMeta.codec();
        }
        final Object value = valueCodec.deserializeWithTracker(propertyMetadata, context, slice, valueLength);
        return new TLV(keyWrapper, new FieldLength.DefaultFieldLength(lengthMeta.type()).setValue(valueLength), requireNonNull(value));
    }

}

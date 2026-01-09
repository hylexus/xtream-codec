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

package io.github.hylexus.xtream.codec.core.impl.codec.pair;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.ExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.DefaultExtendMetaRegistry;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.domain.KeyMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.ValueMatcherMeta;
import io.github.hylexus.xtream.codec.core.impl.domain.XtreamPairFieldSequenceMeta;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.CollectionFieldSpan;
import io.github.hylexus.xtream.codec.core.tracker.CollectionItemSpan;
import io.github.hylexus.xtream.codec.core.tracker.NestedFieldSpan;
import io.github.hylexus.xtream.codec.core.type.Pair;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PairCodecs {
    public static PairCodec INSTANCE = new PairCodec();

    public static class PairCodec implements FieldCodec<@Nullable Pair> {
        private PairCodec() {
        }

        @Override
        public @Nullable Pair deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new NotYetImplementedException("Not yet implemented");
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Pair pair) {
            if (pair == null) {
                return;
            }
            encodePair(context, output, pair);
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Pair pair) {
            if (pair == null) {
                return;
            }
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            encodePairWithTracker(context, output, pair, codecTracker, this.getClass().getSimpleName());
        }
    }

    public static class PairSequenceCodec implements FieldCodec<Iterable<@Nullable Pair>> {
        private final ExtendMetaRegistry extendMetaRegistry;

        @FieldCodecCreator
        public PairSequenceCodec(BeanMetadataRegistry beanMetadataRegistry) {
            this.extendMetaRegistry = new DefaultExtendMetaRegistry(beanMetadataRegistry);
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Iterable<@Nullable Pair> pairList) {
            if (pairList == null) {
                return;
            }
            for (Pair pair : pairList) {
                if (pair == null) {
                    continue;
                }
                encodePair(context, output, pair);
            }
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Iterable<@Nullable Pair> pairList) {
            if (pairList == null) {
                return;
            }
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(propertyMetadata);
            final int parentIndexBeforeWrite = output.writerIndex();
            int sequence = 0;
            for (Pair pair : pairList) {
                if (pair == null) {
                    continue;
                }

                final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
                final int indexBeforeWrite = output.writerIndex();

                encodePairWithTracker(context, output, pair, codecTracker, this.getClass().getSimpleName());

                collectionItemSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }

            collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
            codecTracker.finishCurrentSpan();
        }


        @Override
        public @Nullable Iterable<@Nullable Pair> deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final XtreamPairFieldSequenceMeta meta = this.extendMetaRegistry.getXtreamPairFieldSequenceMeta(context.version(), propertyMetadata.field());
            final List<Pair> results = new ArrayList<>();
            final ByteBuf slice = length > 0
                    ? input.readSlice(length)
                    : input; // all remaining

            final Map<Object, ValueMatcherMeta> valueMatchersByKey = meta.decoder().valueMatchers().valueMatchersByKey();
            int iterationTimes = propertyMetadata.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
            while (slice.isReadable() && iterationTimes-- > 0) {
                // 1. key
                final KeyMeta keyMeta = meta.decoder().key();
                final DataField.DictKey key = DataField.DictKey.deserialize(keyMeta.type(), slice, keyMeta.sizeInBytes(), keyMeta.charset());

                // 2. value
                final ValueMatcherMeta valueMatcherMeta = valueMatchersByKey.get(key.value());
                if (valueMatcherMeta == null) {
                    final String remainingHex = StringFieldCodecs.INSTANCE_HEX.deserialize(propertyMetadata, context, slice, slice.readableBytes());
                    results.add(new Pair(key, null, remainingHex));
                    break;
                } else {
                    final int valueLength = valueMatcherMeta.length();
                    final FieldCodec<Object> valueCodec = valueMatcherMeta.valueCodec();
                    final Object value = valueCodec.deserialize(propertyMetadata, context, slice.readSlice(valueLength), valueLength);
                    final Pair pair = new Pair(key, value);
                    results.add(pair);
                }
            }

            return results;
        }

        @Override
        public @Nullable Iterable<@Nullable Pair> deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final XtreamPairFieldSequenceMeta meta = this.extendMetaRegistry.getXtreamPairFieldSequenceMeta(context.version(), propertyMetadata.field());
            final List<Pair> results = new ArrayList<>();
            final ByteBuf slice = length > 0
                    ? input.readSlice(length)
                    : input; // all remaining

            final Map<Object, ValueMatcherMeta> valueMatchersByKey = meta.decoder().valueMatchers().valueMatchersByKey();
            int iterationTimes = propertyMetadata.iterationTimesExtractor().extractIterationTimes(context, context.evaluationContext());
            final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpan(propertyMetadata);
            int sequence = 0;
            final int parentIndexBeforeRead = slice.readerIndex();

            while (slice.isReadable() && iterationTimes-- > 0) {
                final CollectionItemSpan collectionItemSpan = codecTracker.startNewCollectionItemSpan(collectionFieldSpan, collectionFieldSpan.getFieldName(), sequence++);
                final int indexBeforeRead = slice.readerIndex();

                // 1. key
                final KeyMeta keyMeta = meta.decoder().key();
                final DataField.DictKey key = DataField.DictKey.deserializeWithTracker(codecTracker, keyMeta.type(), slice, keyMeta.sizeInBytes(), keyMeta.charset(), "key");

                // 2. value
                final ValueMatcherMeta valueMatcherMeta = valueMatchersByKey.get(key.value());
                codecTracker.updateTempFieldName("value");
                if (valueMatcherMeta == null) {
                    final String remainingHex = StringFieldCodecs.INSTANCE_HEX.deserializeWithTracker(propertyMetadata, context, slice, slice.readableBytes());
                    results.add(new Pair(key, null, remainingHex));
                    collectionItemSpan.setHexString(FormatUtils.toHexString(slice, indexBeforeRead, slice.readerIndex() - indexBeforeRead));
                    codecTracker.finishCurrentSpan();
                    break;
                } else {
                    final int valueLength = valueMatcherMeta.length();
                    final FieldCodec<Object> valueCodec = valueMatcherMeta.valueCodec();

                    final Object value = valueCodec.deserializeWithTracker(propertyMetadata, context, slice.readSlice(valueLength), valueLength);
                    final Pair pair = new Pair(key, value);
                    results.add(pair);
                    collectionItemSpan.setHexString(FormatUtils.toHexString(slice, indexBeforeRead, slice.readerIndex() - indexBeforeRead));
                    codecTracker.finishCurrentSpan();
                }
            }

            collectionFieldSpan.setHexString(FormatUtils.toHexString(slice, parentIndexBeforeRead, slice.readerIndex() - parentIndexBeforeRead));
            codecTracker.finishCurrentSpan();
            return results;
        }
    }

    private static void encodePair(FieldCodec.SerializeContext context, ByteBuf output, Pair pair) {
        // 1. 编码 key
        final DataField.DictKey key = pair.key();
        key.encode(output);

        // 2. 编码 value
        final Object value = pair.value();
        Objects.requireNonNull(value, "Pair.value can not be null");
        context.entityEncoder().encode(value, output);
    }

    static void encodePairWithTracker(FieldCodec.SerializeContext context, ByteBuf output, Pair pair, CodecTracker codecTracker, String fieldCodec) {
        // 1. 编码 key
        final DataField.DictKey key = pair.key();
        key.encodeWithTracker(output, codecTracker, "key");

        final Object value = Objects.requireNonNull(pair.value());
        final boolean flattedSpan = value instanceof CodecTracker.FlattedSpan;
        // 2. 编码 value
        if (flattedSpan) {
            codecTracker.updateTempFieldName("value");
            context.entityEncoder().encodeWithTracker(value, output, codecTracker);
        } else {
            final NestedFieldSpan valueTracker = codecTracker.startNewNestedFieldSpan("value", "", value.getClass().getSimpleName(), fieldCodec);
            final int index = output.writerIndex();
            context.entityEncoder().encodeWithTracker(value, output, codecTracker);

            valueTracker.setHexString(FormatUtils.toHexString(output, index, output.writerIndex() - index));
            codecTracker.finishCurrentSpan();
        }
    }

}

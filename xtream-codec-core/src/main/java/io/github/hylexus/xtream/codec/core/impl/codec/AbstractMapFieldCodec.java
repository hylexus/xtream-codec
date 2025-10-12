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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.tracker.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @param <K>    {@link Map.Entry#getKey()}
 * @param <VLFC> ValueLengthFieldCodec
 * @author hylexus
 */
@SuppressWarnings({"unchecked", "rawtypes", "checkstyle:ClassTypeParameterName"})
public abstract class AbstractMapFieldCodec<
        K,
        VLFC extends IntegralFieldCodec
        > implements FieldCodec<Object> {

    protected abstract FieldCodec getKeyFieldCodec();

    protected abstract VLFC getValueLengthFieldCodec();

    protected abstract FieldCodec getValueFieldCodec(K key);

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        final Map<Object, Object> result = new LinkedHashMap<>();
        while (slice.isReadable()) {
            final Object key = this.getKeyFieldCodec().deserialize(propertyMetadata, context, slice, length);
            final int valueLength = this.getValueLengthFieldCodec().deserialize(propertyMetadata, context, slice, length).intValue();
            final FieldCodec<?> valueFieldCodec = this.getValueFieldCodec((K) key);
            final Object value = valueFieldCodec.deserialize(propertyMetadata, context, slice, valueLength);
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Object deserializeWithTracker(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);
        final Map<Object, Object> result = new LinkedHashMap<>();
        final int parentIndexBeforeRead = slice.readerIndex();
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final MapFieldSpan mapFieldSpan = codecTracker.startNewMapFieldSpan(propertyMetadata, this.getClass().getSimpleName());
        int sequence = 0;
        while (slice.isReadable()) {
            final int indexBeforeRead = slice.readerIndex();
            final MapEntrySpan mapEntrySpan = codecTracker.startNewMapEntrySpan(mapFieldSpan, propertyMetadata.name(), sequence++);

            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.KEY);
            final Object key = this.getKeyFieldCodec().deserializeWithTracker(propertyMetadata, context, slice, length);

            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE_LENGTH);
            final int valueLength = this.getValueLengthFieldCodec().deserializeWithTracker(propertyMetadata, context, slice, length).intValue();

            final FieldCodec<?> valueFieldCodec = this.getValueFieldCodec((K) key);
            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE);
            final Object value = valueFieldCodec.deserializeWithTracker(propertyMetadata, context, slice, valueLength);

            mapEntrySpan.setHexString(FormatUtils.toHexString(slice, indexBeforeRead, slice.readerIndex() - indexBeforeRead));
            codecTracker.finishCurrentSpan();
            result.put(key, value);
        }
        mapFieldSpan.setHexString(FormatUtils.toHexString(slice, parentIndexBeforeRead, slice.readerIndex() - parentIndexBeforeRead));
        codecTracker.finishCurrentSpan();
        return result;
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
        if (value == null) {
            return;
        }
        final Map<Object, Object> map = (Map<Object, Object>) value;
        final ByteBuf temp = ByteBufAllocator.DEFAULT.buffer();
        try {
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                final Object mapKey = entry.getKey();
                final Object mapValue = entry.getValue();

                final FieldCodec keyFieldCodec = this.getKeyFieldCodec();
                keyFieldCodec.serialize(propertyMetadata, context, output, mapKey);

                final FieldCodec valueFieldCodec = this.getValueFieldCodec((K) mapKey);
                valueFieldCodec.serialize(propertyMetadata, context, temp, mapValue);

                final int valueLength = temp.writerIndex();
                this.getValueLengthFieldCodec().serialize(propertyMetadata, context, output, valueLength);
                output.writeBytes(temp);
                temp.clear();
            }
        } finally {
            temp.release();
        }
    }

    @Override
    public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
        if (value == null) {
            return;
        }
        final Map<Object, Object> map = (Map<Object, Object>) value;
        final ByteBuf temp = ByteBufAllocator.DEFAULT.buffer();
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final MapFieldSpan mapFieldSpan = codecTracker.startNewMapFieldSpan(propertyMetadata, this.getClass().getSimpleName());
        final int parenIndexBeforeWrite = output.writerIndex();
        final BaseSpan parent = codecTracker.getCurrentSpan();
        int sequence = 0;
        try {
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                final Object mapKey = entry.getKey();
                final Object mapValue = entry.getValue();

                final MapEntrySpan mapEntrySpan = codecTracker.startNewMapEntrySpan(parent, propertyMetadata.name(), sequence++);
                final int writerIndex = output.writerIndex();
                codecTracker.updateTrackerHints(MapEntryItemSpan.Type.KEY);
                this.getKeyFieldCodec().serializeWithTracker(propertyMetadata, context, output, mapKey);

                final FieldCodec valueFieldCodec = this.getValueFieldCodec((K) mapKey);
                codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE);
                valueFieldCodec.serializeWithTracker(propertyMetadata, context, temp, mapValue);

                final int valueLength = temp.writerIndex();
                codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE_LENGTH);
                this.getValueLengthFieldCodec().serializeWithTracker(propertyMetadata, context, output, valueLength);
                output.writeBytes(temp);
                mapEntrySpan.setHexString(FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex));
                temp.clear();
                codecTracker.finishCurrentSpan();
            }
            mapFieldSpan.setHexString(FormatUtils.toHexString(output, parenIndexBeforeWrite, output.writerIndex() - parenIndexBeforeWrite));
            codecTracker.finishCurrentSpan();
        } finally {
            temp.release();
        }
    }
}

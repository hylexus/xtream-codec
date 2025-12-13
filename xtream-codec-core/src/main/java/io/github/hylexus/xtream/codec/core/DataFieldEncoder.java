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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.impl.DefaultSerializeContext;
import io.github.hylexus.xtream.codec.core.tracker.*;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
@ApiStatus.Experimental
public class DataFieldEncoder {

    public void encode(FieldCodec.SerializeContext context, Iterable<? extends @Nullable DataField> simpleFields, ByteBuf output) {
        for (final DataField dataField : simpleFields) {
            if (dataField == null) {
                continue;
            }
            this.encode(context, dataField, output);
        }
    }

    public void encode(FieldCodec.SerializeContext context, @Nullable DataField dataField, ByteBuf output) {
        if (dataField == null || dataField.value() == null) {
            return;
        }
        final PrependLengthFieldType prependLengthFieldType = dataField.prependLengthFieldType();
        final int prependLengthFieldTypeByteCounts = prependLengthFieldType.getByteCounts();
        if (prependLengthFieldTypeByteCounts <= 0) {
            this.doEncodeField(context, output, dataField);
        } else {
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeField(context, output, dataField);

            final int afterEncode = output.writerIndex();
            final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            output.writerIndex(afterEncode);
        }
    }

    private void doEncodeField(FieldCodec.SerializeContext context, ByteBuf output, DataField dataField) {
        switch (dataField) {
            case DataField.I8 i8 -> output.writeByte(i8.value());
            case DataField.U8 u8 -> output.writeByte(u8.value());
            case DataField.I16 i16 -> output.writeShort(i16.value());
            case DataField.U16 u16 -> output.writeShort(u16.value());
            case DataField.I32 i32 -> output.writeInt(i32.value());
            case DataField.U32 u32 -> output.writeInt(u32.value().intValue());
            case DataField.I64 i64 -> output.writeLong(i64.value());
            case DataField.F32 f32 -> output.writeFloat(f32.value());
            case DataField.F64 f64 -> output.writeDouble(f64.value());
            case DataField.Bcd8421String bcd8421String -> BcdOps.encodeBcd8421StringIntoByteBuf(bcd8421String.value(), output);
            case DataField.HexString hexString -> XtreamBytes.writeHexString(output, hexString.value());
            case DataField.GbkString gbkString -> encodeString(output, gbkString.value(), XtreamConstants.CHARSET_GBK);
            case DataField.Gb2312String gb2312String -> encodeString(output, gb2312String.value(), XtreamConstants.CHARSET_GB_2312);
            case DataField.Utf8String utf8String -> encodeString(output, utf8String.value(), XtreamConstants.CHARSET_UTF8);
            case DataField.GenericString genericString -> encodeString(output, genericString.value(), Charset.forName(genericString.charset()));
            case DataField.ByteSequence byteSequence -> output.writeBytes(byteSequence.value());
            case DataField.Struct struct -> {
                final List<DataField> value = struct.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case DataField.Sequence sequence -> {
                final List<DataField> value = sequence.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case DataField.Dict<?> simpleMap -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, simpleMap);
                final Map<? extends DataField.DictKey, DataField> map = simpleMap.value();
                final ByteBuf temp = context.bufferFactory().buffer();
                try {
                    for (Map.Entry<? extends DataField.DictKey, DataField> entry : map.entrySet()) {
                        try {
                            // 1. key
                            final DataField.DictKey key = entry.getKey();
                            this.doEncodeField(newContext, output, key);
                            // 2. value
                            final DataField value = entry.getValue();
                            this.doEncodeField(newContext, temp, value);
                            // 3. valueLength
                            final int valueLength = temp.writerIndex();
                            simpleMap.valueLengthType().writeTo(output, valueLength);
                            output.writeBytes(temp);
                        } finally {
                            temp.clear();
                        }
                    }
                } finally {
                    temp.release();
                }
            }
            case DataField.TlvDataField tlvDataField -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, tlvDataField);
                // 1. tag
                final DataField.DictKey tag = tlvDataField.tag();
                this.doEncodeField(newContext, output, tag);
                final ByteBuf temp = context.bufferFactory().buffer();

                try {
                    // 2. value
                    final DataField value = tlvDataField.value();
                    this.doEncodeField(newContext, temp, value);

                    // 3. length
                    final int valueLength = temp.writerIndex();
                    tlvDataField.length().writeTo(output, valueLength);
                    output.writeBytes(temp);
                } finally {
                    temp.release();
                }
            }
            case DataField.CustomDataField customSimpleField -> customSimpleField.writeTo(output);
        }
    }

    public void encodeWithTracker(FieldCodec.SerializeContext context, Iterable<? extends @Nullable DataField> simpleFields, ByteBuf output) {
        for (final DataField dataField : simpleFields) {
            if (dataField == null) {
                continue;
            }
            this.encodeWithTracker(context, dataField, output);
        }
    }

    public void encodeWithTracker(FieldCodec.SerializeContext context, @Nullable DataField dataField, ByteBuf output) {
        if (dataField == null || dataField.value() == null) {
            return;
        }
        final PrependLengthFieldType prependLengthFieldType = dataField.prependLengthFieldType();
        final int prependLengthFieldTypeByteCounts = prependLengthFieldType.getByteCounts();
        if (prependLengthFieldTypeByteCounts <= 0) {
            this.doEncodeFieldWithTracker(context, output, dataField);
        } else {
            @SuppressWarnings("Duplicated") final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final PrependLengthFieldSpan prependLengthFieldSpan = codecTracker.addPrependLengthFieldSpan(
                    codecTracker.getCurrentSpan(), "prependLengthField", null, null, prependLengthFieldType.name(), "前置长度字段"
            );
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeFieldWithTracker(context, output, dataField);

            final int afterEncode = output.writerIndex();
            @SuppressWarnings("Duplicated") final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            final String hexString = FormatUtils.toHexString(output, lengthFieldWriterIndex, output.writerIndex() - lengthFieldWriterIndex);
            prependLengthFieldSpan.setValue(byteCounts).setHexString(hexString);
            output.writerIndex(afterEncode);
        }
    }

    private void doEncodeFieldWithTracker(FieldCodec.SerializeContext context, ByteBuf output, DataField dataField) {
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final int indexBeforeWrite = output.writerIndex();
        switch (dataField) {
            case DataField.I8 i8 -> output.writeByte(i8.value());
            case DataField.U8 u8 -> output.writeByte(u8.value());
            case DataField.I16 i16 -> output.writeShort(i16.value());
            case DataField.U16 u16 -> output.writeShort(u16.value());
            case DataField.I32 i32 -> output.writeInt(i32.value());
            case DataField.U32 u32 -> output.writeInt(u32.value().intValue());
            case DataField.I64 i64 -> output.writeLong(i64.value());
            case DataField.F32 f32 -> output.writeFloat(f32.value());
            case DataField.F64 f64 -> output.writeDouble(f64.value());
            case DataField.Bcd8421String bcd8421String -> BcdOps.encodeBcd8421StringIntoByteBuf(bcd8421String.value(), output);
            case DataField.HexString hexString -> XtreamBytes.writeHexString(output, hexString.value());
            case DataField.GbkString gbkString -> encodeString(output, gbkString.value(), XtreamConstants.CHARSET_GBK);
            case DataField.Gb2312String gb2312String -> encodeString(output, gb2312String.value(), XtreamConstants.CHARSET_GB_2312);
            case DataField.Utf8String utf8String -> encodeString(output, utf8String.value(), XtreamConstants.CHARSET_UTF8);
            case DataField.GenericString genericString -> encodeString(output, genericString.value(), Charset.forName(genericString.charset()));
            case DataField.ByteSequence byteSequence -> output.writeBytes(byteSequence.value());
            case DataField.Struct struct -> {
                final List<DataField> value = struct.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                final NestedFieldSpan nestedFieldSpan = codecTracker.startNewNestedFieldSpan(dataField.name(), "", dataField.type(), this.getClass().getSimpleName());
                this.encodeWithTracker(newContext, value, output);
                nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }
            case DataField.Sequence sequence -> {
                final List<DataField> value = sequence.value();
                final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpanForSimpleField(dataField.name());
                final int parentIndexBeforeWrite = output.writerIndex();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encodeWithTracker(newContext, value, output);
                collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }
            case DataField.Dict<?> simpleMap -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, simpleMap);
                final Map<? extends DataField.DictKey, DataField> map = simpleMap.value();
                final ByteBuf temp = context.bufferFactory().buffer();
                final MapFieldSpan mapFieldSpan = codecTracker.startNewMapFieldSpan(simpleMap.name(), dataField.type(), this.getClass().getSimpleName());
                final int parenIndexBeforeWrite = output.writerIndex();
                final BaseSpan parent = codecTracker.getCurrentSpan();
                int sequence = 0;
                try {
                    for (Map.Entry<? extends DataField.DictKey, DataField> entry : map.entrySet()) {
                        try {
                            // 1. key
                            final DataField.DictKey key = entry.getKey();
                            final MapEntrySpan mapEntrySpan = codecTracker.startNewMapEntrySpan(parent, key.name(), sequence++);
                            final int writerIndex = output.writerIndex();
                            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.KEY);
                            this.doEncodeFieldWithTracker(newContext, output, key);
                            // 2. value
                            final DataField value = entry.getValue();
                            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE);
                            this.doEncodeFieldWithTracker(newContext, temp, value);
                            // 3. valueLength
                            final int valueLength = temp.writerIndex();
                            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.VALUE_LENGTH);
                            simpleMap.valueLengthType().writeToWithTracker(output, valueLength, codecTracker);
                            output.writeBytes(temp);
                            mapEntrySpan.setHexString(FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex));
                            codecTracker.finishCurrentSpan();
                        } finally {
                            temp.clear();
                        }
                    }
                    mapFieldSpan.setHexString(FormatUtils.toHexString(output, parenIndexBeforeWrite, output.writerIndex() - parenIndexBeforeWrite));
                    codecTracker.finishCurrentSpan();
                } finally {
                    temp.release();
                }
            }
            case DataField.TlvDataField tlvDataField -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, tlvDataField);
                final NestedFieldSpan nestedFieldSpan = codecTracker.startNewNestedFieldSpan(dataField.name(), "", dataField.type(), this.getClass().getSimpleName());
                // 1. tag
                final DataField.DictKey tag = tlvDataField.tag();
                this.doEncodeFieldWithTracker(newContext, output, tag);
                final ByteBuf temp = context.bufferFactory().buffer();

                try {
                    // 2. value
                    final DataField value = tlvDataField.value();
                    this.doEncodeFieldWithTracker(newContext, temp, value);

                    // 3. length
                    final int valueLength = temp.writerIndex();
                    tlvDataField.length().writeToWithTracker(output, valueLength, codecTracker);
                    output.writeBytes(temp);

                    nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                    codecTracker.finishCurrentSpan();
                } finally {
                    temp.release();
                }
            }
            case DataField.CustomDataField customSimpleField -> customSimpleField.writeTo(output);
        }
        if (!(dataField instanceof DataField.Struct)
                && !(dataField instanceof DataField.Dict<?>)
                && !(dataField instanceof DataField.Sequence)
                && !(dataField instanceof DataField.SimpleTlvDataField<?>)) {
            final String hexString = FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), dataField.name(), dataField.value(), hexString, this.getClass().getSimpleName(), dataField.getClass().getSimpleName());
        }
    }

    private static void encodeString(ByteBuf output, String value, Charset charset) {
        output.writeCharSequence(value, charset);
    }

}

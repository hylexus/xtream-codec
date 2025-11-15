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
import io.github.hylexus.xtream.codec.core.type.simple.SimpleField;
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
public class SimpleFieldEncoder {

    public void encode(FieldCodec.SerializeContext context, Iterable<? extends @Nullable SimpleField> simpleFields, ByteBuf output) {
        for (final SimpleField simpleField : simpleFields) {
            if (simpleField == null) {
                continue;
            }
            this.encode(context, simpleField, output);
        }
    }

    public void encode(FieldCodec.SerializeContext context, @Nullable SimpleField simpleField, ByteBuf output) {
        if (simpleField == null || simpleField.value() == null) {
            return;
        }
        final PrependLengthFieldType prependLengthFieldType = simpleField.prependLengthFieldType();
        final int prependLengthFieldTypeByteCounts = prependLengthFieldType.getByteCounts();
        if (prependLengthFieldTypeByteCounts <= 0) {
            this.doEncodeField(context, output, simpleField);
        } else {
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeField(context, output, simpleField);

            final int afterEncode = output.writerIndex();
            final int byteCounts = afterEncode - beforeEncode;

            output.writerIndex(lengthFieldWriterIndex);
            // 写入长度字段
            prependLengthFieldType.writeTo(output, byteCounts);
            output.writerIndex(afterEncode);
        }
    }

    private void doEncodeField(FieldCodec.SerializeContext context, ByteBuf output, SimpleField simpleField) {
        switch (simpleField) {
            case SimpleField.I8 i8 -> output.writeByte(i8.value());
            case SimpleField.U8 u8 -> output.writeByte(u8.value());
            case SimpleField.I16 i16 -> output.writeShort(i16.value());
            case SimpleField.U16 u16 -> output.writeShort(u16.value());
            case SimpleField.I32 i32 -> output.writeInt(i32.value());
            case SimpleField.U32 u32 -> output.writeInt(u32.value().intValue());
            case SimpleField.I64 i64 -> output.writeLong(i64.value());
            case SimpleField.F32 f32 -> output.writeFloat(f32.value());
            case SimpleField.F64 f64 -> output.writeDouble(f64.value());
            case SimpleField.StrBcd8421(String value, var ignored) -> BcdOps.encodeBcd8421StringIntoByteBuf(value, output);
            case SimpleField.StrHex(String value, var ignored) -> XtreamBytes.writeHexString(output, value);
            case SimpleField.StrGbk strGbk -> encodeString(output, strGbk.value(), XtreamConstants.CHARSET_GBK);
            case SimpleField.StrGb2312 strGb2312 -> encodeString(output, strGb2312.value(), XtreamConstants.CHARSET_GB_2312);
            case SimpleField.StrUtf8 strUtf8 -> encodeString(output, strUtf8.value(), XtreamConstants.CHARSET_UTF8);
            case SimpleField.Str(String value, String charset, var ignored) -> encodeString(output, value, Charset.forName(charset));
            case SimpleField.ByteSequence byteSequence -> output.writeBytes(byteSequence.value());
            case SimpleField.Struct struct -> {
                final List<SimpleField> value = struct.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case SimpleField.Sequence sequence -> {
                final List<SimpleField> value = sequence.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encode(newContext, value, output);
            }
            case SimpleField.Dict<?> simpleMap -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, simpleMap);
                final Map<? extends SimpleField.DictKey, SimpleField> map = simpleMap.value();
                final ByteBuf temp = context.bufferFactory().buffer();
                try {
                    for (Map.Entry<? extends SimpleField.DictKey, SimpleField> entry : map.entrySet()) {
                        try {
                            // 1. key
                            final SimpleField.DictKey key = entry.getKey();
                            this.doEncodeField(newContext, output, key);
                            // 2. value
                            final SimpleField value = entry.getValue();
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
            case SimpleField.CustomSimpleField customSimpleField -> customSimpleField.writeTo(output);
        }
    }

    public void encodeWithTracker(FieldCodec.SerializeContext context, Iterable<? extends @Nullable SimpleField> simpleFields, ByteBuf output) {
        for (final SimpleField simpleField : simpleFields) {
            if (simpleField == null) {
                continue;
            }
            this.encodeWithTracker(context, simpleField, output);
        }
    }

    public void encodeWithTracker(FieldCodec.SerializeContext context, @Nullable SimpleField simpleField, ByteBuf output) {
        if (simpleField == null || simpleField.value() == null) {
            return;
        }
        final PrependLengthFieldType prependLengthFieldType = simpleField.prependLengthFieldType();
        final int prependLengthFieldTypeByteCounts = prependLengthFieldType.getByteCounts();
        if (prependLengthFieldTypeByteCounts <= 0) {
            this.doEncodeFieldWithTracker(context, output, simpleField);
        } else {
            @SuppressWarnings("Duplicated") final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
            final PrependLengthFieldSpan prependLengthFieldSpan = codecTracker.addPrependLengthFieldSpan(
                    codecTracker.getCurrentSpan(), "prependLengthField", null, null, prependLengthFieldType.name(), "前置长度字段"
            );
            final int lengthFieldWriterIndex = output.writerIndex();
            // 写入长度字段占位符
            prependLengthFieldType.writeTo(output, 0);
            final int beforeEncode = output.writerIndex();

            this.doEncodeFieldWithTracker(context, output, simpleField);

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

    private void doEncodeFieldWithTracker(FieldCodec.SerializeContext context, ByteBuf output, SimpleField simpleField) {
        final CodecTracker codecTracker = Objects.requireNonNull(context.codecTracker());
        final int indexBeforeWrite = output.writerIndex();
        switch (simpleField) {
            case SimpleField.I8 i8 -> output.writeByte(i8.value());
            case SimpleField.U8 u8 -> output.writeByte(u8.value());
            case SimpleField.I16 i16 -> output.writeShort(i16.value());
            case SimpleField.U16 u16 -> output.writeShort(u16.value());
            case SimpleField.I32 i32 -> output.writeInt(i32.value());
            case SimpleField.U32 u32 -> output.writeInt(u32.value().intValue());
            case SimpleField.I64 i64 -> output.writeLong(i64.value());
            case SimpleField.F32 f32 -> output.writeFloat(f32.value());
            case SimpleField.F64 f64 -> output.writeDouble(f64.value());
            case SimpleField.StrBcd8421(String value, var ignored) -> BcdOps.encodeBcd8421StringIntoByteBuf(value, output);
            case SimpleField.StrHex(String value, var ignored) -> XtreamBytes.writeHexString(output, value);
            case SimpleField.StrGbk strGbk -> encodeString(output, strGbk.value(), XtreamConstants.CHARSET_GBK);
            case SimpleField.StrGb2312 strGb2312 -> encodeString(output, strGb2312.value(), XtreamConstants.CHARSET_GB_2312);
            case SimpleField.StrUtf8 strUtf8 -> encodeString(output, strUtf8.value(), XtreamConstants.CHARSET_UTF8);
            case SimpleField.Str(String value, String charset, var ignored) -> encodeString(output, value, Charset.forName(charset));
            case SimpleField.ByteSequence byteSequence -> output.writeBytes(byteSequence.value());
            case SimpleField.Struct struct -> {
                final List<SimpleField> value = struct.value();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                final NestedFieldSpan nestedFieldSpan = codecTracker.startNewNestedFieldSpan(simpleField.name(), "", simpleField.type(), this.getClass().getSimpleName());
                this.encodeWithTracker(newContext, value, output);
                nestedFieldSpan.setHexString(FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }
            case SimpleField.Sequence sequence -> {
                final List<SimpleField> value = sequence.value();
                final CollectionFieldSpan collectionFieldSpan = codecTracker.startNewCollectionFieldSpanForSimpleField(simpleField.name());
                final int parentIndexBeforeWrite = output.writerIndex();
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, value);
                this.encodeWithTracker(newContext, value, output);
                collectionFieldSpan.setHexString(FormatUtils.toHexString(output, parentIndexBeforeWrite, output.writerIndex() - parentIndexBeforeWrite));
                codecTracker.finishCurrentSpan();
            }
            case SimpleField.Dict<?> simpleMap -> {
                final DefaultSerializeContext newContext = new DefaultSerializeContext(context, simpleMap);
                final Map<? extends SimpleField.DictKey, SimpleField> map = simpleMap.value();
                final ByteBuf temp = context.bufferFactory().buffer();
                final MapFieldSpan mapFieldSpan = codecTracker.startNewMapFieldSpan(simpleMap.name(), simpleField.type(), this.getClass().getSimpleName());
                final int parenIndexBeforeWrite = output.writerIndex();
                final BaseSpan parent = codecTracker.getCurrentSpan();
                int sequence = 0;
                try {
                    for (Map.Entry<? extends SimpleField.DictKey, SimpleField> entry : map.entrySet()) {
                        try {
                            // 1. key
                            final SimpleField.DictKey key = entry.getKey();
                            final MapEntrySpan mapEntrySpan = codecTracker.startNewMapEntrySpan(parent, key.name(), sequence++);
                            final int writerIndex = output.writerIndex();
                            codecTracker.updateTrackerHints(MapEntryItemSpan.Type.KEY);
                            this.doEncodeFieldWithTracker(newContext, output, key);
                            // 2. value
                            final SimpleField value = entry.getValue();
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
            case SimpleField.CustomSimpleField customSimpleField -> customSimpleField.writeTo(output);
        }
        if (!(simpleField instanceof SimpleField.Struct)
                && !(simpleField instanceof SimpleField.Dict<?>)
                && !(simpleField instanceof SimpleField.Sequence)) {
            final String hexString = FormatUtils.toHexString(output, indexBeforeWrite, output.writerIndex() - indexBeforeWrite);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), simpleField.name(), simpleField.value(), hexString, this.getClass().getSimpleName(), simpleField.getClass().getSimpleName());
        }
    }

    private static void encodeString(ByteBuf output, String value, Charset charset) {
        output.writeCharSequence(value, charset);
    }

}

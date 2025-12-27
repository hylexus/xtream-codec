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

package io.github.hylexus.xtream.codec.core.annotation.ext;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.netty.buffer.ByteBuf;

public enum LengthFieldType {
    i8(XtreamDataType.i8),
    u8(XtreamDataType.u8),
    i16(XtreamDataType.i16),
    u16(XtreamDataType.u16),
    i32(XtreamDataType.i32),
    u32(XtreamDataType.u32),
    i64(XtreamDataType.i64),
    ;

    private final XtreamDataType dataType;

    LengthFieldType(XtreamDataType dataType) {
        this.dataType = dataType;
    }

    public void writeToWithTracker(ByteBuf output, long value, CodecTracker codecTracker, String fieldName) {
        final int writerIndex = output.writerIndex();
        this.writeTo(output, value);
        final String hexString = FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex);
        codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), fieldName, value, hexString, this.getDeclaringClass().getSimpleName(), "");
    }

    public void writeTo(ByteBuf output, long value) {
        switch (this) {
            case i8, u8 -> output.writeByte((int) value);
            case i16, u16 -> output.writeShort((int) value);
            case i32, u32 -> output.writeInt((int) value);
            case i64 -> output.writeLong(value);
            default -> throw new IllegalArgumentException("Unsupported value length type: " + this);
        }
    }

    public Number readFromWithTracker(ByteBuf input, CodecTracker codecTracker, String fieldName) {
        final int readerIndex = input.readerIndex();
        final Number number = this.readFrom(input);
        final String hexString = FormatUtils.toHexString(input, readerIndex, input.readerIndex() - readerIndex);
        codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), fieldName, number, hexString, LengthFieldType.class.getSimpleName(), "");
        return number;
    }

    public Number readFrom(ByteBuf input) {
        return switch (this) {
            case i8 -> input.readByte();
            case u8 -> input.readUnsignedByte();
            case i16 -> input.readShort();
            case u16 -> input.readUnsignedShort();
            case i32 -> input.readInt();
            case u32 -> input.readUnsignedInt();
            case i64 -> input.readLong();
        };
    }

    public XtreamDataType type() {
        return dataType;
    }

}

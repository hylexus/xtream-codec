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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;

import static java.util.Objects.requireNonNullElse;

/**
 * TLV（Tag-Length-Value）结构
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@NullMarked
@ApiStatus.Experimental
public interface TLV {

    static TLV of(Tag tag, LengthType length, Object value) {
        return new TLVImpl(tag, new LengthImpl(length), value);
    }

    Tag tag();

    Length length();

    Object value();

    sealed interface Tag
            permits
            I8Tag, U8Tag,
            I16Tag, U16Tag,
            I32Tag, U32Tag,
            I64Tag,
            StringTag, Bcd8421Tag, HexStringTag {

        Object value();
    }

    sealed interface Length permits LengthImpl {
        long value();

        LengthType type();
    }

    record U8Tag(Short value) implements Tag {
    }

    record I8Tag(Byte value) implements Tag {
    }

    record U16Tag(Integer value) implements Tag {
    }

    record I16Tag(Short value) implements Tag {
    }

    record U32Tag(Long value) implements Tag {
    }

    record I32Tag(Integer value) implements Tag {
    }

    record I64Tag(Long value) implements Tag {
    }

    record StringTag(String value, Charset charset, PaddingConfig paddingConfig) implements Tag {

        public StringTag(String value, Charset charset, @Nullable PaddingConfig paddingConfig) {
            this.value = value;
            this.charset = charset;
            this.paddingConfig = requireNonNullElse(paddingConfig, PaddingConfig.none());
        }
    }

    record Bcd8421Tag(String value, PaddingConfig paddingConfig) implements Tag {
        public Bcd8421Tag(String value, @Nullable PaddingConfig paddingConfig) {
            this.value = value;
            this.paddingConfig = requireNonNullElse(paddingConfig, PaddingConfig.none());
        }
    }

    record HexStringTag(String value, PaddingConfig paddingConfig) implements Tag {
        public HexStringTag(String value, @Nullable PaddingConfig paddingConfig) {
            this.value = value;
            this.paddingConfig = requireNonNullElse(paddingConfig, PaddingConfig.none());
        }
    }

    enum LengthType {
        i8, u8, i16, u16, i32, u32, i64;

        public void writeToWithTracker(ByteBuf output, long value, CodecTracker codecTracker) {
            // noinspection Duplicates
            final int writerIndex = output.writerIndex();
            this.writeTo(output, value);
            final String hexString = FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), "length", value, hexString, this.getDeclaringClass().getSimpleName(), "");
        }

        public void writeTo(ByteBuf output, long value) {
            switch (this) {
                case i8, u8 -> output.writeByte((int) value);
                case i16, u16 -> output.writeShort((int) value);
                case i32, u32 -> output.writeInt((int) value);
                case i64 -> output.writeLong(value);
                default -> throw new IllegalArgumentException("Unsupported length type: " + this);
            }
        }
    }

    final class LengthImpl implements Length {
        private final LengthType type;
        private long value;

        public LengthImpl(LengthType type) {
            this.type = type;
        }

        @Override
        public long value() {
            return this.value;
        }

        public void value(long value) {
            this.value = value;
        }

        @Override
        public LengthType type() {
            return this.type;
        }
    }

    record TLVImpl(Tag tag, Length length, Object value) implements TLV {
    }
}

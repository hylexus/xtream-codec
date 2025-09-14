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
import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.type.wrapper.*;
import io.netty.buffer.ByteBuf;

public final class DataWrapperFieldCodes {
    public static final DataWrapperFieldCodec INSTANCE = new DataWrapperFieldCodec();

    public static final DwordWrapperFieldCodec INSTANCE_STRING_UTF_8 = new DwordWrapperFieldCodec();
    public static final StringWrapperGbkFieldCodec INSTANCE_STRING_GBK = new StringWrapperGbkFieldCodec();
    public static final StringWrapperBcd8421FieldCodec INSTANCE_STRING_BCD_8421 = new StringWrapperBcd8421FieldCodec();

    public static final I8WrapperFieldCodec INSTANCE_I8 = new I8WrapperFieldCodec();
    public static final U8WrapperFieldCodec INSTANCE_U8 = new U8WrapperFieldCodec();
    public static final I16WrapperFieldCodec INSTANCE_I16 = new I16WrapperFieldCodec();
    public static final U16WrapperFieldCodec INSTANCE_U16 = new U16WrapperFieldCodec();
    public static final I32WrapperFieldCodec INSTANCE_I32 = new I32WrapperFieldCodec();
    public static final U32WrapperFieldCodec INSTANCE_U32 = new U32WrapperFieldCodec();

    public static final DwordWrapperFieldCodec INSTANCE_DWORD = new DwordWrapperFieldCodec();
    public static final WordWrapperFieldCodec INSTANCE_WORD = new WordWrapperFieldCodec();

    private DataWrapperFieldCodes() {
        throw new UnsupportedOperationException();
    }

    public static final class I8WrapperFieldCodec extends BaseDataWrapperFieldCodec<I8Wrapper> {
        private I8WrapperFieldCodec() {
        }

        @Override
        public I8Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final byte value = input.readByte();
            return new I8Wrapper(value);
        }
    }

    public static final class U8WrapperFieldCodec extends BaseDataWrapperFieldCodec<U8Wrapper> {
        private U8WrapperFieldCodec() {
        }

        @Override
        public U8Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short value = input.readUnsignedByte();
            return new U8Wrapper(value);
        }
    }

    public static final class I16WrapperFieldCodec extends BaseDataWrapperFieldCodec<I16Wrapper> {
        private I16WrapperFieldCodec() {
        }

        @Override
        public I16Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short value = input.readShort();
            return new I16Wrapper(value);
        }
    }

    public static final class U16WrapperFieldCodec extends BaseDataWrapperFieldCodec<U16Wrapper> {
        private U16WrapperFieldCodec() {
        }

        @Override
        public U16Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final int value = input.readUnsignedShort();
            return new U16Wrapper(value);
        }
    }

    public static final class I32WrapperFieldCodec extends BaseDataWrapperFieldCodec<I32Wrapper> {
        private I32WrapperFieldCodec() {
        }

        @Override
        public I32Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final int value = input.readInt();
            return new I32Wrapper(value);
        }
    }

    public static final class U32WrapperFieldCodec extends BaseDataWrapperFieldCodec<U32Wrapper> {
        private U32WrapperFieldCodec() {
        }

        @Override
        public U32Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final long value = input.readUnsignedInt();
            return new U32Wrapper(value);
        }
    }

    public static final class StringWrapperBcd8421FieldCodec extends BaseDataWrapperFieldCodec<StringWrapperBcd> {
        private StringWrapperBcd8421FieldCodec() {
        }

        @Override
        public StringWrapperBcd deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final String value = BcdOps.decodeBcd8421AsString(input, length);
            return new StringWrapperBcd(value);
        }
    }

    public static final class StringWrapperGbkFieldCodec extends BaseDataWrapperFieldCodec<StringWrapperGbk> {
        private StringWrapperGbkFieldCodec() {
        }

        @Override
        public StringWrapperGbk deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final CharSequence value = input.readCharSequence(length, XtreamConstants.CHARSET_GBK);
            return new StringWrapperGbk(value.toString());
        }
    }

    public static final class StringWrapperUtf8FieldCodec extends BaseDataWrapperFieldCodec<StringWrapperUtf8> {
        private StringWrapperUtf8FieldCodec() {
        }

        @Override
        public StringWrapperUtf8 deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final CharSequence value = input.readCharSequence(length, XtreamConstants.CHARSET_UTF8);
            return new StringWrapperUtf8(value.toString());
        }
    }

    public static final class WordWrapperFieldCodec extends BaseDataWrapperFieldCodec<WordWrapper> {
        private WordWrapperFieldCodec() {
        }

        @Override
        public WordWrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final int value = input.readUnsignedShort();
            return new WordWrapper(value);
        }
    }

    public static final class DwordWrapperFieldCodec extends BaseDataWrapperFieldCodec<DwordWrapper> {
        private DwordWrapperFieldCodec() {
        }

        @Override
        public DwordWrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final long value = input.readUnsignedInt();
            return new DwordWrapper(value);
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class DataWrapperFieldCodec extends AbstractFieldCodec<DataWrapper> {
        private DataWrapperFieldCodec() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, DataWrapper value) {
            value.writeTo(output);
        }

        @Override
        public DataWrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final byte[] bytes = XtreamBytes.readBytes(input, length);
            return new BytesDataWrapper(bytes);
        }
    }

    public abstract static class BaseDataWrapperFieldCodec<T extends DataWrapper<?>> implements FieldCodec<T> {

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, T value) {
            if (value != null) {
                value.writeTo(output);
            }
        }

    }
}

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
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.ByteBufContainer;
import io.netty.buffer.ByteBuf;

public class BytesFieldCodecs {
    public static BytesFieldCodecByteBuf INSTANCE_BYTE_BUF = new BytesFieldCodecByteBuf();
    public static BytesFieldCodecByteBufContainer INSTANCE_BYTE_BUF_CONTAINER = new BytesFieldCodecByteBufContainer();
    public static ByteeFieldCodecByteArray INSTANCE_BYTE_ARRAY = new ByteeFieldCodecByteArray();
    public static BytesFieldCodecByteArrayBoxed INSTANCE_BYTE_ARRAY_BOXED = new BytesFieldCodecByteArrayBoxed();
    public static BytesFieldCodecByteArrayContainer INSTANCE_BYTE_ARRAY_CONTAINER = new BytesFieldCodecByteArrayContainer();

    private BytesFieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class ByteeFieldCodecByteArray extends AbstractFieldCodec<byte[]> {
        public static ByteeFieldCodecByteArray INSTANCE = new ByteeFieldCodecByteArray();

        private ByteeFieldCodecByteArray() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, byte[] value) {
            output.writeBytes(value);
        }

        @Override
        public byte[] deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            if (length < 0) {
                // all remaining
                return XtreamBytes.readBytes(input, input.readableBytes());
            }
            return XtreamBytes.readBytes(input, length);
        }
    }

    public static final class BytesFieldCodecByteArrayBoxed extends AbstractFieldCodec<Byte[]> {
        public static final BytesFieldCodecByteArrayBoxed INSTANCE = new BytesFieldCodecByteArrayBoxed();

        private BytesFieldCodecByteArrayBoxed() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Byte[] value) {
            for (final Byte b : value) {
                output.writeByte(b);
            }
        }

        @Override
        public Byte[] deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final byte[] bytes = XtreamBytes.readBytes(input, length);
            final Byte[] result = new Byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = bytes[i];
            }
            return result;
        }
    }

    public static final class BytesFieldCodecByteBufContainer extends AbstractFieldCodec<ByteBufContainer> {
        private BytesFieldCodecByteBufContainer() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, ByteBufContainer value) {
            value.writeTo(output);
        }

        @Override
        public ByteBufContainer deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            // 这里没有 retain: 随着 input.release() 一起释放掉
            final ByteBuf content = input.readSlice(length);
            return ByteBufContainer.ofBytes(content);
        }
    }

    public static final class BytesFieldCodecByteBuf extends AbstractFieldCodec<ByteBuf> {
        private BytesFieldCodecByteBuf() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, ByteBuf value) {
            output.writeBytes(value);
        }

        @Override
        public ByteBuf deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            if (length < 0) {
                // all remaining
                return input;
            }
            return input.slice(input.readerIndex(), length);
        }
    }

    public static final class BytesFieldCodecByteArrayContainer extends AbstractFieldCodec<ByteArrayContainer> {
        private BytesFieldCodecByteArrayContainer() {
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, ByteArrayContainer value) {
            value.writeTo(output);
        }

        @Override
        public ByteArrayContainer deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            // 这里必须立即读取: 不要返回Lambda(Lazy)
            final byte[] bytes = XtreamBytes.readBytes(input, length);
            return ByteArrayContainer.ofBytes(bytes);
        }
    }
}

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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.ByteBufContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BytesFieldCodecsTest {

    private final BeanPropertyMetadata metadata = mock(BeanPropertyMetadata.class);
    private final FieldCodec.SerializeContext serializeContext = mock(FieldCodec.SerializeContext.class);
    private final FieldCodec.DeserializeContext deserializeContext = mock(FieldCodec.DeserializeContext.class);

    @Test
    void shouldReadFixedLengthOrAllRemainingByteArray() {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{99, 1, 2, 3});
        try {
            buffer.readByte();
            assertArrayEquals(new byte[]{1, 2}, BytesFieldCodecs.INSTANCE_BYTE_ARRAY.deserialize(metadata, deserializeContext, buffer, 2));
            assertEquals(3, buffer.readerIndex());
            assertArrayEquals(new byte[]{3}, BytesFieldCodecs.INSTANCE_BYTE_ARRAY.deserialize(metadata, deserializeContext, buffer, -1));
            assertFalse(buffer.isReadable());
            buffer.clear();
            BytesFieldCodecs.INSTANCE_BYTE_ARRAY.serialize(metadata, serializeContext, buffer, new byte[]{1, 2});
            BytesFieldCodecs.INSTANCE_BYTE_ARRAY.serialize(metadata, serializeContext, buffer, null);
            assertEquals("0102", ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldEncodeAndDecodeBoxedBytesIncludingEmptyArray() {
        final ByteBuf buffer = Unpooled.buffer();
        try {
            BytesFieldCodecs.INSTANCE_BYTE_ARRAY_BOXED.serialize(metadata, serializeContext, buffer, new Byte[]{-1, 0, 127});
            assertEquals("ff007f", ByteBufUtil.hexDump(buffer));
            assertArrayEquals(new Byte[]{-1, 0, 127}, BytesFieldCodecs.INSTANCE_BYTE_ARRAY_BOXED.deserialize(metadata, deserializeContext, buffer, 3));
            assertArrayEquals(new Byte[0], BytesFieldCodecs.INSTANCE_BYTE_ARRAY_BOXED.deserialize(metadata, deserializeContext, buffer, 0));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldReturnSharedSliceWithoutConsumingByteBuf() {
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{99, 1, 2, 3});
        final ByteBuf output = Unpooled.buffer();
        try {
            input.readByte();
            final ByteBuf slice = BytesFieldCodecs.INSTANCE_BYTE_BUF.deserialize(metadata, deserializeContext, input, 2);
            assertEquals("0102", ByteBufUtil.hexDump(slice));
            assertEquals(1, input.readerIndex());
            assertEquals(1, input.refCnt());
            slice.setByte(0, 7);
            assertEquals(7, input.getByte(1));
            assertSame(input, BytesFieldCodecs.INSTANCE_BYTE_BUF.deserialize(metadata, deserializeContext, input, -1));
            BytesFieldCodecs.INSTANCE_BYTE_BUF.serialize(metadata, serializeContext, output, slice);
            assertEquals("0702", ByteBufUtil.hexDump(output));
            assertEquals(2, slice.readerIndex());
        } finally {
            input.release();
            output.release();
        }
    }

    @Test
    void shouldDecodeByteArrayContainerAsIndependentCopy() {
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        final ByteBuf output = Unpooled.buffer();
        try {
            final ByteArrayContainer container = BytesFieldCodecs.INSTANCE_BYTE_ARRAY_CONTAINER.deserialize(metadata, deserializeContext, input, 2);
            input.setByte(0, 9);
            assertEquals(2, input.readerIndex());
            assertArrayEquals(new byte[]{1, 2}, container.asBytes());
            BytesFieldCodecs.INSTANCE_BYTE_ARRAY_CONTAINER.serialize(metadata, serializeContext, output, container);
            assertEquals("0102", ByteBufUtil.hexDump(output));
        } finally {
            input.release();
            output.release();
        }
    }

    @Test
    void shouldDecodeByteBufContainerAsSharedSliceAndConsumeInput() {
        final ByteBuf input = Unpooled.wrappedBuffer(new byte[]{99, 1, 2, 3});
        final ByteBuf output = Unpooled.buffer();
        try {
            input.readByte();
            final ByteBufContainer container = BytesFieldCodecs.INSTANCE_BYTE_BUF_CONTAINER.deserialize(metadata, deserializeContext, input, 2);
            assertEquals(3, input.readerIndex());
            assertEquals(1, input.refCnt());
            input.setByte(1, 7);
            assertArrayEquals(new byte[]{7, 2}, container.asBytes());
            BytesFieldCodecs.INSTANCE_BYTE_BUF_CONTAINER.serialize(metadata, serializeContext, output, container);
            assertEquals("0702", ByteBufUtil.hexDump(output));
        } finally {
            input.release();
            output.release();
        }
    }
}

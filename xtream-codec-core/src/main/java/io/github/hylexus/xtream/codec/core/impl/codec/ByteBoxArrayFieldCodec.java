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
import io.netty.buffer.ByteBuf;

/**
 * @deprecated Use {@link BytesFieldCodecs.BytesFieldCodecByteArrayBoxed} instead.
 */
@Deprecated
public class ByteBoxArrayFieldCodec extends AbstractFieldCodec<Byte[]> {
    /**
     * @deprecated Use {@link BytesFieldCodecs#INSTANCE_BYTE_ARRAY_BOXED} instead.
     */
    @Deprecated
    public static final ByteBoxArrayFieldCodec INSTANCE = new ByteBoxArrayFieldCodec();

    private ByteBoxArrayFieldCodec() {
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

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
import io.netty.buffer.ByteBuf;

/**
 * @deprecated Use {@link BytesFieldCodecs.BytesFieldCodecByteArrayContainer}
 */
@Deprecated
public class ByteArrayContainerFieldCodec extends AbstractFieldCodec<ByteArrayContainer> {
    /**
     * @deprecated Use {@link BytesFieldCodecs#INSTANCE_BYTE_ARRAY_CONTAINER}
     */
    @Deprecated
    public static final ByteArrayContainerFieldCodec INSTANCE = new ByteArrayContainerFieldCodec();

    private ByteArrayContainerFieldCodec() {
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

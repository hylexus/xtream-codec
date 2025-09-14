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

package io.github.hylexus.xtream.codec.core.impl.codec.wrapper;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.impl.codec.DataWrapperFieldCodes;
import io.github.hylexus.xtream.codec.core.type.wrapper.U8Wrapper;
import io.netty.buffer.ByteBuf;

/**
 * @deprecated Use {@link DataWrapperFieldCodes.U8WrapperFieldCodec} instead.
 */
@Deprecated(forRemoval = true, since = "0.1.0")
@SuppressWarnings("removal")
public class U8WrapperFieldCodec extends BaseDataWrapperFieldCodec<U8Wrapper> {
    /**
     * @deprecated Use {@link DataWrapperFieldCodes#INSTANCE_U8} instead.
     */
    @Deprecated
    public static final U8WrapperFieldCodec INSTANCE = new U8WrapperFieldCodec();

    private U8WrapperFieldCodec() {
    }

    @Override
    public U8Wrapper deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final short value = input.readUnsignedByte();
        return new U8Wrapper(value);
    }

}

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
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.netty.buffer.ByteBuf;

public class U32FieldCodec extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
    public static final U32FieldCodec INSTANCE = new U32FieldCodec();

    private U32FieldCodec() {
    }

    @Override
    public Long deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        return input.readUnsignedInt();
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
        output.writeInt(value.intValue());
    }

    @Override
    public Class<?> underlyingJavaType() {
        return Long.class;
    }

    @Override
    public NumberSignedness signedness() {
        return NumberSignedness.UNSIGNED;
    }


}

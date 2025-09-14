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
import io.netty.buffer.ByteBuf;

/**
 * @deprecated Use {@link F32FieldCodecs.F32FieldCodec} instead.
 */
@Deprecated(forRemoval = true, since = "0.1.0")
public class F32FieldCodec extends AbstractFieldCodec<Number> {
    /**
     * @deprecated Use {@link F32FieldCodecs#FLOAT_INSTANCE} instead.
     */
    @Deprecated
    public static final F32FieldCodec INSTANCE = new F32FieldCodec();

    private F32FieldCodec() {
    }

    @Override
    public Float deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        return input.readFloat();
    }


    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
        output.writeFloat(value.floatValue());
    }
}

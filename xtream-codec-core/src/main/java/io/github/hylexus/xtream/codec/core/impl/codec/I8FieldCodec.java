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

import java.util.function.Function;

/**
 * @deprecated Use {@link I8FieldCodecs.I8FieldCodec} instead. Will be removed in 1.0.0.
 */
@Deprecated(forRemoval = true, since = "0.1.0")
public class I8FieldCodec extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
    /**
     *
     * @deprecated Use {@link I8FieldCodecs#BYTE_INSTANCE} instead.
     */
    @Deprecated
    public static final I8FieldCodec INSTANCE = new I8FieldCodec(Byte.class, Function.identity());

    private final Class<?> targetType;
    private final Function<Byte, ? extends Number> converter;

    private I8FieldCodec(Class<?> targetType, Function<Byte, ? extends Number> converter) {
        this.targetType = targetType;
        this.converter = converter;
    }

    @Override
    public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final byte value = input.readByte();
        return this.converter.apply(value);
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
        output.writeByte(value.intValue());
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.targetType;
    }

    @Override
    public NumberSignedness signedness() {
        return NumberSignedness.SIGNED;
    }

}

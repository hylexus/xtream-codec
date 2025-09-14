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
 * @deprecated Use {@link U16FieldCodecs.U16FieldCodecLittleEndian} instead.
 */
@Deprecated(forRemoval = true, since = "0.1.0")
public class U16FieldCodecLittleEndian extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
    /**
     * @deprecated Use {@link U16FieldCodecs#INTEGER_INSTANCE_LE} instead.
     */
    @Deprecated
    public static final U16FieldCodecLittleEndian INSTANCE = new U16FieldCodecLittleEndian(Integer.class, Function.identity());

    private final Class<?> targetType;
    private final Function<Integer, ? extends Number> converter;

    private U16FieldCodecLittleEndian(Class<?> targetType, Function<Integer, ? extends Number> converter) {
        this.targetType = targetType;
        this.converter = converter;
    }

    @Override
    public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final int value = input.readUnsignedShortLE();
        return this.converter.apply(value);
    }

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
        output.writeShortLE(value.intValue());
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.targetType;
    }

    @Override
    public NumberSignedness signedness() {
        return NumberSignedness.UNSIGNED;
    }


}

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

public class U8FieldCodecs {
    public static final U8FieldCodec SHORT_INSTANCE = new U8FieldCodec();
    public static final U8ToIntegerFieldCodec INTEGER_INSTANCE = new U8ToIntegerFieldCodec();
    public static final U8ToLongFieldCodec LONG_INSTANCE = new U8ToLongFieldCodec();

    private U8FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class U8FieldCodec extends BaseU8FieldCodec<Short> {
        private U8FieldCodec() {
            super(Short.class, Function.identity());
        }
    }

    public static final class U8ToIntegerFieldCodec extends BaseU8FieldCodec<Integer> {
        private U8ToIntegerFieldCodec() {
            super(Integer.class, Short::intValue);
        }
    }

    public static final class U8ToLongFieldCodec extends BaseU8FieldCodec<Long> {
        private U8ToLongFieldCodec() {
            super(Long.class, Short::longValue);
        }
    }

    private static class BaseU8FieldCodec<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<T> targetType;
        private final Function<Short, T> converter;

        private BaseU8FieldCodec(Class<T> targetType, Function<Short, T> converter) {
            this.targetType = targetType;
            this.converter = converter;
        }

        @Override
        public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short value = input.readUnsignedByte();
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
            return NumberSignedness.UNSIGNED;
        }
    }

}

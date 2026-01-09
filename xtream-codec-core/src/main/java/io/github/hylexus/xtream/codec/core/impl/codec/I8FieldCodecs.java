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

public class I8FieldCodecs {
    public static final I8FieldCodec BYTE_INSTANCE = new I8FieldCodec();
    public static final I8ToShortFieldCodec SHORT_INSTANCE = new I8ToShortFieldCodec();
    public static final I8ToIntegerFieldCodec INTEGER_INSTANCE = new I8ToIntegerFieldCodec();
    public static final I8ToLongFieldCodec LONG_INSTANCE = new I8ToLongFieldCodec();

    private I8FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class I8FieldCodec extends BaseI8FieldCodec<Byte> {
        private I8FieldCodec() {
            super(Byte.class, Function.identity());
        }
    }

    public static final class I8ToShortFieldCodec extends BaseI8FieldCodec<Short> {
        private I8ToShortFieldCodec() {
            super(Short.class, b -> (short) b);
        }
    }

    public static final class I8ToIntegerFieldCodec extends BaseI8FieldCodec<Integer> {
        private I8ToIntegerFieldCodec() {
            super(Integer.class, b -> (int) b);
        }
    }

    public static final class I8ToLongFieldCodec extends BaseI8FieldCodec<Long> {
        private I8ToLongFieldCodec() {
            super(Long.class, b -> (long) b);
        }
    }

    private static class BaseI8FieldCodec<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<?> targetType;
        private final Function<Byte, T> converter;

        private BaseI8FieldCodec(Class<?> targetType, Function<Byte, T> converter) {
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

}

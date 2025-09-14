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

public class U16FieldCodecs {
    private U16FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final U16FieldCodec INTEGER_INSTANCE = new U16FieldCodec();
    public static final U16ToLongFieldCodec LONG_INSTANCE = new U16ToLongFieldCodec();
    public static final U16FieldCodecLittleEndian INTEGER_INSTANCE_LE = new U16FieldCodecLittleEndian();
    public static final U16ToLongFieldCodecLittleEndian LONG_INSTANCE_LE = new U16ToLongFieldCodecLittleEndian();

    public static final class U16FieldCodec extends BaseU16FieldCodec<Integer> {
        private U16FieldCodec() {
            super(Integer.class, Function.identity());
        }
    }

    public static final class U16ToLongFieldCodec extends BaseU16FieldCodec<Long> {
        private U16ToLongFieldCodec() {
            super(Long.class, Integer::longValue);
        }
    }

    public static final class U16FieldCodecLittleEndian extends BaseU16FieldCodecLittleEndian<Integer> {
        private U16FieldCodecLittleEndian() {
            super(Integer.class, Function.identity());
        }
    }

    public static final class U16ToLongFieldCodecLittleEndian extends BaseU16FieldCodecLittleEndian<Long> {
        private U16ToLongFieldCodecLittleEndian() {
            super(Long.class, Integer::longValue);
        }
    }

    private static class BaseU16FieldCodec<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<T> targetType;
        private final Function<Integer, T> converter;

        private BaseU16FieldCodec(Class<T> targetType, Function<Integer, T> converter) {
            this.targetType = targetType;
            this.converter = converter;
        }

        @Override
        public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final int value = input.readUnsignedShort();
            return this.converter.apply(value);
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
            output.writeShort(value.intValue());
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

    private static class BaseU16FieldCodecLittleEndian<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<T> targetType;
        private final Function<Integer, T> converter;

        private BaseU16FieldCodecLittleEndian(Class<T> targetType, Function<Integer, T> converter) {
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

}

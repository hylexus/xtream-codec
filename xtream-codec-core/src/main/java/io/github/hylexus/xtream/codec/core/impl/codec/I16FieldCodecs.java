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

public class I16FieldCodecs {
    public static final I16FieldCodec SHORT_INSTANCE = new I16FieldCodec();
    public static final I16ToIntegerFieldCodec INTEGER_INSTANCE = new I16ToIntegerFieldCodec();
    public static final I16ToLongFieldCodec LONG_INSTANCE = new I16ToLongFieldCodec();
    public static final I16FieldCodecLittleEndian SHORT_INSTANCE_LE = new I16FieldCodecLittleEndian();
    public static final I16ToIntegerFieldCodecLittleEndian INTEGER_INSTANCE_LE = new I16ToIntegerFieldCodecLittleEndian();
    public static final I16ToLongFieldCodecLittleEndian LONG_INSTANCE_LE = new I16ToLongFieldCodecLittleEndian();

    private I16FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class I16FieldCodec extends BaseI16FieldCodec<Short> {
        private I16FieldCodec() {
            super(Short.class, Function.identity());
        }
    }

    public static final class I16ToIntegerFieldCodec extends BaseI16FieldCodec<Integer> {
        public I16ToIntegerFieldCodec() {
            super(Integer.class, Short::intValue);
        }
    }

    public static final class I16ToLongFieldCodec extends BaseI16FieldCodec<Long> {
        public I16ToLongFieldCodec() {
            super(Long.class, Short::longValue);
        }
    }

    public static final class I16FieldCodecLittleEndian extends BaseI16FieldCodecLittleEndian<Short> {
        private I16FieldCodecLittleEndian() {
            super(Short.class, Function.identity());
        }
    }

    public static final class I16ToIntegerFieldCodecLittleEndian extends BaseI16FieldCodecLittleEndian<Integer> {
        private I16ToIntegerFieldCodecLittleEndian() {
            super(Integer.class, Short::intValue);
        }
    }

    public static final class I16ToLongFieldCodecLittleEndian extends BaseI16FieldCodecLittleEndian<Long> {
        private I16ToLongFieldCodecLittleEndian() {
            super(Long.class, Short::longValue);
        }
    }

    private static class BaseI16FieldCodec<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<T> targetType;
        private final Function<Short, T> converter;

        private BaseI16FieldCodec(Class<T> targetType, Function<Short, T> converter) {
            this.targetType = targetType;
            this.converter = converter;
        }

        @Override
        public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short value = input.readShort();
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
            return NumberSignedness.SIGNED;
        }
    }

    private static class BaseI16FieldCodecLittleEndian<T extends Number> extends AbstractFieldCodec<Number> implements IntegralFieldCodec {
        private final Class<T> targetType;
        private final Function<Short, T> converter;

        private BaseI16FieldCodecLittleEndian(Class<T> targetType, Function<Short, T> converter) {
            this.targetType = targetType;
            this.converter = converter;
        }

        @Override
        public Number deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short value = input.readShortLE();
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
            return NumberSignedness.SIGNED;
        }
    }

}

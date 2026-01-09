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

public class F64FieldCodecs {
    public static final F64FieldCodec DOUBLE_INSTANCE = new F64FieldCodec();
    public static final F64FieldCodecLittleEndian DOUBLE_INSTANCE_LE = new F64FieldCodecLittleEndian();

    private F64FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class F64FieldCodec extends AbstractFieldCodec<Number> {
        private F64FieldCodec() {
        }

        @Override
        public Double deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readDouble();
        }

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
            output.writeDouble(value.doubleValue());
        }
    }

    public static final class F64FieldCodecLittleEndian extends AbstractFieldCodec<Number> {

        private F64FieldCodecLittleEndian() {
        }

        @Override
        public Double deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readDoubleLE();
        }


        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
            output.writeDoubleLE(value.doubleValue());
        }

        @Override
        public Class<?> underlyingJavaType() {
            return Double.class;
        }
    }

}

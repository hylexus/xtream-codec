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

public class F32FieldCodecs {
    public static final F32FieldCodec FLOAT_INSTANCE = new F32FieldCodec();
    public static final F32FieldCodecLittleEndian FLOAT_INSTANCE_LE = new F32FieldCodecLittleEndian();

    private F32FieldCodecs() {
        throw new UnsupportedOperationException();
    }

    public static final class F32FieldCodec extends AbstractFieldCodec<Number> {
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

    public static final class F32FieldCodecLittleEndian extends AbstractFieldCodec<Number> {
        private F32FieldCodecLittleEndian() {
        }

        @Override
        public Float deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return input.readFloatLE();
        }


        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Number value) {
            output.writeFloatLE(value.floatValue());
        }
    }

}

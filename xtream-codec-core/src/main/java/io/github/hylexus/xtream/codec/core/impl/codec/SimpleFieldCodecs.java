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
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.SimpleFieldEncoder;
import io.github.hylexus.xtream.codec.core.type.simple.SimpleField;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public final class SimpleFieldCodecs {

    public static final SimpleFieldCommonCodec INSTANCE = new SimpleFieldCommonCodec();

    private SimpleFieldCodecs() {
        throw new UnsupportedOperationException("No instance");
    }

    public static class SimpleFieldCommonCodec implements SimpleFieldCodec {
        private final SimpleFieldEncoder simpleFieldEncoder = new SimpleFieldEncoder();

        private SimpleFieldCommonCodec() {
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable SimpleField value) {
            if (value != null && value.value() != null) {
                this.simpleFieldEncoder.encode(context, value, output);
            }
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable SimpleField value) {
            if (value != null && value.value() != null) {
                this.simpleFieldEncoder.encodeWithTracker(context, value, output);
            }
        }
    }

    public interface SimpleFieldCodec extends FieldCodec<SimpleField> {

        @Override
        default @Nullable SimpleField deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException("Cannot deserialize: " + SimpleField.class.getName());
        }

    }

}

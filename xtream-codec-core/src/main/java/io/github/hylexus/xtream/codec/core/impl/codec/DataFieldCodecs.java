/*
 * Copyright 2024-present the original author or authors.
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
import io.github.hylexus.xtream.codec.core.DataFieldEncoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public final class DataFieldCodecs {

    public static final DataFieldCommonCodec INSTANCE = new DataFieldCommonCodec();

    private DataFieldCodecs() {
        throw new UnsupportedOperationException("No instance");
    }

    public static class DataFieldCommonCodec implements DataFieldCodec {
        private final DataFieldEncoder dataFieldEncoder = new DataFieldEncoder();

        private DataFieldCommonCodec() {
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable DataField value) {
            if (value != null && value.value() != null) {
                this.dataFieldEncoder.encode(context, value, output);
            }
        }

        @Override
        public void serializeWithTracker(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable DataField value) {
            if (value != null && value.value() != null) {
                this.dataFieldEncoder.encodeWithTracker(context, value, output);
            }
        }

        @Override
        public @Nullable DataField deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            return this.dataFieldEncoder.decode(context, input);
        }

    }

    public interface DataFieldCodec extends FieldCodec<DataField> {

        @Override
        default @Nullable DataField deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException("Cannot deserialize: " + DataField.class.getName());
        }

    }

}

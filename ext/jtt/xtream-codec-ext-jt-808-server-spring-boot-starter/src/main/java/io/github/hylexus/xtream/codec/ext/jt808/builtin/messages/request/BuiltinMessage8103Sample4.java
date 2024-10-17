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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BuiltinMessage8103Sample4 {

    @Preset.JtStyle.Byte
    private short parameterCount;

    @XtreamField(fieldCodec = CustomParameterListFieldCodec.class)
    private List<ParameterItem> parameterItemList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ParameterItem {

        // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.Dword 注解了
        private long parameterId;

        // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.Byte 注解了
        private short parameterLength;

        // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.RuntimeType 注解了
        private Object parameterValue;

        public ParameterItem() {
        }

        public ParameterItem(long parameterId, short parameterLength, Object parameterValue) {
            this.parameterId = parameterId;
            this.parameterLength = parameterLength;
            this.parameterValue = parameterValue;
        }
    }

    public static class CustomParameterListFieldCodec extends AbstractFieldCodec<List<ParameterItem>> {

        @SuppressWarnings("rawtypes")
        static final Map<Long, FieldCodec> VALUE_CODEC_MAPPING = Map.of(
                0x0001L, U32FieldCodec.INSTANCE,
                0x0040L, StringFieldCodec.createStringCodec("GBK"),
                0x0081L, U16FieldCodec.INSTANCE,
                0x0082L, U16FieldCodec.INSTANCE,
                0x0084L, U8FieldCodec.INSTANCE
        );

        @Override
        protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, List<ParameterItem> value) {
            for (final ParameterItem item : value) {
                if (item == null) {
                    continue;
                }
                this.serializeItem(propertyMetadata, context, output, item);
            }
        }

        private void serializeItem(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, ParameterItem item) {
            final long parameterId = item.getParameterId();
            @SuppressWarnings("unchecked") final FieldCodec<Object> valueCodec = VALUE_CODEC_MAPPING.get(parameterId);
            if (valueCodec == null) {
                throw new IllegalArgumentException("Unsupported parameterId: " + parameterId);
            }

            // 1. key:u32
            output.writeInt((int) parameterId);
            // 2. valueLength:u8
            output.writeByte(item.getParameterLength());
            // 3: value
            valueCodec.serialize(propertyMetadata, context, output, item.getParameterValue());
        }

        @Override
        public List<ParameterItem> deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final List<ParameterItem> list = new ArrayList<>();
            while (input.isReadable()) {
                // 1. key:u32
                final long parameterId = input.readUnsignedInt();
                // 2. valueLength:u8
                final short valueLength = input.readUnsignedByte();
                @SuppressWarnings("unchecked") final FieldCodec<Object> valueCodec = VALUE_CODEC_MAPPING.get(parameterId);
                if (valueCodec == null) {
                    throw new IllegalArgumentException("Unsupported parameterId: " + parameterId);
                }
                final Object value = valueCodec.deserialize(propertyMetadata, context, input, valueLength);
                final ParameterItem item = new ParameterItem()
                        .setParameterId(parameterId)
                        .setParameterLength(valueLength)
                        .setParameterValue(value);
                list.add(item);
            }
            return list;
        }
    }
}

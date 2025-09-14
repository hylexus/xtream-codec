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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hylexus
 */
public class CustomParameterListFieldCodec extends AbstractFieldCodec<List<ParameterItem>> {

    @SuppressWarnings("rawtypes")
    static final Map<Long, FieldCodec> VALUE_CODEC_MAPPING;
    private static final Logger log = LoggerFactory.getLogger(CustomParameterListFieldCodec.class);

    static {
        VALUE_CODEC_MAPPING = new HashMap<>();
        for (long parameterId = 0x0001L; parameterId <= 0x0007L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        for (long parameterId = 0x0010L; parameterId <= 0x0017L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, StringFieldCodecs.INSTANCE_GBK);
        }

        VALUE_CODEC_MAPPING.put(0x001AL, StringFieldCodecs.INSTANCE_GBK);
        VALUE_CODEC_MAPPING.put(0x001BL, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x001CL, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x001DL, StringFieldCodecs.INSTANCE_GBK);

        for (long parameterId = 0x0020L; parameterId <= 0x0022L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        for (long parameterId = 0x0023L; parameterId <= 0x0026L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, StringFieldCodecs.INSTANCE_GBK);
        }

        for (long parameterId = 0x0027L; parameterId <= 0x0029L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        for (long parameterId = 0x002CL; parameterId <= 0x0030L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        VALUE_CODEC_MAPPING.put(0x0031L, U16FieldCodecs.INTEGER_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0032L, StringFieldCodecs.INSTANCE_HEX);

        for (long parameterId = 0x0040L; parameterId <= 0x0044L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, StringFieldCodecs.INSTANCE_GBK);
        }

        for (long parameterId = 0x0045L; parameterId <= 0x0047L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        VALUE_CODEC_MAPPING.put(0x0048L, StringFieldCodecs.INSTANCE_GBK);
        VALUE_CODEC_MAPPING.put(0x0049L, StringFieldCodecs.INSTANCE_GBK);

        for (long parameterId = 0x0050L; parameterId <= 0x005AL; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }
        for (long parameterId = 0x005BL; parameterId <= 0x005EL; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U16FieldCodecs.INTEGER_INSTANCE);
        }
        VALUE_CODEC_MAPPING.put(0x0064L, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0065L, U32FieldCodecs.LONG_INSTANCE);

        for (long parameterId = 0x0070L; parameterId <= 0x0074L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U32FieldCodecs.LONG_INSTANCE);
        }

        // 0x0075 ~ 0x007F: 自定义
        for (long parameterId = 0x0075L; parameterId <= 0x007FL; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, StringFieldCodecs.INSTANCE_HEX);
        }

        VALUE_CODEC_MAPPING.put(0x0080L, U32FieldCodecs.LONG_INSTANCE);

        VALUE_CODEC_MAPPING.put(0x0081L, U16FieldCodecs.INTEGER_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0082L, U16FieldCodecs.INTEGER_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0083L, StringFieldCodecs.INSTANCE_GBK);
        VALUE_CODEC_MAPPING.put(0x0084L, U8FieldCodecs.SHORT_INSTANCE);

        for (long parameterId = 0x0090L; parameterId <= 0x0092L; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, U8FieldCodecs.SHORT_INSTANCE);
        }

        VALUE_CODEC_MAPPING.put(0x0093L, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0094L, U8FieldCodecs.SHORT_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0095L, U32FieldCodecs.LONG_INSTANCE);

        VALUE_CODEC_MAPPING.put(0x0100L, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0101L, U16FieldCodecs.INTEGER_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0102L, U32FieldCodecs.LONG_INSTANCE);
        VALUE_CODEC_MAPPING.put(0x0103L, U16FieldCodecs.INTEGER_INSTANCE);
        // byte[8]
        VALUE_CODEC_MAPPING.put(0x0110L, StringFieldCodecs.INSTANCE_HEX);

        // byte[8]
        for (long parameterId = 0x0111L; parameterId <= 0x01FFL; parameterId++) {
            VALUE_CODEC_MAPPING.put(parameterId, StringFieldCodecs.INSTANCE_HEX);
        }
    }

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
            throw new IllegalArgumentException("Unsupported parameterId: 0x" + FormatUtils.toHexString(parameterId, 4));
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

            final Object value;
            FieldCodec<?> valueCodec = VALUE_CODEC_MAPPING.get(parameterId);
            if (valueCodec == null) {
                log.warn("Unsupported parameterId: 0x{}. Using [InternalHexStringFieldCodec] as fallback", FormatUtils.toHexString(parameterId, 4));
                valueCodec = StringFieldCodecs.INSTANCE_HEX;
                value = "[fallback:hex]" + valueCodec.deserialize(propertyMetadata, context, input, valueLength);
                // throw new IllegalArgumentException("Unsupported parameterId: 0x" + FormatUtils.toHexString(parameterId, 4));
            } else {
                value = valueCodec.deserialize(propertyMetadata, context, input, valueLength);
            }
            final ParameterItem item = new ParameterItem()
                    .setParameterId(parameterId)
                    .setParameterLength(valueLength)
                    .setParameterValue(value)
                    .setParameterType(ParameterItem.ParameterType.fromFieldCodec(valueCodec));
            list.add(item);
        }
        return list;
    }
}

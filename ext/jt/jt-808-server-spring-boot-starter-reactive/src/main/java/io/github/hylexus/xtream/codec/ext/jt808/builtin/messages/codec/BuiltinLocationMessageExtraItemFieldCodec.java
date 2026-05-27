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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.*;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class BuiltinLocationMessageExtraItemFieldCodec
        extends AbstractMapFieldCodec<Short, U8FieldCodecs.U8FieldCodec> {
    // implements BeanMetadataRegistryAware

    @FieldCodecCreator
    public BuiltinLocationMessageExtraItemFieldCodec(BeanMetadataRegistry registry, int version) {
        super(registry, version);
    }

    @Override
    protected void initValueCodec(int version, BeanMetadataRegistry registry) {
        this.registerValueFieldCodec((short) 0x01, U32FieldCodecs.LONG_INSTANCE);
        this.registerValueFieldCodec((short) 0x02, U16FieldCodecs.INTEGER_INSTANCE);
        this.registerValueFieldCodec((short) 0x03, U16FieldCodecs.INTEGER_INSTANCE);
        this.registerValueFieldCodec((short) 0x04, U16FieldCodecs.INTEGER_INSTANCE);
        this.registerValueFieldCodec((short) 0x05, BytesFieldCodecs.INSTANCE_BYTE_ARRAY);
        // -32767 ~ +32767
        this.registerValueFieldCodec((short) 0x06, I16FieldCodecs.SHORT_INSTANCE);
        this.registerValueFieldCodec((short) 0x25, U32FieldCodecs.LONG_INSTANCE);
        this.registerValueFieldCodec((short) 0x2a, U16FieldCodecs.INTEGER_INSTANCE);
        this.registerValueFieldCodec((short) 0x2b, U16FieldCodecs.INTEGER_INSTANCE);
        this.registerValueFieldCodec((short) 0x30, U8FieldCodecs.SHORT_INSTANCE);
        this.registerValueFieldCodec((short) 0x31, U8FieldCodecs.SHORT_INSTANCE);

        // 上面几个都是基本类型字段
        // 对于内嵌类型，有以下几种方式：
        // 1. 自定义编解码器 -- 传入编解码器实例
        // this.registerValueFieldCodec((short) 0x11, LocationItem0x11FieldCodec.INSTANCE);
        // 2. 自定义编解码器 -- 传入编解码器 Class
        // this.registerValueFieldCodec(version, (short) 0x11, LocationItem0x11FieldCodec.class);
        // 3. 直接传入实体类类型
        this.registerValueFieldCodec(version, (short) 0x11, LocationItem0x11.class);

        this.registerValueFieldCodec(version, (short) 0x12, LocationItem0x12.class);
        this.registerValueFieldCodec(version, (short) 0x13, LocationItem0x13.class);
        this.registerValueFieldCodec(version, (short) 0x64, LocationItem0x64.class);
        this.registerValueFieldCodec(version, (short) 0x65, LocationItem0x65.class);
        this.registerValueFieldCodec(version, (short) 0x66, LocationItem0x66.class);
        this.registerValueFieldCodec(version, (short) 0x67, LocationItem0x67.class);
    }

    @Override
    protected FieldCodec<?> getKeyFieldCodec() {
        return U8FieldCodecs.SHORT_INSTANCE;
    }

    @Override
    protected U8FieldCodecs.U8FieldCodec getValueLengthFieldCodec() {
        return U8FieldCodecs.SHORT_INSTANCE;
    }

    public static class LocationItem0x11FieldCodec implements CustomFieldCodec<LocationItem0x11> {
        public static LocationItem0x11FieldCodec INSTANCE = new LocationItem0x11FieldCodec();

        @Override
        public LocationItem0x11 deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final short locationType = input.readUnsignedByte();
            if (locationType == 0) {
                return new LocationItem0x11(locationType, null);
            }
            final long areaId = input.readUnsignedInt();
            return new LocationItem0x11(locationType, areaId);
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable LocationItem0x11 value) {
            if (value == null) {
                return;
            }
            output.writeByte(value.getLocationType());
            if (value.locationType() == 0) {
                return;
            }
            output.writeInt(Objects.requireNonNull(value.locationId()).intValue());
        }
    }

}

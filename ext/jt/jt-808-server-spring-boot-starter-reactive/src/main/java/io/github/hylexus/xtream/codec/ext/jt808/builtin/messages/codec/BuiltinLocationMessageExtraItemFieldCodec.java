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
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistryAware;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.*;
import io.netty.buffer.ByteBuf;

public class BuiltinLocationMessageExtraItemFieldCodec
        extends AbstractMapFieldCodec<Short, U8FieldCodecs.U8FieldCodec>
        implements BeanMetadataRegistryAware {

    private EntityFieldCodec<LocationItem0x12> locationItem0x12FieldCodec;
    private EntityFieldCodec<LocationItem0x13> locationItem0x13FieldCodec;
    private EntityFieldCodec<LocationItem0x64> locationItem0x64FieldCodec;
    private EntityFieldCodec<LocationItem0x65> locationItem0x65FieldCodec;
    private EntityFieldCodec<LocationItem0x66> locationItem0x66FieldCodec;
    private EntityFieldCodec<LocationItem0x67> locationItem0x67FieldCodec;

    public BuiltinLocationMessageExtraItemFieldCodec() {
    }

    @Override
    protected FieldCodec<?> getKeyFieldCodec() {
        return U8FieldCodecs.SHORT_INSTANCE;
    }

    @Override
    protected U8FieldCodecs.U8FieldCodec getValueLengthFieldCodec() {
        return U8FieldCodecs.SHORT_INSTANCE;
    }

    @Override
    protected FieldCodec<?> getValueFieldCodec(Short key) {
        return switch (key) {
            case null -> throw new NullPointerException("MapKey is null");
            case 0x01, 0x25 -> U32FieldCodecs.LONG_INSTANCE;
            case 0x02, 0x03, 0x04, 0x2a, 0x2b -> U16FieldCodecs.INTEGER_INSTANCE;
            case 0x30, 0x31 -> U8FieldCodecs.SHORT_INSTANCE;
            case 0x05 -> BytesFieldCodecs.INSTANCE_BYTE_ARRAY;
            // -32767 ~ +32767
            case 0x06 -> I16FieldCodecs.SHORT_INSTANCE;
            // 0x11 的编解码也可以通过 EntityFieldCodec 来实现，这里使用自定义的 FieldCodec 作演示
            case 0x11 -> OverSpeedAlarmItemFieldCodec.INSTANCE;
            // 可以直接 new 一个 EntityFieldCodec<LocationItem0x12>() 实例，但是没必要(直接返回单例即可)
            case 0x12 -> this.locationItem0x12FieldCodec;
            case 0x13 -> this.locationItem0x13FieldCodec;
            case 0x64 -> this.locationItem0x64FieldCodec;
            case 0x65 -> this.locationItem0x65FieldCodec;
            case 0x66 -> this.locationItem0x66FieldCodec;
            case 0x67 -> this.locationItem0x67FieldCodec;
            default -> throw new UnsupportedOperationException("Unsupported key: " + key + "(0x" + FormatUtils.toHexString(key, 2) + ")");
        };
    }

    @Override
    public void setBeanMetadataRegistry(int version, BeanMetadataRegistry registry) {
        this.locationItem0x12FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x12.class);
        this.locationItem0x13FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x13.class);
        this.locationItem0x64FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x64.class);
        this.locationItem0x65FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x65.class);
        this.locationItem0x66FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x66.class);
        this.locationItem0x67FieldCodec = new EntityFieldCodec<>(version, registry, LocationItem0x67.class);
    }

    public static class OverSpeedAlarmItemFieldCodec implements FieldCodec<OverSpeedAlarmItem> {
        public static OverSpeedAlarmItemFieldCodec INSTANCE = new OverSpeedAlarmItemFieldCodec();

        @Override
        public OverSpeedAlarmItem deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final OverSpeedAlarmItem item = new OverSpeedAlarmItem();
            item.setLocationType(input.readUnsignedByte());
            if (item.getLocationType() == 0) {
                return item;
            }
            item.setAreaId(input.readUnsignedInt());
            return item;
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, OverSpeedAlarmItem value) {
            if (value == null) {
                return;
            }
            output.writeByte(value.getLocationType());
            if (value.getLocationType() == 0) {
                return;
            }
            output.writeInt(value.getAreaId().intValue());
        }
    }

    public static class OverSpeedAlarmItem {
        /**
         * 位置类型
         * <li>0：无特定位置</li>
         * <li>1：圆形区域</li>
         * <li>2：矩形区域</li>
         * <li>3：多边形区域</li>
         * <li>4：路段</li>
         */
        private short locationType;
        /**
         * 区域或路段ID
         * <p>
         * 若位置类型为0，无该字段
         */
        private Long areaId;

        public short getLocationType() {
            return locationType;
        }

        public OverSpeedAlarmItem setLocationType(short locationType) {
            this.locationType = locationType;
            return this;
        }

        public Long getAreaId() {
            return areaId;
        }

        public OverSpeedAlarmItem setAreaId(Long areaId) {
            this.areaId = areaId;
            return this;
        }
    }


}

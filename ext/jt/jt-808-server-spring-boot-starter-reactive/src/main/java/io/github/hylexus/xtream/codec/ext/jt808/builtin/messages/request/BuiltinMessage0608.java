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
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8600V2019;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8602V2019;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8604V2019;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8606V2019;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询区域或线路数据应答
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BuiltinMessage0608 {

    /**
     * * 查询类型
     * * <li>1 -- 查询圆形区域数据</li>
     * * <li>2 -- 查询矩形区域数据</li>
     * * <li>3 -- 查询多边形区域数据</li>
     * * <li>4 -- 查询线路数据</li>
     */
    @Preset.JtStyle.Byte
    private short type;

    /**
     * 查询返回的数据数量
     */
    @Preset.JtStyle.Dword
    private long count;

    @Preset.JtStyle.List(fieldCodec = BuiltinMessage0608.Message0608FieldCodec.class)
    private List<Object> dataList;

    public static class Message0608FieldCodec implements FieldCodec<List<Object>> {

        @Override
        public List<Object> deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {

            final BuiltinMessage0608 self = (BuiltinMessage0608) context.containerInstance();
            final short msgType = self.getType();
            long remainingItemCount = self.getCount();

            final List<Object> result = new ArrayList<>();
            while (input.isReadable() && remainingItemCount-- > 0) {
                final Object item = switch (msgType) {
                    // 查询圆形区域数据
                    case 1 -> context.entityDecoder().decode(BuiltinMessage8600V2019.class, input);
                    // 查询矩形区域数据
                    case 2 -> context.entityDecoder().decode(BuiltinMessage8602V2019.class, input);
                    // 查询多边形区域数据
                    case 3 -> context.entityDecoder().decode(BuiltinMessage8604V2019.class, input);
                    // 查询线路数据
                    case 4 -> context.entityDecoder().decode(BuiltinMessage8606V2019.class, input);
                    default -> throw new UnsupportedOperationException("未知消息类型 type:" + msgType);
                };
                result.add(item);
            }
            return result;
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, List<Object> value) {
            for (final Object item : value) {
                context.entityEncoder().encode(context.version(), item, output);
            }
        }
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 车辆控制
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8500)
public class BuiltinMessage8500V2019 {

    private static final Logger log = LoggerFactory.getLogger(BuiltinMessage8500V2019.class);
    /**
     * 控制类型数量
     */
    @Preset.JtStyle.Word
    private int paramCount;

    /**
     * 控制类型ID: 控制参数
     *
     * <li>0x0001: 车门(BYTE);</li>
     * 0:车门锁闭；1: 车门开启
     * <li>0x0002~0x8000: 为标注修订预留</li>
     * <li>0xF001~0xFFFF: 为厂商自定义控制类型</li>
     */
    @Preset.JtStyle.Bytes(fieldCodec = Message8500FieldCodec.class)
    private Map<Integer, Object> params;

    public int getParamCount() {
        return paramCount;
    }

    public BuiltinMessage8500V2019 setParamCount(int paramCount) {
        this.paramCount = paramCount;
        return this;
    }

    public Map<Integer, Object> getParams() {
        return params;
    }

    public BuiltinMessage8500V2019 setParams(Map<Integer, Object> params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8500V2019.class.getSimpleName() + "[", "]")
                .add("paramCount=" + paramCount)
                .add("params=" + params)
                .toString();
    }

    public static class Message8500FieldCodec implements FieldCodec<Map<Integer, Object>> {

        @Override
        public Map<Integer, Object> deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
            final Map<Integer, Object> params = new LinkedHashMap<>();
            while (input.isReadable()) {
                // 控制类型 ID (WORD)
                final int itemId = input.readUnsignedShort();
                if (itemId == 0x0001) {
                    final short value = input.readUnsignedByte();
                    params.put(itemId, value);
                } else {
                    throw new NotYetImplementedException("未知控制参数: 0x" + FormatUtils.toHexString(itemId));
                }
            }
            return params;
        }

        @Override
        public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, @Nullable Map<Integer, Object> value) {
            if (value == null) {
                return;
            }
            for (final Map.Entry<Integer, Object> entry : value.entrySet()) {
                final Integer itemId = entry.getKey();
                if (Objects.equals(itemId, 0x0001)) {
                    final short itemValue = ((Number) entry.getValue()).shortValue();
                    output.writeShort(itemId);
                    output.writeByte(itemValue);
                } else {
                    log.error("不支持的参数类型: 0x{}", FormatUtils.toHexString(itemId));
                }
            }
        }
    }
}

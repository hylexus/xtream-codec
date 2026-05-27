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

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.StringFieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U16FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U32FieldCodecs;
import io.github.hylexus.xtream.codec.core.impl.codec.U8FieldCodecs;
import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

/**
 * @author hylexus
 */
public class ParameterItem {

    // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.Dword 注解了
    private long parameterId;

    // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.Byte 注解了
    private short parameterLength;

    // 自定义 FieldCodec 时就不用加 @Preset.JtStyle.RuntimeType 注解了
    private @Nullable Object parameterValue;

    // 非请求参数；仅仅为了调试方便；可以忽略这个参数
    private ParameterType parameterType;

    public ParameterItem() {
    }

    public ParameterItem(long parameterId, short parameterLength, @Nullable Object parameterValue) {
        this.parameterId = parameterId;
        this.parameterLength = parameterLength;
        this.parameterValue = parameterValue;
    }

    public long getParameterId() {
        return parameterId;
    }

    public ParameterItem setParameterId(long parameterId) {
        this.parameterId = parameterId;
        return this;
    }

    public short getParameterLength() {
        return parameterLength;
    }

    public ParameterItem setParameterLength(short parameterLength) {
        this.parameterLength = parameterLength;
        return this;
    }

    public @Nullable Object getParameterValue() {
        return parameterValue;
    }

    public ParameterItem setParameterValue(@Nullable Object parameterValue) {
        this.parameterValue = parameterValue;
        return this;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public ParameterItem setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ParameterItem.class.getSimpleName() + "[", "]")
                .add("parameterId=" + parameterId)
                .add("parameterLength=" + parameterLength)
                .add("parameterValue=" + parameterValue)
                .add("parameterType=" + parameterType)
                .toString();
    }

    // 下面代码都是为了调试方便添加的  可以忽略
    public String getParameterIdAsHexString() {
        return "0x" + FormatUtils.toHexString(this.parameterId, 4);
    }

    public enum ParameterType {
        BYTE,
        WORD,
        DWORD,
        STRING_BCD_8421,
        STRING_GBK,
        STRING_HEX,
        STRING_UTF_8,
        UNKNOWN,
        ;

        public static ParameterType fromFieldCodec(FieldCodec<?> fieldCodec) {
            return switch (fieldCodec) {
                case U8FieldCodecs.U8FieldCodec ignored -> BYTE;
                case U16FieldCodecs.U16FieldCodec ignored -> WORD;
                case U32FieldCodecs.U32FieldCodec ignored -> DWORD;
                case StringFieldCodecs.StringFieldCodecHex ignored -> STRING_HEX;
                case StringFieldCodecs.StringFieldCodec stringFieldCodec -> detectStringType(stringFieldCodec.charset().name());
                default -> UNKNOWN;
            };
        }

        private static ParameterType detectStringType(String stringFieldCodec) {
            return switch (stringFieldCodec.toUpperCase()) {
                case XtreamConstants.CHARSET_NAME_BCD_8421 -> STRING_BCD_8421;
                case "GBK" -> STRING_GBK;
                case "UTF-8" -> STRING_UTF_8;
                default -> UNKNOWN;
            };
        }
    }
}

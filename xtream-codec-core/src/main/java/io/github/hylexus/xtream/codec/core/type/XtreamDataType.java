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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Set;

import static java.util.Set.of;

@Getter
@Accessors(fluent = true)
public enum XtreamDataType {

    placeholder(0, FieldCodec.Placeholder.createForInternalUse("placeholder"), false, of()),

    i8(1, I8FieldCodecs.BYTE_INSTANCE, true, of(byte.class)),
    i8_as_short(1, I8FieldCodecs.SHORT_INSTANCE, true, of(short.class)),
    i8_as_int(1, I8FieldCodecs.INTEGER_INSTANCE, true, of(int.class)),
    i8_as_long(1, I8FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    u8(1, U8FieldCodecs.SHORT_INSTANCE, true, of(short.class)),
    u8_as_int(1, U8FieldCodecs.INTEGER_INSTANCE, true, of(int.class)),
    u8_as_long(1, U8FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    i16(2, I16FieldCodecs.SHORT_INSTANCE, true, of(short.class)),
    i16_as_int(2, I16FieldCodecs.INTEGER_INSTANCE, true, of(int.class)),
    i16_as_long(2, I16FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    i16_le(2, I16FieldCodecs.SHORT_INSTANCE_LE, true, of(short.class)),
    i16_le_as_int(2, I16FieldCodecs.INTEGER_INSTANCE_LE, true, of(int.class)),
    i16_le_as_long(2, I16FieldCodecs.LONG_INSTANCE_LE, true, of(long.class)),

    u16(2, U16FieldCodecs.INTEGER_INSTANCE, true, of(int.class)),
    u16_as_long(2, U16FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    u16_le(2, U16FieldCodecs.INTEGER_INSTANCE_LE, true, of(int.class)),
    u16_le_as_long(2, U16FieldCodecs.LONG_INSTANCE_LE, true, of(long.class)),

    i32(4, I32FieldCodecs.INTEGER_INSTANCE, true, of(int.class)),
    i32_long(4, I32FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    i32_le(4, I32FieldCodecs.INTEGER_INSTANCE_LE, true, of(int.class)),
    i32_le_as_long(4, I32FieldCodecs.LONG_INSTANCE_LE, true, of(long.class)),

    u32(4, U32FieldCodecs.LONG_INSTANCE, true, of(long.class)),

    u32_le(4, U32FieldCodecs.LONG_INSTANCE_LE, true, of(long.class)),

    f32(4, F32FieldCodecs.FLOAT_INSTANCE, true, of(Float.class)),
    f32_le(4, F32FieldCodecs.FLOAT_INSTANCE_LE, true, of(Float.class)),

    i64(8, I64FieldCodecs.LONG_INSTANCE, true, of(long.class)),
    i64_le(8, I64FieldCodecs.LONG_INSTANCE_LE, true, of(long.class)),

    // u64(8, I64FieldCodec.INSTANCE, true, of(BigInteger.class)),
    // u64_le(8, I64FieldCodecLittleEndian.INSTANCE, true, of(BigInteger.class)),
    f64(8, F64FieldCodecs.DOUBLE_INSTANCE, true, of(Double.class)),
    f64_le(8, F64FieldCodecs.DOUBLE_INSTANCE_LE, true, of(Double.class)),
    // i128(16, of(BigInteger.class)),
    // i128_le(16, of(BigInteger.class)),
    // u128(16, of(BigInteger.class)),
    // u128_le(16, of(BigInteger.class)),
    string(-1, FieldCodec.Placeholder.createForInternalUse("placeholder string FieldCodec"), false, of(String.class)) {
        @Override
        public CodecCharset codecCharset() {
            return CodecCharset.DYNAMIC;
        }
    },
    string_gbk(-1, StringFieldCodecs.INSTANCE_GBK, false, of(String.class)) {
        @Override
        public CodecCharset codecCharset() {
            return CodecCharset.GBK;
        }
    },
    string_gb_2312(-1, StringFieldCodecs.INSTANCE_GB_2312, false, of(String.class)) {
        @Override
        public CodecCharset codecCharset() {
            return CodecCharset.GB_2312;
        }
    },
    string_utf8(-1, StringFieldCodecs.INSTANCE_UTF8, false, of(String.class)) {
        @Override
        public CodecCharset codecCharset() {
            return CodecCharset.UTF_8;
        }
    },
    string_hex(-1, StringFieldCodecs.INSTANCE_HEX, false, of(String.class)) {
        @Override
        public CodecCharset codecCharset() {
            return CodecCharset.HEX;
        }
    },
    byte_array(-1, BytesFieldCodecs.INSTANCE_BYTE_ARRAY, false, of(byte[].class)),
    ;
    private final int sizeInBytes;
    private final FieldCodec<Object> codec;
    private final boolean isNumberType;
    private final Set<Class<?>> recommendedJavaTypes;

    public boolean isSimpleType() {
        return this.isNumberType() && this != string;
    }

    public boolean isPlaceholder() {
        return this == placeholder;
    }

    @SuppressWarnings("unchecked")
    XtreamDataType(int sizeInBytes, FieldCodec<?> codec, boolean isNumberType, Set<Class<?>> recommendedJavaTypes) {
        this.sizeInBytes = sizeInBytes;
        this.codec = (FieldCodec<Object>) codec;
        this.isNumberType = isNumberType;
        this.recommendedJavaTypes = recommendedJavaTypes;
    }

    public CodecCharset codecCharset() {
        return CodecCharset.UNSUPPORTED;
    }

}

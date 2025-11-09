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

package io.github.hylexus.xtream.codec.core.type.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonTypeIdResolver(SimpleFieldTypeIdResolver.class)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@ApiStatus.Experimental
public sealed interface SimpleField {

    String type();

    @Nullable Object value();

    default PrependLengthFieldType prependLengthFieldType() {
        return PrependLengthFieldType.none;
    }

    default String charset() {
        throw new UnsupportedOperationException();
    }

    sealed interface StrField extends SimpleField
            permits Str, StrUtf8, StrGb2312, StrGbk {

        @Override
        String charset();

    }

    sealed interface DictKey extends SimpleField
            permits I8, U8, I16, U16, I32, U32, I64 {

        static DictKey from(Class<? extends DictKey> clazz, String value) {
            return switch (clazz.getSimpleName().toLowerCase()) {
                case "i8", "int8" -> new I8(Byte.parseByte(value));
                case "u8", "uint8" -> new U8(Short.parseShort(value));
                case "i16", "int16" -> new I16(Short.parseShort(value));
                case "u16", "uint16" -> new U16(Integer.parseInt(value));
                case "i32", "int32" -> new I32(Integer.parseInt(value));
                case "u32", "uint32" -> new U32(Long.parseLong(value));
                case "i64", "int64" -> new I64(Long.parseLong(value));
                default -> throw new IllegalArgumentException("Unknown dict key type: " + clazz.getSimpleName());
            };
        }

    }

    non-sealed interface CustomSimpleField extends SimpleField {

        void writeTo(ByteBuf output);

        @Override
        default String type() {
            return this.getClass().getName();
        }
    }

    record F32(Float value) implements SimpleField {
        @Override
        public String type() {
            return "f32";
        }
    }

    record F64(Double value) implements SimpleField {
        @Override
        public String type() {
            return "f64";
        }
    }

    record StrGbk(String value, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {

        public StrGbk(String value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrGbk(String value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "str_gbk";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_GBK;
        }
    }

    record Str(String value, String charset, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {
        public Str(String value, String charset, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.charset = Objects.requireNonNull(charset, "charset is null");
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Str(String value, String charset) {
            this(value, charset, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "str";
        }
    }

    record StrGb2312(String value, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {
        public StrGb2312(String value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrGb2312(String value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "str_gb2312";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_GB_2312;
        }
    }

    record StrUtf8(String value, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {

        public StrUtf8(String value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrUtf8(String value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "str_utf8";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_UTF8;
        }
    }

    record Struct(List<SimpleField> value, PrependLengthFieldType prependLengthFieldType) implements SimpleField {

        public Struct(List<SimpleField> value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Struct(List<SimpleField> value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "struct";
        }

    }

    enum KeyLengthType {
        i8, u8, i16, u16, i32, u32, i64;

        public void writeTo(ByteBuf output, long value) {
            switch (this) {
                case null -> throw new IllegalArgumentException("Key length type cannot be null");
                case i8, u8 -> output.writeByte((int) value);
                case i16, u16 -> output.writeShort((int) value);
                case i32, u32 -> output.writeInt((int) value);
                case i64 -> output.writeLong(value);
            }
        }
    }


    record I8(Byte value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "i8";
        }

    }

    record U8(Short value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "u8";
        }
    }

    record I16(Short value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "i16";
        }
    }

    record U16(Integer value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "u16";
        }
    }

    record I32(Integer value) implements SimpleField.DictKey {

        @Override
        public String type() {
            return "i32";
        }

    }

    record U32(Long value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "u32";
        }
    }

    record I64(Long value) implements SimpleField.DictKey {
        @Override
        public String type() {
            return "i64";
        }
    }

    /**
     * @param keyType                {@code key} 的类型
     * @param valueLengthType        描述 {@code value} 的长度的类型
     * @param value                  要编码的数据
     * @param prependLengthFieldType 如果要写入一个前置长度字段，传入具体类型；否则传入 {@code null}
     */
    record Dict<K extends DictKey>(
            @JsonProperty("keyType")
            @JsonSerialize(using = DictKeyTypeJsonSerializer.class)
            Class<K> keyType,
            @JsonProperty("valueLengthType") KeyLengthType valueLengthType,
            @JsonProperty("value") Map<K, SimpleField> value,
            @JsonProperty("prependLengthFieldType") PrependLengthFieldType prependLengthFieldType
    ) implements SimpleField {

        public Dict(Class<K> keyType, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.keyType = keyType;
            this.value = value;
            this.valueLengthType = valueLengthType;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Dict(Class<K> keyClass, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
            this(keyClass, valueLengthType, value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "dict";
        }
    }

    record Sequence(List<SimpleField> value, PrependLengthFieldType prependLengthFieldType) implements SimpleField {

        public Sequence(List<SimpleField> value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Sequence(List<SimpleField> value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "seq";
        }
    }

    record ByteSequence(
            @JsonSerialize(using = ByteArrayJsonSerializer.class)
            byte[] value,
            PrependLengthFieldType prependLengthFieldType
    ) implements SimpleField {

        public ByteSequence(byte[] value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = Objects.requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public ByteSequence(byte[] value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "byte_seq";
        }
    }

}

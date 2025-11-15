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
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

@JsonTypeIdResolver(SimpleFieldTypeIdResolver.class)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@ApiStatus.Experimental
public sealed interface SimpleField {

    default String name() {
        return this.getClass().getSimpleName();
    }

    String type();

    @Nullable Object value();

    default PrependLengthFieldType prependLengthFieldType() {
        return PrependLengthFieldType.none;
    }

    default String charset() {
        throw new UnsupportedOperationException();
    }

    sealed interface StrField extends SimpleField
            permits Str, StrBcd8421, StrGb2312, StrGbk, StrHex, StrUtf8 {

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

    record F32(String name, Float value) implements SimpleField {
        public F32(@Nullable String name, Float value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public F32(Float value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "f32";
        }
    }

    record F64(String name, Double value) implements SimpleField {
        public F64(@Nullable String name, Double value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public F64(Double value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "f64";
        }
    }

    record StrGbk(String name, PrependLengthFieldType prependLengthFieldType, String value) implements SimpleField.StrField {

        public StrGbk(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value) {
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public StrGbk(PrependLengthFieldType prependLengthFieldType, String value) {
            this(null, prependLengthFieldType, value);
        }

        public StrGbk(String value) {
            this(null, PrependLengthFieldType.none, value);
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
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Str(String value, String charset) {
            this(value, charset, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "str";
        }
    }

    record StrGb2312(String name, PrependLengthFieldType prependLengthFieldType, String value) implements SimpleField.StrField {
        public StrGb2312(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value) {
            this.name = filedName(name, this);
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrGb2312(@Nullable PrependLengthFieldType prependLengthFieldType, String value) {
            this(null, prependLengthFieldType, value);
        }

        public StrGb2312(String value) {
            this(null, PrependLengthFieldType.none, value);
        }

        public StrGb2312(String name, String value) {
            this(name, PrependLengthFieldType.none, value);
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

    record StrUtf8(String name, PrependLengthFieldType prependLengthFieldType, String value) implements SimpleField.StrField {

        public StrUtf8(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value) {
            this.name = filedName(name, this);
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrUtf8(@Nullable PrependLengthFieldType prependLengthFieldType, String value) {
            this(null, prependLengthFieldType, value);
        }

        public StrUtf8(String name, String value) {
            this(name, PrependLengthFieldType.none, value);
        }

        public StrUtf8(String value) {
            this(null, PrependLengthFieldType.none, value);
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

    record StrBcd8421(String value, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {

        public StrBcd8421(String value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrBcd8421(String value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "bcd_8421";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_BCD_8421;
        }
    }

    record StrHex(String value, PrependLengthFieldType prependLengthFieldType) implements SimpleField.StrField {

        public StrHex(String value, @Nullable PrependLengthFieldType prependLengthFieldType) {
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public StrHex(String value) {
            this(value, PrependLengthFieldType.none);
        }

        @Override
        public String type() {
            return "hex";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_HEX;
        }
    }

    record Struct(@Nullable String name, PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) implements SimpleField {

        public Struct(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
            this.name = filedName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
        }

        public Struct(@Nullable PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
            this(null, prependLengthFieldType, value);
        }

        public Struct(List<SimpleField> value) {
            this(null, PrependLengthFieldType.none, value);
        }

        @Override
        public String type() {
            return "struct";
        }

    }

    enum KeyLengthType {
        i8, u8, i16, u16, i32, u32, i64;

        public void writeToWithTracker(ByteBuf output, long value, CodecTracker codecTracker) {
            final int writerIndex = output.writerIndex();
            this.writeTo(output, value);
            final String hexString = FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), this.getDeclaringClass().getSimpleName(), value, hexString, this.getDeclaringClass().getSimpleName(), "");
        }

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

    record I8(String name, Byte value) implements SimpleField.DictKey {
        public I8(@Nullable String name, Byte value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public I8(Byte value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "i8";
        }

    }

    record U8(String name, Short value) implements SimpleField.DictKey {
        public U8(@Nullable String name, Short value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            SimpleField.checkU8(value);
            this.value = value;
        }

        public U8(Short value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "u8";
        }
    }

    record I16(String name, Short value) implements SimpleField.DictKey {
        public I16(@Nullable String name, Short value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public I16(Short value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "i16";
        }
    }

    record U16(String name, Integer value) implements SimpleField.DictKey {
        public U16(@Nullable String name, Integer value) {
            SimpleField.checkU16(value);
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public U16(Integer value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "u16";
        }
    }

    record I32(String name, Integer value) implements SimpleField.DictKey {
        public I32(@Nullable String name, Integer value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public I32(Integer value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "i32";
        }

    }

    record U32(String name, Long value) implements SimpleField.DictKey {
        public U32(@Nullable String name, Long value) {
            SimpleField.checkU32(value);
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public U32(Long value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "u32";
        }
    }

    record I64(String name, Long value) implements SimpleField.DictKey {
        public I64(@Nullable String name, Long value) {
            this.name = nonNull(name, this.getClass().getSimpleName());
            this.value = value;
        }

        public I64(Long value) {
            this(null, value);
        }

        @Override
        public String type() {
            return "i64";
        }
    }

    /**
     * @param name                   名称；默认为类型名（目前仅仅用来调试）
     * @param prependLengthFieldType 如果要写入一个前置长度字段，传入具体类型；否则传入 {@code null}
     * @param keyType                {@code key} 的类型
     * @param valueLengthType        描述 {@code value} 的长度的类型
     * @param value                  要编码的数据
     */
    record Dict<K extends DictKey>(
            @JsonProperty("name") String name,

            @JsonProperty("prependLengthFieldType")
            PrependLengthFieldType prependLengthFieldType,

            @JsonProperty("keyType")
            @JsonSerialize(using = DictKeyTypeJsonSerializer.class)
            Class<K> keyType,

            @JsonProperty("valueLengthType") KeyLengthType valueLengthType,
            @JsonProperty("value") Map<K, SimpleField> value
    ) implements SimpleField {

        public Dict(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, Class<K> keyType, KeyLengthType valueLengthType, Map<K, SimpleField> value) {
            this.name = filedName(name, this);
            this.keyType = keyType;
            this.value = value;
            this.valueLengthType = valueLengthType;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
        }

        public Dict(PrependLengthFieldType prependLengthFieldType, Class<K> keyClass, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
            this(null, prependLengthFieldType, keyClass, valueLengthType, value);
        }

        public Dict(Class<K> keyClass, SimpleField.KeyLengthType valueLengthType, Map<K, SimpleField> value) {
            this(null, PrependLengthFieldType.none, keyClass, valueLengthType, value);
        }

        @Override
        public String type() {
            return "dict";
        }
    }

    record Sequence(String name, PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) implements SimpleField {

        public Sequence(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
            this.name = filedName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
        }

        public Sequence(PrependLengthFieldType prependLengthFieldType, List<SimpleField> value) {
            this(null, prependLengthFieldType, value);
        }

        public Sequence(List<SimpleField> value) {
            this(null, PrependLengthFieldType.none, value);
        }

        @Override
        public String type() {
            return "seq";
        }
    }

    record ByteSequence(
            String name,
            PrependLengthFieldType prependLengthFieldType, @JsonSerialize(using = ByteArrayJsonSerializer.class)
            byte[] value
    ) implements SimpleField {

        public ByteSequence(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, byte[] value) {
            this.name = filedName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
        }

        public ByteSequence(@Nullable PrependLengthFieldType prependLengthFieldType, byte[] value) {
            this(null, prependLengthFieldType, value);
        }

        public ByteSequence(byte[] value) {
            this(null, PrependLengthFieldType.none, value);
        }

        @Override
        public String type() {
            return "byte_seq";
        }
    }

    static String filedName(@Nullable String name, SimpleField self) {
        if (name == null) {
            return self.getClass().getSimpleName();
        }
        return name;
    }

    static <T> T nonNull(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private static void checkU8(@Nullable Short v) {
        if (v == null) {
            return;
        }
        if (v < 0 || v > 0xFF) {
            throw outOfRange("U8", v);
        }
    }

    private static void checkU16(@Nullable Integer v) {
        if (v == null) {
            return;
        }
        if (v < 0 || v > 0xFFFF) {
            throw outOfRange("U16", v);
        }
    }

    private static void checkU32(@Nullable Long v) {
        if (v == null) {
            return;
        }
        if (v < 0 || v > 0xFFFFFFFFL) {
            throw outOfRange("U32", v);
        }
    }

    private static IllegalArgumentException outOfRange(String type, Number v) {
        return new IllegalArgumentException(type + " value out of range: " + v);
    }

}

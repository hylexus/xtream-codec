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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.DataFieldEncoder;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

/**
 * 当前类中定义的数据类型，都提供了对应的工厂方法：{@link DataFields}
 *
 * @see DataFields
 */
@JsonTypeIdResolver(DataFieldTypeIdResolver.class)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@ApiStatus.Experimental
public sealed interface DataField {

    /**
     * 返回该字段的名称，主要用于调试和日志追踪。
     * <p>
     * 如果构造时未指定名称，则默认返回实现类的简单类名（如 "U32", "GbkString"）。
     * <p>
     * 此方法不应在业务逻辑中依赖，仅用于可观测性（observability）。
     *
     * @see DataFieldEncoder#encodeWithTracker(FieldCodec.SerializeContext, DataField, ByteBuf)
     * @see DataFieldEncoder#encodeWithTracker(FieldCodec.SerializeContext, Iterable, ByteBuf)
     */
    default String name() {
        return this.getClass().getSimpleName();
    }

    String type();

    @Nullable Object value();

    @JsonAnyGetter
    @JsonAnySetter
    @JsonProperty("__attributes__")
    default @Nullable Map<String, @Nullable Object> attributes() {
        return null;
    }

    default PrependLengthFieldType prependLengthFieldType() {
        return PrependLengthFieldType.none;
    }

    default String charset() {
        throw new UnsupportedOperationException();
    }

    sealed interface StringDataField extends DataField
            permits GenericString, Bcd8421String, Gb2312String, GbkString, HexString, Utf8String {

        @Override
        String charset();

    }

    sealed interface IntegralDataField extends DataField
            permits DictKey {
    }

    sealed interface DictKey extends IntegralDataField
            permits I8, U8, I16, U16, I32, U32, I64 {

        static DictKey from(Class<? extends DictKey> clazz, String value) {
            return switch (clazz.getSimpleName().toLowerCase()) {
                case "i8", "int8" -> new I8(null, Byte.parseByte(value), null);
                case "u8", "uint8" -> new U8(null, Short.parseShort(value), null);
                case "i16", "int16" -> new I16(null, Short.parseShort(value), null);
                case "u16", "uint16" -> new U16(null, Integer.parseInt(value), null);
                case "i32", "int32" -> new I32(null, Integer.parseInt(value), null);
                case "u32", "uint32" -> new U32(null, Long.parseLong(value), null);
                case "i64", "int64" -> new I64(null, Long.parseLong(value), null);
                default -> throw new IllegalArgumentException("Unknown dict key type: " + clazz.getSimpleName());
            };
        }

    }

    sealed interface FloatDataField extends DataField
            permits F32, F64 {
    }

    non-sealed interface CustomDataField extends DataField {

        void writeTo(ByteBuf output);

        @Override
        default String type() {
            return this.getClass().getName();
        }
    }

    record F32(String name, Float value, @Nullable Map<String, @Nullable Object> attributes) implements FloatDataField {
        public F32(@Nullable String name, Float value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "f32";
        }
    }

    record F64(String name, Double value, @Nullable Map<String, @Nullable Object> attributes) implements FloatDataField {
        public F64(@Nullable String name, Double value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "f64";
        }
    }

    record GbkString(
            String name,
            PrependLengthFieldType prependLengthFieldType,
            String value,
            @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {

        public GbkString(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "gbk_string";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_GBK;
        }
    }

    record GenericString(String name, PrependLengthFieldType prependLengthFieldType, String value, String charset, @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {
        public GenericString(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, String charset, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
            this.charset = Objects.requireNonNull(charset, "charset is null");
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "string";
        }
    }

    record Gb2312String(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {
        public Gb2312String(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "gb2312_string";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_GB_2312;
        }
    }

    record Utf8String(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {

        public Utf8String(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "utf8_string";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_UTF8;
        }
    }

    static PrependLengthFieldType filedLengthType(@Nullable PrependLengthFieldType input) {
        return requireNonNullElse(input, PrependLengthFieldType.none);
    }

    record Bcd8421String(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {

        public Bcd8421String(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.prependLengthFieldType = filedLengthType(prependLengthFieldType);
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "bcd_8421_string";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_BCD_8421;
        }
    }

    record HexString(String name, PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) implements StringDataField {

        public HexString(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, String value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "hex_string";
        }

        @Override
        public String charset() {
            return XtreamConstants.CHARSET_NAME_HEX;
        }
    }

    record Struct(@Nullable String name, PrependLengthFieldType prependLengthFieldType, List<DataField> value, @Nullable Map<String, @Nullable Object> attributes) implements DataField {

        public Struct(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, List<DataField> value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "struct";
        }

    }

    enum ValueLengthType {
        i8, u8, i16, u16, i32, u32, i64;

        public void writeToWithTracker(ByteBuf output, long value, CodecTracker codecTracker) {
            final int writerIndex = output.writerIndex();
            this.writeTo(output, value);
            final String hexString = FormatUtils.toHexString(output, writerIndex, output.writerIndex() - writerIndex);
            codecTracker.addFieldSpan(codecTracker.getCurrentSpan(), this.getDeclaringClass().getSimpleName(), value, hexString, this.getDeclaringClass().getSimpleName(), "");
        }

        public void writeTo(ByteBuf output, long value) {
            switch (this) {
                case i8, u8 -> output.writeByte((int) value);
                case i16, u16 -> output.writeShort((int) value);
                case i32, u32 -> output.writeInt((int) value);
                case i64 -> output.writeLong(value);
                default -> throw new IllegalArgumentException("Unsupported value length type: " + this);
            }
        }
    }

    record I8(String name, Byte value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public I8(@Nullable String name, Byte value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "i8";
        }

    }

    record U8(String name, Short value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public U8(@Nullable String name, Short value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            DataField.checkU8(value);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "u8";
        }
    }

    record I16(String name, Short value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public I16(@Nullable String name, Short value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "i16";
        }
    }

    record U16(String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public U16(@Nullable String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) {
            DataField.checkU16(value);
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "u16";
        }
    }

    record I32(String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public I32(@Nullable String name, Integer value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "i32";
        }

    }

    record U32(String name, Long value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public U32(@Nullable String name, Long value, @Nullable Map<String, @Nullable Object> attributes) {
            DataField.checkU32(value);
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "u32";
        }
    }

    record I64(String name, Long value, @Nullable Map<String, @Nullable Object> attributes) implements DataField.DictKey {
        public I64(@Nullable String name, Long value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.value = value;
            this.attributes = attributes;
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

            @JsonProperty("valueLengthType") ValueLengthType valueLengthType,
            @JsonProperty("value") Map<K, DataField> value,
            @Nullable Map<String, @Nullable Object> attributes
    ) implements DataField {

        public Dict(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, Class<K> keyType, ValueLengthType valueLengthType, Map<K, DataField> value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.keyType = keyType;
            this.value = value;
            this.valueLengthType = valueLengthType;
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "dict";
        }
    }

    /**
     * TLV: Tag-Length-Value
     */
    sealed interface TlvDataField extends DataField permits SimpleTlvDataField {
        DictKey tag();

        ValueLengthType length();

        @Override
        DataField value();
    }

    record SimpleTlvDataField<T extends DictKey>(
            String name,
            PrependLengthFieldType prependLengthFieldType,
            T tag,
            ValueLengthType length,
            DataField value,
            @Nullable Map<String, @Nullable Object> attributes
    ) implements TlvDataField {

        public SimpleTlvDataField(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, T tag, ValueLengthType length, DataField value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.tag = tag;
            this.length = length;
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "tlv";
        }

    }

    /**
     * 表示协议中的字段序列（有序字段列表）。
     * <p>
     * 本类型用于描述一段由多个 {@link DataField} 按固定顺序组成的结构化数据，
     * 常见于变长记录、嵌套消息体、或需要显式顺序的协议段（如 TLV 列表、日志条目等）。
     * <p>
     * 命名为 {@code Sequence}（而非 {@code List<DataField>} 或 {@code FieldList}）是为了：
     * <ul>
     *   <li>强调其作为“协议语义序列”的角色，而非通用的 Java 集合；</li>
     *   <li>避免与 {@code java.util.List} 等标准库类型在概念上混淆；</li>
     *   <li>与本框架中其他协议字段类型（如 {@link ByteSequence}）保持统一的命名风格。</li>
     * </ul>
     * <p>
     * 编码时可选择是否在序列内容前附加总长度或元素个数字段
     * （通过 {@link PrependLengthFieldType} 控制）。
     */
    record Sequence(String name, PrependLengthFieldType prependLengthFieldType, List<DataField> value, @Nullable Map<String, @Nullable Object> attributes) implements DataField {

        public Sequence(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, List<DataField> value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
            this.attributes = attributes;
        }

        @Override
        public String type() {
            return "seq";
        }
    }

    /**
     * 表示协议中的原始字节序列字段。
     * <p>
     * 与通用的 {@code byte[]} 或 {@code ByteArray} 不同，本类型专用于描述协议中的一段
     * 无结构、未经编码的原始字节数据（例如：加密载荷、二进制附件、自定义 TLV 的值等）。
     * <p>
     * 命名为 {@code ByteSequence}（而非 {@code ByteArray}）是为了：
     * <ul>
     *   <li>强调其作为“协议字节流”的语义，而非内存中的数组实现；</li>
     *   <li>与本框架中的 {@link Sequence}（字段序列）命名风格保持一致；</li>
     *   <li>避免与其他库中泛化的“字节数组”概念混淆，突出领域专用性。</li>
     * </ul>
     * <p>
     * 编码时可选择是否在字节内容前附加长度字段（通过 {@link PrependLengthFieldType} 控制）。
     */
    record ByteSequence(
            String name,
            PrependLengthFieldType prependLengthFieldType,

            @JsonSerialize(using = ByteArrayJsonSerializer.class)
            byte[] value,

            @Nullable Map<String, @Nullable Object> attributes
    ) implements DataField {

        public ByteSequence(@Nullable String name, @Nullable PrependLengthFieldType prependLengthFieldType, byte[] value, @Nullable Map<String, @Nullable Object> attributes) {
            this.name = fieldName(name, this);
            this.prependLengthFieldType = requireNonNullElse(prependLengthFieldType, PrependLengthFieldType.none);
            this.value = value;
            this.attributes = attributes;
        }


        @Override
        public String type() {
            return "byte_seq";
        }
    }

    static String fieldName(@Nullable String name, DataField self) {
        if (name == null) {
            return self.getClass().getSimpleName();
        }
        return name;
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

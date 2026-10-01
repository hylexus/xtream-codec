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

package io.github.hylexus.xtream.codec.core.dynamic.schema;

import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 字段构造时使用的局部规则，按数值布局、子结构、派生值、长度和选项分别检查。
 * 此类不接收协议版本、字段位置或扩展注册表；需要这些上下文的校验由编译器承担。
 * 方法参数只传入该组规则涉及的属性，避免用一个长参数列表混合所有字段职责。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicFieldValidation {
    private static final Pattern INTEGER_TYPE = Pattern.compile("[ui][1-9][0-9]?");
    private static final Set<String> OTHER_TYPES = Set.copyOf(Arrays.stream(DynamicFieldType.values()).map(DynamicFieldType::value).toList());
    private static final Set<String> BYTE_TYPES = Set.of(
            DynamicFieldType.STRING.value(),
            DynamicFieldType.BCD.value(),
            DynamicFieldType.HEX.value(),
            DynamicFieldType.BYTES.value(),
            DynamicFieldType.PADDING.value(),
            DynamicFieldType.EXTENSION.value()
    );
    private static final Set<String> VARIANT_TYPES = Set.of(
            DynamicFieldType.TLV.value(),
            DynamicFieldType.MAP.value(),
            DynamicFieldType.PAIR.value(),
            DynamicFieldType.CHOICE.value()
    );

    private DynamicFieldValidation() {
    }

    static void validateNumericLayout(String type, @Nullable Integer bits, @Nullable DynamicEndian endian,
                                      @Nullable Integer alignTo, @Nullable Integer fillBit) {
        final boolean integerName = type != null && INTEGER_TYPE.matcher(type).matches();
        if (!integerName && (type == null || !OTHER_TYPES.contains(type))) {
            throw new IllegalArgumentException("未知的字段类型: " + type);
        }
        if (integerName && bits != null) {
            throw new IllegalArgumentException("整数类型已包含位宽，不能同时声明 bits");
        }
        if ("float".equals(type) || "bits".equals(type)) {
            if (bits == null || bits < 1 || bits > 1048576) {
                throw new IllegalArgumentException("必须指定有效的 bits");
            }
            if ("float".equals(type) && bits != 32 && bits != 64) {
                throw new IllegalArgumentException("浮点数位宽必须为 32 或 64");
            }
        } else if (!integerName && bits != null) {
            throw new IllegalArgumentException("当前类型不能声明 bits");
        }
        final int width = integerName ? Integer.parseInt(type.substring(1)) : bits == null ? 0 : bits;
        if (integerName && width > 64) {
            throw new IllegalArgumentException("整数位宽必须为 1..64");
        }
        if (endian == DynamicEndian.LITTLE && width > 0 && width % 8 != 0) {
            throw new IllegalArgumentException("小端字节序要求完整字节位宽");
        }
        validateRequiredProperty(DynamicFieldType.ALIGN.value().equals(type), alignTo != null,
                "只有 align 类型必须声明 alignTo");
        // 正的二次幂只有一位为 1；n & (n - 1) 会清除最低的 1，因此结果为 0 当且仅当 n 是二次幂。
        // 例如 8 & 7 = 0，而 12 & 11 = 8；前面的范围检查同时排除 0 和负数。
        if (alignTo != null && (alignTo < 1 || alignTo > 64 || (alignTo & (alignTo - 1)) != 0)) {
            throw new IllegalArgumentException("alignTo 必须为不超过 64 的二次幂");
        }
        if (fillBit != null && fillBit != 0 && fillBit != 1) {
            throw new IllegalArgumentException("fillBit 必须为 0 或 1");
        }
    }

    /**
     * 子结构属性由容器类型决定：struct 使用 fields，array 使用 item，分支节点使用 variants。
     */
    static void validateChildren(String type, @Nullable List<DynamicField> fields, @Nullable DynamicField item,
                                 @Nullable Map<String, DynamicField> variants, @Nullable Map<String, Object> options) {
        validateRequiredProperty(DynamicFieldType.STRUCT.value().equals(type), fields != null,
                "struct 必须声明 fields，其他类型不能声明 fields");
        validateRequiredProperty(DynamicFieldType.ARRAY.value().equals(type), item != null,
                "array 必须声明 item，其他类型不能声明 item");
        validateRequiredProperty(VARIANT_TYPES.contains(type), variants != null,
                "条目或选择字段必须声明 variants，其他类型不能声明 variants");
        if ("pair".equals(type) && options != null && options.containsKey("lengthBits")) {
            throw new IllegalArgumentException("pair 不声明条目长度");
        }
    }

    /**
     * 派生值选择位段引用或表达式；位段必须提供偏移和宽度，范围由编译器结合源字段检查。
     */
    static void validateDerived(String type, @Nullable String ref, @Nullable Map<String, Object> options,
                                @Nullable DynamicExpression expression) {
        if ("derived".equals(type) && ref == null && expression == null) {
            throw new IllegalArgumentException("派生字段必须声明 ref 或 expression");
        }
        if ("derived".equals(type) && expression == null && (options == null
                || !options.containsKey("bitOffset") || !options.containsKey("bitWidth")
                || options.get("bitOffset") == null || options.get("bitWidth") == null)) {
            throw new IllegalArgumentException("派生位段必须声明 bitOffset 和 bitWidth");
        }
        if (expression != null && (!"derived".equals(type) || ref != null)) {
            throw new IllegalArgumentException("expression 仅用于派生字段，且不能同时指定 ref");
        }
        if ("choice".equals(type) && ref == null) {
            throw new IllegalArgumentException("choice 必须引用已解析的判别字段");
        }
        if (ref != null && ref.isBlank()) {
            throw new IllegalArgumentException("字段引用不能为空");
        }
    }

    static void validateComputedLength(String type, @Nullable String computedLengthOf) {
        if (computedLengthOf != null && !isUnsignedInteger(type)) {
            throw new IllegalArgumentException("只有无符号整数字段可以回填长度");
        }
        if (computedLengthOf != null && !DynamicIdentifiers.isValidRange(computedLengthOf)) {
            throw new IllegalArgumentException("长度区间必须为字段标识或 start..end");
        }
    }

    static void validateGenerated(String type, @Nullable DynamicGeneratedValue generated, @Nullable String computedLengthOf) {
        if (generated == null) {
            return;
        }
        if (computedLengthOf != null) {
            throw new IllegalArgumentException("generated 与 computedLengthOf 互斥");
        }
        if (generated.countOf() != null || generated.checksum() != null) {
            if (!isUnsignedInteger(type)) {
                throw new IllegalArgumentException("计数和校验和只能用于无符号整数");
            }
            if (generated.checksum() != null && Integer.parseInt(type.substring(1)) != generated.checksum().algorithm().bits()) {
                throw new IllegalArgumentException("校验和字段位宽必须等于算法位宽");
            }
        } else if (!INTEGER_TYPE.matcher(type).matches()
                && !Set.of("float", "string", "bcd", "hex", "bytes", "bits").contains(type)) {
            throw new IllegalArgumentException("固定值与生成表达式只能用于线上标量");
        }
    }

    static void validateCharset(String type, @Nullable String charset) {
        if (charset != null) {
            if (!"string".equals(type)) {
                throw new IllegalArgumentException("只有字符串可以声明 charset");
            }
            try {
                Charset.forName(charset);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("不支持的字符集: " + charset, exception);
            }
        }
    }

    static void validateVariants(@Nullable Map<String, DynamicField> variants) {
        if (variants != null) {
            for (final String key : variants.keySet()) {
                if (!key.matches("[0-9]+")) {
                    throw new IllegalArgumentException("variant 键必须为非负十进制整数");
                }
            }
        }
    }

    private static boolean isUnsignedInteger(String type) {
        return type.startsWith("u") && INTEGER_TYPE.matcher(type).matches();
    }

    static void validateLength(String type, @Nullable DynamicLength length) {
        if ((BYTE_TYPES.contains(type) || Set.of("array", "tlv", "map", "pair").contains(type)) && length == null) {
            throw new IllegalArgumentException("变长字段必须声明边界");
        }
        if (length == null) {
            return;
        }
        if (!BYTE_TYPES.contains(type) && !Set.of("array", "struct", "tlv", "map", "pair").contains(type)) {
            throw new IllegalArgumentException("当前类型不能声明 length");
        }
        if (length.unit() == DynamicLengthUnit.ITEM && !Set.of("array", "tlv", "map", "pair").contains(type)) {
            throw new IllegalArgumentException("item 单位仅可用于集合或条目");
        }
        if (BYTE_TYPES.contains(type) && length.unit() == DynamicLengthUnit.BIT
                && length.fixed() != null && length.fixed() % 8 != 0) {
            throw new IllegalArgumentException("字节字段的 bit 长度必须为 8 的倍数");
        }
    }

    static void validateOptions(@Nullable Map<String, Object> options) {
        if (options == null) {
            return;
        }
        for (final String key : List.of("keyBits", "lengthBits")) {
            final Object width = options.get(key);
            if (width != null && (!(width instanceof Number number) || !Set.of(8, 16, 32).contains(number.intValue())
                    || number.doubleValue() != number.intValue())) {
                throw new IllegalArgumentException(key + " 必须为 8、16 或 32");
            }
        }
        final Object pad = options.get("padByte");
        if (pad != null && (!(pad instanceof Number number) || number.intValue() < 0 || number.intValue() > 255
                || number.doubleValue() != number.intValue())) {
            throw new IllegalArgumentException("padByte 必须为 0..255 的整数");
        }
        final Object side = options.get("padSide");
        if (side != null && !DynamicPaddingSide.LEFT.value().equals(side)
                && !DynamicPaddingSide.RIGHT.value().equals(side)) {
            throw new IllegalArgumentException("padSide 必须为 left 或 right");
        }
        final Object strip = options.get("stripPadding");
        if (strip != null && !(strip instanceof Boolean)) {
            throw new IllegalArgumentException("stripPadding 必须为布尔值");
        }
        final Object lengthUnit = options.get("lengthUnit");
        if (lengthUnit != null && !DynamicLengthUnit.BIT.value().equals(lengthUnit)
                && !DynamicLengthUnit.BYTE.value().equals(lengthUnit)) {
            throw new IllegalArgumentException("回填长度单位必须为 bit 或 byte");
        }
        for (final String key : List.of("bitOffset", "bitWidth")) {
            final Object value = options.get(key);
            if (value != null && (!(value instanceof Number number) || number.doubleValue() != number.intValue()
                    || number.intValue() < ("bitWidth".equals(key) ? 1 : 0))) {
                throw new IllegalArgumentException(key + " 必须为有效的非负整数");
            }
        }
    }

    private static void validateRequiredProperty(boolean required, boolean present, String message) {
        if (required) {
            if (!present) {
                throw new IllegalArgumentException(message);
            }
        } else if (present) {
            throw new IllegalArgumentException(message);
        }
    }

}

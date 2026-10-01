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

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/**
 * 字段边界。固定值、前序字段引用、前置长度和容器剩余量四种来源必须且只能选择一种。
 * {@code unit} 明确表示 bit、byte 或 item；其中 item 表示集合元素个数，不能用于字节标量。
 * 前置长度本身占用的位数由 {@code prefixBits} 指定，不计入所描述的内容长度。
 *
 * @param fixed      固定长度
 * @param ref        保存长度的先前整数值字段标识
 * @param prefixBits 长度前缀自身占用的位数
 * @param remaining  是否使用当前容器的剩余空间
 * @param unit       长度单位：bit、byte 或 item
 * @author Codex (AI)
 * @since 0.11.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DynamicLength(
        @Nullable Integer fixed,
        @Nullable String ref,
        @Nullable Integer prefixBits,
        @Nullable Boolean remaining,
        DynamicLengthUnit unit,
        @Nullable DynamicExpression expression) {
    public DynamicLength {
        if (unit == null) {
            throw new IllegalArgumentException("长度单位不能为空");
        }
        final int sources = (fixed == null ? 0 : 1)
                + (ref == null ? 0 : 1)
                + (prefixBits == null ? 0 : 1)
                + (Boolean.TRUE.equals(remaining) ? 1 : 0)
                + (expression == null ? 0 : 1);
        if (sources != 1) {
            throw new IllegalArgumentException("length 必须且只能指定一种来源");
        }

        if (fixed != null && fixed < 0) {
            throw new IllegalArgumentException("固定长度不能为负数");
        }

        if (ref != null && ref.isBlank()) {
            throw new IllegalArgumentException("长度引用不能为空");
        }

        if (prefixBits != null && prefixBits != 8 && prefixBits != 16 && prefixBits != 32) {
            throw new IllegalArgumentException("长度前缀位宽必须为 8、16 或 32");
        }

        if (Boolean.TRUE.equals(remaining) && unit == DynamicLengthUnit.ITEM) {
            throw new IllegalArgumentException("剩余长度不能使用 item 单位");
        }
    }

    public DynamicLength(
            @Nullable Integer fixed, @Nullable String ref, @Nullable Integer prefixBits,
            @Nullable Boolean remaining, DynamicLengthUnit unit) {
        this(fixed, ref, prefixBits, remaining, unit, null);
    }

    public static DynamicLength expression(DynamicExpression expression, DynamicLengthUnit unit) {
        return new DynamicLength(null, null, null, null, unit, expression);
    }

    /**
     * 字符串兼容入口；规范构造器统一执行长度来源及取值检查。
     */
    public DynamicLength(
            @Nullable Integer fixed, @Nullable String ref, @Nullable Integer prefixBits,
            @Nullable Boolean remaining, String unit) {
        this(fixed, ref, prefixBits, remaining, DynamicLengthUnit.from(unit));
    }

    /**
     * 使用结构定义中写死的长度。
     */
    public static DynamicLength fixed(int value, String unit) {
        return fixed(value, DynamicLengthUnit.from(unit));
    }

    public static DynamicLength fixed(int value, DynamicLengthUnit unit) {
        return new DynamicLength(value, null, null, null, unit);
    }

    /**
     * 使用已解析字段的整数值作为长度。
     */
    public static DynamicLength ref(String id, String unit) {
        return ref(id, DynamicLengthUnit.from(unit));
    }

    public static DynamicLength ref(String id, DynamicLengthUnit unit) {
        return new DynamicLength(null, id, null, null, unit);
    }

    /**
     * 在内容之前读取或写入指定宽度的无符号长度。
     */
    public static DynamicLength prefix(int bits, String unit) {
        return prefix(bits, DynamicLengthUnit.from(unit));
    }

    public static DynamicLength prefix(int bits, DynamicLengthUnit unit) {
        return new DynamicLength(null, null, bits, null, unit);
    }

    /**
     * 使用当前容器的剩余空间，仅适用于容器末尾字段。
     */
    public static DynamicLength remaining(String unit) {
        return remaining(DynamicLengthUnit.from(unit));
    }

    public static DynamicLength remaining(DynamicLengthUnit unit) {
        return new DynamicLength(null, null, null, true, unit);
    }
}

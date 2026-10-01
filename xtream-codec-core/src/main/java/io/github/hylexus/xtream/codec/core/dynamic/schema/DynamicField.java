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

import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompiler;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 有序结构中的一个字段定义。{@code id} 是值对象、字段引用和错误路径使用的稳定标识，
 * {@code name} 仅用于页面展示，允许与其他字段重名。
 * <p>
 * 整数通过 {@code uN}/{@code iN} 声明位宽，浮点数与位串使用 {@code bits}，字节标量使用 {@code length}；容器字段使用
 * {@code fields}、{@code item} 或 {@code variants} 描述子结构。可选属性之间的组合约束
 * 在构造时校验；依赖字段顺序、协议版本或扩展注册表的规则由 {@link DynamicCompiler} 校验。
 *
 * @param id               字段标识，符合 [A-Za-z_][A-Za-z0-9_]*，同级在选定版本中唯一，供值映射和引用使用
 * @param name             可选展示名称，不参与寻址
 * @param type             字段类型，如 u16、i32、struct、array、choice 或 extension
 * @param bits             浮点数或原始位串宽度，单位为 bit；整数位宽由类型名称指定
 * @param endian           整字节数值的字节序，默认为 big
 * @param length           字节标量或容器的边界来源
 * @param charset          字符串字符集，未指定时使用 UTF-8
 * @param bitOrder         字节内部位序，默认为 msb0
 * @param alignTo          对齐节点使用的 bit 边界
 * @param fillBit          对齐节点写入并校验的填充位
 * @param ref              条件分支或派生位段引用的先前字段
 * @param computedLengthOf 编码时要回填长度的后续字段区间
 * @param fields           struct 的有序子字段
 * @param item             array 中每个元素的结构定义
 * @param options          类型专属配置，例如扩展标识或填充规则
 * @param variants         判别键与 choice、TLV、map 或 pair 子结构的映射
 * @param when             基于先前字段值决定是否出现的条件
 * @param expression       不消费报文空间的派生值表达式
 * @param versions         适用的协议版本，省略表示默认定义；精确版本优先于默认定义
 * @param generated        可选的线上字段编码生成规则
 * @param defaultVariant   choice 未命中显式判别键时使用的分支
 * @author Codex (AI)
 * @since 0.11.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DynamicField(
        String id,
        @Nullable String name,
        String type,
        @Nullable Integer bits,
        @Nullable DynamicEndian endian,
        @Nullable DynamicLength length,
        @Nullable String charset,
        @Nullable DynamicBitOrder bitOrder,
        @Nullable Integer alignTo,
        @Nullable Integer fillBit,
        @Nullable String ref,
        @Nullable String computedLengthOf,
        @Nullable List<DynamicField> fields,
        @Nullable DynamicField item,
        @Nullable Map<String, Object> options,
        @Nullable Map<String, DynamicField> variants,
        @Nullable DynamicCondition when,
        @Nullable DynamicExpression expression,
        @Nullable List<Integer> versions,
        @Nullable DynamicGeneratedValue generated,
        @Nullable DynamicField defaultVariant
) {
    public DynamicField {
        if (!DynamicIdentifiers.isValid(id)) {
            throw new IllegalArgumentException("字段 id 必须符合 [A-Za-z_][A-Za-z0-9_]*，实际值: " + id);
        }
        // 冻结嵌套配置，防止编译后的计划仍被调用方持有的集合间接修改。
        fields = fields == null ? null : List.copyOf(fields);
        options = options == null ? null : DynamicData.freeze(options);
        variants = variants == null ? null : Map.copyOf(variants);
        versions = versions == null ? null : List.copyOf(versions);

        // 按职责检查局部规则；同级唯一性、引用可见性和版本覆盖由编译器处理。
        DynamicFieldValidation.validateNumericLayout(type, bits, endian, alignTo, fillBit);
        DynamicFieldValidation.validateChildren(type, fields, item, variants, options);
        DynamicFieldValidation.validateDerived(type, ref, options, expression);
        DynamicFieldValidation.validateComputedLength(type, computedLengthOf);
        DynamicFieldValidation.validateCharset(type, charset);
        DynamicFieldValidation.validateVariants(variants);
        DynamicFieldValidation.validateLength(type, length);
        DynamicFieldValidation.validateOptions(options);
        DynamicFieldValidation.validateGenerated(type, generated, computedLengthOf);
        if (defaultVariant != null && !"choice".equals(type)) {
            throw new IllegalArgumentException("只有 choice 可以声明 defaultVariant");
        }

        if (versions != null && (versions.isEmpty() || versions.size() != Set.copyOf(versions).size())) {
            throw new IllegalArgumentException("versions 不能为空且不能包含重复版本");
        }
    }

    /**
     * 保留没有生成配置和默认分支的原构造入口。
     */
    public DynamicField(String id, @Nullable String name, String type, @Nullable Integer bits,
                        @Nullable DynamicEndian endian, @Nullable DynamicLength length, @Nullable String charset,
                        @Nullable DynamicBitOrder bitOrder, @Nullable Integer alignTo, @Nullable Integer fillBit,
                        @Nullable String ref, @Nullable String computedLengthOf, @Nullable List<DynamicField> fields,
                        @Nullable DynamicField item, @Nullable Map<String, Object> options,
                        @Nullable Map<String, DynamicField> variants, @Nullable DynamicCondition when,
                        @Nullable DynamicExpression expression, @Nullable List<Integer> versions) {
        this(id, name, type, bits, endian, length, charset, bitOrder, alignTo, fillBit, ref, computedLengthOf,
                fields, item, options, variants, when, expression, versions, null, null);
    }

    /**
     * 创建 Java 结构定义入口。build 时检查字段局部规则，引用关系在编译阶段校验。
     */
    public static Builder field(String id, String type) {
        return new Builder(id, type);
    }

    /**
     * 使用枚举选择固定名称的字段类型；整数仍以带位宽的名称声明。
     */
    public static Builder field(String id, DynamicFieldType type) {
        return new Builder(id, type.value());
    }

    /**
     * 字段定义构造器，支持按节点类型逐项声明可选属性。
     *
     * @author Codex (AI)
     * @since 0.11.0
     */
    public static final class Builder {
        // Builder 收集属性；build 调用规范构造器统一检查字段局部规则。
        private final String id;
        private final String type;
        private @Nullable String name;
        private @Nullable Integer bits;
        private @Nullable DynamicEndian endian;
        private @Nullable DynamicLength length;
        private @Nullable String charset;
        private @Nullable DynamicBitOrder bitOrder;
        private @Nullable Integer alignTo;
        private @Nullable Integer fillBit;
        private @Nullable String ref;
        private @Nullable String computedLengthOf;
        private @Nullable List<DynamicField> fields;
        private @Nullable DynamicField item;
        private @Nullable Map<String, Object> options;
        private @Nullable Map<String, DynamicField> variants;
        private @Nullable DynamicCondition when;
        private @Nullable DynamicExpression expression;
        private @Nullable List<Integer> versions;
        private @Nullable DynamicGeneratedValue generated;
        private @Nullable DynamicField defaultVariant;

        private Builder(String id, String type) {
            this.id = id;
            this.type = type;
        }


        /**
         * 设置展示名称，允许与其他字段重名。
         */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /**
         * 设置 float 或 bits 节点的位宽；整数请直接使用 u16、i32 等类型名称。
         */
        public Builder bits(int value) {
            this.bits = value;
            return this;
        }

        /**
         * 设置数值字节序：big 或 little。
         */
        public Builder endian(String value) {
            this.endian = DynamicEndian.from(value);
            return this;
        }

        public Builder endian(DynamicEndian value) {
            this.endian = value;
            return this;
        }

        /**
         * 设置字节标量或容器的边界，集合也可使用 item 单位。
         */
        public Builder length(DynamicLength value) {
            this.length = value;
            return this;
        }

        /**
         * 设置 string 节点的字符集。
         */
        public Builder charset(String value) {
            this.charset = value;
            return this;
        }

        /**
         * 设置字节内位序：msb0 或 lsb0，共享字节内必须保持一致。
         */
        public Builder bitOrder(String value) {
            this.bitOrder = DynamicBitOrder.from(value);
            return this;
        }

        public Builder bitOrder(DynamicBitOrder value) {
            this.bitOrder = value;
            return this;
        }

        /**
         * 设置 align 节点的 bit 对齐边界，必须为不超过 64 的二次幂。
         */
        public Builder alignTo(int value) {
            this.alignTo = value;
            return this;
        }

        /**
         * 设置 align 节点的填充位，解码时也校验该位。
         */
        public Builder fillBit(int value) {
            this.fillBit = value;
            return this;
        }

        /**
         * 设置 derived 或 choice 节点使用的前序字段引用。
         */
        public Builder ref(String value) {
            this.ref = value;
            return this;
        }

        /**
         * 设置无符号长度字段的回填区间，可使用单字段或 start..end。
         */
        public Builder computedLengthOf(String value) {
            this.computedLengthOf = value;
            return this;
        }

        /**
         * 设置 struct 的有序子字段。
         */
        public Builder fields(DynamicField... value) {
            this.fields = List.of(value);
            return this;
        }

        /**
         * 设置 array 的元素结构，所有元素使用相同定义。
         */
        public Builder item(DynamicField value) {
            this.item = value;
            return this;
        }

        /**
         * 设置节点专属选项；构建字段时会递归冻结其中的 JSON 容器。
         */
        public Builder options(Map<String, Object> value) {
            this.options = value;
            return this;
        }

        /**
         * 设置十进制判别键到分支结构的映射。
         */
        public Builder variants(Map<String, DynamicField> value) {
            this.variants = value;
            return this;
        }

        /**
         * 设置字段出现条件，为假时不读取也不写入该字段。
         */
        public Builder when(DynamicCondition value) {
            this.when = value;
            return this;
        }

        /**
         * 设置 derived 节点的派生表达式，与源字段 ref 互斥。
         */
        public Builder expression(DynamicExpression value) {
            this.expression = value;
            return this;
        }

        /**
         * 设置适用的协议版本。省略此调用表示默认定义；同一标识的精确版本定义优先。
         * ALL_VERSION 表示默认匹配，不表示同时处理所有版本的布局。
         */
        public Builder versions(Integer... value) {
            this.versions = List.of(value);
            return this;
        }

        /**
         * 设置线上字段的生成规则，与 computedLengthOf 互斥。
         */
        public Builder generated(DynamicGeneratedValue value) {
            this.generated = value;
            return this;
        }

        /**
         * 设置 choice 未命中显式判别键时的默认分支。
         */
        public Builder defaultVariant(DynamicField value) {
            this.defaultVariant = value;
            return this;
        }

        /**
         * 创建不可变定义；跨字段关系交给编译器检查。
         */
        public DynamicField build() {
            return new DynamicField(
                    id, name, type, bits, endian, length, charset, bitOrder,
                    alignTo, fillBit, ref, computedLengthOf, fields, item, options, variants, when, expression, versions,
                    generated, defaultVariant
            );
        }
    }
}

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
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 基于已经可用字段值的声明式条件。条件为假时，该字段既不消费输入，也不写入输出。
 * {@code eq}/{@code ne} 比较精确字符串；数值比较和位测试将 {@code value} 解释为十进制整数。
 *
 * @param ref   已解析字段的标识
 * @param op    比较或位测试运算符
 * @param value 与字段值比较的字符串或十进制整数
 * @author Codex (AI)
 * @since 0.11.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DynamicCondition(
        @Nullable String ref,
        @Nullable DynamicConditionOperator op,
        @Nullable String value,
        @Nullable DynamicExpression expression) {

    public DynamicCondition {
        if (expression != null) {
            if (ref != null || op != null || value != null) {
                throw new IllegalArgumentException("条件表达式与简写条件互斥");
            }
        } else {
            if (ref == null || ref.isBlank()) {
                throw new IllegalArgumentException("条件引用不能为空");
            }
            if (op == null) {
                throw new IllegalArgumentException("条件运算符不能为空");
            }
            if (value == null) {
                throw new IllegalArgumentException("条件值不能为空");
            }
            if (op != DynamicConditionOperator.EQ && op != DynamicConditionOperator.NE) {
                try {
                    new java.math.BigInteger(value);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("条件值必须为十进制整数", exception);
                }
            }
        }
    }

    public DynamicCondition(String ref, DynamicConditionOperator op, String value) {
        this(ref, op, value, null);
    }

    public static DynamicCondition expression(DynamicExpression expression) {
        return new DynamicCondition(null, null, null, expression);
    }

    public DynamicCondition(String ref, String op, String value) {
        this(ref, DynamicConditionOperator.from(op), value);
    }
}

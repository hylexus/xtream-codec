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

import java.util.List;
import java.util.Map;

/**
 * 表达式定义：受控树支持字段引用、JSON 常量或已知运算，也支持显式引擎表达式。
 * 引用只访问执行到当前位置时已经解析的值；运算符及参数数量在构造时校验。
 * 默认使用受控运算；engine/source/bindings 形式仅在宿主显式提供引擎时可用。
 *
 * @param ref      先前字段的引用标识
 * @param value    JSON 可表达的标量常量
 * @param op       内置运算符名称
 * @param args     运算所需的子表达式
 * @param engine   宿主注册的引擎标识
 * @param source   引擎表达式源码
 * @param bindings 表达式变量到 DSL 字段的显式绑定
 * @author Codex (AI)
 * @since 0.11.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DynamicExpression(
        @Nullable String ref,
        @Nullable Object value,
        @Nullable DynamicExpressionOperator op,
        @Nullable List<DynamicExpression> args,
        @Nullable String engine,
        @Nullable String source,
        @Nullable Map<String, String> bindings
) {
    public DynamicExpression {
        bindings = bindings == null ? null : Map.copyOf(bindings);
        if (engine != null) {
            if (engine.isBlank() || source == null || source.isBlank() || bindings == null
                    || ref != null || value != null || op != null || args != null) {
                throw new IllegalArgumentException("引擎表达式必须仅声明 engine、source 和 bindings");
            }
            bindings.forEach((name, target) -> {
                if (!DynamicIdentifiers.isValid(name) || "self".equals(name) || target.isBlank()) {
                    throw new IllegalArgumentException("无效的表达式绑定: " + name);
                }
            });
        } else if (source != null || bindings != null) {
            throw new IllegalArgumentException("source 和 bindings 必须与 engine 一起声明");
        }

        // 常量保持 JSON 可表达的简单类型，避免引入隐式对象转换或执行能力。
        args = args == null ? null : List.copyOf(args);
        if (value != null && !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
            throw new IllegalArgumentException("表达式常量只接受字符串、数值或布尔值");
        }

        final int sources = (ref == null ? 0 : 1) + (value == null ? 0 : 1) + (op == null ? 0 : 1) + (engine == null ? 0 : 1);
        if (sources != 1) {
            throw new IllegalArgumentException("表达式必须且只能声明 ref、value、op 或 engine");
        }

        if (ref != null && ref.isBlank()) {
            throw new IllegalArgumentException("表达式引用不能为空");
        }

        if (op != null && (args == null || args.size() != (op == DynamicExpressionOperator.NOT
                || op == DynamicExpressionOperator.BIT_NOT ? 1 : 2))) {
            throw new IllegalArgumentException("表达式参数数量无效");
        }

        if (op == null && args != null) {
            throw new IllegalArgumentException("只有 op 表达式可以声明 args");
        }

        if (args != null) {
            // 最大 4096
            final int[] remaining = {4095};
            args.forEach(arg -> validateDepth(arg, 1, remaining));
        }
    }

    public DynamicExpression(
            @Nullable String ref,
            @Nullable Object value,
            @Nullable DynamicExpressionOperator op,
            @Nullable List<DynamicExpression> args) {
        this(ref, value, op, args, null, null, null);
    }

    public static DynamicExpression engine(String engine, String source, Map<String, String> bindings) {
        return new DynamicExpression(null, null, null, null, engine, source, bindings);
    }

    private static void validateDepth(DynamicExpression expression, int depth, int[] remaining) {
        if (--remaining[0] < 0) {
            throw new IllegalArgumentException("表达式节点数量超过 4096");
        }
        if (depth > 32) {
            throw new IllegalArgumentException("表达式深度超过 32");
        }
        if (expression.args() != null) {
            for (DynamicExpression arg : expression.args()) {
                validateDepth(arg, depth + 1, remaining);
            }
        }
    }

    public static DynamicExpression ref(String id) {
        return new DynamicExpression(id, null, null, null);
    }

    public static DynamicExpression value(Object value) {
        return new DynamicExpression(null, value, null, null);
    }

    public static DynamicExpression op(String op, DynamicExpression... args) {
        return op(DynamicExpressionOperator.from(op), args);
    }

    public static DynamicExpression op(DynamicExpressionOperator op, DynamicExpression... args) {
        return new DynamicExpression(null, null, op, List.of(args));
    }
}

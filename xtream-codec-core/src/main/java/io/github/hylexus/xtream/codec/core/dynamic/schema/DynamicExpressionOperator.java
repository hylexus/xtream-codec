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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Set;

/**
 * 受控表达式支持的有限运算符集合，接受英文名称和符号别名，序列化统一使用英文名称。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DynamicExpressionOperator {
    ADD("add", "+"),
    SUB("sub", "-"),
    MUL("mul", "*"),
    DIV("div", "/"),
    MOD("mod", "%"),
    BIT_AND("bitAnd", "&"),
    BIT_OR("bitOr", "|"),
    BIT_XOR("bitXor", "^", "xor"),
    BIT_NOT("bitNot", "~"),
    SHL("shl", "<<"),
    SHR("shr", ">>"),
    EQ("eq", "=="),
    NE("ne", "!="),
    GT("gt", ">"),
    GTE("gte", ">="),
    LT("lt", "<"),
    LTE("lte", "<="),
    AND("and", "&&", "all"),
    OR("or", "||", "any"),
    NOT("not", "!"),
    ;

    private final String value;
    private final String symbol;
    private final Set<String> aliases;

    DynamicExpressionOperator(String value, String symbol, String... aliases) {
        this.value = value;
        this.symbol = symbol;
        this.aliases = Set.of(aliases);
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static DynamicExpressionOperator from(String value) {
        for (final DynamicExpressionOperator candidate : values()) {
            if (candidate.value.equals(value) || candidate.symbol.equals(value) || (value != null && candidate.aliases.contains(value))) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("未知表达式运算: " + value);
    }
}

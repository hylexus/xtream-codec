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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicCondition;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpressionOperator;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 有限表达式树的计算与拒绝规则测试。
 * 检查算术、布尔和位运算结果，同时确认派生计算不扩大报文消费量。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExpressionTest {
    @ParameterizedTest
    @CsvSource({
            "add, +, 8", "sub, -, 4", "mul, *, 12", "div, /, 3", "mod, %, 0",
            "bitAnd, &, 2", "bitOr, |, 6", "bitXor, ^, 4", "bitNot, ~, -7", "shl, <<, 24", "shr, >>, 1",
            "eq, ==, false", "ne, !=, true", "gt, >, true", "gte, >=, true", "lt, <, false", "lte, <=, false",
            "and, &&, false", "or, ||, true", "not, !, false"
    })
    void shouldAcceptSymbolAliasesAndSerializeCanonicalNames(String name, String symbol, String expected) {
        final DynamicExpressionOperator operator = DynamicExpressionOperator.from(name);
        assertSame(operator, DynamicExpressionOperator.from(symbol));
        final DynamicExpression[] arguments = switch (operator) {
            case AND, OR -> new DynamicExpression[]{DynamicExpression.value(true), DynamicExpression.value(false)};
            case NOT -> new DynamicExpression[]{DynamicExpression.value(true)};
            case BIT_NOT -> new DynamicExpression[]{DynamicExpression.value(6)};
            default -> new DynamicExpression[]{DynamicExpression.value(6), DynamicExpression.value(2)};
        };
        final DynamicExpression canonical = DynamicExpression.op(name, arguments);
        assertEquals(canonical, DynamicExpression.op(symbol, arguments));
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("result", "derived").expression(canonical).build());
        final String canonicalJson = DynamicSchemaJson.write(schema);
        assertTrue(canonicalJson.contains("\"op\":\"" + name + "\""));
        final String symbolJson = canonicalJson.replace("\"op\":\"" + name + "\"", "\"op\":\"" + symbol + "\"");
        final DynamicSchema restored = DynamicSchemaJson.read(symbolJson);
        assertEquals(schema, restored);
        assertEquals(canonicalJson, DynamicSchemaJson.write(restored));
        final DynamicPlan plan = DynamicCompiler.compile(restored);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertEquals(0, plan.encode(DynamicValues.of(Map.of()), buffer));
            final DynamicDecodeResult result = plan.decode(buffer);
            assertEquals(expected, String.valueOf(result.values().fields().get("result")));
            assertEquals(0, result.consumedBits());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldComposeArithmeticBooleanAndBitOperationsWithoutConsumingInput() {
        final DynamicExpression masked = DynamicExpression.op("bitAnd", DynamicExpression.ref("flags"), DynamicExpression.value(4));
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("flags", "u8").build(),
                DynamicField.field("sum", "derived").expression(DynamicExpression.op("add",
                        DynamicExpression.ref("flags"), DynamicExpression.value(2))).build(),
                DynamicField.field("enabled", "derived").expression(DynamicExpression.op("and",
                        DynamicExpression.op("eq", masked, DynamicExpression.value(4)),
                        DynamicExpression.op("gt", DynamicExpression.ref("sum"), DynamicExpression.value(5)))).build(),
                DynamicField.field("optional", "u8")
                        .when(new DynamicCondition("enabled", "eq", "true")).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("flags", 4, "optional", 9)), buffer);
            final DynamicDecodeResult result = plan.decode(buffer);
            assertEquals(16, result.consumedBits());
            assertEquals(6L, result.values().fields().get("sum"));
            assertEquals(true, result.values().fields().get("enabled"));
            assertEquals(9L, result.values().fields().get("optional"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldComplementUnboundedIntegersAndAllowExplicitMasking() {
        assertThrows(IllegalArgumentException.class, () -> DynamicExpression.op("~"));
        assertThrows(IllegalArgumentException.class, () -> DynamicExpression.op("bitNot",
                DynamicExpression.value(1), DynamicExpression.value(2)));

        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("zero", "derived").expression(DynamicExpression.op("~",
                        DynamicExpression.value(0))).build(),
                DynamicField.field("negative", "derived").expression(DynamicExpression.op("bitNot",
                        DynamicExpression.value(-1))).build(),
                DynamicField.field("masked", "derived").expression(DynamicExpression.op("bitAnd",
                        DynamicExpression.op("~", DynamicExpression.value(6)), DynamicExpression.value(255))).build(),
                DynamicField.field("large", "derived").expression(DynamicExpression.op("bitNot",
                        DynamicExpression.value("18446744073709551615"))).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertEquals(0, plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals(Map.of("zero", -1L, "negative", 0L, "masked", 249L,
                    "large", "-18446744073709551616"), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectNonIntegerBitwiseComplementOperand() {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("value", "derived").expression(DynamicExpression.op("~",
                        DynamicExpression.value(true))).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final DynamicCodecException error = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals("$.value", error.fieldPath());
            assertEquals(0, error.bitOffset());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectUnknownOperationAndReference() {
        assertThrows(IllegalArgumentException.class, () -> DynamicExpression.op("eval",
                DynamicExpression.value("Runtime")));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("bad", "derived").expression(DynamicExpression.ref("missing")).build())));
    }

    @ParameterizedTest
    @CsvSource({"all, and", "any, or", "xor, bitXor"})
    void shouldNormalizeLegacyAliasesDuringJsonImport(String alias, String canonicalName) {
        final DynamicExpressionOperator canonical = DynamicExpressionOperator.from(canonicalName);
        assertSame(canonical, DynamicExpressionOperator.from(alias));
        final DynamicExpression[] arguments = canonical == DynamicExpressionOperator.BIT_XOR
                ? new DynamicExpression[]{DynamicExpression.value(6), DynamicExpression.value(2)}
                : new DynamicExpression[]{DynamicExpression.value(true), DynamicExpression.value(false)};
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("value", "derived")
                .expression(DynamicExpression.op(canonical, arguments)).build());
        final String json = DynamicSchemaJson.write(schema);
        final DynamicSchema restored = DynamicSchemaJson.read(json.replace("\"op\":\"" + canonicalName + "\"",
                "\"op\":\"" + alias + "\""));
        assertEquals(schema, restored);
        assertEquals(json, DynamicSchemaJson.write(restored));
    }

    @ParameterizedTest
    @CsvSource({"and, 0, true", "and, 1, true", "and, true, 1", "or, 0, false", "or, 1, false", "or, false, 0"})
    void shouldRejectNonBooleanLogicalOperands(String op, String left, String right) {
        final DynamicSchema schema = DynamicSchemaJson.read("""
                {"schemaVersion":1,"fields":[{"id":"value","type":"derived","expression":
                  {"op":"%s","args":[{"value":%s},{"value":%s}]}}]}
                """.formatted(op, left, right));
        final DynamicPlan plan = DynamicCompiler.compile(schema);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            final var encodeError = assertThrows(DynamicCodecException.class,
                    () -> plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals("$.value", encodeError.fieldPath());
            assertEquals(0, encodeError.bitOffset());
            assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"and, or", "&&, ||", "all, any"})
    void shouldShortCircuitBooleanExpressions(String and, String or) {
        // 右侧除零表达式不能执行；这也验证求值器拆分后仍保持短路语义。
        final DynamicExpression invalidRight = DynamicExpression.op("/",
                DynamicExpression.value(1), DynamicExpression.value(0));
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("all", "derived").expression(DynamicExpression.op(and,
                        DynamicExpression.value(false), invalidRight)).build(),
                DynamicField.field("any", "derived").expression(DynamicExpression.op(or,
                        DynamicExpression.value(true), invalidRight)).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertEquals(0, plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals(Map.of("all", false, "any", true), plan.decode(buffer).values().fields());
        } finally {
            buffer.release();
        }
    }
}

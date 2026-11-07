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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpression;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicExpressionOperator;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对照 Java 和 JSON 入口验证表达式定义的校验、集合隔离及树复杂度边界。
 *
 * @author Codex (AI)
 */
class DynamicExpressionDefinitionTest {

    @ParameterizedTest
    @MethodSource("invalidDefinitions")
    void rejectsMalformedDefinitionsInJavaAndJson(Map<String, Object> definition, String message) throws Exception {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> construct(definition));
        assertEquals(message, error.getMessage());
        assertJsonRejected(new ObjectMapper().writeValueAsString(definition), message);
    }

    static Stream<Arguments> invalidDefinitions() {
        final String sources = "表达式必须且只能声明 ref、value、op 或 engine";
        final String engine = "引擎表达式必须仅声明 engine、source 和 bindings";
        final Map<String, Object> validEngine = Map.of("engine", "SpEL", "source", "1", "bindings", Map.of());
        final Stream<Arguments> forms = Stream.of(
                Arguments.of(Map.of(), sources),
                Arguments.of(Map.of("ref", "n", "value", 1), sources),
                Arguments.of(Map.of("ref", "n", "op", "not", "args", List.of(DynamicExpression.value(true))), sources),
                Arguments.of(Map.of("value", true, "op", "not", "args", List.of(DynamicExpression.value(true))), sources),
                Arguments.of(Map.of("ref", ""), "表达式引用不能为空"),
                Arguments.of(Map.of("ref", " \t"), "表达式引用不能为空"),
                Arguments.of(Map.of("value", Map.of("key", 1)), "表达式常量只接受字符串、数值或布尔值"),
                Arguments.of(Map.of("value", List.of(1)), "表达式常量只接受字符串、数值或布尔值"),
                Arguments.of(Map.of("ref", "n", "args", List.of()), "只有 op 表达式可以声明 args"),
                Arguments.of(Map.of("value", 1, "args", List.of()), "只有 op 表达式可以声明 args"),
                Arguments.of(Map.of("source", "1"), "source 和 bindings 必须与 engine 一起声明"),
                Arguments.of(Map.of("bindings", Map.of()), "source 和 bindings 必须与 engine 一起声明"),
                Arguments.of(Map.of("engine", "", "source", "1", "bindings", Map.of()), engine),
                Arguments.of(Map.of("engine", " \t", "source", "1", "bindings", Map.of()), engine),
                Arguments.of(Map.of("engine", "SpEL", "bindings", Map.of()), engine),
                Arguments.of(Map.of("engine", "SpEL", "source", "", "bindings", Map.of()), engine),
                Arguments.of(Map.of("engine", "SpEL", "source", " \t", "bindings", Map.of()), engine),
                Arguments.of(Map.of("engine", "SpEL", "source", "1"), engine));
        final Stream<Arguments> conflicts = Map.<String, Object>of(
                        "ref", "n", "value", 1, "op", "add", "args", List.of())
                .entrySet().stream().map(entry -> {
                    final Map<String, Object> definition = new LinkedHashMap<>(validEngine);
                    definition.put(entry.getKey(), entry.getValue());
                    return Arguments.of(definition, engine);
                });
        return Stream.concat(forms, conflicts);
    }

    @ParameterizedTest
    @CsvSource(value = {"'',n", "0name,n", "has-dash,n", "中文,n", "self,n", "valid,''", "valid,' '"})
    void rejectsInvalidEngineBindingNamesAndBlankTargets(String name, String target) throws Exception {
        final Map<String, Object> definition = Map.of("engine", "SpEL", "source", "1", "bindings", Map.of(name, target));
        final String message = "无效的表达式绑定: " + name;
        assertEquals(message, assertThrows(IllegalArgumentException.class, () -> construct(definition)).getMessage());
        assertJsonRejected(new ObjectMapper().writeValueAsString(definition), message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"_", "_n1", "Name2"})
    void acceptsValidBindingIdentifiersAndPreservesDefinitions(String name) {
        final DynamicExpression expression = DynamicExpression.engine("SpEL", "1", Map.of(name, "sourceField"));
        assertEquals(Map.of(name, "sourceField"), expression.bindings());
        assertRoundTrip(expression);
    }

    @ParameterizedTest
    @EnumSource(value = DynamicExpressionOperator.class, names = {"NOT", "BIT_NOT", "ADD"})
    void enforcesUnaryAndBinaryArgumentCountsInBothEntrances(DynamicExpressionOperator operator) throws Exception {
        final int required = operator == DynamicExpressionOperator.ADD ? 2 : 1;
        final Map<String, Object> missingArgs = Map.of("op", operator.value());
        assertThrows(IllegalArgumentException.class, () -> construct(missingArgs));
        assertJsonRejected(new ObjectMapper().writeValueAsString(missingArgs), "表达式参数数量无效");
        for (int count = 0; count <= 3; count++) {
            final List<DynamicExpression> args = Collections.nCopies(count, DynamicExpression.value(1));
            final Map<String, Object> definition = Map.of("op", operator.value(), "args", args);
            if (count == required) {
                assertRoundTrip(assertDoesNotThrow(() -> construct(definition)));
            } else {
                final IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> construct(definition));
                assertEquals("表达式参数数量无效", error.getMessage());
                assertJsonRejected(new ObjectMapper().writeValueAsString(definition), "表达式参数数量无效");
            }
        }
    }

    @Test
    void argumentAndBindingInputsAreCopiedAndReturnedCollectionsAreReadOnly() {
        final List<DynamicExpression> args = new ArrayList<>(List.of(DynamicExpression.value(1), DynamicExpression.value(2)));
        final DynamicExpression sum = new DynamicExpression(null, null, DynamicExpressionOperator.ADD, args);
        args.set(0, DynamicExpression.value(100));
        args.clear();
        assertEquals(List.of(DynamicExpression.value(1), DynamicExpression.value(2)), sum.args());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(sum.args()).clear());
        assertEvaluatesWithoutInput(sum, 3L);
        assertRoundTrip(sum);

        final Map<String, String> bindings = new LinkedHashMap<>(Map.of("n", "count"));
        final DynamicExpression engine = DynamicExpression.engine("SpEL", "#n", bindings);
        bindings.put("n", "other");
        bindings.clear();
        assertEquals(Map.of("n", "count"), engine.bindings());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(engine.bindings()).put("n", "other"));
        assertThrows(UnsupportedOperationException.class,
                () -> Objects.requireNonNull(engine.bindings()).entrySet().iterator().next().setValue("other"));
        assertRoundTrip(engine);
    }

    @Test
    void acceptsDepth32AndRejectsDepth33WithNodeCountWellBelowLimit() throws Exception {
        DynamicExpression expression = DynamicExpression.value(0);
        for (int depth = 0; depth < 32; depth++) {
            expression = DynamicExpression.op("bitNot", expression);
        }
        assertEquals(32, treeDepth(expression));
        assertEquals(33, nodeCount(expression));
        assertRoundTrip(expression);
        assertEvaluatesWithoutInput(expression, 0L);
        final DynamicExpression limit = expression;
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DynamicExpression.op("bitNot", limit));
        assertEquals("表达式深度超过 32", error.getMessage());
        final String excessive = "{\"op\":\"bitNot\",\"args\":[" + new ObjectMapper().writeValueAsString(limit) + "]}";
        assertJsonRejected(excessive, "表达式深度超过 32");
    }

    @Test
    void accepts4096NodesAndRejects4097WithDepthWellBelowLimit() throws Exception {
        final DynamicExpression binary = balancedSum(11);
        assertEquals(4095, nodeCount(binary));
        assertEquals(11, treeDepth(binary));
        final DynamicExpression limit = DynamicExpression.op("bitNot", binary);
        assertEquals(4096, nodeCount(limit));
        assertEquals(12, treeDepth(limit));
        assertRoundTrip(limit);
        assertEvaluatesWithoutInput(limit, -2049L);

        // 两个子树各自合法，仅在合并为同一棵树时超过总节点限制。
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DynamicExpression.op("add", binary, DynamicExpression.value(1)));
        assertEquals("表达式节点数量超过 4096", error.getMessage());
        final String excessive = "{\"op\":\"add\",\"args\":["
                + new ObjectMapper().writeValueAsString(binary) + ",{\"value\":1}]}";
        assertJsonRejected(excessive, "表达式节点数量超过 4096");
    }

    private static DynamicExpression balancedSum(int depth) {
        if (depth == 0) {
            return DynamicExpression.value(1);
        }
        return DynamicExpression.op("add", balancedSum(depth - 1), balancedSum(depth - 1));
    }

    private static int nodeCount(DynamicExpression expression) {
        final List<DynamicExpression> args = expression.args();
        return 1 + (args == null ? 0 : args.stream().mapToInt(DynamicExpressionDefinitionTest::nodeCount).sum());
    }

    private static int treeDepth(DynamicExpression expression) {
        final List<DynamicExpression> args = expression.args();
        return args == null ? 0 : 1 + args.stream().mapToInt(DynamicExpressionDefinitionTest::treeDepth).max().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static DynamicExpression construct(Map<String, Object> definition) {
        return new DynamicExpression((String) definition.get("ref"), definition.get("value"),
                definition.containsKey("op") ? DynamicExpressionOperator.from((String) definition.get("op")) : null,
                (List<DynamicExpression>) definition.get("args"), (String) definition.get("engine"),
                (String) definition.get("source"), (Map<String, String>) definition.get("bindings"));
    }

    private static void assertJsonRejected(String expressionJson, String message) {
        final String schemaJson = "{\"schemaVersion\":1,\"fields\":[{\"id\":\"result\",\"type\":\"derived\",\"expression\":"
                + expressionJson + "}]}";
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read(schemaJson));
        final Throwable cause = Objects.requireNonNull(error.getCause());
        assertTrue(cause.getMessage().contains(message), cause::getMessage);
    }

    private static void assertRoundTrip(DynamicExpression expression) {
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("result", "derived").expression(expression).build());
        assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
    }

    private static void assertEvaluatesWithoutInput(DynamicExpression expression, long expected) {
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("result", "derived").expression(expression).build()));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertEquals(0, plan.encode(DynamicValues.of(Map.of()), buffer));
            assertEquals(expected, plan.decode(buffer).values().fields().get("result"));
            assertEquals(0, buffer.readerIndex());
            assertEquals(0, buffer.writerIndex());
        } finally {
            buffer.release();
        }
    }
}

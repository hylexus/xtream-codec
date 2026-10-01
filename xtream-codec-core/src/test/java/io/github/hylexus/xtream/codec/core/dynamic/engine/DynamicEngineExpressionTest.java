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

import io.github.hylexus.xtream.codec.base.expression.*;
import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.dynamic.spi.*;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 引擎表达式的绑定、结果合同及并发执行隔离。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicEngineExpressionTest {
    private static DynamicCompileOptions options(XtreamExpressionEngine engine) {
        final var registry = new XtreamExpressionEngineRegistry(false);
        registry.register(engine);
        return new DynamicCompileOptions(DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, registry);
    }

    private static DynamicExpression expression(XtreamExpressionEngine engine, String source, Map<String, String> bindings) {
        return DynamicExpression.engine(engine.id().value(), source, bindings);
    }

    @Test
    void builtinEnginesMustSupportConditionsLengthsAndDerivedValues() throws Exception {
        for (final var engine : List.of(new SpelXtreamExpressionEngine(), new AviatorXtreamExpressionEngine(), new MvelXtreamExpressionEngine())) {
            final String n = engine instanceof SpelXtreamExpressionEngine ? "#n" : "n";
            final var binding = Map.of("n", "count");
            final var schema = DynamicSchema.of(
                    DynamicField.field("count", "u8").build(),
                    DynamicField.field("payload", "hex")
                            .length(DynamicLength.expression(expression(engine, n, binding), DynamicLengthUnit.BYTE))
                            .when(DynamicCondition.expression(expression(engine, n + " > 0", binding))).build(),
                    DynamicField.field("sum", "derived").expression(expression(engine, n + " + 2", binding)).build());
            final var countPlan = DynamicCompiler.compile(DynamicSchema.of(
                    DynamicField.field("count", "u8").build(),
                    DynamicField.field("items", "array").length(DynamicLength.expression(
                            expression(engine, n, binding), DynamicLengthUnit.ITEM))
                            .item(DynamicField.field("item", "u8").build()).build()), options(engine));
            final var counted = Unpooled.buffer();
            try {
                countPlan.encode(DynamicValues.of(Map.of("count", 2, "items", List.of(3, 4))), counted);
                assertEquals("020304", ByteBufUtil.hexDump(counted));
                assertEquals(List.of(3L, 4L), countPlan.decode(counted).values().fields().get("items"));
            } finally {
                counted.release();
            }
            final var restored = DynamicSchemaJson.read(DynamicSchemaJson.write(schema));
            assertEquals(schema, restored);
            final var plan = DynamicCompiler.compile(restored, options(engine));
            try (final var executor = Executors.newFixedThreadPool(4)) {
                final List<Callable<Void>> calls = new ArrayList<>();
                for (int index = 0; index < 32; index++) {
                    final int count = index % 4;
                    calls.add(() -> {
                        final ByteBuf buffer = Unpooled.buffer();
                        try {
                            final var values = DynamicValues.of(Map.of("count", count, "payload", "ab".repeat(count)));
                            plan.encode(values, buffer);
                            assertEquals(String.format("%02x", count) + "ab".repeat(count), ByteBufUtil.hexDump(buffer));
                            final var result = plan.decode(buffer).values().fields();
                            assertEquals(count + 2L, ((Number) result.get("sum")).longValue());
                            assertEquals(count > 0, result.containsKey("payload"));
                        } finally {
                            buffer.release();
                        }
                        return null;
                    });
                }
                for (final var future : executor.invokeAll(calls)) {
                    future.get();
                }
            }
        }
    }

    @Test
    void rejectDisabledEnginesAndInvalidBindingsAtCompileTime() {
        final var expression = DynamicExpression.engine("SpEL", "#n", Map.of("n", "count"));
        final var schema = DynamicSchema.of(DynamicField.field("count", "u8").build(),
                DynamicField.field("result", "derived").expression(expression).build());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("result", "derived").expression(expression).build()), options(new SpelXtreamExpressionEngine())));
        assertThrows(IllegalArgumentException.class, () -> DynamicExpression.engine("SpEL", "1", Map.of("self", "count")));
    }

    @Test
    void rejectMutuallyExclusiveFormsAndVersionInvisibleBindings() {
        for (final String json : List.of(
                """
                {"schemaVersion":1,"fields":[{"id":"v","type":"derived",
                "expression":{"engine":"SpEL","source":"1","bindings":{},"value":1}}]}
                """,
                """
                {"schemaVersion":1,"fields":[{"id":"v","type":"u8",
                "when":{"ref":"n","op":"eq","value":"1","expression":{"value":true}}}]}
                """,
                """
                {"schemaVersion":1,"fields":[{"id":"v","type":"hex",
                "length":{"fixed":1,"expression":{"value":1},"unit":"byte"}}]}
                """)) {
            assertThrows(IllegalArgumentException.class, () -> DynamicSchemaJson.read(json));
        }
        final var engine = new SpelXtreamExpressionEngine();
        final var schema = DynamicSchema.of(DynamicField.field("n", "u8").versions(2019).build(),
                DynamicField.field("v", "derived").expression(expression(engine, "#n", Map.of("n", "n"))).build());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema, options(engine).withVersion(2013)));
        assertDoesNotThrow(() -> DynamicCompiler.compile(schema, options(engine).withVersion(2019)));
    }

    @Test
    void preserveUnsigned64BitExpressionResultsAndReferences() {
        final var engine = new SpelXtreamExpressionEngine();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(
                DynamicField.field("n", "u64").build(),
                DynamicField.field("copy", "derived").expression(expression(engine, "#n", Map.of("n", "n"))).build(),
                DynamicField.field("next", "derived").expression(
                        DynamicExpression.op("add", DynamicExpression.ref("copy"), DynamicExpression.value(1))).build()),
                options(engine));
        final var buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("n", "18446744073709551615")), buffer);
            assertEquals("ffffffffffffffff", ByteBufUtil.hexDump(buffer));
            final var result = plan.decode(buffer);
            assertEquals("18446744073709551615", result.values().fields().get("copy"));
            assertEquals("18446744073709551616", result.values().fields().get("next"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void engineIsBoundOnceAndContextsAreIsolated() {
        final AtomicInteger compilations = new AtomicInteger();
        final AtomicInteger evaluations = new AtomicInteger();
        final CustomXtreamExpressionEngine engine = new CustomXtreamExpressionEngine() {
            @Override
            public CustomXtreamExpressionEngineId id() {
                return () -> "counting";
            }

            @Override
            public XtreamExpression createExpression(String source) {
                compilations.incrementAndGet();
                return new CustomXtreamExpression() {
                    @Override
                    public <T> T evaluate(XtreamEvaluationContext context, Class<T> expectedType) {
                        evaluations.incrementAndGet();
                        assertFalse(context.getVariables().containsKey("leaked"));
                        context.setVariable("leaked", true);
                        @SuppressWarnings("unchecked") final T result = (T) context.getVariables().get("n");
                        return result;
                    }

                    @Override
                    public String expressionString() {
                        return source;
                    }
                };
            }

            @Override
            public XtreamEvaluationContext createEvaluationContext(Object root) {
                return new AbstractXtreamEvaluationContext(root);
            }
        };
        final var configuration = options(engine);
        final var item = DynamicField.field("item", "struct").fields(
                DynamicField.field("value", "u8").build(),
                DynamicField.field("copy", "derived").expression(expression(engine, "n", Map.of("n", "value"))).build()).build();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("items", "array")
                .length(DynamicLength.fixed(2, "item")).item(item).build()), configuration);
        configuration.expressionEngines().unregister(engine.id());
        for (int call = 0; call < 3; call++) {
            final var buffer = Unpooled.wrappedBuffer(new byte[]{1, 2});
            try {
                final var items = (List<?>) plan.decode(buffer).values().fields().get("items");
                assertEquals(1L, ((Map<?, ?>) items.getFirst()).get("copy"));
                assertEquals(2L, ((Map<?, ?>) items.getLast()).get("copy"));
            } finally {
                buffer.release();
            }
        }
        assertEquals(1, compilations.get());
        assertEquals(6, evaluations.get());
    }

    @Test
    void resultErrorsRetainLocationAndTrace() {
        final var engine = new SpelXtreamExpressionEngine();
        for (final String source : List.of("-1", "1.5", "null", "'bad'", "'9223372036854775808'")) {
            final var schema = DynamicSchema.of(DynamicField.field("prefix", "u8").build(),
                    DynamicField.field("payload", "hex").length(DynamicLength.expression(
                            expression(engine, source, Map.of()), DynamicLengthUnit.BYTE)).build());
            final var plan = DynamicCompiler.compile(schema, options(engine));
            final var buffer = Unpooled.wrappedBuffer(new byte[]{1, 2});
            try {
                final var tracker = new CodecTracker();
                final var error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer, tracker));
                assertEquals("$.payload", error.fieldPath());
                assertEquals(8, error.bitOffset());
                assertEquals(0, buffer.readerIndex());
                assertFalse(tracker.getTrace().getDiagnostics().isEmpty());
            } finally {
                buffer.release();
            }
        }
        final var invalidCondition = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("v", "u8")
                .when(DynamicCondition.expression(expression(engine, "1", Map.of()))).build()), options(engine));
        final var buffer = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            assertThrows(DynamicCodecException.class, () -> invalidCondition.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void expressionCountMustFailBeforeReadingElements() {
        final var engine = new SpelXtreamExpressionEngine();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("items", "array")
                .length(DynamicLength.expression(expression(engine, "3", Map.of()), DynamicLengthUnit.ITEM))
                .item(DynamicField.field("v", "u8").build()).build()), options(engine));
        final var buffer = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        try {
            final var error = assertThrows(DynamicCodecException.class,
                    () -> plan.decode(buffer, new DynamicLimits(8, 8, 2, 8)));
            assertEquals("$.items", error.fieldPath());
            assertEquals(0, error.bitOffset());
            assertEquals(0, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void skippedBindingsMustNotReadPreviousElement() {
        final var engine = new SpelXtreamExpressionEngine();
        final var item = DynamicField.field("item", "struct").fields(
                DynamicField.field("flag", "u8").build(),
                DynamicField.field("v", "u8").when(new DynamicCondition("flag", "eq", "1")).build(),
                DynamicField.field("copy", "derived").expression(expression(engine, "#v", Map.of("v", "v"))).build()).build();
        final var plan = DynamicCompiler.compile(DynamicSchema.of(DynamicField.field("items", "array")
                .length(DynamicLength.fixed(2, "item")).item(item).build()), options(engine));
        final var buffer = Unpooled.wrappedBuffer(new byte[]{1, 9, 0});
        try {
            final var error = assertThrows(DynamicCodecException.class, () -> plan.decode(buffer));
            assertEquals("$.items[].item.copy", error.fieldPath());
            assertEquals(24, error.bitOffset());
        } finally {
            buffer.release();
        }
    }

    @Test
    void engineFailuresAndUnsupportedResultsPreserveCause() {
        final var engine = new SpelXtreamExpressionEngine();
        for (final String source : List.of("1 / 0", "new java.lang.Object()", "#v.put('n', 99)")) {
            final var plan = DynamicCompiler.compile(DynamicSchema.of(
                    DynamicField.field("nested", "struct").fields(DynamicField.field("n", "u8").build()).build(),
                    DynamicField.field("result", "derived").expression(
                            expression(engine, source, Map.of("v", "nested"))).build()), options(engine));
            final var input = Map.<String, Object>of("nested", Map.of("n", 7));
            final var buffer = Unpooled.buffer();
            try {
                final var error = assertThrows(DynamicCodecException.class,
                        () -> plan.encode(DynamicValues.of(input), buffer));
                assertEquals("$.result", error.fieldPath());
                assertEquals(8, error.bitOffset());
                if (!source.startsWith("new ")) {
                    assertNotNull(error.getCause());
                }
                assertEquals(Map.of("n", 7), input.get("nested"));
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void pendingLengthCannotBeUsedByExpressions() {
        final var schema = DynamicSchema.of(
                DynamicField.field("size", "u8").computedLengthOf("last").build(),
                DynamicField.field("derived", "derived").expression(DynamicExpression.ref("size")).build(),
                DynamicField.field("last", "u8").build());
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(schema));
    }
}

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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.base.expression.*;
import io.github.hylexus.xtream.codec.common.bean.FieldConditionEvaluator;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.common.bean.IterationTimesExtractor;
import io.github.hylexus.xtream.codec.core.XtreamExpressionFactory;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.utils.XtreamFieldUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class DefaultXtreamExpressionFactoryTest {
    private static final FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR = FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
    private static final IterationTimesExtractor.PlaceholderIterationTimesExtractor DEFAULT_ITERATION_TIMES_EXTRACTOR = IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;

    @Test
    void testSpelEngineWithRecord() {
        doAssertSpelEngine(Entity01.class);
    }

    @Test
    void testSpelEngineWithClass() {
        doAssertSpelEngine(Entity02.class);
    }

    @Test
    void testMvelEngineWithRecord() {
        doAssertMvelEngine(Entity01.class);
    }

    @Test
    void testMvelEngineWithClass() {
        doAssertMvelEngine(Entity02.class);
    }

    @Test
    void testAviatorEngineWithRecord() {
        doAssertAviatorEngine(Entity01.class);
    }

    @Test
    void testAviatorEngineWithClass() {
        doAssertAviatorEngine(Entity02.class);
    }

    @Test
    void testCustomEngineWithRecord() {
        doAssertCustomEngine(Entity01.class);
    }

    @Test
    void testCustomEngineWithClass() {
        doAssertCustomEngine(Entity02.class);
    }

    void doAssertCustomEngine(Class<?> cls) {
        final DefaultXtreamExpressionFactory factory = mockCustomExpressionFactory();

        final XtreamEvaluationContext evaluationContext = factory.createEvaluationContext(Map.of());
        assertInstanceOf(CustomXtreamEvaluationContext.class, evaluationContext);

        doAssert(
                factory, cls, "field14",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("conditions-custom-14", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("length-expressions-custom-14", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("iteration-times-expressions-custom-14", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );

        doAssert(
                factory, cls, "field15",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT, iterationTimesExtractor)
        );

    }

    private DefaultXtreamExpressionFactory mockCustomExpressionFactory() {
        return new DefaultXtreamExpressionFactory(new CustomXtreamExpressionEngine() {
            @Override
            public CustomXtreamExpressionEngineId id() {
                return () -> "customId";
            }

            @Override
            public XtreamExpression createExpression(String expressionString) {
                return new CustomXtreamExpression() {
                    @Override
                    public @Nullable <T> T evaluate(XtreamEvaluationContext context, @Nullable Class<T> expectedType) throws XtreamExpressionException {
                        return null;
                    }

                    @Override
                    public String expressionString() {
                        return expressionString;
                    }
                };
            }

            @Override
            public XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject) {
                return new CustomXtreamEvaluationContext(new HashMap<>());
            }
        });
    }

    static class CustomXtreamEvaluationContext extends AbstractXtreamEvaluationContext {
        public CustomXtreamEvaluationContext(@Nullable Object rootObject) {
            super(rootObject);
        }
    }

    private void doAssertSpelEngine(Class<?> cls) {
        final XtreamExpressionFactory factory = newSpelExpressionFactory();

        final XtreamEvaluationContext evaluationContext = factory.createEvaluationContext(Map.of());
        assertInstanceOf(SpelXtreamExpressionEngine.SpelXtreamEvaluationContext.class, evaluationContext);

        doAssert(
                factory,
                cls, "field0",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field1",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(1), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field2",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(PrependLengthFieldType.u16), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field3",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.ConstantFieldLengthExtractor(3), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field4",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("getField4Length()", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field5",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("getField5Length()", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field6",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("true", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field7",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("getField7Condition()", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field8",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("spel8()", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field9",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ConstantIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals(9, ((IterationTimesExtractor.ConstantIterationTimesExtractor) iterationTimesExtractor).length());
                }
        );
        doAssert(
                factory,
                cls, "field10",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("10", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field11",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("getField11Length()", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field12",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("spel12()", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );

        if (Entity01.class.equals(cls)) {
            doAssert(
                    factory,
                    cls, "field13",
                    conditionEvaluator -> assertSame(FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator.INSTANCE, conditionEvaluator),
                    Assertions::assertNull,
                    iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
            );
        }

    }

    private void doAssertMvelEngine(Class<?> cls) {
        final XtreamExpressionFactory factory = newMvelExpressionFactory();

        final XtreamEvaluationContext evaluationContext = factory.createEvaluationContext(Map.of());
        assertInstanceOf(MvelXtreamExpressionEngine.MvelXtreamEvaluationContext.class, evaluationContext);

        doAssert(
                factory,
                cls, "field0",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field1",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(1), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field2",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(PrependLengthFieldType.u16), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field3",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.ConstantFieldLengthExtractor(3), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field4",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("getField4Length()", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field5",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("self.getField5Length()", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field6",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("true", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field7",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("getField7Condition()", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field8",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("self.mvel8()", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field9",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ConstantIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals(9, ((IterationTimesExtractor.ConstantIterationTimesExtractor) iterationTimesExtractor).length());
                }
        );
        doAssert(
                factory,
                cls, "field10",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("10", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field11",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("getField11Length()", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field12",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("self.mvel12()", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );

        if (Entity01.class.equals(cls)) {
            doAssert(
                    factory,
                    cls, "field13",
                    conditionEvaluator -> assertSame(FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator.INSTANCE, conditionEvaluator),
                    Assertions::assertNull,
                    iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
            );
        }
    }

    private void doAssertAviatorEngine(Class<?> cls) {
        final XtreamExpressionFactory factory = newAviatorExpressionFactory();

        final XtreamEvaluationContext evaluationContext = factory.createEvaluationContext(Map.of());
        assertInstanceOf(AviatorXtreamExpressionEngine.AviatorXtreamEvaluationContext.class, evaluationContext);

        doAssert(
                factory,
                cls, "field0",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field1",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(1), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field2",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.PrependFieldLengthExtractor(PrependLengthFieldType.u16), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field3",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> assertEquals(new FieldLengthExtractor.ConstantFieldLengthExtractor(3), fieldLengthExtractor),
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field4",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("getField4Length()", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field5",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                fieldLengthExtractor -> {
                    assertInstanceOf(FieldLengthExtractor.ExpressionFieldLengthExtractor.class, fieldLengthExtractor);
                    assertEquals("self.field5Length", ((FieldLengthExtractor.ExpressionFieldLengthExtractor) fieldLengthExtractor).expressionString());
                },
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field6",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("true", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field7",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("getField7Condition()", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field8",
                conditionEvaluator -> {
                    assertInstanceOf(FieldConditionEvaluator.ExpressionFieldConditionEvaluator.class, conditionEvaluator);
                    assertEquals("self.aiator8", ((FieldConditionEvaluator.ExpressionFieldConditionEvaluator) conditionEvaluator).expressionString());
                },
                Assertions::assertNull,
                iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
        );
        doAssert(
                factory,
                cls, "field9",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ConstantIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals(9, ((IterationTimesExtractor.ConstantIterationTimesExtractor) iterationTimesExtractor).length());
                }
        );
        doAssert(
                factory,
                cls, "field10",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("10", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field11",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("getField11Length()", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );
        doAssert(
                factory,
                cls, "field12",
                conditionEvaluator -> assertSame(ALWAYS_TRUE_FIELD_CONDITION_EVALUATOR, conditionEvaluator),
                Assertions::assertNull,
                iterationTimesExtractor -> {
                    assertInstanceOf(IterationTimesExtractor.ExpressionIterationTimesExtractor.class, iterationTimesExtractor);
                    assertEquals("self.aviator12", ((IterationTimesExtractor.ExpressionIterationTimesExtractor) iterationTimesExtractor).expressionString());
                }
        );

        if (Entity01.class.equals(cls)) {
            doAssert(
                    factory,
                    cls, "field13",
                    conditionEvaluator -> assertSame(FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator.INSTANCE, conditionEvaluator),
                    Assertions::assertNull,
                    iterationTimesExtractor -> assertSame(DEFAULT_ITERATION_TIMES_EXTRACTOR, iterationTimesExtractor)
            );
        }
    }

    @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
    public static class Entity02 {
        @Preset.RustStyle.str
        String field0;

        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
        String field1;

        @Preset.RustStyle.str(prependLengthFieldLength = 2)
        String field2;

        @Preset.RustStyle.str(length = 3)
        String field3;

        @Preset.RustStyle.str(lengthExpression = "getField4Length()")
        String field4;

        @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getField5Length()", mvel = "self.getField5Length()", aviator = "self.field5Length"))
        String field5;

        @Preset.RustStyle.str(condition = "true")
        String field6;

        @Preset.RustStyle.str(condition = "getField7Condition()", conditions = @Expression(spel = "x()", mvel = "self.y()", aviator = "self.z"))
        String field7;

        @Preset.RustStyle.str(conditions = @Expression(spel = "spel8()", mvel = "self.mvel8()", aviator = "self.aiator8"))
        String field8;

        @Preset.RustStyle.list(iterationTimes = 9)
        List<Object> field9;

        @Preset.RustStyle.list(iterationTimesExpression = "10")
        List<Object> field10;

        @Preset.RustStyle.list(iterationTimesExpression = "getField11Length()", iterationTimesExpressions = @Expression(spel = "spel11()", mvel = "self.mvel11()", aviator = "self.aviator11"))
        List<Object> field11;

        @Preset.RustStyle.list(iterationTimesExpressions = @Expression(spel = "spel12()", mvel = "self.mvel12()", aviator = "self.aviator12"))
        List<Object> field12;

        @Preset.RustStyle.list(
                conditions = @Expression(custom = "conditions-custom-14"),
                lengthExpressions = @Expression(custom = "length-expressions-custom-14"),
                iterationTimesExpressions = @Expression(custom = "iteration-times-expressions-custom-14")
        )
        String field14;

        @Preset.RustStyle.list
        String field15;
    }

    public record Entity01(
            @Preset.RustStyle.str String field0,
            @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8) String field1,
            @Preset.RustStyle.str(prependLengthFieldLength = 2) String field2,
            @Preset.RustStyle.str(length = 3) String field3,
            @Preset.RustStyle.str(lengthExpression = "getField4Length()", lengthExpressions = @Expression(spel = "spel4()", mvel = "self.mvel4()", aviator = "self.aviator4")) String field4,
            @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getField5Length()", mvel = "self.getField5Length()", aviator = "self.field5Length")) String field5,
            @Preset.RustStyle.str(condition = "true") String field6,
            @Preset.RustStyle.str(condition = "getField7Condition()", conditions = @Expression(spel = "x()", mvel = "self.y()", aviator = "self.z")) String field7,
            @Preset.RustStyle.str(conditions = @Expression(spel = "spel8()", mvel = "self.mvel8()", aviator = "self.aiator8")) String field8,
            @Preset.RustStyle.list(iterationTimes = 9) List<Object> field9,
            @Preset.RustStyle.list(iterationTimesExpression = "10") List<Object> field10,
            @Preset.RustStyle.list(iterationTimesExpression = "getField11Length()", iterationTimesExpressions = @Expression(spel = "spel11()", mvel = "self.mvel11()", aviator = "self.aviator11")) List<Object> field11,
            @Preset.RustStyle.list(iterationTimesExpressions = @Expression(spel = "spel12()", mvel = "self.mvel12()", aviator = "self.aviator12")) List<Object> field12,
            @Preset.RustStyle.transient_rc String field13,
            @Preset.RustStyle.list(
                    conditions = @Expression(custom = "conditions-custom-14"),
                    lengthExpressions = @Expression(custom = "length-expressions-custom-14"),
                    iterationTimesExpressions = @Expression(custom = "iteration-times-expressions-custom-14")
            ) String field14,
            @Preset.RustStyle.list String field15) {
    }

    private static void doAssert(
            XtreamExpressionFactory factory,
            Class<?> cls,
            String fieldName,
            Consumer<@Nullable FieldConditionEvaluator> conditionEvaluatorConsumer,
            Consumer<@Nullable FieldLengthExtractor> fieldLengthExtractorConsumer,
            Consumer<IterationTimesExtractor> iterationTimesExtractorConsumer) {
        final XtreamField xtreamField = getXtreamField(cls, fieldName);

        final FieldConditionEvaluator conditionEvaluator = factory.createFieldConditionEvaluator(xtreamField);
        conditionEvaluatorConsumer.accept(conditionEvaluator);

        final FieldLengthExtractor fieldLengthExtractor = factory.createFieldLengthExtractor(xtreamField);
        fieldLengthExtractorConsumer.accept(fieldLengthExtractor);

        final IterationTimesExtractor iterationTimesExtractor = factory.createIterationTimesExtractor(xtreamField);
        iterationTimesExtractorConsumer.accept(iterationTimesExtractor);
    }

    static XtreamField getXtreamField(Class<?> cls, String fieldName) {
        try {
            if (cls.isRecord()) {
                final RecordComponent[] recordComponents = cls.getRecordComponents();
                final RecordComponent recordComponent = Arrays.stream(recordComponents).filter(it -> it.getName().equals(fieldName)).findFirst().orElseThrow();
                final List<XtreamField> list = XtreamFieldUtils.getOrDefault(recordComponent);
                return list.getFirst();
            }
            final Field field = cls.getDeclaredField(fieldName);
            final List<XtreamField> list = XtreamFieldUtils.getOrEmpty(field);
            return list.getFirst();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DefaultXtreamExpressionFactory newSpelExpressionFactory() {
        return new DefaultXtreamExpressionFactory(new SpelXtreamExpressionEngine());
    }

    private static DefaultXtreamExpressionFactory newMvelExpressionFactory() {
        return new DefaultXtreamExpressionFactory(new MvelXtreamExpressionEngine());
    }

    private static DefaultXtreamExpressionFactory newAviatorExpressionFactory() {
        return new DefaultXtreamExpressionFactory(new AviatorXtreamExpressionEngine());
    }
}

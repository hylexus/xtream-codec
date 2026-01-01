/*
 * Copyright 2024 the original author or authors.
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
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.XtreamExpressionFactory;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author hylexus
 * @since 0.4.0
 */
@NullMarked
@ApiStatus.AvailableSince("0.4.0")
@ApiStatus.Experimental
public class DefaultXtreamExpressionFactory implements XtreamExpressionFactory {
    private final XtreamExpressionEngine expressionEngine;

    public DefaultXtreamExpressionFactory(XtreamExpressionEngine expressionEngine) {
        this.expressionEngine = Objects.requireNonNull(expressionEngine);
    }

    @Override
    public XtreamEvaluationContext createEvaluationContext(Object rootObject) {
        return this.expressionEngine.createEvaluationContext(rootObject);
    }

    @Override
    public @Nullable FieldConditionEvaluator createFieldConditionEvaluator(XtreamField xtreamField) {
        if (xtreamField.codecStrategy() == XtreamField.CodecStrategy.TRANSIENT) {
            return FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator.INSTANCE;
        }
        if (XtreamUtils.hasElement(xtreamField.condition())) {
            return new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(xtreamField.condition(), expressionEngine);
        }
        final Expression conditions = xtreamField.conditions();
        return switch (expressionEngine) {
            case SpelXtreamExpressionEngine spel -> {
                if (XtreamUtils.hasElement(conditions.spel())) {
                    yield new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(spel.createExpression(conditions.spel()), conditions.spel());
                }
                yield FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
            }
            case AviatorXtreamExpressionEngine aviator -> {
                if (XtreamUtils.hasElement(conditions.aviator())) {
                    yield new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(aviator.createExpression(conditions.aviator()), conditions.aviator());
                }
                yield FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
            }
            case MvelXtreamExpressionEngine mvel -> {
                if (XtreamUtils.hasElement(conditions.mvel())) {
                    yield new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(mvel.createExpression(conditions.mvel()), conditions.mvel());
                }
                yield FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
            }
            case CustomXtreamExpressionEngine custom -> {
                if (XtreamUtils.hasElement(conditions.custom())) {
                    yield new FieldConditionEvaluator.ExpressionFieldConditionEvaluator(custom.createExpression(conditions.custom()), conditions.mvel());
                }
                yield FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator.INSTANCE;
            }
        };
    }

    @Override
    public @Nullable FieldLengthExtractor createFieldLengthExtractor(XtreamField xtreamField) {
        if (xtreamField.prependLengthFieldType() != PrependLengthFieldType.none) {
            return new FieldLengthExtractor.PrependFieldLengthExtractor(xtreamField.prependLengthFieldType());
        }

        if (xtreamField.prependLengthFieldLength() > 0) {
            return new FieldLengthExtractor.PrependFieldLengthExtractor(xtreamField.prependLengthFieldLength());
        }

        if (xtreamField.length() > 0) {
            return new FieldLengthExtractor.ConstantFieldLengthExtractor(xtreamField.length());
        }

        if (XtreamUtils.hasElement(xtreamField.lengthExpression())) {
            return new FieldLengthExtractor.ExpressionFieldLengthExtractor(xtreamField, this.expressionEngine);
        }

        final Expression lengthExpressions = xtreamField.lengthExpressions();
        return switch (this.expressionEngine) {
            case SpelXtreamExpressionEngine spel -> {
                if (StringUtils.hasText(lengthExpressions.spel())) {
                    yield new FieldLengthExtractor.ExpressionFieldLengthExtractor(spel.createExpression(lengthExpressions.spel()), lengthExpressions.spel());
                }
                yield null;
            }
            case AviatorXtreamExpressionEngine aviator -> {
                if (StringUtils.hasText(lengthExpressions.aviator())) {
                    yield new FieldLengthExtractor.ExpressionFieldLengthExtractor(aviator.createExpression(lengthExpressions.aviator()), lengthExpressions.aviator());
                }
                yield null;
            }
            case MvelXtreamExpressionEngine mvel -> {
                if (StringUtils.hasText(lengthExpressions.mvel())) {
                    yield new FieldLengthExtractor.ExpressionFieldLengthExtractor(mvel.createExpression(lengthExpressions.mvel()), lengthExpressions.mvel());
                }
                yield null;
            }
            case CustomXtreamExpressionEngine custom -> {
                if (StringUtils.hasText(lengthExpressions.custom())) {
                    yield new FieldLengthExtractor.ExpressionFieldLengthExtractor(custom.createExpression(lengthExpressions.custom()), lengthExpressions.custom());
                }
                yield null;
            }
        };
    }

    @Override
    public IterationTimesExtractor createIterationTimesExtractor(XtreamField xtreamField) {
        if (xtreamField.iterationTimes() > 0) {
            return new IterationTimesExtractor.ConstantIterationTimesExtractor(xtreamField.iterationTimes());
        }
        if (StringUtils.hasText(xtreamField.iterationTimesExpression())) {
            return new IterationTimesExtractor.ExpressionIterationTimesExtractor(xtreamField, this.expressionEngine);
        }
        final Expression expressions = xtreamField.iterationTimesExpressions();
        return switch (this.expressionEngine) {
            case SpelXtreamExpressionEngine spel -> {
                if (StringUtils.hasText(expressions.spel())) {
                    yield new IterationTimesExtractor.ExpressionIterationTimesExtractor(spel.createExpression(expressions.spel()), expressions.spel());
                }
                yield IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
            }
            case AviatorXtreamExpressionEngine aviator -> {
                if (StringUtils.hasText(expressions.aviator())) {
                    yield new IterationTimesExtractor.ExpressionIterationTimesExtractor(aviator.createExpression(expressions.aviator()), expressions.aviator());
                }
                yield IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
            }
            case MvelXtreamExpressionEngine mvel -> {
                if (StringUtils.hasText(expressions.mvel())) {
                    yield new IterationTimesExtractor.ExpressionIterationTimesExtractor(mvel.createExpression(expressions.mvel()), expressions.mvel());
                }
                yield IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
            }
            case CustomXtreamExpressionEngine custom -> {
                if (StringUtils.hasText(expressions.custom())) {
                    yield new IterationTimesExtractor.ExpressionIterationTimesExtractor(custom.createExpression(expressions.custom()), expressions.custom());
                }
                yield IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
            }
            // noinspection UnnecessaryDefault
            default -> IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
        };
    }


}

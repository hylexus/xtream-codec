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

package io.github.hylexus.xtream.codec.base.expression;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

/**
 * 基于Spring SpEL 的实现
 *
 * @author hylexus
 * @see <a href="https://docs.spring.io/spring-framework/reference/core/expressions.html">https://docs.spring.io/spring-framework/reference/core/expressions.html</a>
 */
public final class SpelXtreamExpressionEngine implements XtreamExpressionEngine {

    private static final Logger log = LoggerFactory.getLogger(SpelXtreamExpressionEngine.class);
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public SpelXtreamExpressionEngine() {
    }

    @Override
    public XtreamExpressionEngineId id() {
        return XtreamExpressionEngineId.SPEL;
    }

    @Override
    public XtreamExpression createExpression(String expressionString) {
        final Expression expr = parser.parseExpression(expressionString);
        return new SpelXtreamExpression(expr, expressionString);
    }

    @Override
    public XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject) {
        return new SpelXtreamEvaluationContext(rootObject);
    }

    public record SpelXtreamExpression(Expression expr, String expressionString) implements XtreamExpression {

        @SuppressWarnings("redundant")
        public SpelXtreamExpression {
        }

        @Override
        public <T> @Nullable T evaluate(XtreamEvaluationContext context, @Nullable Class<T> expectedType) throws XtreamExpressionException {
            if (context instanceof SpelXtreamEvaluationContext spelCtx) {
                try {
                    return this.expr.getValue(spelCtx.delegate, expectedType);
                } catch (Exception e) {
                    if (e instanceof XtreamExpressionException) {
                        throw e;
                    }
                    log.error("Failed to evaluate SpEL expression: {}", expressionString, e);
                    throw new XtreamExpressionException(e);
                }
            }
            throw new IllegalArgumentException(SpelXtreamExpression.class.getSimpleName() + " requires " + SpelXtreamEvaluationContext.class.getSimpleName());
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class SpelXtreamEvaluationContext implements XtreamEvaluationContext {
        private final EvaluationContext delegate;

        public SpelXtreamEvaluationContext(@Nullable Object rootObject) {
            this.delegate = SpelXtreamExpressionEngine.createSpelEvaluationContext(rootObject);
            this.setVariable(ROOT_OBJECT_KEY, rootObject);
        }

        @Override
        public void setVariable(String name, @Nullable Object value) {
            this.delegate.setVariable(name, value);
        }

    }

    static final List<PropertyAccessor> STANDARD_EVALUATION_CONTEXT_PROPERTY_ACCESSORS = List.of(
            // 以访问普通对象的风格访问 Map(mapObj.key.attr1.att2)
            new MapAccessor(),
            new ReflectivePropertyAccessor()
    );

    static EvaluationContext createSpelEvaluationContext(@Nullable Object rootObject) {
        // 1. rootObject
        final StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        context.setPropertyAccessors(STANDARD_EVALUATION_CONTEXT_PROPERTY_ACCESSORS);
        // 2. #self
        context.setVariable(XtreamEvaluationContext.ROOT_OBJECT_KEY, rootObject);
        return context;
    }
}

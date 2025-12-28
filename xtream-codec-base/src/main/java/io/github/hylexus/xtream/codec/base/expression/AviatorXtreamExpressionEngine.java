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

package io.github.hylexus.xtream.codec.base.expression;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.jspecify.annotations.Nullable;

/**
 * 基于 Aviator 的实现
 *
 * @author hylexus
 * @see <a href="https://github.com/killme2008/aviatorscript">https://github.com/killme2008/aviatorscript</a>
 */
public final class AviatorXtreamExpressionEngine implements XtreamExpressionEngine {
    public AviatorXtreamExpressionEngine() {
    }

    @Override
    public XtreamExpressionEngineId id() {
        return XtreamExpressionEngineId.AVIATOR;
    }

    @Override
    public XtreamExpression createExpression(String expressionString) {
        final Expression compiled = AviatorEvaluator.getInstance().compile(expressionString);
        return new AviatorXtreamExpression(compiled, expressionString);
    }

    @Override
    public XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject) {
        return new AviatorXtreamEvaluationContext(rootObject);
    }

    public record AviatorXtreamExpression(Expression expr, String expressionString) implements XtreamExpression {

        @Override
        public <T> @Nullable T evaluate(XtreamEvaluationContext context, @Nullable Class<T> expectedType) {
            if (!(context instanceof AviatorXtreamEvaluationContext aviatorCtx)) {
                throw new IllegalArgumentException(AviatorXtreamExpression.class.getSimpleName() + " requires " + AviatorXtreamEvaluationContext.class.getSimpleName());
            }

            final Object result = this.expr.execute(aviatorCtx.variables);
            if (result == null) {
                return null;
            }
            if (expectedType != null && expectedType.isInstance(result)) {
                return expectedType.cast(result);
            }
            throw new ClassCastException("Cannot cast result " + result.getClass() + " to " + expectedType);
        }
    }

    public static final class AviatorXtreamEvaluationContext extends AbstractXtreamEvaluationContext {
        public AviatorXtreamEvaluationContext(@Nullable Object rootObject) {
            super(rootObject);
        }
    }

}

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

import org.jspecify.annotations.Nullable;
import org.mvel2.MVEL;

import java.io.Serializable;

/**
 * 基于 MVEL 2.x 的实现
 *
 * @author hylexus
 * @see <a href="http://mvel.documentnode.com/">http://mvel.documentnode.com/</a>
 */
public final class MvelXtreamExpressionEngine implements XtreamExpressionEngine {
    public MvelXtreamExpressionEngine() {
    }

    @Override
    public XtreamExpressionEngineId id() {
        return XtreamExpressionEngineId.MVEL;
    }

    @Override
    public XtreamExpression createExpression(String expressionString) {
        final Serializable compiled = MVEL.compileExpression(expressionString);
        return new MvelXtreamExpression(compiled, expressionString);
    }

    @Override
    public XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject) {
        return new MvelXtreamEvaluationContext(rootObject);
    }

    public record MvelXtreamExpression(Serializable compiledExpression, String expressionString) implements XtreamExpression {

        @Override
        public <T> T evaluate(XtreamEvaluationContext context, @Nullable Class<T> expectedType) {
            if (!(context instanceof MvelXtreamEvaluationContext mvelCtx)) {
                throw new IllegalArgumentException(MvelXtreamExpression.class.getSimpleName() + " requires " + MvelXtreamEvaluationContext.class.getSimpleName());
            }
            if (expectedType == null) {
                final Object result = MVEL.executeExpression(this.compiledExpression, mvelCtx.getVariables(), Object.class);
                @SuppressWarnings("unchecked") final T cast = (T) result;
                return cast;
            }
            return MVEL.executeExpression(this.compiledExpression, mvelCtx.getVariables(), expectedType);
        }
    }

    public static final class MvelXtreamEvaluationContext extends AbstractXtreamEvaluationContext {
        public MvelXtreamEvaluationContext(@Nullable Object rootObject) {
            super(rootObject);
        }
    }
}

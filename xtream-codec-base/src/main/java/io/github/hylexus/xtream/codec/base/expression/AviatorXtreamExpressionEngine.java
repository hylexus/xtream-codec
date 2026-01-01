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

import com.googlecode.aviator.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 Aviator 的实现
 *
 * @author hylexus
 * @see <a href="https://github.com/killme2008/aviatorscript">https://github.com/killme2008/aviatorscript</a>
 */
public final class AviatorXtreamExpressionEngine implements XtreamExpressionEngine {
    private static final Logger log = LoggerFactory.getLogger(AviatorXtreamExpressionEngine.class);
    private final AviatorEvaluatorInstance defaultInstance;

    public AviatorXtreamExpressionEngine() {
        this(createDefaultAviatorEvaluatorInstance());
    }

    public AviatorXtreamExpressionEngine(AviatorEvaluatorInstance defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    @Override
    public XtreamExpressionEngineId id() {
        return XtreamExpressionEngineId.AVIATOR;
    }

    @Override
    public XtreamExpression createExpression(String expressionString) {
        final Expression compiled = this.defaultInstance.compile(expressionString);
        return new AviatorXtreamExpression(compiled, expressionString);
    }

    @Override
    public XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject) {
        return new AviatorXtreamEvaluationContext(rootObject);
    }

    public record AviatorXtreamExpression(Expression expr, String expressionString) implements XtreamExpression {

        @Override
        public <T> @Nullable T evaluate(XtreamEvaluationContext context, @Nullable Class<T> expectedType) throws XtreamExpressionException {
            if (!(context instanceof AviatorXtreamEvaluationContext aviatorCtx)) {
                throw new IllegalArgumentException(AviatorXtreamExpression.class.getSimpleName() + " requires " + AviatorXtreamEvaluationContext.class.getSimpleName());
            }

            try {
                final Object result = this.expr.execute(aviatorCtx.variables);
                if (result == null) {
                    return null;
                }
                if (expectedType == null) {
                    @SuppressWarnings("unchecked") final T cast = (T) result;
                    return cast;
                }

                try {
                    return expectedType.cast(result);
                } catch (ClassCastException e) {
                    throw new XtreamExpressionException(
                            "Expression [" + expressionString + "] returned " + result.getClass().getName() + ", expected " + expectedType.getName(),
                            e
                    );
                }
            } catch (Exception e) {
                if (e instanceof XtreamExpressionException) {
                    throw e;
                }
                log.error("Failed to evaluate Aviator expression: {}", expressionString, e);
                throw new XtreamExpressionException(e);
            }
        }
    }

    public static final class AviatorXtreamEvaluationContext extends AbstractXtreamEvaluationContext {
        public AviatorXtreamEvaluationContext(@Nullable Object rootObject) {
            super(rootObject);
        }
    }

    public static AviatorEvaluatorInstance createDefaultAviatorEvaluatorInstance() {
        final AviatorEvaluatorInstance instance = AviatorEvaluator.newInstance();
        instance.setOption(Options.EVAL_MODE, EvalMode.ASM);
        instance.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.COMPILE);
        instance.setOption(Options.ENABLE_PROPERTY_SYNTAX_SUGAR, true);
        instance.setOption(Options.SERIALIZABLE, false);
        instance.setOption(Options.EVAL_TIMEOUT_MS, 5000);
        // 只对「属性访问」（如 self.prop, obj.prop）生效，对 「顶层变量」 无效
        instance.setOption(Options.NIL_WHEN_PROPERTY_NOT_FOUND, false);
        instance.setOption(Options.USE_USER_ENV_AS_TOP_ENV_DIRECTLY, false);
        instance.setOption(Options.MAX_LOOP_COUNT, 1024);
        return instance;
    }

}

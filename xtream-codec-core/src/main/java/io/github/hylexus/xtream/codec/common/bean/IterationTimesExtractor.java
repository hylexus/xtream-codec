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

package io.github.hylexus.xtream.codec.common.bean;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import lombok.ToString;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

public interface IterationTimesExtractor {

    int extractIterationTimes(FieldCodec.DeserializeContext context, EvaluationContext evaluationContext);

    static IterationTimesExtractor from(XtreamField xtreamField) {
        if (xtreamField.iterationTimes() > 0) {
            return new IterationTimesExtractor.ConstantIterationTimesExtractor(xtreamField.iterationTimes());
        }
        if (StringUtils.hasText(xtreamField.iterationTimesExpression())) {
            return new IterationTimesExtractor.ExpressionIterationTimesExtractor(xtreamField);
        }
        return IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
    }

    @ToString
    class ConstantIterationTimesExtractor implements IterationTimesExtractor {
        private final int length;

        public ConstantIterationTimesExtractor(int length) {
            this.length = length;
        }

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, EvaluationContext evaluationContext) {
            return this.length;
        }
    }

    @ToString(exclude = "expression")
    class ExpressionIterationTimesExtractor implements IterationTimesExtractor {
        final Expression expression;
        private final String expressionString;

        public ExpressionIterationTimesExtractor(XtreamField field) {
            this.expressionString = field.iterationTimesExpression();
            this.expression = new SpelExpressionParser().parseExpression(expressionString);
        }

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, EvaluationContext evaluationContext) {
            final Number number = expression.getValue(evaluationContext, Number.class);
            if (number == null) {
                throw new IllegalArgumentException("Can not determine field length with Expression[" + expressionString + "]");
            }
            return number.intValue();
        }
    }

    enum PlaceholderIterationTimesExtractor implements IterationTimesExtractor {

        DEFAULT,
        ;

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, EvaluationContext evaluationContext) {
            return 1024;
        }
    }

}

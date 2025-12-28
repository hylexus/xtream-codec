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

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpression;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngine;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.springframework.util.StringUtils;

import java.util.StringJoiner;

public interface IterationTimesExtractor {

    int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext);

    static IterationTimesExtractor from(XtreamField xtreamField, XtreamExpressionEngine expressionEngine) {
        if (xtreamField.iterationTimes() > 0) {
            return new IterationTimesExtractor.ConstantIterationTimesExtractor(xtreamField.iterationTimes());
        }
        if (StringUtils.hasText(xtreamField.iterationTimesExpression())) {
            return new IterationTimesExtractor.ExpressionIterationTimesExtractor(xtreamField, expressionEngine);
        }
        return IterationTimesExtractor.PlaceholderIterationTimesExtractor.DEFAULT;
    }

    class ConstantIterationTimesExtractor implements IterationTimesExtractor {
        private final int length;

        public ConstantIterationTimesExtractor(int length) {
            this.length = length;
        }

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext) {
            return this.length;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ConstantIterationTimesExtractor.class.getSimpleName() + "[", "]")
                    .add("length=" + length)
                    .toString();
        }
    }

    class ExpressionIterationTimesExtractor implements IterationTimesExtractor {
        final XtreamExpression expression;
        private final String expressionString;

        public ExpressionIterationTimesExtractor(XtreamField field, XtreamExpressionEngine expressionEngine) {
            this.expressionString = field.iterationTimesExpression();
            // this.expression = new SpelExpressionParser().parseExpression(expressionString);
            this.expression = expressionEngine.createExpression(this.expressionString);
        }

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext) {
            final Number number = expression.getValue(evaluationContext, Number.class);
            if (number == null) {
                throw new IllegalArgumentException("Can not determine field length with Expression[" + expressionString + "]");
            }
            return number.intValue();
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ExpressionIterationTimesExtractor.class.getSimpleName() + "[", "]")
                    .add("expressionString='" + expressionString + "'")
                    .toString();
        }
    }

    enum PlaceholderIterationTimesExtractor implements IterationTimesExtractor {

        DEFAULT,
        ;

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext) {
            return 1024;
        }
    }

}

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

package io.github.hylexus.xtream.codec.common.bean;

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpression;
import io.github.hylexus.xtream.codec.common.utils.XtreamAssertions;
import io.github.hylexus.xtream.codec.core.FieldCodec;

import java.util.StringJoiner;

public sealed interface IterationTimesExtractor
        permits
        IterationTimesExtractor.ConstantIterationTimesExtractor,
        IterationTimesExtractor.ExpressionIterationTimesExtractor,
        IterationTimesExtractor.PlaceholderIterationTimesExtractor {

    int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext);

    record ConstantIterationTimesExtractor(int length) implements IterationTimesExtractor {

        public ConstantIterationTimesExtractor {
            XtreamAssertions.assertThat(length > 0, "ConstantIterationTimesExtractor.length() must be greater than 0");
        }

        @Override
        public int extractIterationTimes(FieldCodec.DeserializeContext context, XtreamEvaluationContext evaluationContext) {
            return this.length;
        }

    }

    record ExpressionIterationTimesExtractor(XtreamExpression expression, String expressionString) implements IterationTimesExtractor {

        public ExpressionIterationTimesExtractor(XtreamExpression expression) {
            this(expression, expression.expressionString());
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

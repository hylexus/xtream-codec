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

import io.github.hylexus.xtream.codec.base.expression.XtreamExpression;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngine;
import io.github.hylexus.xtream.codec.core.FieldCodec;

import java.util.StringJoiner;

public sealed interface FieldConditionEvaluator
        permits FieldConditionEvaluator.AlwaysFalseFieldConditionEvaluator,
        FieldConditionEvaluator.AlwaysTrueFieldConditionEvaluator,
        FieldConditionEvaluator.ExpressionFieldConditionEvaluator,
        FieldConditionEvaluator.CustomFieldConditionEvaluator {

    boolean evaluate(FieldCodec.CodecContext context);

    enum AlwaysFalseFieldConditionEvaluator implements FieldConditionEvaluator {
        INSTANCE;

        @Override
        public boolean evaluate(FieldCodec.CodecContext context) {
            return false;
        }
    }

    enum AlwaysTrueFieldConditionEvaluator implements FieldConditionEvaluator {
        INSTANCE;

        @Override
        public boolean evaluate(FieldCodec.CodecContext context) {
            return true;
        }
    }

    final class ExpressionFieldConditionEvaluator implements FieldConditionEvaluator {
        private final XtreamExpression expression;
        private final String expressionString;

        public ExpressionFieldConditionEvaluator(String expressionString, XtreamExpressionEngine expressionEngine) {
            this.expressionString = expressionString;
            // this.expression = new SpelExpressionParser().parseExpression(expressionString);
            this.expression = expressionEngine.createExpression(this.expressionString);
        }

        @Override
        public boolean evaluate(FieldCodec.CodecContext context) {
            final Boolean value = expression.getValue(context.evaluationContext(), Boolean.class);
            return value != null && value;
        }

        public String expressionString() {
            return expressionString;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ExpressionFieldConditionEvaluator.class.getSimpleName() + "[", "]")
                    .add("expressionString='" + expressionString + "'")
                    .toString();
        }
    }

    non-sealed interface CustomFieldConditionEvaluator extends FieldConditionEvaluator {
    }

}

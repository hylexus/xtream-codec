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

public sealed interface XtreamExpressionEngine permits
        SpelXtreamExpressionEngine,
        AviatorXtreamExpressionEngine,
        MvelXtreamExpressionEngine,
        CustomXtreamExpressionEngine {

    XtreamExpressionEngineId id();

    XtreamExpression createExpression(String expressionString);

    XtreamEvaluationContext createEvaluationContext(@Nullable Object rootObject);

    interface XtreamExpressionEngineId {
        XtreamExpressionEngineId SPEL = BuiltinXtreamExpressionEngineId.SpEL;
        XtreamExpressionEngineId AVIATOR = BuiltinXtreamExpressionEngineId.Aviator;
        XtreamExpressionEngineId MVEL = BuiltinXtreamExpressionEngineId.MVEL;

        String value();
    }

    enum BuiltinXtreamExpressionEngineId implements XtreamExpressionEngineId {
        SpEL("SpEL"),
        Aviator("Aviator"),
        MVEL("MVEL"),
        ;
        private final String value;

        BuiltinXtreamExpressionEngineId(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return this.value;
        }
    }
}

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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.common.bean.FieldConditionEvaluator;
import io.github.hylexus.xtream.codec.common.bean.FieldLengthExtractor;
import io.github.hylexus.xtream.codec.common.bean.IterationTimesExtractor;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @author hylexus
 * @since 0.4.0
 */
@NullMarked
@ApiStatus.AvailableSince("0.4.0")
@ApiStatus.Experimental
public interface XtreamExpressionFactory {

    XtreamEvaluationContext createEvaluationContext(Object rootObject);

    @Nullable FieldConditionEvaluator createFieldConditionEvaluator(XtreamField field);

    @Nullable FieldLengthExtractor createFieldLengthExtractor(XtreamField field);

    IterationTimesExtractor createIterationTimesExtractor(XtreamField field);

}

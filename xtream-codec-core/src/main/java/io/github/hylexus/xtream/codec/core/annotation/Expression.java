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

package io.github.hylexus.xtream.codec.core.annotation;

import org.jetbrains.annotations.ApiStatus;

/**
 * {@link XtreamField @XtreamField} 注解用到的表达式
 * <p>
 * 内置三种表达式引擎的实现：
 *
 * <ol>
 *     <li><a href="https://docs.spring.io/spring-framework/reference/core/expressions.html">SpEL</a></li>
 *     <li><a href="http://mvel.documentnode.com/">MVEL</a></li>
 *     <li><a href="https://github.com/killme2008/aviatorscript">Aviator</a></li>
 * </ol>
 *
 * @author hylexus
 * @see XtreamField#lengthExpressions()
 * @see XtreamField#conditions()
 * @see XtreamField#iterationTimesExpressions()
 * @see io.github.hylexus.xtream.codec.core.XtreamExpressionFactory
 * @see io.github.hylexus.xtream.codec.base.expression.XtreamExpression
 * @see io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngine
 * @see io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext
 * @since 0.4.0
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.4.0")
public @interface Expression {

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine.SpelXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine.SpelXtreamEvaluationContext
     */
    String spel() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine.MvelXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine.MvelXtreamEvaluationContext
     */
    String mvel() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine.AviatorXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine.AviatorXtreamEvaluationContext
     */
    String aviator() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.CustomXtreamExpressionEngine
     */
    String custom() default "";
}

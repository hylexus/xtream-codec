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
 * {@link XtreamField @XtreamField} 注解用到的表达式。
 *
 * <h3 color="red">使用建议</h3>
 * 应将表达式视为 <strong color="green">逻辑路由器</strong>，而非 <strong color="red">逻辑处理器</strong>。
 * <ol>
 *   <li>表达式仅用于调用无副作用的方法或访问属性。</li>
 *   <li>业务逻辑应尽量封装在 Java 方法中，而不是直接写在表达式里。</li>
 *   <li>不建议在表达式中编写复杂运算、条件或流程控制。</li>
 *   <li>若表达式逻辑开始变复杂，应回退为 Java 方法。</li>
 *   <li>表达式的可读性优先于灵活性。</li>
 * </ol>
 *
 * <h3 color="red">安全说明</h3>
 * <ol>
 *   <li>表达式应仅来自硬编码注解属性，不允许通过外部配置或用户输入注入。</li>
 *   <li>表达式是否安全，和表达式引擎无关，取决于表达式本身(表达式引擎不保证表达式的执行是否安全可控)</li>
 *   <li>使用方应遵守“无副作用、简单可读”的原则，避免在表达式中写业务逻辑或复杂操作。</li>
 * </ol>
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

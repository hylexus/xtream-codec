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

package io.github.hylexus.xtream.codec.base.web.annotation;

import java.lang.annotation.*;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.base.web.handler.reactive.ClientIpArgumentResolverReactive
 * @see io.github.hylexus.xtream.codec.base.web.handler.servlet.ClientIpArgumentResolverServlet
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientIp {

    String NULL_PLACEHOLDER = "<<<NULL>>>";
    String LOCALHOST_VALUE = "127.0.0.1";

    /**
     * 是否忽略本地地址
     * <p>
     * 如果为 {@code true} 则忽略本地地址(返回{@code null})
     *
     * @see io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils#isLocalhost(String)
     */
    boolean ignoreLocalhost() default false;

    /**
     * {@link #ignoreLocalhost()} 为 false 时生效；遇到本地地址时返回此值
     *
     * @see io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils#filterClientIp(String, String, boolean, String)
     */
    String localhostValue() default LOCALHOST_VALUE;

    /**
     * @see io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils#filterNullIp(String)
     */
    String defaultValue() default NULL_PLACEHOLDER;

}

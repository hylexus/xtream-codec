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

import java.lang.reflect.Modifier;

/**
 * @author hylexus
 */
public interface XtreamCacheableClassPredicate {

    /**
     * 默认实现，下面类型不缓存:
     * <li>Lambda表达式</li>
     * <li>匿名内部类</li>
     * <li>动态代理类</li>
     * <li>编译器合成的类</li>
     * <li>...</li>
     *
     * @implNote 静态内部类除外
     */
    boolean test(Class<?> cls);

    class Default implements XtreamCacheableClassPredicate {

        @Override
        public boolean test(Class<?> cls) {
            if (cls.getName().contains("$")) {
                return isStaticInnerClass(cls);
            }
            return !cls.isSynthetic();
        }

        /**
         * 判断是否为静态内部类
         *
         * @param cls 类对象
         * @return 是否为静态内部类
         */
        private boolean isStaticInnerClass(Class<?> cls) {
            final Class<?> enclosingClass = cls.getEnclosingClass();
            return enclosingClass != null && Modifier.isStatic(cls.getModifiers());
        }
    }
}

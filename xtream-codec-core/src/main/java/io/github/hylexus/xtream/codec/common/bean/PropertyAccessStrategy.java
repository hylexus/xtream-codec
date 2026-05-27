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

import io.github.hylexus.xtream.codec.core.utils.BeanUtils;

/**
 * 属性访问策略
 * <p>
 * 用于控制生成 {@link io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertyGetter} / {@link io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertySetter} 的策略
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertyAccessor
 * @see PropertyAccessorFactory
 * @see io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertyGetter
 * @see io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertySetter
 * @since 0.3.0
 */
public enum PropertyAccessStrategy {

    /**
     * 自动选择（推荐）
     * <p>
     * 选择策略：
     * <table border="1" cellpadding="4" cellspacing="0" summary="PropertyAccessor 优先级说明">
     *     <thead>
     *         <tr>
     *             <th>场景</th>
     *             <th>优先顺序</th>
     *             <th>说明</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>有 getter/setter 方法</td>
     *             <td>LambdaMetafactory → MethodHandle → Reflection</td>
     *             <td>Lambda 通常最优（可被 JIT 内联）</td>
     *         </tr>
     *         <tr>
     *             <td>仅有字段</td>
     *             <td>VarHandle → MethodHandle → Reflection</td>
     *             <td>VarHandle 对字段访问最快</td>
     *         </tr>
     *         <tr>
     *             <td>特殊情况</td>
     *             <td>Lambda 失败则降级</td>
     *             <td>确保兼容性</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @see PropertyAccessorFactory#createPropertyAccessor(PropertyAccessStrategy, BeanUtils.BasicPropertyDescriptor)
     */
    AUTO,

    /**
     * 使用 {@link java.lang.invoke.LambdaMetafactory} （仅方法）
     *
     * @see PropertyGetters.LambdaMetaFactoryMethodPropertyGetter
     * @see PropertySetters.LambdaMetaFactoryMethodPropertySetter
     */
    LAMBDA_META_FACTORY,

    /**
     * 使用 {@link java.lang.invoke.VarHandle} （仅字段）
     *
     * @see PropertyGetters.VarHandleFieldPropertyGetter
     * @see PropertySetters.VarHandleFieldPropertySetter
     */
    VAR_HANDLE,

    /**
     * 使用 {@link java.lang.invoke.MethodHandle}
     *
     * @see PropertyGetters.MethodHandleMethodPropertyGetter
     * @see PropertySetters.MethodHandleMethodPropertySetter
     */
    METHOD_HANDLE,

    /**
     * 使用 {@link java.lang.reflect.Method} 或 {@link java.lang.reflect.Field} 直接访问
     *
     * @see PropertyGetters.ReflectionMethodPropertyGetter
     * @see PropertySetters.ReflectionMethodPropertySetter
     */
    REFLECTION,
}

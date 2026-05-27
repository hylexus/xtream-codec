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

import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * {@code PropertyAccessorFactory} 根据 {@link PropertyAccessStrategy}
 * 和 {@link io.github.hylexus.xtream.codec.core.utils.BeanUtils.BasicPropertyDescriptor} 自动创建最优的属性访问器实现。
 *
 * <p>策略说明：</p>
 *
 * <table border="1" cellspacing="0" cellpadding="4">
 *     <caption>属性访问策略优先级表</caption>
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
 *             <td>Lambda 通常最优（JIT inline 后几乎无开销）</td>
 *         </tr>
 *         <tr>
 *             <td>仅有字段</td>
 *             <td>VarHandle → MethodHandle → Reflection</td>
 *             <td>VarHandle 对字段访问速度最快</td>
 *         </tr>
 *         <tr>
 *             <td>特殊情况</td>
 *             <td>Lambda 失败则自动降级</td>
 *             <td>确保在不同 JVM 下的兼容性</td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public final class PropertyAccessorFactory {

    private PropertyAccessorFactory() {
        throw new UnsupportedOperationException("No instance");
    }

    public static BeanPropertyMetadata.PropertyAccessor createPropertyAccessor(BeanMetadataRegistry.PropertyInfo pi) {
        final BeanUtils.BasicPropertyDescriptor basicPropertyDescriptor = (BeanUtils.BasicPropertyDescriptor) pi.propertyDescriptor();
        final PropertyAccessStrategy classLevelStrategy = pi.xtreamEntity().propertyAccessStrategy();
        final PropertyAccessStrategy fieldLevelStrategy = pi.xtreamField().propertyAccessStrategy();

        // 优先使用字段上的策略，否则使用类级别策略，否则默认 AUTO
        final PropertyAccessStrategy strategy = fieldLevelStrategy != PropertyAccessStrategy.AUTO
                ? fieldLevelStrategy
                : classLevelStrategy;

        return createPropertyAccessor(strategy, basicPropertyDescriptor);
    }

    /**
     * 根据策略和属性描述符创建合适的属性访问器。
     *
     * @param strategy   属性访问策略（可能来自注解）
     * @param descriptor 属性元信息
     * @return 可读写属性的访问器
     */
    public static BeanPropertyMetadata.PropertyAccessor createPropertyAccessor(PropertyAccessStrategy strategy, BeanUtils.BasicPropertyDescriptor descriptor) {
        // Record 类型字段
        if (descriptor.getRecordComponent() != null) {
            final RecordComponent rc = descriptor.getRecordComponent();
            final Method accessor = rc.getAccessor();
            final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forMethodViaLambdaMetaFactory(accessor);
            // Record 没有可用 setter，使用只读 setter 作为占位符
            final BeanPropertyMetadata.PropertySetter setter = PropertySetters.RecordReadOnlyPropertySetter.INSTANCE;
            return new BeanPropertyMetadata.PropertyAccessor(getter, setter);
        }
        final BeanPropertyMetadata.PropertyGetter getter = createGetter(strategy, descriptor);
        final BeanPropertyMetadata.PropertySetter setter = createSetter(strategy, descriptor);
        return new BeanPropertyMetadata.PropertyAccessor(getter, setter);
    }

    private static BeanPropertyMetadata.PropertyGetter createGetter(
            PropertyAccessStrategy strategy,
            BeanUtils.BasicPropertyDescriptor descriptor) {

        final Field field = descriptor.getField();
        final Method readMethod = descriptor.getReadMethod();

        return switch (strategy) {
            // 字段不支持 Lambda
            case LAMBDA_META_FACTORY -> readMethod != null ? PropertyGetters.forMethodViaLambdaMetaFactory(readMethod) : PropertyGetters.forFieldViaMethodHandle(field);
            case REFLECTION -> readMethod != null ? PropertyGetters.forMethodViaReflection(readMethod) : PropertyGetters.forFieldViaReflection(field);
            case METHOD_HANDLE -> readMethod != null ? PropertyGetters.forMethodViaMethodHandle(readMethod) : PropertyGetters.forFieldViaMethodHandle(field);
            // VarHandle 仅字段
            case VAR_HANDLE -> PropertyGetters.forFieldViaVarHandle(field);
            case AUTO -> autoDetectGetter(readMethod, field);
        };
    }

    private static BeanPropertyMetadata.PropertySetter createSetter(
            PropertyAccessStrategy strategy,
            BeanUtils.BasicPropertyDescriptor descriptor) {

        final Field field = descriptor.getField();
        final Method writeMethod = descriptor.getWriteMethod();

        return switch (strategy) {
            // 字段不支持 Lambda
            case LAMBDA_META_FACTORY -> writeMethod != null ? PropertySetters.forMethodViaLambdaMetaFactory(writeMethod) : PropertySetters.forFieldViaMethodHandle(field);
            case REFLECTION -> writeMethod != null ? PropertySetters.forMethodViaReflection(writeMethod) : PropertySetters.forFieldViaReflection(field);
            case METHOD_HANDLE -> writeMethod != null ? PropertySetters.forMethodViaMethodHandle(writeMethod) : PropertySetters.forFieldViaMethodHandle(field);
            // 仅字段
            case VAR_HANDLE -> PropertySetters.forFieldViaVarHandle(field);
            case AUTO -> autoDetectSetter(writeMethod, field);
        };
    }

    private static BeanPropertyMetadata.PropertyGetter autoDetectGetter(@Nullable Method readMethod, Field field) {
        // 优先级：Lambda（getter 方法）> VarHandle（字段）> MethodHandle > Reflection
        if (readMethod != null) {
            try {
                return PropertyGetters.forMethodViaLambdaMetaFactory(readMethod);
            } catch (Throwable ignored) {
                try {
                    return PropertyGetters.forMethodViaMethodHandle(readMethod);
                } catch (Throwable ignored2) {
                    return PropertyGetters.forMethodViaReflection(readMethod);
                }
            }
        } else {
            try {
                return PropertyGetters.forFieldViaVarHandle(field);
            } catch (Throwable ignored) {
                try {
                    return PropertyGetters.forFieldViaMethodHandle(field);
                } catch (Throwable ignored2) {
                    return PropertyGetters.forFieldViaReflection(field);
                }
            }
        }
    }

    private static BeanPropertyMetadata.PropertySetter autoDetectSetter(@Nullable Method writeMethod, Field field) {
        // 优先级：Lambda（setter 方法）> VarHandle（字段）> MethodHandle > Reflection
        if (writeMethod != null) {
            try {
                return PropertySetters.forMethodViaLambdaMetaFactory(writeMethod);
            } catch (Throwable ignored) {
                try {
                    return PropertySetters.forMethodViaMethodHandle(writeMethod);
                } catch (Throwable ignored2) {
                    return PropertySetters.forMethodViaReflection(writeMethod);
                }
            }
        } else {
            try {
                return PropertySetters.forFieldViaVarHandle(field);
            } catch (Throwable ignored) {
                try {
                    return PropertySetters.forFieldViaMethodHandle(field);
                } catch (Throwable ignored2) {
                    return PropertySetters.forFieldViaReflection(field);
                }
            }
        }
    }
}

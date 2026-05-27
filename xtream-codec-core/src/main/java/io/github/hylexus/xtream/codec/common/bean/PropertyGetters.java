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


import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 该类提供 {@link io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertyGetter} 的几个内置实现类
 *
 * <h3>属性访问方式对比</h3>
 *
 * <table border="1px solid red">
 *     <tr>
 *         <th>类型</th>
 *         <th>LambdaMetafactory</th>
 *         <th>MethodHandle</th>
 *         <th>VarHandle</th>
 *         <th>反射</th>
 *     </tr>
 *     <tr>
 *         <td>方法</td>
 *         <td>✅ 支持</td>
 *         <td>✅ 支持</td>
 *         <td>❌ 不支持</td>
 *         <td>✅ 支持</td>
 *     </tr>
 *     <tr>
 *         <td>字段</td>
 *         <td>❌ 不支持</td>
 *         <td>✅ 支持</td>
 *         <td>✅ 支持</td>
 *         <td>✅ 支持</td>
 *     </tr>
 * </table>
 *
 * <h3>建议</h3>
 * <li>方法访问: 推荐基于 {@link LambdaMetafactory} 或 {@link MethodHandle} 的实现</li>
 * <li>字段访问: 推荐基于 {@link VarHandle} 或 {@link MethodHandle}的实现</li>
 *
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertySetter
 * @see LambdaMetafactory
 * @see MethodHandle
 * @see VarHandle
 * @see Field
 * @see Method
 * @since 0.2.0
 */
public final class PropertyGetters {

    private PropertyGetters() {
        throw new UnsupportedOperationException("No instance");
    }

    /* =========================================================
     * =============== 辅助工厂方法 ==============================
     * ========================================================= */

    public static BeanPropertyMetadata.PropertyGetter forFieldViaReflection(Field field) {
        return new ReflectionFieldPropertyGetter(field);
    }

    public static BeanPropertyMetadata.PropertyGetter forMethodViaReflection(Method method) {
        return new ReflectionMethodPropertyGetter(method);
    }

    public static BeanPropertyMetadata.PropertyGetter forMethodViaMethodHandle(Method method) {
        return MethodHandleMethodPropertyGetter.of(method);
    }

    public static BeanPropertyMetadata.PropertyGetter forFieldViaMethodHandle(Field field) {
        return MethodHandleFieldPropertyGetter.of(field);
    }

    public static BeanPropertyMetadata.PropertyGetter forMethodViaLambdaMetaFactory(Method method) {
        return LambdaMetaFactoryMethodPropertyGetter.of(method);
    }

    public static BeanPropertyMetadata.PropertyGetter forFieldViaVarHandle(Field field) {
        return VarHandleFieldPropertyGetter.of(field);
    }

    /* =========================================================
     * =============== 1. 基于反射 Reflection（方法 + 字段）  ====================
     * ========================================================= */

    public record ReflectionMethodPropertyGetter(Method method)
            implements BeanPropertyMetadata.PropertyGetter {

        public ReflectionMethodPropertyGetter {
            method.setAccessible(true);
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            return ReflectionUtils.invokeMethod(this.method, instance);
        }
    }

    public record ReflectionFieldPropertyGetter(Field field)
            implements BeanPropertyMetadata.PropertyGetter {

        public ReflectionFieldPropertyGetter {
            field.setAccessible(true);
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            return ReflectionUtils.getField(this.field, instance);
        }
    }

    /* =========================================================
     * ============ 2. 基于 MethodHandle（方法 + 字段） ========================
     * ========================================================= */

    public record MethodHandleMethodPropertyGetter(Method method, MethodHandle handle)
            implements BeanPropertyMetadata.PropertyGetter {
        public static MethodHandleMethodPropertyGetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);
                // 适配成 (Object)Object 类型，避免 invokeWithArguments
                final MethodType targetType = MethodType.methodType(Object.class, method.getDeclaringClass());
                final MethodHandle adapted = target.asType(targetType);
                return new MethodHandleMethodPropertyGetter(method, adapted);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            try {
                // return handle.bindTo(instance).invoke();
                return this.handle.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public record MethodHandleFieldPropertyGetter(Field field, MethodHandle handle)
            implements BeanPropertyMetadata.PropertyGetter {
        public static MethodHandleFieldPropertyGetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final MethodHandle target = lookup.unreflectGetter(field);
                // 适配成 (Object)Object 类型，避免 invokeWithArguments
                final MethodType targetType = MethodType.methodType(Object.class, field.getDeclaringClass());
                final MethodHandle adapted = target.asType(targetType);
                return new MethodHandleFieldPropertyGetter(field, adapted);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            try {
                // return handle.bindTo(instance).invoke();
                return this.handle.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* =========================================================
     * ========== 3. 基于 LambdaMetafactory（方法） ======================
     * ========================================================= */

    public record LambdaMetaFactoryMethodPropertyGetter(Method method, Function<Object, @Nullable Object> getter) implements BeanPropertyMetadata.PropertyGetter {
        public static LambdaMetaFactoryMethodPropertyGetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);

                final MethodType invokedType = MethodType.methodType(Function.class);
                final MethodType methodType = MethodType.methodType(Object.class, Object.class);

                final CallSite site = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        invokedType,
                        methodType,
                        target,
                        target.type()
                );

                @SuppressWarnings("unchecked") final Function<Object, Object> getter = (Function<Object, Object>) site.getTarget().invoke();
                return new LambdaMetaFactoryMethodPropertyGetter(method, getter);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            return this.getter.apply(instance);
        }
    }

    /* =========================================================
     * =============== 4. 基于 VarHandle（字段） ==================
     * ========================================================= */

    public record VarHandleFieldPropertyGetter(Field field, VarHandle varHandle)
            implements BeanPropertyMetadata.PropertyGetter {
        public static VarHandleFieldPropertyGetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final VarHandle varHandle = lookup.unreflectVarHandle(field);
                return new VarHandleFieldPropertyGetter(field, varHandle);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public @Nullable Object getProperty(BeanPropertyMetadata metadata, Object instance) {
            return this.varHandle.get(instance);
        }
    }

}

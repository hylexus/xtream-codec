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

import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 *
 * 该类提供 {@link io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertySetter} 的几个内置实现类
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
public final class PropertySetters {

    private PropertySetters() {
        throw new UnsupportedOperationException("No instance");
    }

    /* =========================================================
     * =============== 辅助工厂方法 ==============================
     * ========================================================= */

    public static BeanPropertyMetadata.PropertySetter forFieldViaReflection(Field field) {
        return new ReflectionFieldPropertySetter(field);
    }

    public static BeanPropertyMetadata.PropertySetter forMethodViaReflection(Method method) {
        return new ReflectionMethodPropertySetter(method);
    }

    public static BeanPropertyMetadata.PropertySetter forMethodViaMethodHandle(Method method) {
        return MethodHandleMethodPropertySetter.of(method);
    }

    public static BeanPropertyMetadata.PropertySetter forFieldViaMethodHandle(Field field) {
        return MethodHandleFieldPropertySetter.of(field);
    }

    public static BeanPropertyMetadata.PropertySetter forMethodViaLambdaMetaFactory(Method method) {
        return LambdaMetaFactoryMethodPropertySetter.of(method);
    }

    public static BeanPropertyMetadata.PropertySetter forFieldViaVarHandle(Field field) {
        return VarHandleFieldPropertySetter.of(field);
    }

    /* =========================================================
     * =============== 1. 基于反射 Reflection（方法 + 字段）  ====================
     * ========================================================= */

    public record ReflectionMethodPropertySetter(Method method) implements BeanPropertyMetadata.PropertySetter {

        public ReflectionMethodPropertySetter {
            method.setAccessible(true);
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            ReflectionUtils.invokeMethod(this.method, instance, value);
        }
    }

    public record ReflectionFieldPropertySetter(Field field) implements BeanPropertyMetadata.PropertySetter {

        public ReflectionFieldPropertySetter {
            field.setAccessible(true);
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            ReflectionUtils.setField(this.field, instance, value);
        }
    }

    /* =========================================================
     * ============ 2. 基于 MethodHandle（方法 + 字段） ========================
     * ========================================================= */

    public record MethodHandleMethodPropertySetter(MethodHandle handle) implements BeanPropertyMetadata.PropertySetter {

        public static MethodHandleMethodPropertySetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);
                // 适配成 (Object, Object) -> void
                final MethodHandle adapted = target.asType(MethodType.methodType(void.class, method.getDeclaringClass(), Object.class));
                return new MethodHandleMethodPropertySetter(adapted);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            try {
                this.handle.invoke(instance, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public record MethodHandleFieldPropertySetter(MethodHandle handle) implements BeanPropertyMetadata.PropertySetter {

        public static MethodHandleFieldPropertySetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final MethodHandle target = lookup.unreflectSetter(field);
                final MethodHandle adapted = target.asType(MethodType.methodType(void.class, field.getDeclaringClass(), Object.class));
                return new MethodHandleFieldPropertySetter(adapted);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            try {
                this.handle.invoke(instance, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* =========================================================
     * ========== 3. 基于 LambdaMetafactory（方法） ======================
     * ========================================================= */

    public sealed interface LambdaMetaFactoryMethodPropertySetter
            extends BeanPropertyMetadata.PropertySetter
            permits LambdaMetaFactoryMethodNonChainSetter, LambdaMetaFactoryMethodChainSetter {

        static LambdaMetaFactoryMethodPropertySetter of(Method method) {
            if (method.getReturnType() == void.class) {
                return LambdaMetaFactoryMethodNonChainSetter.of(method);
            }
            return LambdaMetaFactoryMethodChainSetter.of(method);
        }
    }

    /* =========================================================
     * =============== 4. 基于 VarHandle（字段） ==================
     * ========================================================= */

    public record VarHandleFieldPropertySetter(VarHandle varHandle) implements BeanPropertyMetadata.PropertySetter {

        public static VarHandleFieldPropertySetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final VarHandle varHandle = lookup.unreflectVarHandle(field);
                return new VarHandleFieldPropertySetter(varHandle);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            this.varHandle.set(instance, value);
        }
    }

    // ====================== 普通 setter =========================
    record LambdaMetaFactoryMethodNonChainSetter(BiConsumer<Object, @Nullable Object> setter)
            implements LambdaMetaFactoryMethodPropertySetter {

        static LambdaMetaFactoryMethodNonChainSetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);
                final Class<?> paramType = method.getParameterTypes()[0];
                final MethodType targetType = target.type()
                        .changeParameterType(1, paramType);

                final MethodType invokedType = MethodType.methodType(BiConsumer.class);
                final MethodType interfaceType = MethodType.methodType(void.class, Object.class, Object.class);

                final CallSite site = LambdaMetafactory.metafactory(
                        lookup,
                        "accept",
                        invokedType,
                        interfaceType,
                        target,
                        targetType
                );

                @SuppressWarnings("unchecked") final BiConsumer<Object, Object> setter = (BiConsumer<Object, Object>) site.getTarget().invoke();
                return new LambdaMetaFactoryMethodNonChainSetter(setter);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            this.setter.accept(instance, value);
        }
    }

    // ====================== 链式 setter =========================
    record LambdaMetaFactoryMethodChainSetter(BiFunction<Object, @Nullable Object, Object> setter) implements LambdaMetaFactoryMethodPropertySetter {

        static LambdaMetaFactoryMethodChainSetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);

                final Class<?> paramType = method.getParameterTypes()[0];
                final Class<?> returnType = method.getReturnType();

                final MethodType targetType = target.type()
                        .changeParameterType(1, paramType)
                        .changeReturnType(returnType);

                final MethodType invokedType = MethodType.methodType(BiFunction.class);
                final MethodType interfaceMethodType = MethodType.methodType(Object.class, Object.class, Object.class);

                final CallSite site = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        invokedType,
                        interfaceMethodType,
                        target,
                        targetType
                );

                @SuppressWarnings("unchecked") final BiFunction<Object, Object, Object> setter = (BiFunction<Object, Object, Object>) site.getTarget().invoke();
                return new LambdaMetaFactoryMethodChainSetter(setter);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            this.setter.apply(instance, value);
        }

    }

}

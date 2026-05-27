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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.*;

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

    public record MethodHandleMethodPropertySetter(Method method, MethodHandle handle) implements BeanPropertyMetadata.PropertySetter {

        public static MethodHandleMethodPropertySetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle target = lookup.unreflect(method);
                // 适配成 (Object, Object) -> void
                final MethodHandle adapted = target.asType(MethodType.methodType(void.class, method.getDeclaringClass(), Object.class));
                return new MethodHandleMethodPropertySetter(method, adapted);
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

    public record MethodHandleFieldPropertySetter(Field field, MethodHandle handle) implements BeanPropertyMetadata.PropertySetter {

        public static MethodHandleFieldPropertySetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final MethodHandle target = lookup.unreflectSetter(field);
                final MethodHandle adapted = target.asType(MethodType.methodType(void.class, field.getDeclaringClass(), Object.class));
                return new MethodHandleFieldPropertySetter(field, adapted);
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
            permits LambdaMetaFactoryMethodPropertydNonChainSetter, LambdaMetaFactoryMethodPropertyChainSetter {

        Method method();

        static LambdaMetaFactoryMethodPropertySetter of(Method method) {
            if (method.getReturnType() == void.class) {
                return LambdaMetaFactoryMethodPropertydNonChainSetter.of(method);
            }
            return LambdaMetaFactoryMethodPropertyChainSetter.of(method);
        }
    }

    /* =========================================================
     * =============== 4. 基于 VarHandle（字段） ==================
     * ========================================================= */

    public record VarHandleFieldPropertySetter(Field field, VarHandle varHandle) implements BeanPropertyMetadata.PropertySetter {

        public static VarHandleFieldPropertySetter of(Field field) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
                final VarHandle varHandle = lookup.unreflectVarHandle(field);
                return new VarHandleFieldPropertySetter(field, varHandle);
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
    record LambdaMetaFactoryMethodPropertydNonChainSetter(Method method, BiConsumer<Object, @Nullable Object> setter)
            implements LambdaMetaFactoryMethodPropertySetter {

        static LambdaMetaFactoryMethodPropertydNonChainSetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
                final MethodHandle rawTarget = lookup.unreflect(method);

                final Class<?> paramType = method.getParameterTypes()[0];

                // 适配基础类型
                // 下面代码理解起来可能有点费力
                // 为方便调试 就不重构了
                if (paramType == byte.class) {
                    final MethodType invokedType = MethodType.methodType(ObjByteConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, byte.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final ObjByteConsumer<Object> oc = (ObjByteConsumer<@NonNull Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).byteValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else if (paramType == short.class) {
                    final MethodType invokedType = MethodType.methodType(ObjShortConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, short.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final ObjShortConsumer<Object> oc = (ObjShortConsumer<@NonNull Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).shortValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else if (paramType == int.class) {
                    final MethodType invokedType = MethodType.methodType(ObjIntConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, int.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);
                    @SuppressWarnings("unchecked") final ObjIntConsumer<Object> oc = (ObjIntConsumer<Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).intValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else if (paramType == long.class) {
                    final MethodType invokedType = MethodType.methodType(ObjLongConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, long.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final ObjLongConsumer<Object> oc = (ObjLongConsumer<Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).longValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else if (paramType == float.class) {
                    final MethodType invokedType = MethodType.methodType(ObjFloatConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, float.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final ObjFloatConsumer<Object> oc = (ObjFloatConsumer<@NonNull Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).floatValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else if (paramType == double.class) {
                    final MethodType invokedType = MethodType.methodType(ObjDoubleConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, double.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final ObjDoubleConsumer<Object> oc = (ObjDoubleConsumer<Object>) cs.getTarget().invoke();
                    final BiConsumer<Object, Object> unified = (instance, value) -> oc.accept(instance, ((Number) value).doubleValue());
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, unified);
                } else {
                    // 引用类型（包括 包装类型）
                    final MethodType invokedType = MethodType.methodType(BiConsumer.class);
                    final MethodType samMethodType = MethodType.methodType(void.class, Object.class, Object.class);
                    final MethodType implMethodType = rawTarget.type();

                    final CallSite cs = LambdaMetafactory.metafactory(lookup, "accept", invokedType, samMethodType, rawTarget, implMethodType);

                    @SuppressWarnings("unchecked") final BiConsumer<Object, Object> bc = (BiConsumer<Object, Object>) cs.getTarget().invoke();
                    return new LambdaMetaFactoryMethodPropertydNonChainSetter(method, bc);
                }
            } catch (Throwable e) {
                throw new RuntimeException("无法创建 LambdaMetaFactory setter (non-chain): " + method, e);
            }
        }

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            this.setter.accept(instance, value);
        }
    }

    // ====================== 链式 setter =========================
    record LambdaMetaFactoryMethodPropertyChainSetter(Method method, BiFunction<Object, @Nullable Object, Object> setter) implements LambdaMetaFactoryMethodPropertySetter {

        static LambdaMetaFactoryMethodPropertyChainSetter of(Method method) {
            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
                final MethodHandle target = lookup.unreflect(method);

                final Class<?> declaringClass = method.getDeclaringClass();
                final Class<?> paramType = method.getParameterTypes()[0];
                final Class<?> returnType = method.getReturnType();

                // implMethodType = (declaringClass, paramType) -> returnType
                final MethodType implMethodType = MethodType.methodType(returnType, declaringClass, paramType);

                final MethodType invokedType;
                final MethodType samMethodType;

                // 根据参数类型决定函数式接口
                if (paramType == byte.class) {
                    invokedType = MethodType.methodType(ObjByteFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, byte.class);
                } else if (paramType == short.class) {
                    invokedType = MethodType.methodType(ObjShortFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, short.class);
                } else if (paramType == int.class) {
                    invokedType = MethodType.methodType(ObjIntFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, int.class);
                } else if (paramType == long.class) {
                    invokedType = MethodType.methodType(ObjLongFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, long.class);
                } else if (paramType == float.class) {
                    invokedType = MethodType.methodType(ObjFloatFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, float.class);
                } else if (paramType == double.class) {
                    invokedType = MethodType.methodType(ObjDoubleFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, double.class);
                } else {
                    // Object类型，正常 BiFunction
                    invokedType = MethodType.methodType(BiFunction.class);
                    samMethodType = MethodType.methodType(Object.class, Object.class, Object.class);
                }

                final CallSite site = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        invokedType,
                        samMethodType,
                        target,
                        implMethodType
                );

                final Object fn = site.getTarget().invoke();

                // 用统一包装器包装成 BiFunction<Object,Object,Object>
                @SuppressWarnings("unchecked") final BiFunction<Object, Object, Object> unified = (instance, value) -> {
                    try {
                        return switch (fn) {
                            case BiFunction<?, ?, ?> f -> ((BiFunction<Object, Object, Object>) f).apply(instance, value);
                            case ObjByteFunction<?, ?> f -> ((ObjByteFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).byteValue());
                            case ObjShortFunction<?, ?> f -> ((ObjShortFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).shortValue());
                            case ObjIntFunction<?, ?> f -> ((ObjIntFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).intValue());
                            case ObjLongFunction<?, ?> f -> ((ObjLongFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).longValue());
                            case ObjFloatFunction<?, ?> f -> ((ObjFloatFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).floatValue());
                            case ObjDoubleFunction<?, ?> f -> ((ObjDoubleFunction<@NonNull Object, @NonNull Object>) f).apply(instance, ((Number) value).doubleValue());
                            case null, default -> throw new IllegalStateException("Unexpected lambda type: " + fn);
                        };
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };

                return new LambdaMetaFactoryMethodPropertyChainSetter(method, unified);
            } catch (Throwable e) {
                throw new RuntimeException("无法创建 LambdaMetaFactory setter(chain): " + method, e);
            }
        }

        @Override
        @SuppressWarnings("ReturnValueIgnored")
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            this.setter.apply(instance, value);
        }

    }

    /* ========================================================================
     * =============== 5. Record 类型没有 Setter，这里用作占位符 ===================
     * ======================================================================== */
    public enum RecordReadOnlyPropertySetter implements BeanPropertyMetadata.PropertySetter {
        INSTANCE;

        @Override
        public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
            throw new UnsupportedOperationException("Record property is read-only: " + metadata);
        }

    }

    @FunctionalInterface
    public interface ObjByteFunction<T, R> {
        R apply(T t, byte value);
    }

    @FunctionalInterface
    public interface ObjShortFunction<T, R> {
        R apply(T t, short value);
    }

    @FunctionalInterface
    public interface ObjIntFunction<T, R> {
        R apply(T t, int value);
    }

    @FunctionalInterface
    public interface ObjLongFunction<T, R> {
        R apply(T t, long value);
    }

    @FunctionalInterface
    public interface ObjFloatFunction<T, R> {
        R apply(T t, float value);
    }

    @FunctionalInterface
    public interface ObjDoubleFunction<T, R> {
        R apply(T t, double value);
    }

    @FunctionalInterface
    public interface ObjByteConsumer<T> {
        void accept(T t, byte value);
    }

    @FunctionalInterface
    public interface ObjShortConsumer<T> {
        void accept(T t, short value);
    }

    @FunctionalInterface
    public interface ObjFloatConsumer<T> {
        void accept(T t, float value);
    }

}

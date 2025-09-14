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

package io.github.hylexus.xtream.codec.core.utils;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.impl.FieldPropertySetter;
import io.github.hylexus.xtream.codec.common.bean.impl.FiledPropertyGetter;
import io.github.hylexus.xtream.codec.common.bean.impl.MethodPropertyGetter;
import io.github.hylexus.xtream.codec.common.bean.impl.MethodPropertySetter;
import io.github.hylexus.xtream.codec.common.exception.BeanIntrospectionException;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

public class BeanUtils {
    public static final BasicPropertyDescriptor[] EMPTY_ARRAY = {};

    private static final Map<Class<?>, BeanInfo> CACHE = new HashMap<>();

    public static BeanInfo getBeanInfo(Class<?> beanClass, XtreamCacheableClassPredicate cacheableClassPredicate, Predicate<Field> fieldPredicate) throws BeanIntrospectionException {

        BeanInfo beanInfo;
        if ((beanInfo = CACHE.get(beanClass)) != null) {
            return beanInfo;
        }

        final BasicPropertyDescriptor[] propertyDescriptors = determineBasicProperties(beanClass, fieldPredicate);

        beanInfo = new SimpleBeanInfo() {
            @Override
            public BeanDescriptor getBeanDescriptor() {
                return new BeanDescriptor(beanClass);
            }

            @Override
            public BasicPropertyDescriptor[] getPropertyDescriptors() {
                return propertyDescriptors;
            }

            @Override
            public MethodDescriptor[] getMethodDescriptors() {
                throw new UnsupportedOperationException();
            }

        };

        if (cacheableClassPredicate.test(beanClass)) {
            CACHE.put(beanClass, beanInfo);
        }
        return beanInfo;
    }

    public static BasicPropertyDescriptor[] determineBasicProperties(Class<?> beanClass, Predicate<Field> fieldPredicate) {

        final Map<String, BasicPropertyDescriptor> pdMap = new LinkedHashMap<>();

        ReflectionUtils.doWithFields(beanClass, field -> {
            if (!fieldPredicate.test(field)) {
                return;
            }
            final String name = field.getName();
            field.setAccessible(true);
            final BasicPropertyDescriptor pd = pdMap.get(name);
            if (pd == null) {
                pdMap.put(name, BasicPropertyDescriptor.of(field, null, null));
            }
        });

        ReflectionUtils.doWithMethods(beanClass, method -> {
            final String methodName = method.getName();

            boolean setter;
            int nameIndex;
            if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                setter = true;
                nameIndex = 3;
            } else if (methodName.startsWith("get") && method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE) {
                setter = false;
                nameIndex = 3;
            } else if (methodName.startsWith("is") && method.getParameterCount() == 0 && method.getReturnType() == boolean.class) {
                setter = false;
                nameIndex = 2;
            } else if (methodName.startsWith("with") && method.getParameterCount() == 1) {
                setter = true;
                nameIndex = 4;
            } else {
                return;
            }

            final String propertyName = StringUtils.uncapitalizeAsProperty(methodName.substring(nameIndex));
            if (propertyName.isEmpty()) {
                return;
            }

            final BasicPropertyDescriptor pd = pdMap.get(propertyName);
            if (pd != null) {
                if (setter) {
                    final Method writeMethod = pd.getWriteMethod();
                    if (writeMethod == null || writeMethod.getParameterTypes()[0].isAssignableFrom(method.getParameterTypes()[0])) {
                        pd.setWriteMethod(method);
                        method.setAccessible(true);
                    }
                } else {
                    final Method readMethod = pd.getReadMethod();
                    if (readMethod == null || (readMethod.getReturnType() == method.getReturnType() && method.getName().startsWith("is"))) {
                        pd.setReadMethod(method);
                        method.setAccessible(true);
                    }
                }
            }
        });


        return pdMap.values().toArray(EMPTY_ARRAY);
    }

    public static BeanPropertyMetadata.PropertySetter createSetter(BeanUtils.BasicPropertyDescriptor pd) {
        if (pd.getWriteMethod() != null) {
            return new MethodPropertySetter(pd.getWriteMethod());
        }
        return new FieldPropertySetter(pd.getField());
    }

    public static BeanPropertyMetadata.PropertyGetter createGetter(BeanUtils.BasicPropertyDescriptor pd) {
        if (pd.getReadMethod() != null) {
            return new MethodPropertyGetter(pd.getReadMethod());
        }
        return new FiledPropertyGetter(pd.getField());
    }

    public static Constructor<?> getConstructor(BeanDescriptor beanDescriptor) {
        try {
            return beanDescriptor.getBeanClass().getConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeanIntrospectionException(e);
        }
    }

    public static <T extends FieldCodec<?>> T createFieldCodecInstance(Class<T> cls, BeanMetadataRegistry beanMetadataRegistry) {
        final Constructor<?>[] constructors = cls.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException(
                    "Cannot create FieldCodec instance for class [" + cls.getName() + "]"
                    + "\nNo Constructor found");
        }
        if (constructors.length == 1) {
            return createFieldCodecInstance(cls, constructors[0], beanMetadataRegistry);
        }
        final List<Constructor<?>> constructorList = Arrays.stream(constructors).filter(it -> it.getAnnotation(FieldCodec.FieldCodecCreator.class) != null).toList();
        if (constructorList.size() == 1) {
            return createFieldCodecInstance(cls, constructorList.getFirst(), beanMetadataRegistry);
        }
        if (constructorList.isEmpty()) {
            throw new IllegalStateException(
                    "Failed to create FieldCodec instance for class [" + cls.getName() + "]"
                    + "\nMore than 1 Constructor found."
                    + "\nConsider using the @" + FieldCodec.FieldCodecCreator.class.getSimpleName() + " annotation to mark which constructor to use.");
        }
        throw new IllegalStateException(
                "Failed to create FieldCodec instance for class [" + cls.getName() + "]"
                + "\nMore than 1 @" + FieldCodec.FieldCodecCreator.class.getSimpleName() + " found.");
    }

    private static <T extends FieldCodec<?>> T createFieldCodecInstance(Class<T> cls, Constructor<?> constructor, BeanMetadataRegistry beanMetadataRegistry) {
        final @Nullable Object[] args = new @Nullable Object[constructor.getParameterCount()];
        final boolean ignoreUnknownParameters = Optional.ofNullable(constructor.getAnnotation(FieldCodec.FieldCodecCreator.class)).map(FieldCodec.FieldCodecCreator::ignoreUnknownParameters).orElse(false);
        final Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            if (BeanMetadataRegistry.class.isAssignableFrom(parameter.getType())) {
                args[i] = beanMetadataRegistry;
            } else if (FieldCodecRegistry.class.isAssignableFrom(parameter.getType())) {
                args[i] = beanMetadataRegistry.getFieldCodecRegistry();
            } else {
                if (ignoreUnknownParameters) {
                    args[i] = null;
                } else {
                    throw new IllegalStateException("Cannot create FieldCodec instance for class: " + cls);
                }
            }
        }
        @SuppressWarnings("unchecked") final T newInstance = createNewInstance((Constructor<T>) constructor, args);
        return newInstance;
    }

    public static <T> T createNewInstance(
            Class<T> cls,
            @SuppressWarnings("checkstyle:NoWhitespaceBefore")
            @Nullable Object @Nullable ... args) {

        try {
            final Constructor<T> constructor = cls.getConstructor();
            return createNewInstance(constructor, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T createNewInstance(
            Constructor<T> constructor,
            @SuppressWarnings("checkstyle:NoWhitespaceBefore")
            @Nullable Object @Nullable ... args) {

        constructor.setAccessible(true);
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static class BasicPropertyDescriptor extends PropertyDescriptor {

        @Getter
        private final Field field;

        @Nullable
        private Method readMethod;

        @Nullable
        private Method writeMethod;

        public BasicPropertyDescriptor(Field field, @Nullable Method readMethod, @Nullable Method writeMethod)
                throws IntrospectionException {
            super(field.getName(), readMethod, writeMethod);
            this.field = field;
        }

        public static BasicPropertyDescriptor of(Field field, @Nullable Method readMethod, @Nullable Method writeMethod) {
            try {
                return new BasicPropertyDescriptor(field, readMethod, writeMethod);
            } catch (IntrospectionException e) {
                throw new BeanIntrospectionException(e);
            }
        }

        @Override
        @Nullable
        public Method getReadMethod() {
            return this.readMethod;
        }

        @Override
        public void setReadMethod(Method readMethod) {
            this.readMethod = readMethod;
        }

        @Override
        @Nullable
        public Method getWriteMethod() {
            return this.writeMethod;
        }

        @Override
        public void setWriteMethod(Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        @Override
        public synchronized Class<?> getPropertyType() {
            return field.getType();
        }
    }
}

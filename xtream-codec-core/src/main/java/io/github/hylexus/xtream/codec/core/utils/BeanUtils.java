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

public final class BeanUtils {
    public static final BasicPropertyDescriptor[] EMPTY_ARRAY = {};

    private static final Map<Class<?>, XtreamSimpleBeanInfo> CACHE = new HashMap<>();

    public static class XtreamSimpleBeanInfo extends SimpleBeanInfo {
        private final Class<?> beanClass;
        private final BeanDescriptor beanDescriptor;
        private final BasicPropertyDescriptor[] propertyDescriptors;
        private final boolean recordClass;

        public XtreamSimpleBeanInfo(Class<?> beanClass, BasicPropertyDescriptor[] propertyDescriptors) {
            this.beanDescriptor = new BeanDescriptor(beanClass);
            this.beanClass = beanClass;
            this.propertyDescriptors = propertyDescriptors;
            this.recordClass = beanClass.isRecord();
        }

        @SuppressWarnings({"redundent", "unused"})
        public boolean isRecordClass() {
            return this.recordClass;
        }

        @SuppressWarnings({"redundent", "unused"})
        public Class<?> getBeanClass() {
            return this.beanClass;
        }

        @Override
        public BeanDescriptor getBeanDescriptor() {
            return this.beanDescriptor;
        }

        @Override
        public BasicPropertyDescriptor[] getPropertyDescriptors() {
            return this.propertyDescriptors;
        }

        @Override
        public MethodDescriptor[] getMethodDescriptors() {
            throw new UnsupportedOperationException();
        }

    }

    public static XtreamSimpleBeanInfo getBeanInfo(Class<?> beanClass, XtreamCacheableClassPredicate cacheableClassPredicate, Predicate<AnnotatedElement> fieldPredicate) throws BeanIntrospectionException {

        XtreamSimpleBeanInfo beanInfo;
        if ((beanInfo = CACHE.get(beanClass)) != null) {
            return beanInfo;
        }

        if (beanClass.isRecord()) {
            beanInfo = createRecordTypeBeanInfo(beanClass);
        } else {
            final BasicPropertyDescriptor[] propertyDescriptors = determineBasicProperties(beanClass, fieldPredicate);
            beanInfo = new XtreamSimpleBeanInfo(beanClass, propertyDescriptors);
        }

        if (cacheableClassPredicate.test(beanClass)) {
            CACHE.put(beanClass, beanInfo);
        }
        return beanInfo;
    }

    private static XtreamSimpleBeanInfo createRecordTypeBeanInfo(Class<?> beanClass) {
        XtreamSimpleBeanInfo beanInfo;
        final Field[] declaredFields = beanClass.getDeclaredFields();
        final BasicPropertyDescriptor[] pdList = new BasicPropertyDescriptor[declaredFields.length];
        final RecordComponent[] recordComponents = beanClass.getRecordComponents();
        for (int i = 0; i < recordComponents.length; i++) {
            final RecordComponent recordComponent = recordComponents[i];
            final Field field = declaredFields[i];
            final BasicPropertyDescriptor pd = BasicPropertyDescriptor.ofRecord(field, recordComponent);
            pdList[i] = pd;
        }
        beanInfo = new XtreamSimpleBeanInfo(beanClass, pdList);
        return beanInfo;
    }

    public static BasicPropertyDescriptor[] determineBasicProperties(Class<?> beanClass, Predicate<AnnotatedElement> fieldPredicate) {

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

    public static Constructor<?> getConstructor(BeanInfo beanInfo) {
        try {
            final Class<?> beanClass = beanInfo.getBeanDescriptor().getBeanClass();
            if (beanClass.isRecord()) {
                return XtreamRecordUtils.findCanonicalRecordConstructor(beanClass);
            }

            return beanClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeanIntrospectionException(e);
        }
    }

    public static <T extends FieldCodec<?>> T createFieldCodecInstance(Class<T> cls, BeanMetadataRegistry beanMetadataRegistry, int version) {
        final Constructor<?>[] constructors = cls.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException(
                    "Cannot create FieldCodec instance for class [" + cls.getName() + "]"
                    + "\nNo Constructor found");
        }
        if (constructors.length == 1) {
            return createFieldCodecInstance(version, cls, constructors[0], beanMetadataRegistry);
        }
        final List<Constructor<?>> constructorList = Arrays.stream(constructors).filter(it -> it.getAnnotation(FieldCodec.FieldCodecCreator.class) != null).toList();
        if (constructorList.size() == 1) {
            return createFieldCodecInstance(version, cls, constructorList.getFirst(), beanMetadataRegistry);
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

    private static <T extends FieldCodec<?>> T createFieldCodecInstance(int version, Class<T> cls, Constructor<?> constructor, BeanMetadataRegistry beanMetadataRegistry) {
        final @Nullable Object[] args = new @Nullable Object[constructor.getParameterCount()];
        final boolean ignoreUnknownParameters = Optional.ofNullable(constructor.getAnnotation(FieldCodec.FieldCodecCreator.class)).map(FieldCodec.FieldCodecCreator::ignoreUnknownParameters).orElse(false);
        final Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            if (BeanMetadataRegistry.class.isAssignableFrom(parameter.getType())) {
                args[i] = beanMetadataRegistry;
            } else if (FieldCodecRegistry.class.isAssignableFrom(parameter.getType())) {
                args[i] = beanMetadataRegistry.getFieldCodecRegistry();
            } else if (int.class == parameter.getType() || Integer.class == parameter.getType()) {
                args[i] = version;
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

        @Nullable
        private final RecordComponent recordComponent;

        public BasicPropertyDescriptor(
                Field field,
                @Nullable Method readMethod,
                @Nullable Method writeMethod,
                @Nullable RecordComponent recordComponent) throws IntrospectionException {
            super(field.getName(), readMethod, writeMethod);
            this.field = field;
            this.recordComponent = recordComponent;
        }

        public static BasicPropertyDescriptor of(Field field, @Nullable Method readMethod, @Nullable Method writeMethod) {
            try {
                return new BasicPropertyDescriptor(field, readMethod, writeMethod, null);
            } catch (IntrospectionException e) {
                throw new BeanIntrospectionException(e);
            }
        }

        public static BasicPropertyDescriptor ofRecord(Field field, RecordComponent recordComponent) {
            try {
                return new BasicPropertyDescriptor(field, recordComponent.getAccessor(), null, recordComponent);
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

        @Nullable
        public RecordComponent getRecordComponent() {
            return this.recordComponent;
        }

    }
}

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

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata.PropertyAccessor;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PropertyAccessorFactoryTest {

    // region 测试类定义
    @SuppressWarnings("all")
    public static class TestEntity1 {
        private Long id;
        private String name;
        private int age;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        // 链式 setter
        public TestEntity1 setName(String name) {
            this.name = name;
            return this;
        }

        public int getAge() {
            return age;
        }

        public TestEntity1 setAge(int age) {
            this.age = age;
            return this;
        }
    }

    public record TestEntity2(Long id, String name, int age) {
    }

    @SuppressWarnings("all")
    public static class TestEntity3 {
        private String onlyField;
    }
    // endregion 测试类定义

    // region 工具方法
    private static BeanUtils.BasicPropertyDescriptor descFor(Class<?> clazz, String name)
            throws NoSuchFieldException, IntrospectionException {
        final Field f = clazz.getDeclaredField(name);
        final Method read = findMethod(clazz, "get" + capitalize(name));
        final Method write = findMethod(clazz, "set" + capitalize(name), f.getType());
        final RecordComponent rc = clazz.isRecord() ? clazz.getRecordComponents()[indexOfComponent(clazz, name)] : null;
        return new BeanUtils.BasicPropertyDescriptor(f, read, write, rc);
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static int indexOfComponent(Class<?> recordClass, String name) {
        final RecordComponent[] components = recordClass.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    // endregion 工具方法

    BeanPropertyMetadata beanPropertyMetadata;

    @BeforeEach
    void setUp() {
        beanPropertyMetadata = mock(BeanPropertyMetadata.class);
    }

    @Test
    void printDebugInfo() {
        for (Class<?> clazz : new Class[]{TestEntity1.class, TestEntity2.class, TestEntity3.class}) {
            final BeanUtils.XtreamSimpleBeanInfo info = BeanUtils.getBeanInfo(clazz, clz -> false, ae -> true);
            for (BeanUtils.BasicPropertyDescriptor pd : info.getPropertyDescriptors()) {
                final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.AUTO, pd);
                System.out.printf("[%s] %s => getter=%s, setter=%s%n",
                        clazz.getSimpleName(), pd.getName(),
                        accessor.getter().getClass().getSimpleName(),
                        accessor.setter().getClass().getSimpleName());
            }
        }
    }

    // region 测试 AUTO 策略

    @Test
    void testAutoStrategyWithPojo() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "name");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.AUTO, descriptor);

        assertInstanceOf(PropertyGetters.LambdaMetaFactoryMethodPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.LambdaMetaFactoryMethodChainSetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, "Tom");
        assertEquals("Tom", accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    @Test
    void testAutoStrategyWithRecord() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity2.class, "name");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.AUTO, descriptor);

        assertInstanceOf(PropertyGetters.LambdaMetaFactoryMethodPropertyGetter.class, accessor.getter());
        assertSame(PropertySetters.RecordReadOnlyPropertySetter.INSTANCE, accessor.setter());

        final TestEntity2 user = new TestEntity2(1L, "Tom", 18);
        assertEquals("Tom", accessor.getter().getProperty(beanPropertyMetadata, user));

        assertThrows(UnsupportedOperationException.class,
                () -> accessor.setter().setProperty(beanPropertyMetadata, user, "Jerry"));
    }

    @Test
    void testAutoStrategyWithFieldOnly() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity3.class, "onlyField");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.AUTO, descriptor);

        assertInstanceOf(PropertyGetters.VarHandleFieldPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.VarHandleFieldPropertySetter.class, accessor.setter());

        final TestEntity3 user = new TestEntity3();
        accessor.setter().setProperty(beanPropertyMetadata, user, "OK");
        assertEquals("OK", accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    // endregion 测试 AUTO 策略

    // region 测试不同策略（反射、MethodHandle、Lambda、VarHandle）

    @Test
    void testReflectionStrategy() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "id");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.REFLECTION, descriptor);

        assertInstanceOf(PropertyGetters.ReflectionMethodPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.ReflectionMethodPropertySetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, 99L);
        assertEquals(99L, accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    @Test
    void testMethodHandleStrategy() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "age");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.METHOD_HANDLE, descriptor);

        assertInstanceOf(PropertyGetters.MethodHandleMethodPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.MethodHandleMethodPropertySetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, 20);
        assertEquals(20, accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    @Test
    void testLambdaStrategy() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "name");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.LAMBDA_META_FACTORY, descriptor);

        assertInstanceOf(PropertyGetters.LambdaMetaFactoryMethodPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.LambdaMetaFactoryMethodChainSetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, "Alice");
        assertEquals("Alice", accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    @Test
    void testLambdaStrategyForInt() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "age");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.LAMBDA_META_FACTORY, descriptor);

        assertInstanceOf(PropertyGetters.LambdaMetaFactoryMethodPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.LambdaMetaFactoryMethodChainSetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, 1111);
        assertEquals(1111, accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    @Test
    void testVarHandleStrategy() throws Exception {
        final BeanUtils.BasicPropertyDescriptor descriptor = descFor(TestEntity1.class, "id");
        final PropertyAccessor accessor = PropertyAccessorFactory.createPropertyAccessor(PropertyAccessStrategy.VAR_HANDLE, descriptor);

        assertInstanceOf(PropertyGetters.VarHandleFieldPropertyGetter.class, accessor.getter());
        assertInstanceOf(PropertySetters.VarHandleFieldPropertySetter.class, accessor.setter());

        final TestEntity1 user = new TestEntity1();
        accessor.setter().setProperty(beanPropertyMetadata, user, 123L);
        assertEquals(123L, accessor.getter().getProperty(beanPropertyMetadata, user));
    }

    // endregion

}

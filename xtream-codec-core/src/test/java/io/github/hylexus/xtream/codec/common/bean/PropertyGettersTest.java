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

import io.github.hylexus.xtream.codec.common.bean.impl.BasicBeanPropertyMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PropertyGettersTest {

    BasicBeanPropertyMetadata beanPropertyMetadata;
    PersonChainStyle bean;

    @BeforeEach
    void setUp() {
        beanPropertyMetadata = mock(BasicBeanPropertyMetadata.class);
        bean = new PersonChainStyle("张三", 1000);
    }

    @Test
    void forFieldViaReflection() throws Exception {
        final Field field = PersonChainStyle.class.getDeclaredField("name");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forFieldViaReflection(field);
        assertEquals(bean.getName(), getter.getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaReflection() throws Exception {
        final Method method = PersonChainStyle.class.getMethod("getName");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forMethodViaReflection(method);
        assertEquals(bean.getName(), getter.getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaMethodHandle() throws Exception {
        Method method = PersonChainStyle.class.getMethod("getAge");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forMethodViaMethodHandle(method);
        assertEquals(bean.getAge(), getter.getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forFieldViaMethodHandle() throws Exception {
        Field field = PersonChainStyle.class.getDeclaredField("age");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forFieldViaMethodHandle(field);
        assertEquals(bean.getAge(), getter.getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaLambdaMetaFactory() throws Exception {
        final Method method = PersonChainStyle.class.getMethod("getName");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forMethodViaLambdaMetaFactory(method);
        assertEquals(bean.getName(), getter.getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forFieldViaVarHandle() throws Exception {
        final Field field = PersonChainStyle.class.getDeclaredField("age");
        final BeanPropertyMetadata.PropertyGetter getter = PropertyGetters.forFieldViaVarHandle(field);
        assertEquals(bean.getAge(), getter.getProperty(beanPropertyMetadata, bean));
    }
}

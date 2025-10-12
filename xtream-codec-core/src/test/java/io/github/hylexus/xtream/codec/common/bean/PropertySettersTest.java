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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PropertySettersTest {

    BasicBeanPropertyMetadata beanPropertyMetadata;
    PersonChainStyle beanChainStyle;
    PersonNonChainStyle beanNonChainStyle;

    @BeforeEach
    void setUp() {
        beanPropertyMetadata = mock(BasicBeanPropertyMetadata.class);
        beanChainStyle = new PersonChainStyle(null, 1000);
        beanNonChainStyle = new PersonNonChainStyle(null, 1000);
    }

    @Test
    void forFieldViaReflection() throws Exception {
        var setterChainStyle = PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("name"));
        final String name = "张三";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("name"));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

    @Test
    void forMethodViaReflection() throws Exception {
        var setterChainStyle = PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setName", String.class));
        final String name = "李四";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setName", String.class));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

    @Test
    void forMethodViaMethodHandle() throws Exception {
        var setterChainStyle = PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setName", String.class));
        final String name = "王五";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setName", String.class));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

    @Test
    void forFieldViaMethodHandle() throws Exception {
        var setterChainStyle = PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("name"));
        final String name = "赵六";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("name"));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

    @Test
    void forMethodViaLambdaMetaFactory() throws Exception {
        var setterChainStyle = PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setName", String.class));
        final String name = "孙七";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setName", String.class));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

    @Test
    void forFieldViaVarHandle() throws Exception {
        var setterChainStyle = PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("name"));
        final String name = "周八";
        setterChainStyle.setProperty(beanPropertyMetadata, beanChainStyle, name);
        assertEquals(name, beanChainStyle.getName());

        var setterNonChainStyle = PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("name"));
        setterNonChainStyle.setProperty(beanPropertyMetadata, beanNonChainStyle, name);
        assertEquals(name, beanNonChainStyle.getName());
    }

}

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

class PropertyGettersTest {

    BasicBeanPropertyMetadata beanPropertyMetadata;
    PersonChainStyle bean;

    @BeforeEach
    void setUp() {
        beanPropertyMetadata = mock(BasicBeanPropertyMetadata.class);
        bean = new PersonChainStyle();
        bean.setStringField("张三");
        bean.setByteField((byte) 1);
        bean.setByteObjField((byte) 1);
        bean.setShortField((short) 1);
        bean.setShortObjField((short) 1);
        bean.setIntField(1);
        bean.setIntObjField(1);
        bean.setLongField(1L);
        bean.setLongObjField(1L);
        bean.setFloatField(1.0f);
        bean.setFloatObjField(1.0f);
        bean.setDoubleField(1.0d);
        bean.setDoubleObjField(1.0d);
    }

    @Test
    void forFieldViaReflection() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("stringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("byteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("byteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("shortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("shortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("intField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("intObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("longField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("longObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("floatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("floatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("doubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("doubleObjField")).getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaReflection() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getStringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getByteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getByteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getShortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getShortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getIntField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getIntObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getLongField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getLongObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getFloatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getFloatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getDoubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forMethodViaReflection(PersonChainStyle.class.getMethod("getDoubleObjField")).getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaMethodHandle() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getStringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getByteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getByteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getShortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getShortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getIntField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getIntObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getLongField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getLongObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getFloatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getFloatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getDoubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("getDoubleObjField")).getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forFieldViaMethodHandle() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("stringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("byteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("byteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("shortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("shortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("intField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("intObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("longField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("longObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("floatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("floatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("doubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("doubleObjField")).getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forMethodViaLambdaMetaFactory() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getStringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getByteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getByteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getShortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getShortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getIntField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getIntObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getLongField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getLongObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getFloatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getFloatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getDoubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("getDoubleObjField")).getProperty(beanPropertyMetadata, bean));
    }

    @Test
    void forFieldViaVarHandle() throws Exception {
        assertEquals(bean.getStringField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("stringField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("byteField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getByteObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("byteObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("shortField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getShortObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("shortObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("intField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getIntObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("intObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("longField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getLongObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("longObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("floatField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getFloatObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("floatObjField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("doubleField")).getProperty(beanPropertyMetadata, bean));
        assertEquals(bean.getDoubleObjField(), PropertyGetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("doubleObjField")).getProperty(beanPropertyMetadata, bean));
    }
}

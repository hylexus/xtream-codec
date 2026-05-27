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

import io.github.hylexus.xtream.codec.common.bean.impl.BasicBeanPropertyMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PropertySettersTest {

    BasicBeanPropertyMetadata beanPropertyMetadata;
    PersonChainStyle beanChainStyle;
    PersonNonChainStyle beanNonChainStyle;

    @BeforeEach
    void setUp() {
        beanPropertyMetadata = mock(BasicBeanPropertyMetadata.class);
        beanChainStyle = new PersonChainStyle();
        beanNonChainStyle = new PersonNonChainStyle();
    }

    @Test
    void forFieldViaReflection() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("stringField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("byteField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("byteObjField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("shortField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("shortObjField")));
        doTestChainStyle(1111, PersonChainStyle::getIntField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("intField")));
        doTestChainStyle(1111, PersonChainStyle::getIntObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("intObjField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("longField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("longObjField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("floatField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("floatObjField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("doubleField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forFieldViaReflection(PersonChainStyle.class.getDeclaredField("doubleObjField")));

        // non-chain style
        doTestNonChainStyle("java1", PersonNonChainStyle::getStringField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("stringField")));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("byteField")));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("byteObjField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("shortField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("shortObjField")));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("intField")));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("intObjField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("longField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("longObjField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("floatField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("floatObjField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("doubleField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forFieldViaReflection(PersonNonChainStyle.class.getDeclaredField("doubleObjField")));
    }

    @Test
    void forMethodViaReflection() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setStringField", String.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setShortField", short.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setIntField", int.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setLongField", long.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forMethodViaReflection(PersonChainStyle.class.getMethod("setDoubleObjField", Double.class)));

        // non-chain style
        doTestNonChainStyle("java1", PersonNonChainStyle::getStringField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setStringField", String.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setShortField", short.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setIntField", int.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setLongField", long.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forMethodViaReflection(PersonNonChainStyle.class.getMethod("setDoubleObjField", Double.class)));
    }

    @Test
    void forMethodViaMethodHandle() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setStringField", String.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setShortField", short.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setIntField", int.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setLongField", long.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forMethodViaMethodHandle(PersonChainStyle.class.getMethod("setDoubleObjField", Double.class)));
        // non-chain style
        doTestNonChainStyle("java1", PersonNonChainStyle::getStringField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setStringField", String.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setShortField", short.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setIntField", int.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setLongField", long.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forMethodViaMethodHandle(PersonNonChainStyle.class.getMethod("setDoubleObjField", Double.class)));
    }

    @Test
    void forFieldViaMethodHandle() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("stringField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("byteField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("byteObjField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("shortField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("shortObjField")));
        doTestChainStyle(1111, PersonChainStyle::getIntField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("intField")));
        doTestChainStyle(1111, PersonChainStyle::getIntObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("intObjField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("longField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("longObjField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("floatField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("floatObjField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("doubleField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forFieldViaMethodHandle(PersonChainStyle.class.getDeclaredField("doubleObjField")));
        // non-chain style
        doTestNonChainStyle("java1", PersonNonChainStyle::getStringField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("stringField")));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("byteField")));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("byteObjField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("shortField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("shortObjField")));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("intField")));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("intObjField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("longField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("longObjField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("floatField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("floatObjField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("doubleField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forFieldViaMethodHandle(PersonNonChainStyle.class.getDeclaredField("doubleObjField")));
    }

    @Test
    void forMethodViaLambdaMetaFactory() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setStringField", String.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setShortField", short.class)));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setIntField", int.class)));
        doTestChainStyle(1111, PersonChainStyle::getIntObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setLongField", long.class)));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonChainStyle.class.getMethod("setDoubleObjField", Double.class)));

        // non-chain style
        doTestNonChainStyle("java1", PersonNonChainStyle::getStringField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setStringField", String.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setByteField", byte.class)));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setByteObjField", Byte.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setShortField", short.class)));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setShortObjField", Short.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setIntField", int.class)));
        doTestNonChainStyle(2222, PersonNonChainStyle::getIntObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setIntObjField", Integer.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setLongField", long.class)));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setLongObjField", Long.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setFloatField", float.class)));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setFloatObjField", Float.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setDoubleField", double.class)));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forMethodViaLambdaMetaFactory(PersonNonChainStyle.class.getMethod("setDoubleObjField", Double.class)));
    }

    @Test
    void forFieldViaVarHandle() throws Exception {
        // chain style
        doTestChainStyle("java", PersonChainStyle::getStringField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("stringField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("byteField")));
        doTestChainStyle((byte) 1, PersonChainStyle::getByteObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("byteObjField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("shortField")));
        doTestChainStyle((short) 1, PersonChainStyle::getShortObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("shortObjField")));
        doTestChainStyle(222, PersonChainStyle::getIntField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("intField")));
        doTestChainStyle(222, PersonChainStyle::getIntObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("intObjField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("longField")));
        doTestChainStyle((long) 1, PersonChainStyle::getLongObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("longObjField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("floatField")));
        doTestChainStyle((float) 1, PersonChainStyle::getFloatObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("floatObjField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("doubleField")));
        doTestChainStyle((double) 1, PersonChainStyle::getDoubleObjField, PropertySetters.forFieldViaVarHandle(PersonChainStyle.class.getDeclaredField("doubleObjField")));
        // non-chain style
        doTestNonChainStyle("java", PersonNonChainStyle::getStringField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("stringField")));
        doTestNonChainStyle((byte) 2, PersonNonChainStyle::getByteField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("byteField")));
        doTestNonChainStyle((byte) 21, PersonNonChainStyle::getByteObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("byteObjField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("shortField")));
        doTestNonChainStyle((short) 2, PersonNonChainStyle::getShortObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("shortObjField")));
        doTestNonChainStyle(222, PersonNonChainStyle::getIntField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("intField")));
        doTestNonChainStyle(222, PersonNonChainStyle::getIntObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("intObjField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("longField")));
        doTestNonChainStyle((long) 2, PersonNonChainStyle::getLongObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("longObjField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("floatField")));
        doTestNonChainStyle((float) 2, PersonNonChainStyle::getFloatObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("floatObjField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("doubleField")));
        doTestNonChainStyle((double) 2, PersonNonChainStyle::getDoubleObjField, PropertySetters.forFieldViaVarHandle(PersonNonChainStyle.class.getDeclaredField("doubleObjField")));
    }

    <T> void doTestNonChainStyle(T value, Function<PersonNonChainStyle, T> getter, BeanPropertyMetadata.PropertySetter setter) {
        setter.setProperty(beanPropertyMetadata, beanNonChainStyle, value);
        final Object property = getter.apply(beanNonChainStyle);
        assertEquals(value, property);
    }

    <T> void doTestChainStyle(T value, Function<PersonChainStyle, T> getter, BeanPropertyMetadata.PropertySetter setter) {
        setter.setProperty(beanPropertyMetadata, beanChainStyle, value);
        final Object property = getter.apply(beanChainStyle);
        assertEquals(value, property);
    }

}

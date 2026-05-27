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

import io.github.hylexus.xtream.codec.core.annotation.XtreamRequestBody;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XtreamMethodParameterTest {

    @XtreamResponseBody
    static class ClassA {
    }

    static class MethodParameterTestClass {

        @XtreamResponseBody
        public List<String> method1(@XtreamRequestBody String a, String b, List<String> list) {
            return null;
        }

        @XtreamResponseBody
        public List<ClassA> method2(@XtreamRequestBody String a, String b, List<String> list) {
            return null;
        }

        @XtreamResponseBody
        public ClassA method3(@XtreamRequestBody String a, String b, List<String> list) {
            return null;
        }
    }

    @Test
    void test11() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method1", String.class, String.class, List.class);

        final XtreamMethodParameter parameter0 = new XtreamMethodParameter(0, method1);
        // 方法本身的注解
        assertTrue(parameter0.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter0.getParameterAnnotation(XtreamRequestBody.class).isPresent());
        assertFalse(parameter0.hasGenericType());
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter0.getTypeAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test12() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method1", String.class, String.class, List.class);

        final XtreamMethodParameter parameter1 = new XtreamMethodParameter(1, method1);
        // 方法本身的注解
        assertTrue(parameter1.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter1.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter1.getParameterAnnotation(XtreamRequestBody.class).isEmpty());
        assertFalse(parameter1.hasGenericType());
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter1.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter1.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test13() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method1", String.class, String.class, List.class);

        final XtreamMethodParameter parameter2 = new XtreamMethodParameter(2, method1);
        // 方法本身的注解
        assertTrue(parameter2.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter2.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter2.getParameterAnnotation(XtreamRequestBody.class).isEmpty());
        assertTrue(parameter2.hasGenericType());
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter2.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter2.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test14() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method1", String.class, String.class, List.class);

        final XtreamMethodParameter returnType = new XtreamMethodParameter(-1, method1);
        // 方法本身的注解
        assertTrue(returnType.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(returnType.getMethodAnnotation(XtreamResponseBody.class));

        // 方法返回类型上的注解
        assertThrows(IllegalStateException.class, () -> returnType.getParameterAnnotation(XtreamRequestBody.class));
        assertTrue(returnType.hasGenericType());
        assertNull(returnType.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(returnType.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(returnType.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(returnType.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test21() throws Exception {
        final Method method2 = MethodParameterTestClass.class.getMethod("method2", String.class, String.class, List.class);

        final XtreamMethodParameter parameter0 = new XtreamMethodParameter(0, method2);
        // 方法本身的注解
        assertTrue(parameter0.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter0.getParameterAnnotation(XtreamRequestBody.class).isPresent());
        assertFalse(parameter0.hasGenericType());
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter0.getTypeAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test22() throws Exception {
        final Method method2 = MethodParameterTestClass.class.getMethod("method2", String.class, String.class, List.class);

        final XtreamMethodParameter parameter1 = new XtreamMethodParameter(1, method2);
        // 方法本身的注解
        assertTrue(parameter1.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter1.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter1.getParameterAnnotation(XtreamRequestBody.class).isEmpty());
        assertFalse(parameter1.hasGenericType());
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter1.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter1.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test23() throws Exception {
        final Method method2 = MethodParameterTestClass.class.getMethod("method2", String.class, String.class, List.class);

        final XtreamMethodParameter parameter2 = new XtreamMethodParameter(2, method2);
        // 方法本身的注解
        assertTrue(parameter2.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter2.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter2.getParameterAnnotation(XtreamRequestBody.class).isEmpty());
        assertTrue(parameter2.hasGenericType());
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter2.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter2.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test24() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method2", String.class, String.class, List.class);

        final XtreamMethodParameter returnType = new XtreamMethodParameter(-1, method1);
        // 方法本身的注解
        assertTrue(returnType.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(returnType.getMethodAnnotation(XtreamResponseBody.class));

        // 方法返回类型上的注解
        assertThrows(IllegalStateException.class, () -> returnType.getParameterAnnotation(XtreamRequestBody.class));
        assertTrue(returnType.hasGenericType());
        assertNotNull(returnType.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(returnType.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(returnType.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(returnType.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test31() throws Exception {
        final Method method3 = MethodParameterTestClass.class.getMethod("method3", String.class, String.class, List.class);

        final XtreamMethodParameter parameter0 = new XtreamMethodParameter(0, method3);
        // 方法本身的注解
        assertTrue(parameter0.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertTrue(parameter0.getParameterAnnotation(XtreamRequestBody.class).isPresent());
        assertFalse(parameter0.hasGenericType());
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter0.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter0.getTypeAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter0.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test32() throws Exception {
        final Method method3 = MethodParameterTestClass.class.getMethod("method3", String.class, String.class, List.class);

        final XtreamMethodParameter parameter1 = new XtreamMethodParameter(1, method3);
        // 方法本身的注解
        assertTrue(parameter1.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter1.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertFalse(parameter1.getParameterAnnotation(XtreamRequestBody.class).isPresent());
        assertFalse(parameter1.hasGenericType());
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter1.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter1.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter1.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test33() throws Exception {
        final Method method3 = MethodParameterTestClass.class.getMethod("method3", String.class, String.class, List.class);

        final XtreamMethodParameter parameter2 = new XtreamMethodParameter(2, method3);
        // 方法本身的注解
        assertTrue(parameter2.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(parameter2.getMethodAnnotation(XtreamResponseBody.class));

        // 方法参数上的注解
        assertFalse(parameter2.getParameterAnnotation(XtreamRequestBody.class).isPresent());
        assertTrue(parameter2.hasGenericType());
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter2.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNull(parameter2.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(parameter2.getTypeAnnotation(XtreamRequestBody.class));
    }

    @Test
    void test34() throws Exception {
        final Method method1 = MethodParameterTestClass.class.getMethod("method3", String.class, String.class, List.class);

        final XtreamMethodParameter returnType = new XtreamMethodParameter(-1, method1);
        // 方法本身的注解
        assertTrue(returnType.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(returnType.getMethodAnnotation(XtreamResponseBody.class));

        // 方法返回类型上的注解
        assertThrows(IllegalStateException.class, () -> returnType.getParameterAnnotation(XtreamRequestBody.class));
        assertFalse(returnType.hasGenericType());
        assertNull(returnType.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(returnType.getGenericTypeAnnotation(0, XtreamRequestBody.class));

        assertNotNull(returnType.getTypeAnnotation(XtreamResponseBody.class));
        assertNull(returnType.getTypeAnnotation(XtreamRequestBody.class));
    }

}

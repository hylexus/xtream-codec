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
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 方法参数描述对象的缓存键、索引及组合注解边界测试。
 *
 * @author Codex (AI)
 */
class XtreamMethodParameterBoundaryTest {

    @ParameterizedTest
    @MethodSource("validSlots")
    void exposesMethodDeclaringClassRawTypeAndGenericArguments(int index, Class<?> rawType, List<Type> generics) throws Exception {
        final Method method = handlerMethod();
        final XtreamMethodParameter parameter = new XtreamMethodParameter(index, method);
        assertSame(method, parameter.getMethod());
        assertSame(Handler.class, parameter.getContainerClass());
        assertSame(rawType, parameter.getParameterType());
        assertEquals(generics, parameter.getGenericType());
        assertEquals(!generics.isEmpty(), parameter.hasGenericType());
    }

    static Stream<Arguments> validSlots() {
        return Stream.of(
                Arguments.of(-1, Map.class, List.of(Marked.class, String.class)),
                Arguments.of(0, ByteBuf.class, List.of()),
                Arguments.of(1, List.class, List.of(Marked.class)));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -2, 2, Integer.MAX_VALUE})
    void rejectsIndicesOutsideReturnAndParameterSlots(int index) throws Exception {
        final Method method = handlerMethod();
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new XtreamMethodParameter(index, method));
        assertEquals("Invalid index : " + index, error.getMessage());
    }

    @Test
    void parameterlessVoidMethodOnlyAcceptsItsReturnSlot() throws Exception {
        final Method method = Handler.class.getMethod("noArguments");
        final XtreamMethodParameter result = new XtreamMethodParameter(-1, method);
        assertSame(void.class, result.getParameterType());
        assertFalse(result.hasGenericType());
        assertTrue(result.getGenericType().isEmpty());
        assertNull(result.getTypeAnnotation(XtreamResponseBody.class));
        assertFalse(result.hasMethodAnnotation(XtreamResponseBody.class));
        assertNull(result.getMethodAnnotation(XtreamResponseBody.class));
        assertThrows(IllegalArgumentException.class, () -> new XtreamMethodParameter(0, method));
        assertThrows(IllegalStateException.class, () -> result.getParameterAnnotation(XtreamRequestBody.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void independentlyReflectedEquivalentKeysReuseMapAndSetEntries(int index) throws Exception {
        final XtreamMethodParameter first = new XtreamMethodParameter(index, handlerMethod());
        final XtreamMethodParameter second = new XtreamMethodParameter(index, handlerMethod());
        final XtreamMethodParameter third = new XtreamMethodParameter(index, handlerMethod());
        assertTrue(first.equals(first));
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        final Map<XtreamMethodParameter, String> cache = new HashMap<>();
        cache.put(first, "first");
        assertEquals("first", cache.get(second));
        assertEquals("first", cache.put(third, "replacement"));
        assertEquals(1, cache.size());
        assertEquals("replacement", cache.get(first));
        assertEquals(1, new HashSet<>(List.of(first, second, third)).size());
    }

    @Test
    void differentSlotsOverloadsAndDeclaringClassesRemainSeparateCacheKeys() throws Exception {
        final Method method = handlerMethod();
        final Method overload = Handler.class.getMethod("handle", ByteBuf.class, String.class);
        final Method otherClass = OtherHandler.class.getMethod("handle", ByteBuf.class, List.class);
        final List<XtreamMethodParameter> keys = List.of(
                new XtreamMethodParameter(-1, method), new XtreamMethodParameter(0, method),
                new XtreamMethodParameter(1, method), new XtreamMethodParameter(0, overload),
                new XtreamMethodParameter(0, otherClass));
        final Map<XtreamMethodParameter, Integer> cache = new HashMap<>();
        for (int index = 0; index < keys.size(); index++) {
            final XtreamMethodParameter key = keys.get(index);
            cache.put(key, index);
            assertFalse(key.equals(null));
            assertFalse(key.equals(key.getMethod()));
            for (int previous = 0; previous < index; previous++) {
                assertNotEquals(keys.get(previous), key);
                assertNotEquals(key, keys.get(previous));
            }
        }
        assertEquals(5, cache.size());
        assertEquals(5, new HashSet<>(keys).size());
        for (int index = 0; index < keys.size(); index++) {
            assertEquals(index, cache.get(keys.get(index)));
        }
    }

    @Test
    void inheritedMethodUsesItsOriginalDeclaringClassAndCacheKey() throws Exception {
        final Method inherited = ChildHandler.class.getMethod("handle", ByteBuf.class, List.class);
        final XtreamMethodParameter parameter = new XtreamMethodParameter(0, inherited);
        assertSame(Handler.class, parameter.getContainerClass());
        assertEquals(new XtreamMethodParameter(0, handlerMethod()), parameter);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1})
    void genericAnnotationQueriesReturnNullPastExistingArguments(int index) throws Exception {
        final XtreamMethodParameter parameter = new XtreamMethodParameter(index, handlerMethod());
        assertNotNull(parameter.getGenericTypeAnnotation(0, XtreamResponseBody.class));
        assertNull(parameter.getGenericTypeAnnotation(0, XtreamRequestBody.class));
        final int count = parameter.getGenericType().size();
        assertNull(parameter.getGenericTypeAnnotation(count, XtreamResponseBody.class));
        assertNull(parameter.getGenericTypeAnnotation(count + 1, XtreamResponseBody.class));
        assertNull(parameter.getGenericTypeAnnotation(Integer.MAX_VALUE, XtreamResponseBody.class));
        if (index == -1) {
            assertNull(parameter.getGenericTypeAnnotation(1, XtreamResponseBody.class));
        }
    }

    @Test
    void composedRequestAnnotationPreservesAliasedAttributesAndMethodScope() throws Exception {
        final XtreamMethodParameter body = new XtreamMethodParameter(0, handlerMethod());
        final XtreamRequestBody merged = body.getParameterAnnotation(XtreamRequestBody.class).orElseThrow();
        assertFalse(merged.bufferAsSlice());
        assertEquals(merged, body.getTypeAnnotation(XtreamRequestBody.class));
        assertTrue(body.hasMethodAnnotation(XtreamResponseBody.class));
        assertNotNull(body.getMethodAnnotation(XtreamResponseBody.class));
        assertNull(body.getTypeAnnotation(XtreamResponseBody.class));
        final XtreamMethodParameter items = new XtreamMethodParameter(1, handlerMethod());
        assertTrue(items.getParameterAnnotation(XtreamRequestBody.class).isEmpty());
        assertNull(items.getTypeAnnotation(XtreamRequestBody.class));
        final XtreamMethodParameter result = new XtreamMethodParameter(-1, handlerMethod());
        assertNotNull(result.getMethodAnnotation(XtreamResponseBody.class));
        assertNull(result.getTypeAnnotation(XtreamResponseBody.class));
    }

    private static Method handlerMethod() throws NoSuchMethodException {
        return Handler.class.getMethod("handle", ByteBuf.class, List.class);
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @XtreamRequestBody
    @interface BodyAlias {
        @AliasFor(annotation = XtreamRequestBody.class, attribute = "bufferAsSlice")
        boolean slice() default true;
    }

    @XtreamResponseBody
    static class Marked {
    }

    static class Handler {
        @XtreamResponseBody
        public Map<Marked, String> handle(@BodyAlias(slice = false) ByteBuf body, List<Marked> items) {
            return Map.of();
        }

        public Map<Marked, String> handle(ByteBuf body, String items) {
            return Map.of();
        }

        public void noArguments() {
        }
    }

    static class ChildHandler extends Handler {
    }

    static class OtherHandler {
        public Map<Marked, String> handle(ByteBuf body, List<Marked> items) {
            return Map.of();
        }
    }
}

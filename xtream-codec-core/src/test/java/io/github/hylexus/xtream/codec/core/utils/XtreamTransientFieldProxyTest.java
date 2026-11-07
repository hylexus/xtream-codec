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

package io.github.hylexus.xtream.codec.core.utils;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对照真实注解验证瞬态字段代理的属性、相等性及哈希集合使用约定。
 *
 * @author Codex (AI)
 */
class XtreamTransientFieldProxyTest {

    @ParameterizedTest
    @ValueSource(strings = {"number", "text", "items"})
    void exposesEffectiveAnnotationPropertiesAndTargetType(String fieldName) throws Exception {
        final Field field = Fields.class.getDeclaredField(fieldName);
        final XtreamField proxy = XtreamFieldUtils.createTransientFieldProxy(field);
        final XtreamField real = annotation("transientField");
        assertSame(XtreamField.class, proxy.annotationType());
        assertSame(XtreamField.CodecStrategy.TRANSIENT, proxy.codecStrategy());
        for (final Method attribute : XtreamField.class.getDeclaredMethods()) {
            assertTrue(Objects.deepEquals(attribute.invoke(real), attribute.invoke(proxy)), attribute::getName);
        }
        final XtreamFieldUtils.XtreamTransientFieldProxy marker = (XtreamFieldUtils.XtreamTransientFieldProxy) proxy;
        assertSame(field.getType(), marker.targetType());
        assertEquals(field.getType().isPrimitive() ? 0 : null, marker.defaultValueForNulls());
    }

    @Test
    void equivalentProxiesAreReflexiveSymmetricAndTransitiveAcrossTargetTypes() throws Exception {
        final XtreamField first = proxy("number");
        final XtreamField second = proxy("number");
        final XtreamField third = proxy("text");
        assertAll(
                () -> assertTrue(first.equals(first)),
                () -> assertTrue(first.equals(second)),
                () -> assertTrue(second.equals(first)),
                () -> assertTrue(second.equals(third)),
                () -> assertTrue(first.equals(third)),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertEquals(second.hashCode(), third.hashCode()));
    }

    @Test
    void equalsRealTransientAnnotationInBothDirections() throws Exception {
        final XtreamField proxy = proxy("number");
        final XtreamField real = annotation("transientField");
        assertNotSame(real.version(), proxy.version());
        assertArrayEquals(real.version(), proxy.version());
        assertAll(() -> assertTrue(proxy.equals(real)), () -> assertTrue(real.equals(proxy)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"defaultField", "versionedField", "describedField"})
    void rejectsAnnotationsWithDifferentEffectiveProperties(String fieldName) throws Exception {
        final XtreamField proxy = proxy("number");
        final XtreamField other = annotation(fieldName);
        assertAll(() -> assertFalse(proxy.equals(other)), () -> assertFalse(other.equals(proxy)));
    }

    @Test
    void rejectsNullAndObjectsOfOtherTypes() throws Exception {
        final XtreamField proxy = proxy("number");
        assertFalse(proxy.equals(null));
        assertFalse(proxy.equals("not an annotation"));
        assertFalse(proxy.equals(Fields.class));
    }

    @Test
    void hashCodeMatchesRealAnnotationIncludingArrayContents() throws Exception {
        final XtreamField proxy = proxy("number");
        final XtreamField real = annotation("transientField");
        assertEquals(real.hashCode(), proxy.hashCode());
        assertEquals(proxy.hashCode(), proxy.hashCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void equivalentAnnotationsFindAndReplaceTheSameMapEntry(boolean realFirst) throws Exception {
        final XtreamField first = realFirst ? annotation("transientField") : proxy("number");
        final XtreamField second = realFirst ? proxy("text") : annotation("transientField");
        final Map<XtreamField, String> cache = new HashMap<>();
        cache.put(first, "first");
        assertEquals("first", cache.get(second));
        assertEquals("first", cache.put(second, "second"));
        assertEquals(1, cache.size());
        assertEquals("second", cache.get(first));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void equivalentAnnotationsOccupyOneSetEntry(boolean realFirst) throws Exception {
        final XtreamField real = annotation("transientField");
        final XtreamField first = proxy("number");
        final XtreamField second = proxy("text");
        final Set<XtreamField> annotations = new HashSet<>();
        if (realFirst) {
            annotations.add(real);
        }
        annotations.add(first);
        annotations.add(second);
        annotations.add(real);
        assertEquals(1, annotations.size());
        assertTrue(annotations.contains(proxy("items")));
    }

    @Test
    void descriptionReflectsTheEffectiveCodecStrategy() throws Exception {
        final XtreamField proxy = proxy("number");
        assertTrue(proxy.toString().startsWith("@" + XtreamField.class.getName() + "("));
        assertTrue(proxy.toString().contains("codecStrategy=TRANSIENT"));
    }

    private static XtreamField proxy(String fieldName) throws Exception {
        return XtreamFieldUtils.createTransientFieldProxy(Fields.class.getDeclaredField(fieldName));
    }

    private static XtreamField annotation(String fieldName) throws Exception {
        return Objects.requireNonNull(Fields.class.getDeclaredField(fieldName).getAnnotation(XtreamField.class));
    }

    record Fields(
            int number, String text, List<String> items,
            @XtreamField(codecStrategy = XtreamField.CodecStrategy.TRANSIENT) int transientField,
            @XtreamField int defaultField,
            @XtreamField(codecStrategy = XtreamField.CodecStrategy.TRANSIENT, version = {1, 2}) int versionedField,
            @XtreamField(codecStrategy = XtreamField.CodecStrategy.TRANSIENT, desc = "其他字段") int describedField) {
    }
}

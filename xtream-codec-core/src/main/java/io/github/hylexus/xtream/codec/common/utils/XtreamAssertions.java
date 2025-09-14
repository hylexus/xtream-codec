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

package io.github.hylexus.xtream.codec.common.utils;

import java.util.function.Predicate;

/**
 * @author hylexus
 */
public abstract class XtreamAssertions {

    private XtreamAssertions() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message);
        }
    }

    public static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, "Expected: " + expected + ", but was: " + actual);
    }

    public static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    public static void assertNotSame(Object expected, Object actual) {
        assertNotSame(expected, actual, "Expected: not same but was: <" + actual + ">");
    }

    public static void assertNotSame(Object expected, Object actual, String message) {
        if (expected == actual) {
            throw new AssertionError(message);
        }
    }

    public static void assertThat(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T> T assertThat(T t, Predicate<T> predicate, String msg) {
        if (!predicate.test(t)) {
            throw new IllegalArgumentException(msg);
        }
        return t;
    }

    public static String assertNotBlank(String string, String error) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(error);
        }
        return string;
    }

}

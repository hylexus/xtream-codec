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

package io.github.hylexus.xtream.codec.base.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XtreamInternalStringUtilsTest {

    @Test
    void testEmptyString() {
        assertEquals("", XtreamInternalStringUtils.uncapitalizeAsProperty(""));
    }

    @Test
    void testSingleUpperChar() {
        assertEquals("a", XtreamInternalStringUtils.uncapitalizeAsProperty("A"));
    }

    @Test
    void testSingleLowerChar() {
        assertEquals("a", XtreamInternalStringUtils.uncapitalizeAsProperty("a"));
    }

    @Test
    void testNormalCamelCase() {
        assertEquals("userName", XtreamInternalStringUtils.uncapitalizeAsProperty("UserName"));
    }

    @Test
    void testAlreadyLowerCase() {
        assertEquals("userName", XtreamInternalStringUtils.uncapitalizeAsProperty("userName"));
    }

    @Test
    void testConsecutiveUpperCasePreserved() {
        // When first two chars are both uppercase, the string is returned as-is
        assertEquals("URL", XtreamInternalStringUtils.uncapitalizeAsProperty("URL"));
        assertEquals("URLParser", XtreamInternalStringUtils.uncapitalizeAsProperty("URLParser"));
    }

    @Test
    void testFirstUpperSecondLower() {
        assertEquals("name", XtreamInternalStringUtils.uncapitalizeAsProperty("Name"));
    }
}

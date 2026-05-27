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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.wrapper.U8Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XtreamCacheableClassPredicateTest {

    private XtreamCacheableClassPredicate defaultPredicate;

    @BeforeEach
    public void setUp() {
        defaultPredicate = new XtreamCacheableClassPredicate.Default();
    }

    static class SomeStaticClass {
    }

    @SuppressWarnings("ClassCanBeStatic")
    class SomeNonStaticClass {
    }

    @Test
    void test() {
        assertFalse(defaultPredicate.test(ByteArrayContainer.ofByte(1).getClass()));
        assertFalse(defaultPredicate.test(ByteArrayContainer.ofDword(1).getClass()));

        assertTrue(defaultPredicate.test(U8Wrapper.class));

        assertFalse(defaultPredicate.test(SomeNonStaticClass.class));
        assertTrue(defaultPredicate.test(SomeStaticClass.class));

        assertFalse(defaultPredicate.test(new Runnable() {
            @Override
            public void run() {

            }
        }.getClass()));

        assertFalse(defaultPredicate.test(((Runnable) () -> {
        }).getClass()));

        final Function<Object, Object> function = o -> o;
        assertFalse(defaultPredicate.test(function.getClass()));

        final Function<Object, Object> function1 = Function.identity();
        assertFalse(defaultPredicate.test(function1.getClass()));
    }
}

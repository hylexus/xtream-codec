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

package io.github.hylexus.xtream.codec.base.web.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XtreamWebProxyBackendTest {

    @Test
    void testConstructorAndGetter() {
        XtreamWebProxyBackend backend = new XtreamWebProxyBackend("http://localhost:8080");
        assertEquals("http://localhost:8080", backend.getBaseUrl());
    }

    @Test
    void testFluentSetter() {
        XtreamWebProxyBackend backend = new XtreamWebProxyBackend("http://localhost:8080");
        XtreamWebProxyBackend returned = backend.setBaseUrl("http://localhost:9090");
        assertSame(backend, returned);
        assertEquals("http://localhost:9090", backend.getBaseUrl());
    }

    @Test
    void testToString() {
        XtreamWebProxyBackend backend = new XtreamWebProxyBackend("http://example.com");
        String str = backend.toString();
        assertTrue(str.contains("http://example.com"));
        assertTrue(str.contains("XtreamWebProxyBackend"));
    }
}

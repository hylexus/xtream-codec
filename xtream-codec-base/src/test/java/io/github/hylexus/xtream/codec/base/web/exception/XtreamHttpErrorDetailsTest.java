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

package io.github.hylexus.xtream.codec.base.web.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XtreamHttpErrorDetailsTest {

    @Test
    void testProxyErrorDetails() {
        XtreamHttpErrorDetails.ProxyErrorDetails details =
                XtreamHttpErrorDetails.ProxyErrorDetails.of("/api/test", "http://backend:8080", "connection refused");
        assertEquals("/api/test", details.upstream());
        assertEquals("http://backend:8080", details.downstream());
        assertEquals("connection refused", details.message());
        assertEquals("ProxyError", details.getErrorType());
    }

    @Test
    void testParameterErrorDetails() {
        XtreamHttpErrorDetails.ParameterErrorDetails details =
                new XtreamHttpErrorDetails.ParameterErrorDetails("field is required");
        assertEquals("field is required", details.message());
        assertEquals("ParameterError", details.getErrorType());
    }

    @Test
    void testParameterErrorDetailsOfSingleList() {
        List<XtreamHttpErrorDetails.ParameterErrorDetails> list =
                XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList("name is required");
        assertEquals(1, list.size());
        assertEquals("name is required", list.get(0).message());
        assertEquals("ParameterError", list.get(0).getErrorType());
    }
}

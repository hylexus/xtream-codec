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

package io.github.hylexus.xtream.codec.base.web.domain.values;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XtreamApiErrorCodeTest {

    @Test
    void testServerError() {
        assertEquals(500, XtreamApiErrorCode.SERVER_ERROR.getStatus());
        assertEquals("Internal Server Error", XtreamApiErrorCode.SERVER_ERROR.getTitle());
        assertEquals("000:500", XtreamApiErrorCode.SERVER_ERROR.getErrorCode());
        assertEquals("Server error", XtreamApiErrorCode.SERVER_ERROR.getDetail());
    }

    @Test
    void testBadRequest() {
        assertEquals(400, XtreamApiErrorCode.BAD_REQUEST.getStatus());
        assertEquals("Bad Request", XtreamApiErrorCode.BAD_REQUEST.getTitle());
        assertEquals("000:400", XtreamApiErrorCode.BAD_REQUEST.getErrorCode());
    }

    @Test
    void testNotFound() {
        assertEquals(404, XtreamApiErrorCode.NOT_FOUND.getStatus());
        assertEquals("Not Found", XtreamApiErrorCode.NOT_FOUND.getTitle());
        assertEquals("000:404", XtreamApiErrorCode.NOT_FOUND.getErrorCode());
    }

    @Test
    void testRequestTimeout() {
        assertEquals(408, XtreamApiErrorCode.REQUEST_TIMEOUT.getStatus());
        assertEquals("000:408", XtreamApiErrorCode.REQUEST_TIMEOUT.getErrorCode());
    }

    @Test
    void testGatewayTimeout() {
        assertEquals(504, XtreamApiErrorCode.GATEWAY_TIMEOUT.getStatus());
        assertEquals("Gateway Timeout", XtreamApiErrorCode.GATEWAY_TIMEOUT.getTitle());
        assertEquals("000:504", XtreamApiErrorCode.GATEWAY_TIMEOUT.getErrorCode());
    }

    @Test
    void testProxyError() {
        assertEquals(502, XtreamApiErrorCode.PROXY_ERROR.getStatus());
        assertEquals("Bad Gateway", XtreamApiErrorCode.PROXY_ERROR.getTitle());
        assertEquals("001:502", XtreamApiErrorCode.PROXY_ERROR.getErrorCode());
    }

    @Test
    void testAllEnumValues() {
        assertEquals(6, XtreamApiErrorCode.values().length);
    }
}

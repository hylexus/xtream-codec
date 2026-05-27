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

package io.github.hylexus.xtream.codec.base.web.exception;

import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiErrorCode;
import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiProblemDetails;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XtreamHttpExceptionTest {

    // region factory methods

    @Test
    void testNotFound() {
        XtreamHttpException ex = XtreamHttpException.notFound("resource not found");
        assertInstanceOf(XtreamResourceNotFoundException.class, ex);
        assertEquals(404, ex.getStatus());
        assertEquals("Not Found", ex.getTitle());
        assertEquals("000:404", ex.getErrorCode());
        assertEquals("resource not found", ex.getDetail());
        assertNull(ex.getErrorDetails());
    }

    @Test
    void testTimeout() {
        XtreamHttpException ex = XtreamHttpException.timeout("gateway timeout");
        assertInstanceOf(XtreamHttpGatewayTimeoutException.class, ex);
        assertEquals(504, ex.getStatus());
        assertEquals("Gateway Timeout", ex.getTitle());
        assertEquals("000:504", ex.getErrorCode());
    }

    @Test
    void testBadRequest() {
        XtreamHttpException ex = XtreamHttpException.badRequest("invalid param");
        assertInstanceOf(XtreamBadRequestException.class, ex);
        assertEquals(400, ex.getStatus());
        assertEquals("Bad Request", ex.getTitle());
        assertEquals("000:400", ex.getErrorCode());
    }

    @Test
    void testFactoryWithNullMessage() {
        XtreamHttpException ex = XtreamHttpException.notFound(null);
        assertNull(ex.getDetail());
    }

    // endregion

    // region XtreamBadRequestException

    @Test
    void testBadRequestException() {
        XtreamBadRequestException ex = new XtreamBadRequestException("bad request");
        assertEquals(400, ex.getStatus());
        assertEquals("bad request", ex.getDetail());
        assertNull(ex.getCause());
    }

    // endregion

    // region XtreamResourceNotFoundException

    @Test
    void testResourceNotFoundException() {
        XtreamResourceNotFoundException ex = new XtreamResourceNotFoundException("not found");
        assertEquals(404, ex.getStatus());
        assertEquals("not found", ex.getDetail());
    }

    // endregion

    // region XtreamHttpGatewayTimeoutException

    @Test
    void testGatewayTimeoutException() {
        XtreamHttpGatewayTimeoutException ex = new XtreamHttpGatewayTimeoutException("timeout");
        assertEquals(504, ex.getStatus());
        assertEquals("timeout", ex.getDetail());
    }

    // endregion

    // region XtreamHttpCallException

    @Test
    void testCallException() {
        List<XtreamHttpErrorDetails.ProxyErrorDetails> details = List.of(
                XtreamHttpErrorDetails.ProxyErrorDetails.of("/api/test", "http://backend:8080", "connection refused")
        );
        XtreamHttpCallException ex = new XtreamHttpCallException(HttpStatus.BAD_GATEWAY, "001:502", "proxy failed", details);
        assertEquals(502, ex.getStatus());
        assertEquals("Bad Gateway", ex.getTitle());
        assertEquals("001:502", ex.getErrorCode());
        assertEquals("proxy failed", ex.getDetail());
        assertNotNull(ex.getErrorDetails());
        assertEquals(1, ex.getErrorDetails().size());
    }

    @Test
    void testCallExceptionWithCause() {
        Throwable cause = new RuntimeException("connection refused");
        XtreamHttpCallException ex = new XtreamHttpCallException(cause, HttpStatus.BAD_GATEWAY, "001:502", "proxy failed", List.of());
        assertEquals(cause, ex.getCause());
    }

    // endregion

    // region XtreamHttpTransparentException

    @Test
    void testTransparentException() {
        XtreamHttpTransparentException ex = new XtreamHttpTransparentException(503, "service unavailable");
        assertEquals(503, ex.status());
        assertEquals("service unavailable", ex.error());
    }

    @Test
    void testTransparentExceptionWithObjectError() {
        Object errorBody = List.of("error1", "error2");
        XtreamHttpTransparentException ex = new XtreamHttpTransparentException(500, errorBody);
        assertEquals(500, ex.status());
        assertSame(errorBody, ex.error());
    }

    // endregion

    // region XtreamApiProblemDetails implementation

    @Test
    void testImplementsProblemDetails() {
        XtreamHttpException ex = new XtreamBadRequestException("test");
        assertInstanceOf(XtreamApiProblemDetails.class, ex);
        assertEquals(XtreamApiProblemDetails.BLANK_TYPE, ex.getType());
        assertNull(ex.getInstance());
    }

    // endregion
}

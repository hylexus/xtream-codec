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

import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpErrorDetails;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultXtreamApiProblemDetailsTest {

    // region constructors

    @Test
    void testDefaultConstructor() {
        DefaultXtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails();
        assertEquals(URI.create("about:blank"), details.getType());
        assertEquals(0, details.getStatus());
    }

    @Test
    void testConstructorWithErrorCode() {
        DefaultXtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails(
                XtreamApiErrorCode.BAD_REQUEST, "invalid param", null
        );
        assertEquals(400, details.getStatus());
        assertEquals("Bad Request", details.getTitle());
        assertEquals("000:400", details.getErrorCode());
        assertEquals("invalid param", details.getDetail());
        assertNull(details.getErrorDetails());
    }

    @Test
    void testConstructorWithFullParams() {
        List<XtreamHttpErrorDetails.ParameterErrorDetails> errorDetails =
                XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList("field error");
        DefaultXtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails(
                500, "Internal Server Error", "000:500", "something went wrong", errorDetails
        );
        assertEquals(500, details.getStatus());
        assertEquals("Internal Server Error", details.getTitle());
        assertEquals("000:500", details.getErrorCode());
        assertEquals("something went wrong", details.getDetail());
        assertNotNull(details.getErrorDetails());
        assertEquals(1, details.getErrorDetails().size());
    }

    // endregion

    // region builder pattern

    @Test
    void testBuilder() {
        XtreamApiProblemDetails details = XtreamApiProblemDetails.newBuilder()
                .title("Bad Request")
                .status(400)
                .detail("missing field")
                .errorCode("000:400")
                .build();

        assertEquals(400, details.getStatus());
        assertEquals("Bad Request", details.getTitle());
        assertEquals("missing field", details.getDetail());
        assertEquals("000:400", details.getErrorCode());
    }

    @Test
    void testBuilderWithTypeAndInstance() {
        URI type = URI.create("https://example.com/errors/validation");
        URI instance = URI.create("/api/users/123");
        XtreamApiProblemDetails details = XtreamApiProblemDetails.newBuilder()
                .type(type)
                .title("Validation Error")
                .status(422)
                .detail("field X is required")
                .instance(instance)
                .errorCode("001:422")
                .build();

        assertEquals(type, details.getType());
        assertEquals(instance, details.getInstance());
    }

    @Test
    void testNewBuilderFrom() {
        XtreamApiProblemDetails details = XtreamApiProblemDetails.newBuilderFrom(XtreamApiErrorCode.NOT_FOUND)
                .detail("resource not found")
                .build();

        assertEquals(404, details.getStatus());
        assertEquals("Not Found", details.getTitle());
        assertEquals("000:404", details.getErrorCode());
        assertEquals("resource not found", details.getDetail());
    }

    @Test
    void testBuilderWithHttpStatusCode() {
        XtreamApiProblemDetails details = XtreamApiProblemDetails.newBuilder()
                .status(HttpStatus.FORBIDDEN)
                .errorCode("000:403")
                .detail("access denied")
                .build();

        assertEquals(403, details.getStatus());
        assertEquals("Forbidden", details.getTitle());
    }

    // endregion

    // region buildAsException

    @Test
    void testBuildAsException() {
        ExceptionDelegateXtreamApiProblemDetails exception = XtreamApiProblemDetails.newBuilder()
                .title("Bad Request")
                .status(400)
                .errorCode("000:400")
                .detail("invalid input")
                .buildAsException();

        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(XtreamApiProblemDetails.class, exception);
        assertEquals(400, exception.getStatus());
        assertEquals("Bad Request", exception.getTitle());
        assertEquals("invalid input", exception.getDetail());
        assertEquals("000:400", exception.getErrorCode());
    }

    @Test
    void testBuildAsExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("root cause");
        ExceptionDelegateXtreamApiProblemDetails exception = XtreamApiProblemDetails.newBuilder()
                .title("Server Error")
                .status(500)
                .errorCode("000:500")
                .detail("unexpected error")
                .cause(cause)
                .buildAsException();

        // getCause() delegates to delegate.getCause(), which uses the interface default (null)
        // The cause is passed to RuntimeException's super constructor, but getCause() is overridden
        assertNull(exception.getCause());
        assertEquals("unexpected error", exception.getDetail());
    }

    // endregion

    // region jsonValue

    @Test
    void testJsonValue() {
        XtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails(
                XtreamApiErrorCode.BAD_REQUEST, "test detail", null
        );
        Map<String, Object> json = details.jsonValue(false, false);

        assertEquals(URI.create("about:blank"), json.get("type"));
        assertEquals("Bad Request", json.get("title"));
        assertEquals(400, json.get("status"));
        assertEquals("test detail", json.get("detail"));
        assertEquals("000:400", json.get("errorCode"));
        assertFalse(json.containsKey("timestamp"));
        assertFalse(json.containsKey("errorDetails"));
    }

    @Test
    void testJsonValueWithTimestamp() {
        XtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails(
                XtreamApiErrorCode.SERVER_ERROR, "error", null
        );
        Map<String, Object> json = details.jsonValue(false, true);
        assertTrue(json.containsKey("timestamp"));
    }

    @Test
    void testJsonValueWithErrorDetails() {
        List<XtreamHttpErrorDetails.ParameterErrorDetails> errorDetails =
                XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList("field error");
        XtreamApiProblemDetails details = new DefaultXtreamApiProblemDetails(
                XtreamApiErrorCode.BAD_REQUEST, "error", errorDetails
        );
        Map<String, Object> json = details.jsonValue(true, false);
        assertTrue(json.containsKey("errorDetails"));
    }

    // endregion

    // region static factory methods

    @Test
    void testBadRequestFactory() {
        List<XtreamHttpErrorDetails.ParameterErrorDetails> details =
                XtreamHttpErrorDetails.ParameterErrorDetails.ofSingleList("name is required");
        XtreamApiProblemDetails pd = XtreamApiProblemDetails.badRequest(details);
        assertEquals(400, pd.getStatus());
        assertEquals("Param Error", pd.getDetail());
    }

    @Test
    void testBadRequestFactoryWithMessage() {
        XtreamApiProblemDetails pd = XtreamApiProblemDetails.badRequest("custom error", List.of());
        assertEquals(400, pd.getStatus());
        assertEquals("custom error", pd.getDetail());
    }

    @Test
    void testOfWithHttpStatusCode() {
        XtreamApiProblemDetails pd = XtreamApiProblemDetails.of(
                HttpStatus.CONFLICT, "009:409", "resource conflict", null
        );
        assertEquals(409, pd.getStatus());
        assertEquals("009:409", pd.getErrorCode());
        assertEquals("resource conflict", pd.getDetail());
    }

    @Test
    void testOfWithErrorCode() {
        XtreamApiProblemDetails pd = XtreamApiProblemDetails.of(
                XtreamApiErrorCode.PROXY_ERROR, "proxy failed", List.of()
        );
        assertEquals(502, pd.getStatus());
        assertEquals("Bad Gateway", pd.getTitle());
    }

    // endregion

    // region parseHttpStatusTitle

    @Test
    void testParseHttpStatusTitle() {
        assertEquals("OK", XtreamApiProblemDetails.parseHttpStatusTitle(HttpStatus.OK));
        assertEquals("Not Found", XtreamApiProblemDetails.parseHttpStatusTitle(HttpStatus.NOT_FOUND));
    }

    @Test
    void testParseHttpStatusTitleWithCustomCode() {
        String title = XtreamApiProblemDetails.parseHttpStatusTitle(HttpStatus.valueOf(200));
        assertEquals("OK", title);
    }

    // endregion
}

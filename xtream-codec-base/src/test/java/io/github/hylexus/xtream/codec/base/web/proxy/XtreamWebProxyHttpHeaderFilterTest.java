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

package io.github.hylexus.xtream.codec.base.web.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class XtreamWebProxyHttpHeaderFilterTest {

    @Test
    void testHopByHopHeadersAreRemoved() {
        XtreamWebProxyHttpHeaderFilter filter = new XtreamWebProxyHttpHeaderFilter(Set.of());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Host", "example.com");
        headers.add("Connection", "keep-alive");
        headers.add("Keep-Alive", "timeout=5");
        headers.add("Transfer-Encoding", "chunked");
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "text/html");

        HttpHeaders filtered = filter.filterHeaders(headers);

        assertFalse(filtered.containsKey("Host"));
        assertFalse(filtered.containsKey("Connection"));
        assertFalse(filtered.containsKey("Keep-Alive"));
        assertFalse(filtered.containsKey("Transfer-Encoding"));
        assertTrue(filtered.containsKey("Content-Type"));
        assertTrue(filtered.containsKey("Accept"));
    }

    @Test
    void testAllHopByHopHeaders() {
        XtreamWebProxyHttpHeaderFilter filter = new XtreamWebProxyHttpHeaderFilter(Set.of());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Proxy-Authenticate", "Basic");
        headers.add("Proxy-Authorization", "Basic xxx");
        headers.add("TE", "trailers");
        headers.add("Trailer", "Expires");
        headers.add("Upgrade", "websocket");
        headers.add("X-Application-Context", "app");

        HttpHeaders filtered = filter.filterHeaders(headers);

        assertTrue(filtered.isEmpty());
    }

    @Test
    void testCustomIgnoredHeaders() {
        XtreamWebProxyHttpHeaderFilter filter = new XtreamWebProxyHttpHeaderFilter(Set.of("X-Custom-Header"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Custom-Header", "value");
        headers.add("Content-Type", "application/json");

        HttpHeaders filtered = filter.filterHeaders(headers);

        assertFalse(filtered.containsKey("X-Custom-Header"));
        assertTrue(filtered.containsKey("Content-Type"));
    }

    @Test
    void testCaseInsensitiveHeaderFiltering() {
        XtreamWebProxyHttpHeaderFilter filter = new XtreamWebProxyHttpHeaderFilter(Set.of());

        HttpHeaders headers = new HttpHeaders();
        headers.add("host", "example.com");
        headers.add("connection", "keep-alive");
        headers.add("Authorization", "Bearer token");

        HttpHeaders filtered = filter.filterHeaders(headers);

        assertFalse(filtered.containsKey("host"));
        assertFalse(filtered.containsKey("connection"));
        assertTrue(filtered.containsKey("Authorization"));
    }

    @Test
    void testNormalHeadersPreserved() {
        XtreamWebProxyHttpHeaderFilter filter = new XtreamWebProxyHttpHeaderFilter(Set.of());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer token");
        headers.add("Accept", "*/*");
        headers.add("X-Request-Id", "12345");

        HttpHeaders filtered = filter.filterHeaders(headers);

        assertEquals("application/json", filtered.getFirst("Content-Type"));
        assertEquals("Bearer token", filtered.getFirst("Authorization"));
        assertEquals("*/*", filtered.getFirst("Accept"));
        assertEquals("12345", filtered.getFirst("X-Request-Id"));
    }
}

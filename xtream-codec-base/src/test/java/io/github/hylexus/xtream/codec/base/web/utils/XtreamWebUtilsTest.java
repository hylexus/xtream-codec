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

package io.github.hylexus.xtream.codec.base.web.utils;

import io.github.hylexus.xtream.codec.base.web.annotation.ClientIp;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

import static io.github.hylexus.xtream.codec.base.web.utils.XtreamWebUtils.filterClientIp;
import static org.junit.jupiter.api.Assertions.*;

class XtreamWebUtilsTest {

    @Test
    void testFilterClientIp() {
        assertEquals("1.2.3.4", filterClientIp("1.2.3.4", "1.1.1.1", false, "localhost"));
        assertEquals("1.1.1.1", filterClientIp(null, "1.1.1.1", false, "localhost"));
        assertNull(filterClientIp("127.0.0.1", "1.1.1.1", true, "localhost"));
        assertNull(filterClientIp("127.0.0.1", "1.1.1.1", true, "1.1.1.1"));

        assertEquals("127.0.0.1", filterClientIp("127.0.0.1", "1.1.1.1", false, "127.0.0.1"));
        assertEquals("localhost", filterClientIp("127.0.0.1", "1.1.1.1", false, "localhost"));
        assertEquals("0:0:0:0:0:0:0:1", filterClientIp("127.0.0.1", "1.1.1.1", false, "0:0:0:0:0:0:0:1"));

        assertNull(filterClientIp("127.0.0.1", "1.1.1.1", false, ClientIp.NULL_PLACEHOLDER));
        assertNull(filterClientIp("0:0:0:0:0:0:0:1", "1.1.1.1", false, ClientIp.NULL_PLACEHOLDER));
        assertNull(filterClientIp("localhost", "1.1.1.1", false, ClientIp.NULL_PLACEHOLDER));

        assertNull(filterClientIp("127.0.0.1", "1.1.1.1", true, ClientIp.NULL_PLACEHOLDER));
        assertNull(filterClientIp("0:0:0:0:0:0:0:1", "1.1.1.1", true, ClientIp.NULL_PLACEHOLDER));
        assertNull(filterClientIp("localhost", "1.1.1.1", true, ClientIp.NULL_PLACEHOLDER));
    }

    // region getClientIp(HttpRequestHeaderProvider)

    @Test
    void testGetClientIpFromXForwardedFor() {
        Map<String, String> headers = Map.of("X-Forwarded-For", "10.0.0.1");
        Optional<String> ip = XtreamWebUtils.getClientIp(headers::get);
        assertEquals(Optional.of("10.0.0.1"), ip);
    }

    @Test
    void testGetClientIpFromXRealIp() {
        Map<String, String> headers = Map.of("X-Real-IP", "192.168.1.1");
        Optional<String> ip = XtreamWebUtils.getClientIp(headers::get);
        assertEquals(Optional.of("192.168.1.1"), ip);
    }

    @Test
    void testGetClientIpMultipleIpsInHeader() {
        // X-Forwarded-For may contain multiple IPs separated by comma
        Map<String, String> headers = Map.of("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        Optional<String> ip = XtreamWebUtils.getClientIp(headers::get);
        assertEquals(Optional.of("10.0.0.1"), ip);
    }

    @Test
    void testGetClientIpUnknownHeaderSkipped() {
        Map<String, String> headers = Map.of("X-Forwarded-For", "unknown", "X-Real-IP", "10.0.0.5");
        Optional<String> ip = XtreamWebUtils.getClientIp(headers::get);
        assertEquals(Optional.of("10.0.0.5"), ip);
    }

    @Test
    void testGetClientIpNoHeaders() {
        Optional<String> ip = XtreamWebUtils.getClientIp(name -> null);
        assertEquals(Optional.empty(), ip);
    }

    @Test
    void testGetClientIpEmptyHeaders() {
        Optional<String> ip = XtreamWebUtils.getClientIp(name -> "");
        assertEquals(Optional.empty(), ip);
    }

    // endregion

    // region getClientIp(HttpRequestHeaderProvider, InetSocketAddress)

    @Test
    void testGetClientIpWithRemoteAddressFallback() throws Exception {
        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getByName("172.16.0.1"), 8080);
        Optional<String> ip = XtreamWebUtils.getClientIp(name -> null, remoteAddress);
        assertTrue(ip.isPresent());
        assertEquals("172.16.0.1", ip.get());
    }

    @Test
    void testGetClientIpHeaderTakesPrecedenceOverRemoteAddress() throws Exception {
        Map<String, String> headers = Map.of("X-Forwarded-For", "10.0.0.1");
        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getByName("172.16.0.1"), 8080);
        Optional<String> ip = XtreamWebUtils.getClientIp(headers::get, remoteAddress);
        assertEquals(Optional.of("10.0.0.1"), ip);
    }

    @Test
    void testGetClientIpNullRemoteAddress() {
        Optional<String> ip = XtreamWebUtils.getClientIp(name -> null, null);
        assertEquals(Optional.empty(), ip);
    }

    // endregion

    // region filterClientIp(String) — single-arg convenience

    @Test
    void testFilterClientIpSingleArg() {
        assertEquals("10.0.0.1", XtreamWebUtils.filterClientIp("10.0.0.1"));
        assertEquals("127.0.0.1", XtreamWebUtils.filterClientIp("127.0.0.1"));
    }

    // endregion

    // region filterNullIp

    @Test
    void testFilterNullIpNull() {
        assertNull(XtreamWebUtils.filterNullIp(null));
    }

    @Test
    void testFilterNullIpPlaceholder() {
        assertNull(XtreamWebUtils.filterNullIp(ClientIp.NULL_PLACEHOLDER));
    }

    @Test
    void testFilterNullIpNormal() {
        assertEquals("10.0.0.1", XtreamWebUtils.filterNullIp("10.0.0.1"));
    }

    // endregion

    // region isLocalhost

    @Test
    void testIsLocalhost() {
        assertTrue(XtreamWebUtils.isLocalhost("127.0.0.1"));
        assertTrue(XtreamWebUtils.isLocalhost("localhost"));
        assertTrue(XtreamWebUtils.isLocalhost("0:0:0:0:0:0:0:1"));
    }

    @Test
    void testIsLocalhostWithWhitespace() {
        assertTrue(XtreamWebUtils.isLocalhost("  127.0.0.1  "));
    }

    @Test
    void testIsLocalhostFalse() {
        assertFalse(XtreamWebUtils.isLocalhost("10.0.0.1"));
        assertFalse(XtreamWebUtils.isLocalhost("192.168.1.1"));
    }

    @Test
    void testIsLocalhostNull() {
        assertFalse(XtreamWebUtils.isLocalhost(null));
    }

    // endregion

}

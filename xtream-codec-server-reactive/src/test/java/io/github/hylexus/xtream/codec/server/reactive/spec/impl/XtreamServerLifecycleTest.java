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


package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer.ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.netty.DisposableChannel;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XtreamServerLifecycleTest {
    @ParameterizedTest
    @EnumSource(ServerType.class)
    void repeatedStartKeepsTheBoundChannelAndStopAllowsRestart(ServerType type) {
        final AbstractXtreamServer<?, ?, ?> server = switch (type) {
            case TCP -> new TcpXtreamServer("lifecycle-tcp", List.of(tcp -> tcp.host("127.0.0.1").port(0)));
            case UDP -> new UdpXtreamServer("lifecycle-udp", List.of(udp -> udp.host("127.0.0.1").port(0)));
        };
        server.stop();
        server.start();
        final DisposableChannel first = server.disposableServer;
        assertNotNull(first);
        try {
            assertEquals(type, server.getServerType());
            final var address = assertInstanceOf(InetSocketAddress.class, first.address());
            assertTrue(address.getAddress().isLoopbackAddress());
            assertTrue(address.getPort() > 0);
            server.start();
            assertSame(first, server.disposableServer);
            server.stop();
            server.stop();
            assertNull(server.disposableServer);
            assertTrue(first.isDisposed());
            server.start();
            assertNotNull(server.disposableServer);
            assertNotSame(first, server.disposableServer);
        } finally {
            server.stop();
            first.disposeNow();
        }
    }

    @Test
    void startupErrorKeepsTheOriginalCauseAndAllowsStop() {
        final var cause = new IllegalArgumentException("测试绑定失败");
        final var server = new TcpXtreamServer("failed-start", List.of(tcp -> {
            throw cause;
        }));

        assertSame(cause, assertThrows(IllegalStateException.class, server::start).getCause());
        assertDoesNotThrow(server::stop);
    }
}

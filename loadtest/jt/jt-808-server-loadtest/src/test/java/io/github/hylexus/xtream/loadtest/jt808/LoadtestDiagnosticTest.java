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

package io.github.hylexus.xtream.loadtest.jt808;

import io.github.hylexus.xtream.codec.ext.jt808.extensions.listener.Jt808RequestLoggerListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactor.netty.DisposableChannel;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LoadtestDiagnosticTest {
    @TempDir
    Path logs;

    @Test
    void diagnosticProfileShouldEnablePacketLogsAndAllowTcpOnly() throws Exception {
        try (var context = new SpringApplicationBuilder(Jt808LoadtestApp.class).run(
                "--spring.profiles.active=diagnostic",
                "--spring.main.keep-alive=false",
                "--jt808-server.instruction-server.tcp-server.host=127.0.0.1",
                "--jt808-server.instruction-server.tcp-server.port=0",
                "--jt808-server.instruction-server.udp-server.enabled=false",
                "--loadtest.summary-interval=1h",
                "--logging.file.path=" + logs)) {
            assertEquals(1, context.getBeansOfType(XtreamServer.class).size());
            assertEquals(1, context.getBeansOfType(Jt808RequestLoggerListener.class).size());
            final var server = context.getBean(XtreamServer.class);
            final var field = AbstractXtreamServer.class.getDeclaredField("disposableServer");
            field.setAccessible(true);
            final int port = ((InetSocketAddress) ((DisposableChannel) field.get(server)).address()).getPort();
            try (var terminal = new WireTerminal(2019, false, port)) {
                terminal.exchange(0x0002, 1, new byte[0]).assertCommon(0x0002, 1, 1);
            } finally {
                server.stop();
            }
        }
        final String log = Files.readString(logs.resolve("server.log"));
        assertTrue(log.contains("===> Receive"));
        assertTrue(log.contains("<=== Send"));
    }
}

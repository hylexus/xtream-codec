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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.RequestDispatcherSchedulerFilter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant.*;
import static org.junit.jupiter.api.Assertions.*;

class LoadtestServerIntegrationTest {
    @TempDir
    static Path logs;
    private static ConfigurableApplicationContext context;
    private static int tcpPort;
    private static int udpPort;
    private static final String AUTH = "integration-auth";

    @BeforeAll
    static void start() throws Exception {
        context = new SpringApplicationBuilder(Jt808LoadtestApp.class).run(
                "--jt808-server.instruction-server.tcp-server.host=127.0.0.1",
                "--jt808-server.instruction-server.tcp-server.port=0",
                "--jt808-server.instruction-server.udp-server.host=127.0.0.1",
                "--jt808-server.instruction-server.udp-server.port=0",
                "--spring.main.keep-alive=false",
                "--loadtest.auth-code=" + AUTH,
                "--loadtest.summary-interval=1h",
                "--logging.file.path=" + logs);
        tcpPort = port(BEAN_NAME_JT_808_TCP_XTREAM_SERVER_INSTRUCTION_SERVER);
        udpPort = port(BEAN_NAME_JT_808_UDP_XTREAM_SERVER_INSTRUCTION_SERVER);
    }

    @AfterAll
    static void stop() {
        if (context != null) {
            context.getBeansOfType(XtreamServer.class).values().forEach(XtreamServer::stop);
            context.close();
        }
    }

    @Test
    void defaultApplicationShouldUseMinimalServicesAndOneBusinessDispatch() {
        assertEquals(2, context.getBeansOfType(XtreamServer.class).size());
        assertTrue(context.getBeansOfType(Jt808RequestLoggerListener.class).isEmpty());
        assertNotNull(context.getBean(RequestDispatcherSchedulerFilter.class));
        final var registry = context.getBean(XtreamSchedulerRegistry.class);
        assertSame(Schedulers.immediate(), registry.defaultNonBlockingScheduler());
        assertNotSame(Schedulers.immediate(), registry.requestDispatcherScheduler());
        context.getBean(LoadtestMetrics.class).logSummary();
    }

    @ParameterizedTest
    @CsvSource({"2011,false", "2013,false", "2019,false", "2011,true", "2013,true", "2019,true"})
    void shouldRegisterAuthenticateAndReportThroughActualProtocol(int version, boolean udp) throws Exception {
        final var metrics = context.getBean(LoadtestMetrics.class);
        final long locationsBefore = metrics.snapshot().locationRecords();
        try (var terminal = terminal(version, udp)) {
            final var registered = terminal.exchange(0x0100, 1, terminal.registration());
            assertEquals(0x8100, registered.messageId());
            assertEquals(1, WireTerminal.word(registered.body(), 0));
            assertEquals(0, registered.body()[2]);
            assertEquals(AUTH, new String(registered.body(), 3, registered.body().length - 3, StandardCharsets.US_ASCII));
            final var session = context.getBean(Jt808SessionManager.class).list()
                    .filter(candidate -> candidate.remoteAddress().getPort() == terminal.localPort())
                    .findFirst().orElseThrow();
            assertEquals(version, session.protocolVersion().versionValue());
            terminal.exchange(0x0102, 2, terminal.authentication(AUTH)).assertCommon(0x0102, 2, 0);
            terminal.exchange(0x0002, 3, new byte[0]).assertCommon(0x0002, 3, 0);
            terminal.exchange(0x0200, 4, WireTerminal.location()).assertCommon(0x0200, 4, 0);
            if (version != 2011) {
                terminal.exchange(0x0704, 5, WireTerminal.batch(2)).assertCommon(0x0704, 5, 0);
                terminal.exchange(0x0704, 6, WireTerminal.batch(3)).assertCommon(0x0704, 6, 2);
            }
            assertEquals(locationsBefore + (version == 2011 ? 1 : 3), metrics.snapshot().locationRecords());
        }
    }

    @Test
    void tcpAdmissionShouldCountEachStageOncePerConnection() throws Exception {
        final var metrics = context.getBean(LoadtestMetrics.class);
        final var before = metrics.snapshot().tcpAdmission();
        try (var terminal = terminal(2019, false)) {
            await(() -> metrics.snapshot().tcpAdmission().readyConnections() == before.readyConnections() + 1);
            assertEquals(before.firstDecodedFrames(), metrics.snapshot().tcpAdmission().firstDecodedFrames());
            assertEquals(before.firstResponsesWritten(), metrics.snapshot().tcpAdmission().firstResponsesWritten());

            terminal.sendRaw(terminal.frame(0x0002, 1, new byte[0], true));
            await(() -> metrics.snapshot().tcpAdmission().firstDecodedFrames() == before.firstDecodedFrames() + 1);
            assertEquals(before.firstResponsesWritten(), metrics.snapshot().tcpAdmission().firstResponsesWritten());

            assertEquals(0x8100, terminal.exchange(0x0100, 2, terminal.registration()).messageId());
            terminal.exchange(0x0102, 3, terminal.authentication(AUTH)).assertCommon(0x0102, 3, 0);
            await(() -> metrics.snapshot().tcpAdmission().firstResponsesWritten() == before.firstResponsesWritten() + 1);
        }
        final var after = metrics.snapshot().tcpAdmission();
        assertEquals(before.firstDecodedFrames() + 1, after.firstDecodedFrames());
        assertTrue(after.readyToFirstResponseP95Micros() > 0);
        assertTrue(after.readyToFirstResponseP99Micros() >= after.readyToFirstResponseP95Micros());
        try (var terminal = terminal(2019, true)) {
            terminal.exchange(0x0002, 1, new byte[0]).assertCommon(0x0002, 1, 1);
        }
        assertEquals(after, metrics.snapshot().tcpAdmission());
    }

    @Test
    void pipelinedTcpRequestsShouldPreserveAuthenticationOrder() throws Exception {
        try (var terminal = terminal(2019, false)) {
            terminal.send(0x0100, 1, terminal.registration());
            terminal.send(0x0102, 2, terminal.authentication(AUTH));
            terminal.send(0x0200, 3, WireTerminal.location());
            terminal.send(0x0704, 4, WireTerminal.batch(2));
            assertEquals(0x8100, terminal.receive().messageId());
            terminal.receive().assertCommon(0x0102, 2, 0);
            terminal.receive().assertCommon(0x0200, 3, 0);
            terminal.receive().assertCommon(0x0704, 4, 0);
        }
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void incorrectAuthenticationShouldRefuseTrafficAndAllowRecovery(boolean udp) throws Exception {
        try (var terminal = terminal(2019, udp)) {
            terminal.exchange(0x0002, 1, new byte[0]).assertCommon(0x0002, 1, 1);
            terminal.exchange(0x0102, 2, terminal.authentication("wrong")).assertCommon(0x0102, 2, 1);
            terminal.exchange(0x0200, 3, WireTerminal.location()).assertCommon(0x0200, 3, 1);
            terminal.exchange(0x0102, 4, terminal.authentication(AUTH)).assertCommon(0x0102, 4, 0);
            terminal.exchange(0x0002, 5, new byte[0]).assertCommon(0x0002, 5, 0);
            terminal.exchange(0x0102, 6, terminal.authentication("wrong")).assertCommon(0x0102, 6, 1);
            terminal.exchange(0x0002, 7, new byte[0]).assertCommon(0x0002, 7, 1);
        }
    }

    @Test
    void tcpReconnectShouldCreateUnauthenticatedSessionAndReleaseOldOne() throws Exception {
        final var manager = context.getBean(Jt808SessionManager.class);
        try (var terminal = terminal(2013, false)) {
            terminal.exchange(0x0102, 1, terminal.authentication(AUTH)).assertCommon(0x0102, 1, 0);
        }
        await(() -> manager.countTcp() == 0);
        try (var terminal = terminal(2013, false)) {
            terminal.exchange(0x0002, 1, new byte[0]).assertCommon(0x0002, 1, 1);
            terminal.exchange(0x0102, 2, terminal.authentication(AUTH)).assertCommon(0x0102, 2, 0);
        }
        await(() -> manager.countTcp() == 0);
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void invalidChecksumAndDecodeErrorsShouldNotPreventNextRequest(boolean udp) throws Exception {
        final var metrics = context.getBean(LoadtestMetrics.class);
        final var before = metrics.snapshot();
        try (var terminal = terminal(2019, udp)) {
            terminal.exchange(0x0102, 1, terminal.authentication(AUTH)).assertCommon(0x0102, 1, 0);
            terminal.sendRaw(terminal.frame(0x0002, 2, new byte[0], true));
            terminal.send(0x0002, 3, new byte[0]);
            terminal.receive().assertCommon(0x0002, 3, 0);
            assertEquals(before.invalidChecksums() + 1, metrics.snapshot().invalidChecksums());
            terminal.sendRaw(new byte[]{0x7e, 1, 0x7e});
            terminal.send(0x0200, 4, new byte[]{0});
            terminal.send(0x0002, 5, new byte[0]);
            terminal.receive().assertCommon(0x0002, 5, 0);
            assertEquals(before.processingErrors() + 1, metrics.snapshot().processingErrors());
            if (udp) {
                final var combined = new java.io.ByteArrayOutputStream();
                combined.write(new byte[]{0x7e, 1, 0x7e});
                combined.write(terminal.frame(0x0002, 6, new byte[0], false));
                terminal.sendRaw(combined.toByteArray());
                terminal.receive().assertCommon(0x0002, 6, 0);
            }
        }
    }

    @Test
    void oneHundredMixedTerminalsShouldCompleteProtocolRoundTrips() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final var tasks = new ArrayList<Future<?>>();
            for (int i = 0; i < 100; i++) {
                final int version = new int[]{2011, 2013, 2019}[i % 3];
                final boolean udp = i % 2 == 0;
                tasks.add(executor.submit(() -> {
                    try (var terminal = terminal(version, udp)) {
                        assertEquals(0x8100, terminal.exchange(0x0100, 1, terminal.registration()).messageId());
                        terminal.exchange(0x0102, 2, terminal.authentication(AUTH)).assertCommon(0x0102, 2, 0);
                        terminal.exchange(0x0002, 3, new byte[0]).assertCommon(0x0002, 3, 0);
                        terminal.exchange(0x0200, 4, WireTerminal.location()).assertCommon(0x0200, 4, 0);
                        if (version != 2011) {
                            terminal.exchange(0x0704, 5, WireTerminal.batch(2)).assertCommon(0x0704, 5, 0);
                        }
                    }
                    return null;
                }));
            }
            for (var task : tasks) {
                task.get(15, TimeUnit.SECONDS);
            }
        }
        await(() -> context.getBean(Jt808SessionManager.class).countTcp() == 0);
    }

    private static WireTerminal terminal(int version, boolean udp) throws Exception {
        return new WireTerminal(version, udp, udp ? udpPort : tcpPort);
    }

    private static int port(String beanName) throws Exception {
        final var field = AbstractXtreamServer.class.getDeclaredField("disposableServer");
        field.setAccessible(true);
        final var channel = (DisposableChannel) field.get(context.getBean(beanName));
        return ((InetSocketAddress) channel.address()).getPort();
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.nanoTime() + 3_000_000_000L;
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean());
    }
}

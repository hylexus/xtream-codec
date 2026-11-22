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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.impl;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8201;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener.NoopJt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender.Jt808CommandKey;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Jt808CommandResponseTest {
    @Test
    void shouldSendToNewSessionWhenTerminalReconnectsBeforeSubscription() {
        final var terminalId = "00000000000000000001";
        final int responseMessageId = 0x0201;
        final int flowId = 42;
        final var oldSession = session("old-session", terminalId);
        final var newSession = session("new-session", terminalId);
        final var sessions = new HashMap<String, Jt808Session>();
        sessions.put(oldSession.id(), oldSession);
        final var manager = mock(Jt808SessionManager.class, CALLS_REAL_METHODS);
        when(manager.list()).thenAnswer(ignored -> sessions.values().stream());
        when(manager.getSessionById(anyString())).thenAnswer(call -> Mono.justOrEmpty(sessions.get(call.getArgument(0))));

        final var body = new BuiltinMessage8201();
        final var command = Unpooled.EMPTY_BUFFER;
        final var key = Jt808CommandKey.of(terminalId, responseMessageId, flowId);
        final var encoder = mock(Jt808ResponseEncoder.class);
        when(encoder.encode(eq(body), eq(Jt808ProtocolVersion.VERSION_2019), eq(terminalId), eq(flowId), any())).thenReturn(command);
        final var sender = new DefaultJt808XtreamCommandSender(ByteBufAllocator.DEFAULT, manager, encoder,
                increment -> flowId, new NoopJt808RequestLifecycleListener());
        when(oldSession.writeWith(any())).thenReturn(Mono.error(new IllegalStateException("旧会话已断开")));
        when(newSession.writeWith(any())).thenAnswer(call -> Mono.from(call.<Publisher<? extends ByteBuf>>getArgument(0))
                .doOnNext(buffer -> {
                    assertSame(command, buffer);
                    sender.setClientResponse(key, "location response");
                }).then());

        final Mono<String> reply = sender.sendObjectAndWaitingResponseWithTerminalId(terminalId, Mono.just(body), responseMessageId);
        sessions.remove(oldSession.id());
        sessions.put(newSession.id(), newSession);

        assertEquals("location response", reply.block(Duration.ofSeconds(2)));
        verify(oldSession, never()).writeWith(any());
        verify(newSession).writeWith(any());
    }

    @Test
    void shouldKeepResponseReceivedBeforeWriteCompletes() {
        final var sender = new TestSender();
        sender.write = () -> Mono.fromRunnable(() -> {
            sender.reply("first");
            sender.reply("duplicate");
        });

        assertEquals("first", sender.send().block(Duration.ofSeconds(1)));
    }

    @Test
    void cancellationAndDuplicateKeyShouldNotDisplaceExistingWaiter() {
        final var sender = new TestSender();
        sender.write = Mono::never;
        final var waiting = sender.send().subscribe();
        try {
            final var first = assertThrows(IllegalStateException.class, () -> sender.send().block(Duration.ofSeconds(1)));
            final var second = assertThrows(IllegalStateException.class, () -> sender.send().block(Duration.ofSeconds(1)));
            assertTrue(first.getMessage().startsWith("A command is already awaiting a response for key "));
            assertEquals(first.getMessage(), second.getMessage());
        } finally {
            waiting.dispose();
        }

        sender.write = () -> Mono.fromRunnable(() -> sender.reply("after cancellation"));
        assertEquals("after cancellation", sender.send().block(Duration.ofSeconds(1)));
    }

    @Test
    void writeFailureShouldWinOverEarlyResponseAndReleasePendingKey() {
        final var sender = new TestSender();
        final var failure = new IllegalArgumentException("写入失败");
        sender.write = () -> Mono.defer(() -> {
            sender.reply("early");
            return Mono.error(failure);
        });

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> sender.send().block(Duration.ofSeconds(1))));
        sender.write = () -> Mono.fromRunnable(() -> sender.reply("retry"));
        assertEquals("retry", sender.send().block(Duration.ofSeconds(1)));
    }

    private static Jt808Session session(String id, String terminalId) {
        final var session = mock(Jt808Session.class);
        when(session.id()).thenReturn(id);
        when(session.terminalId()).thenReturn(terminalId);
        when(session.protocolVersion()).thenReturn(Jt808ProtocolVersion.VERSION_2019);
        return session;
    }

    @NullMarked
    private static final class TestSender extends DefaultJt808XtreamCommandSender {
        private static final String TERMINAL = "00000000000000000001";
        private static final int FLOW_ID = 42;
        private final Jt808Session session = mock(Jt808Session.class);
        private Supplier<Mono<Void>> write = Mono::empty;

        private TestSender() {
            super(ByteBufAllocator.DEFAULT, null, null, increment -> FLOW_ID, null);
            when(session.terminalId()).thenReturn(TERMINAL);
            when(session.protocolVersion()).thenReturn(Jt808ProtocolVersion.VERSION_2019);
        }

        private Mono<String> send() {
            return this.sendObjectAndWaitingResponse("test-session", Mono.just(new Object()), 0x0001);
        }

        private void reply(String value) {
            this.setClientResponse(Jt808CommandKey.of(TERMINAL, 0x0001, FLOW_ID), value);
        }

        @Override
        protected Mono<Jt808Session> getSessionById(String sessionId) {
            return Mono.just(session);
        }

        @Override
        protected Mono<ByteBuf> encodeJt808Message(Jt808ProtocolVersion version, String terminalId, int flowId, Object body) {
            return Mono.just(Unpooled.EMPTY_BUFFER);
        }

        @Override
        protected Mono<Void> doWrite(Publisher<? extends ByteBuf> data, Jt808Session selected) {
            return Mono.from(data).then(Mono.defer(write));
        }
    }
}

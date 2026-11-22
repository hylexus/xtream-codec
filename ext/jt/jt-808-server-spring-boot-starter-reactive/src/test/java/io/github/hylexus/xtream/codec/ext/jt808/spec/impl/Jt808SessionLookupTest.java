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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Jt808SessionLookupTest {
    @Test
    void shouldSelectMostRecentlyCommunicatingSession() {
        final var old = session("000000123456", "2026-01-01T00:00:00Z");
        final var recent = session("000000123456", "2026-01-01T01:00:00Z");
        final var other = session("000000654321", "2026-01-01T02:00:00Z");
        final var manager = mock(Jt808SessionManager.class, CALLS_REAL_METHODS);
        when(manager.list()).thenAnswer(ignored -> Stream.of(old, other, recent));
        assertSame(recent, manager.findByTerminalId("000000123456").orElseThrow());
        assertTrue(manager.findByTerminalId("missing").isEmpty());
        assertSame(other, manager.findByTerminalId("000000654321").orElseThrow());
    }

    private static Jt808Session session(String terminalId, String time) {
        final var session = mock(Jt808Session.class);
        when(session.terminalId()).thenReturn(terminalId);
        when(session.lastCommunicateTime()).thenReturn(Instant.parse(time));
        return session;
    }
}

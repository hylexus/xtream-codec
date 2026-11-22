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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.*;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoadtestMessageHandlerTest {
    private final LoadtestMetrics metrics = new LoadtestMetrics(new DefaultListableBeanFactory().getBeanProvider(Jt808SessionManager.class));
    private final LoadtestMessageHandler handler = new LoadtestMessageHandler(new LoadtestProperties("test-auth", 2048, Duration.ofSeconds(30)), metrics);
    private final Jt808Session session = mock(Jt808Session.class);

    @Test
    void registrationShouldReturnConfiguredCodeAndResetAuthentication() {
        final var reply = handler.register(request(0x0100), session, new BuiltinMessage0100AllInOne()).block();
        assertNotNull(reply);
        assertEquals("test-auth", reply.getAuthCode());
        assertEquals(42, reply.getClientFlowId());
        verify(session).verified(false);
        assertEquals(1, metrics.snapshot().messages().get(LoadtestMetrics.MessageType.REGISTER).accepted());
    }

    @Test
    void authenticationShouldVerifyAndClearSessionOnFailure() {
        final var request = request(0x0102);
        assertEquals(0, handler.authenticate(request, session, new BuiltinMessage0102AllInOne().setAuthenticationCode("test-auth")).block().getResult());
        verify(session).verified(true);
        assertEquals(1, handler.authenticate(request, session, new BuiltinMessage0102AllInOne().setAuthenticationCode("wrong")).block().getResult());
        assertEquals(1, handler.authenticate(request, session, new BuiltinMessage0102AllInOne()).block().getResult());
        verify(session, times(2)).verified(false);
        final var counts = metrics.snapshot().messages().get(LoadtestMetrics.MessageType.AUTH);
        assertEquals(1, counts.accepted());
        assertEquals(2, counts.refused());
    }

    @Test
    void heartbeatAndLocationShouldRequireAuthentication() {
        assertEquals(1, handler.heartbeat(request(0x0002), session, new BuiltinMessage0002()).block().getResult());
        assertEquals(1, handler.location(request(0x0200), session, new BuiltinMessage0200()).block().getResult());
        assertEquals(0, metrics.snapshot().locationRecords());
        when(session.verified()).thenReturn(true);
        assertEquals(0, handler.heartbeat(request(0x0002), session, new BuiltinMessage0002()).block().getResult());
        assertEquals(0, handler.location(request(0x0200), session, new BuiltinMessage0200()).block().getResult());
        assertEquals(1, metrics.snapshot().locationRecords());
    }

    @Test
    void batchShouldValidateAndCountActualLocations() {
        final var batch = new BuiltinMessage0704().setCount(2).setType((short) 0).setItemList(List.of(
                new BuiltinMessage0704.Item().setLocationData(new BuiltinMessage0200()),
                new BuiltinMessage0704.Item().setLocationData(new BuiltinMessage0200())));
        assertEquals(1, handler.batchLocation(request(0x0704), session, batch).block().getResult());
        when(session.verified()).thenReturn(true);
        assertEquals(0, handler.batchLocation(request(0x0704), session, batch).block().getResult());
        assertEquals(2, metrics.snapshot().locationRecords());
        batch.setCount(3);
        assertEquals(2, handler.batchLocation(request(0x0704), session, batch).block().getResult());
        assertEquals(2, metrics.snapshot().locationRecords());
        assertEquals(1, metrics.snapshot().messages().get(LoadtestMetrics.MessageType.BATCH_LOCATION).accepted());
    }

    private static Jt808Request request(int messageId) {
        final var request = mock(Jt808Request.class);
        final var header = mock(Jt808RequestHeader.class);
        when(request.messageId()).thenReturn(messageId);
        when(request.header()).thenReturn(header);
        when(header.messageId()).thenReturn(messageId);
        when(header.flowId()).thenReturn(42);
        return request;
    }
}

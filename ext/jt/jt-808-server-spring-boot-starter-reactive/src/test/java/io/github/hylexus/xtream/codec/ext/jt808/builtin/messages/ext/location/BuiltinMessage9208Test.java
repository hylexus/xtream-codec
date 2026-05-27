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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage9208;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinMessage9208Test extends BaseCodecTest {
    LocalDateTime time = LocalDateTime.of(2025, 2, 21, 21, 33, 32);

    @Test
    void testEncode() {
        final BuiltinMessage9208 entity = new BuiltinMessage9208()
                .setAttachmentServerIp("192.168.0.123")
                .setAttachmentServerPortTcp(1111)
                .setAttachmentServerPortUdp(2222)
                .setAlarmIdentifier(
                        new AlarmIdentifier()
                                .setTerminalId("1234567")
                                .setTime(time)
                                .setSequence((short) 111)
                                .setAttachmentCount((short) 1)
                                .setReserved((short) 0)
                )
                .setAlarmNo("12345678901234567890123456789012");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x9208);
        assertEquals("7e92084052010000000001391234432900000d3139322e3136382e302e313233045708ae313233343536372502212133326f0100313233343536373839303132333435363738393031323334353637383930313230303030303030303030303030303030547e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "92084052010000000001391234432900000d3139322e3136382e302e313233045708ae313233343536372502212133326f010031323334353637383930313233343536373839303132333435363738393031323030303030303030303030303030303054";
        final BuiltinMessage9208 entity = decodeAsEntity(BuiltinMessage9208.class, hex);
        assertNotNull(entity);
        assertEquals("192.168.0.123", entity.getAttachmentServerIp());
        assertEquals((short) 1111, entity.getAttachmentServerPortTcp());
        assertEquals((short) 2222, entity.getAttachmentServerPortUdp());
        assertEquals("1234567", entity.getAlarmIdentifier().getTerminalId());
        assertEquals(time, entity.getAlarmIdentifier().getTime());
        assertEquals((short) 111, entity.getAlarmIdentifier().getSequence());
        assertEquals((short) 1, entity.getAlarmIdentifier().getAttachmentCount());
        assertEquals((short) 0, entity.getAlarmIdentifier().getReserved());
        assertEquals("12345678901234567890123456789012", entity.getAlarmNo());
        assertEquals("1234567", entity.getAlarmIdentifier().getTerminalId());
    }

}

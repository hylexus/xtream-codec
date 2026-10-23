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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 固定消息体的编解码回归测试。
 *
 * @author Codex (AI)
 */
class LocationItem0x66Test extends BaseCodecTest {
    private static final LocalDateTime TIME = LocalDateTime.of(2025, 2, 21, 21, 33, 32);
    private static final String HEX = "f0000001015003e801d907f2073d336c250221213332800141424331323334250221213332ff020002010002000300040005ffffff03e807d00bb8";

    @Test
    void testEncode() {
        assertEquals(HEX, encodeBody(createMessage()));
    }

    @Test
    void testDecode() {
        final LocationItem0x66 entity = decodeBody(LocationItem0x66.class, HEX);
        assertEquals(0xf0000001L, entity.getAlarmId());
        assertEquals(1, entity.getStatus());
        assertEquals(80, entity.getSpeed());
        assertEquals(1000, entity.getHeight());
        assertEquals(31000562L, entity.getLatitude());
        assertEquals(121451372L, entity.getLongitude());
        assertEquals(TIME, entity.getDatetime());
        assertEquals(0x8001, entity.getVehicleStatus());
        assertEquals("ABC1234", entity.getAlarmIdentifier().getTerminalId());
        assertEquals(TIME, entity.getAlarmIdentifier().getTime());
        assertEquals(255, entity.getAlarmIdentifier().getSequence());
        assertEquals(2, entity.getAlarmIdentifier().getAttachmentCount());
        assertEquals(0, entity.getAlarmIdentifier().getReserved());
        assertEquals(2, entity.getEventItemCount());
        assertEquals(2, entity.getEventItemList().size());
        final LocationItem0x66.EventItem first = entity.getEventItemList().getFirst();
        assertEquals(1, first.getOffset0());
        assertEquals(2, first.getOffset2());
        assertEquals(3, first.getOffset4());
        assertEquals(4, first.getOffset6());
        assertEquals(5, first.getOffset8());
        final LocationItem0x66.EventItem last = entity.getEventItemList().getLast();
        assertEquals(255, last.getOffset0());
        assertEquals(65535, last.getOffset2());
        assertEquals(1000, last.getOffset4());
        assertEquals(2000, last.getOffset6());
        assertEquals(3000, last.getOffset8());
    }

    @Test
    void testNoEvents() {
        final LocationItem0x66 entity = createMessage().setEventItemCount((short) 0);
        final String hex = "f0000001015003e801d907f2073d336c250221213332800141424331323334250221213332ff020000";
        assertEquals(hex, encodeBody(entity));
        final LocationItem0x66 decoded = decodeBody(LocationItem0x66.class, hex);
        assertEquals(0, decoded.getEventItemCount());
        assertNull(decoded.getEventItemList());
    }

    private LocationItem0x66 createMessage() {
        return new LocationItem0x66().setAlarmId(0xf0000001L).setStatus((short) 1)
                .setSpeed((short) 80).setHeight(1000).setLatitude(31000562L).setLongitude(121451372L)
                .setDatetime(TIME).setVehicleStatus(0x8001)
                .setAlarmIdentifier(new AlarmIdentifier().setTerminalId("ABC1234").setTime(TIME)
                        .setSequence((short) 255).setAttachmentCount((short) 2).setReserved((short) 0))
                .setEventItemCount((short) 2).setEventItemList(List.of(
                        new LocationItem0x66.EventItem().setOffset0((short) 1).setOffset2(2).setOffset4(3).setOffset6(4).setOffset8(5),
                        new LocationItem0x66.EventItem().setOffset0((short) 255).setOffset2(65535).setOffset4(1000).setOffset6(2000).setOffset8(3000)
                ));
    }
}


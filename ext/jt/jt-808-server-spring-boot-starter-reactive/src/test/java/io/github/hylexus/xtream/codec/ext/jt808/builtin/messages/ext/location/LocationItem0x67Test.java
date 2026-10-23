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
import static org.junit.jupiter.api.Assertions.*;

/**
 * 固定消息体的编解码回归测试。
 *
 * @author Codex (AI)
 */
class LocationItem0x67Test extends BaseCodecTest {
    private static final LocalDateTime TIME = LocalDateTime.of(2025, 2, 21, 21, 33, 32);
    private static final String HEX = "f000000101025003e801d907f2073d336c250221213332800141424331323334250221213332ff0200";

    @Test
    void testEncode() {
        assertEquals(HEX, encodeBody(createMessage()));
    }

    @Test
    void testDecode() {
        final LocationItem0x67 entity = decodeBody(LocationItem0x67.class, HEX);
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
        assertEquals(2, entity.getAlarmType());
    }

    private LocationItem0x67 createMessage() {
        return new LocationItem0x67().setAlarmId(0xf0000001L).setStatus((short) 1)
                .setSpeed((short) 80).setHeight(1000).setLatitude(31000562L).setLongitude(121451372L)
                .setDatetime(TIME).setVehicleStatus(0x8001)
                .setAlarmIdentifier(new AlarmIdentifier().setTerminalId("ABC1234").setTime(TIME)
                        .setSequence((short) 255).setAttachmentCount((short) 2).setReserved((short) 0)).setAlarmType((short) 2);
    }
}


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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x64;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0500Test extends BaseCodecTest {

    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void testEncode() {
        final BuiltinMessage0500 entity = new BuiltinMessage0500();
        entity.setFlowId((short) 123);

        final BuiltinMessage0200 location = new BuiltinMessage0200();
        location.setAlarmFlag(123L);
        location.setStatus(222);
        location.setLatitude(31000562);
        location.setLongitude(121451372);
        location.setAltitude(666);
        location.setSpeed(78);
        location.setDirection(0);
        location.setTime(time);

        final Map<Short, Object> extraItems = new LinkedHashMap<>();
        extraItems.put((short) 0x01, 111L);
        extraItems.put((short) 0x02, 222);
        extraItems.put((short) 0x03, 666);
        extraItems.put((short) 0x04, 333);
        extraItems.put((short) 0x25, 777L);
        extraItems.put((short) 0x30, (short) 33);
        extraItems.put((short) 0x31, (short) 55);
        extraItems.put((short) 0x64, new LocationItem0x64()
                .setAlarmId(222L)
                .setStatus((short) 223)
                .setAlarmType((short) 111)
                .setAlarmLevel((short) 3)
                .setSpeedOfFrontObject((short) 66)
                .setDistanceToFrontObject((short) 11)
                .setDeviationType((short) 12)
                .setRoadSignType((short) 13)
                .setRoadSignData((short) 14)
                .setSpeed((short) 15)
                .setHeight(1234)
                .setLatitude(31000562L)
                .setLongitude(121451372L)
                .setDatetime(time)
                .setVehicleStatus(1)
                .setAlarmIdentifier(new AlarmIdentifier()
                        .setTerminalId("1234567")
                        .setTime(time)
                        .setSequence((short) 1)
                        .setAttachmentCount((short) 1)
                        .setReserved((short) 0)
                )
        );
        location.setExtraItems(extraItems);
        entity.setLocation(location);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0500);
        assertEquals("7e0500406d01000000000139123443290000007b0000007b000000de01d907f2073d336c029a004e000014102119510901040000006f020200de0302029a0402014d250400000309300121310137642f000000dedf6f03420b0c0d0e0f04d201d907f2073d336c141021195109000131323334353637141021195109010100e27e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0500406d01000000000139123443290000007b0000007b000000de01d907f2073d336c029a004e000014102119510901040000006f020200de0302029a0402014d250400000309300121310137642f000000dedf6f03420b0c0d0e0f04d201d907f2073d336c141021195109000131323334353637141021195109010100e2";
        final BuiltinMessage0500 entity = decodeAsEntity(BuiltinMessage0500.class, hex);
        assertEquals(123, entity.getFlowId());

        final BuiltinMessage0200 location = entity.getLocation();
        assertEquals(123L, location.getAlarmFlag());
        assertEquals(222, location.getStatus());
        assertEquals(31000562, location.getLatitude());
        assertEquals(121451372, location.getLongitude());
        assertEquals(666, location.getAltitude());
        assertEquals(78, location.getSpeed());
        assertEquals(0, location.getDirection());
        assertEquals(time, location.getTime());

        final Map<Short, Object> extraItems = location.getExtraItems();
        assertEquals(111L, extraItems.get((short) 0x01));
        assertEquals(222, extraItems.get((short) 0x02));
        assertEquals(666, extraItems.get((short) 0x03));
        assertEquals(333, extraItems.get((short) 0x04));
        assertEquals(777L, extraItems.get((short) 0x25));
        assertEquals((short) 33, extraItems.get((short) 0x30));
        assertEquals((short) 55, extraItems.get((short) 0x31));

        final LocationItem0x64 message64 = (LocationItem0x64) extraItems.get((short) 0x64);
        assertEquals(222L, message64.getAlarmId());
        assertEquals(223, message64.getStatus());
        assertEquals(111, message64.getAlarmType());
        assertEquals(3, message64.getAlarmLevel());
        assertEquals(66, message64.getSpeedOfFrontObject());
        assertEquals(11, message64.getDistanceToFrontObject());
        assertEquals(12, message64.getDeviationType());
        assertEquals(13, message64.getRoadSignType());
        assertEquals(14, message64.getRoadSignData());
        assertEquals(15, message64.getSpeed());
        assertEquals(1234, message64.getHeight());
        assertEquals(31000562L, message64.getLatitude());
        assertEquals(121451372L, message64.getLongitude());
        assertEquals(time, message64.getDatetime());
        assertEquals(1, message64.getVehicleStatus());
        assertEquals("1234567", message64.getAlarmIdentifier().getTerminalId());
        assertEquals(time, message64.getAlarmIdentifier().getTime());
        assertEquals((short) 1, message64.getAlarmIdentifier().getSequence());
        assertEquals((short) 1, message64.getAlarmIdentifier().getAttachmentCount());
        assertEquals(0, message64.getAlarmIdentifier().getReserved());
    }
}

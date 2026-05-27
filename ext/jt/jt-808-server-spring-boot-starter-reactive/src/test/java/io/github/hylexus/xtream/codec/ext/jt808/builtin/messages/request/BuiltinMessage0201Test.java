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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0201Test extends BaseCodecTest {
    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void testEncode() {
        final BuiltinMessage0201 entity = new BuiltinMessage0201();
        entity.setFlowId(321);
        entity.setLocationMessage(createLocationMessage());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0201);
        assertEquals("7e0201403c0100000000013912344329000001410000007b000000de01d907f2073d336c029a004e000014102119510901040000006f020200de0302029a0402014d2504000003093001213101374e7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0201403c0100000000013912344329000001410000007b000000de01d907f2073d336c029a004e000014102119510901040000006f020200de0302029a0402014d2504000003093001213101374e";
        final BuiltinMessage0201 entity = decodeAsEntity(BuiltinMessage0201.class, hex);
        assertEquals(321, entity.getFlowId());
        final BuiltinMessage0200 locationMessage = entity.getLocationMessage();
        assertEquals(123L, locationMessage.getAlarmFlag());
        assertEquals(222, locationMessage.getStatus());
        assertEquals(31000562, locationMessage.getLatitude());
        assertEquals(121451372, locationMessage.getLongitude());
        assertEquals(666, locationMessage.getAltitude());
        assertEquals(78, locationMessage.getSpeed());
        assertEquals(0, locationMessage.getDirection());
        assertEquals(time, locationMessage.getTime());
        assertEquals(111L, locationMessage.getExtraItems().get((short) 0x01));
        assertEquals(222, locationMessage.getExtraItems().get((short) 0x02));
        assertEquals(666, locationMessage.getExtraItems().get((short) 0x03));
        assertEquals(333, locationMessage.getExtraItems().get((short) 0x04));
        assertEquals(777L, locationMessage.getExtraItems().get((short) 0x25));
        assertEquals((short) 33, locationMessage.getExtraItems().get((short) 0x30));
        assertEquals((short) 55, locationMessage.getExtraItems().get((short) 0x31));
    }

    BuiltinMessage0200 createLocationMessage() {
        final BuiltinMessage0200 entity = new BuiltinMessage0200();
        entity.setAlarmFlag(123L);
        entity.setStatus(222);
        entity.setLatitude(31000562);
        entity.setLongitude(121451372);
        entity.setAltitude(666);
        entity.setSpeed(78);
        entity.setDirection(0);
        entity.setTime(time);

        final Map<Short, Object> extraItems = new LinkedHashMap<>();
        extraItems.put((short) 0x01, 111L);
        extraItems.put((short) 0x02, 222);
        extraItems.put((short) 0x03, 666);
        extraItems.put((short) 0x04, 333);
        extraItems.put((short) 0x25, 777L);
        extraItems.put((short) 0x30, (short) 33);
        extraItems.put((short) 0x31, (short) 55);
        entity.setExtraItems(extraItems);
        return entity;
    }
}

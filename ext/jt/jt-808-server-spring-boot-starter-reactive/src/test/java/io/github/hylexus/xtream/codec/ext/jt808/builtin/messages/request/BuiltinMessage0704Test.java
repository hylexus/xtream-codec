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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0704Test extends BaseCodecTest {

    LocalDateTime time = LocalDateTime.of(2024, 10, 29, 21, 39, 3);

    @Test
    void testEncode() {
        final BuiltinMessage0704 entity = new BuiltinMessage0704();
        entity.setCount(2);
        entity.setType((short) 0);
        entity.setItemList(
                List.of(
                        new BuiltinMessage0704.Item()
                                .setLocationData(new BuiltinMessage0200()
                                        .setAlarmFlag(1)
                                        .setStatus(1)
                                        .setLatitude(31000562L)
                                        .setLongitude(121451372L)
                                        .setAltitude(666)
                                        .setSpeed(33)
                                        .setDirection(89)
                                        .setTime(time)),
                        new BuiltinMessage0704.Item()
                                .setLocationData(new BuiltinMessage0200()
                                        .setAlarmFlag(1)
                                        .setStatus(1)
                                        .setLatitude(31000563L)
                                        .setLongitude(121451373L)
                                        .setAltitude(777)
                                        .setSpeed(67)
                                        .setDirection(90)
                                        .setTime(time))
                )
        );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0704);
        assertEquals("7e0704403f01000000000139123443290000000200001c000000010000000101d907f2073d336c029a00210059241029213903001c000000010000000101d907f3073d336d03090043005a241029213903f87e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0704403f01000000000139123443290000000200001c000000010000000101d907f2073d336c029a00210059241029213903001c000000010000000101d907f3073d336d03090043005a241029213903f8";
        final BuiltinMessage0704 entity = decodeAsEntity(BuiltinMessage0704.class, hex);
        assertEquals(2, entity.getCount());
        assertEquals(0, entity.getType());
        assertEquals(2, entity.getItemList().size());
        final BuiltinMessage0200 locationData1 = entity.getItemList().getFirst().getLocationData();
        assertEquals(1, locationData1.getAlarmFlag());
        assertEquals(1, locationData1.getStatus());
        assertEquals(31000562L, locationData1.getLatitude());
        assertEquals(121451372L, locationData1.getLongitude());
        assertEquals(666, locationData1.getAltitude());
        assertEquals(33, locationData1.getSpeed());
        assertEquals(89, locationData1.getDirection());
        assertEquals(time, locationData1.getTime());

        final BuiltinMessage0200 locationData2 = entity.getItemList().getLast().getLocationData();
        assertEquals(1, locationData2.getAlarmFlag());
        assertEquals(1, locationData2.getStatus());
        assertEquals(31000563L, locationData2.getLatitude());
        assertEquals(121451373L, locationData2.getLongitude());
        assertEquals(777, locationData2.getAltitude());
        assertEquals(67, locationData2.getSpeed());
        assertEquals(90, locationData2.getDirection());
        assertEquals(time, locationData2.getTime());
    }
}

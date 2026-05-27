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

class BuiltinMessage0802Test extends BaseCodecTest {

    final LocalDateTime time = LocalDateTime.of(2021, 9, 27, 15, 30, 33);

    @Test
    void testEncode() {
        final BuiltinMessage0802 entity = new BuiltinMessage0802()
                .setFlowId(111)
                .setMultimediaDataItemCount((short) 2)
                .setItemList(List.of(
                                new BuiltinMessage0802.Item()
                                        .setMultimediaId(1)
                                        .setMultimediaType((short) 0)
                                        .setChannelId((short) 22)
                                        .setEventItemCode((short) 0)
                                        .setLocation(new BuiltinMessage0200()
                                                .setAlarmFlag(1)
                                                .setStatus(1)
                                                .setLatitude(31000563L)
                                                .setLongitude(121451373L)
                                                .setAltitude(777)
                                                .setSpeed(67)
                                                .setDirection(90)
                                                .setTime(time)
                                        ),
                                new BuiltinMessage0802.Item()
                                        .setMultimediaId(1)
                                        .setMultimediaType((short) 0)
                                        .setChannelId((short) 22)
                                        .setEventItemCode((short) 0)
                                        .setLocation(new BuiltinMessage0200()
                                                .setAlarmFlag(1)
                                                .setStatus(1)
                                                .setLatitude(31000565L)
                                                .setLongitude(121451375L)
                                                .setAltitude(777)
                                                .setSpeed(67)
                                                .setDirection(90)
                                                .setTime(time))
                        )

                );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e0802404a01000000000139123443290000006f000200000001001600000000010000000101d907f3073d336d03090043005a21092715303300000001001600000000010000000101d907f5073d336f03090043005a2109271530331c7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0802404a01000000000139123443290000006f000200000001001600000000010000000101d907f3073d336d03090043005a21092715303300000001001600000000010000000101d907f5073d336f03090043005a2109271530331c";
        final BuiltinMessage0802 entity = decodeAsEntity(BuiltinMessage0802.class, hex);
        assertEquals(111, entity.getFlowId());
        assertEquals(2, entity.getMultimediaDataItemCount());

        final BuiltinMessage0802.Item first = entity.getItemList().getFirst();
        assertEquals(1, first.getMultimediaId());
        assertEquals(0, first.getMultimediaType());
        assertEquals(22, first.getChannelId());
        assertEquals(0, first.getEventItemCode());
        assertEquals(time, first.getLocation().getTime());

        final BuiltinMessage0200 location1 = first.getLocation();
        assertEquals(1, location1.getAlarmFlag());
        assertEquals(1, location1.getStatus());
        assertEquals(31000563L, location1.getLatitude());
        assertEquals(121451373L, location1.getLongitude());
        assertEquals(777, location1.getAltitude());
        assertEquals(67, location1.getSpeed());
        assertEquals(90, location1.getDirection());
        assertEquals(time, location1.getTime());

        final BuiltinMessage0802.Item last = entity.getItemList().getLast();
        assertEquals(1, last.getMultimediaId());
        assertEquals(0, last.getMultimediaType());
        assertEquals(22, last.getChannelId());
        assertEquals(0, last.getEventItemCode());

        final BuiltinMessage0200 location2 = last.getLocation();
        assertEquals(1, location2.getAlarmFlag());
        assertEquals(1, location2.getStatus());
        assertEquals(31000565L, location2.getLatitude());
        assertEquals(121451375L, location2.getLongitude());
        assertEquals(777, location2.getAltitude());
        assertEquals(67, location2.getSpeed());
        assertEquals(90, location2.getDirection());
    }
}

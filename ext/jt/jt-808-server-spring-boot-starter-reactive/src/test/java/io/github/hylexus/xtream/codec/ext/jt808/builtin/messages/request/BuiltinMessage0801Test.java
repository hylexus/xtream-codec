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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0801Test extends BaseCodecTest {

    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void testEncode() {
        final BuiltinMessage0801 entity = new BuiltinMessage0801();
        entity.setMultimediaDataID(111)
                .setMultimediaType((short) 0)
                .setMultimediaFormatCode((short) 0)
                .setEventItemCode((short) 7)
                .setChannelId((short) 123)
                .setLocation(new BuiltinMessage0200()
                        .setAlarmFlag(1)
                        .setStatus(2)
                        .setLatitude(31000562L)
                        .setLongitude(121451372L)
                        .setAltitude(100)
                        .setSpeed(60)
                        .setDirection(78)
                        .setTime(time)
                )
                .setMediaData(new byte[]{1, 2, 3, 4, 5});

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e08014029010000000001391234432900000000006f0000077b000000010000000201d907f2073d336c0064003c004e14102119510901020304053e7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "08014029010000000001391234432900000000006f0000077b000000010000000201d907f2073d336c0064003c004e14102119510901020304053e";
        final BuiltinMessage0801 entity = decodeAsEntity(BuiltinMessage0801.class, hex);
        assertEquals(111, entity.getMultimediaDataID());
        assertEquals(0, entity.getMultimediaType());
        assertEquals(0, entity.getMultimediaFormatCode());
        assertEquals(7, entity.getEventItemCode());
        assertEquals(123, entity.getChannelId());
        final BuiltinMessage0200 location = entity.getLocation();
        assertEquals(1, location.getAlarmFlag());
        assertEquals(2, location.getStatus());
        assertEquals(31000562L, location.getLatitude());
        assertEquals(121451372L, location.getLongitude());
        assertEquals(100, location.getAltitude());
        assertEquals(60, location.getSpeed());
        assertEquals(78, location.getDirection());
        assertEquals(time, location.getTime());

        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, entity.getMediaData());
    }
}

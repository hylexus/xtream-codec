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

import io.github.hylexus.xtream.codec.common.utils.XtreamBitOperator;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8600V2019;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuiltinMessage0608Test extends BaseCodecTest {
    final LocalDateTime startTime = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    final LocalDateTime endTime = startTime.plusHours(2);

    @Test
    void testEncode() {
        final BuiltinMessage0608 entity = new BuiltinMessage0608();

        entity.setType((short) 1);
        entity.setDataList(List.of(
                create8600V2019(111L, "line-1"),
                create8600V2019(112L, "line-2")
        ));
        entity.setCount(entity.getDataList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0608);
        assertEquals("7e0608409d01000000000139123443290000010000000201020000006f400301d907f2073d336c0000029a241026183956241026203956006f0a002100066c696e652d310000006f000201d907f2073d336c0000029a006f0a002100066c696e652d31010200000070400301d907f2073d336c0000029a241026183956241026203956006f0a002100066c696e652d3200000070000201d907f2073d336c0000029a006f0a002100066c696e652d32a57e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0608409d01000000000139123443290000010000000201020000006f400301d907f2073d336c0000029a241026183956241026203956006f0a002100066c696e652d310000006f000201d907f2073d336c0000029a006f0a002100066c696e652d31010200000070400301d907f2073d336c0000029a241026183956241026203956006f0a002100066c696e652d3200000070000201d907f2073d336c0000029a006f0a002100066c696e652d32a5";
        final BuiltinMessage0608 entity = decodeAsEntity(BuiltinMessage0608.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(2, entity.getCount());
        assertEquals(2, entity.getDataList().size());

        doAssertFirst((BuiltinMessage8600V2019) entity.getDataList().getFirst());
        doAssertSecond((BuiltinMessage8600V2019) entity.getDataList().getLast());
    }

    private void doAssertFirst(BuiltinMessage8600V2019 message8600V2019) {
        assertEquals((short) 1, message8600V2019.getType());
        assertEquals(2, message8600V2019.getAreaList().size());

        assertEquals(31000562, message8600V2019.getAreaList().get(0).getLatitude());
        assertEquals(31000562, message8600V2019.getAreaList().get(1).getLatitude());

        assertEquals(121451372, message8600V2019.getAreaList().get(0).getLongitude());
        assertEquals(121451372, message8600V2019.getAreaList().get(1).getLongitude());

        assertEquals(666, message8600V2019.getAreaList().get(0).getRadius());
        assertEquals(666, message8600V2019.getAreaList().get(1).getRadius());

        assertEquals(startTime, message8600V2019.getAreaList().get(0).getStartTime());
        assertNull(message8600V2019.getAreaList().get(1).getStartTime());

        assertEquals(endTime, message8600V2019.getAreaList().get(0).getEndTime());
        assertNull(message8600V2019.getAreaList().get(1).getEndTime());

        assertEquals(111, message8600V2019.getAreaList().get(0).getTopSpeed());
        assertEquals(111, message8600V2019.getAreaList().get(1).getTopSpeed());

        assertEquals(10, message8600V2019.getAreaList().get(0).getDurationOfOverSpeed());
        assertEquals(10, message8600V2019.getAreaList().get(1).getDurationOfOverSpeed());

        assertEquals(33, message8600V2019.getAreaList().get(0).getTopSpeedAtNight());
        assertEquals(33, message8600V2019.getAreaList().get(1).getTopSpeedAtNight());

        assertEquals("line-1", message8600V2019.getAreaList().get(0).getAreaName());
        assertEquals("line-1", message8600V2019.getAreaList().get(1).getAreaName());
    }

    private void doAssertSecond(BuiltinMessage8600V2019 message8600V2019) {
        assertEquals((short) 1, message8600V2019.getType());
        assertEquals(2, message8600V2019.getAreaList().size());

        assertEquals(31000562, message8600V2019.getAreaList().get(0).getLatitude());
        assertEquals(31000562, message8600V2019.getAreaList().get(1).getLatitude());

        assertEquals(121451372, message8600V2019.getAreaList().get(0).getLongitude());
        assertEquals(121451372, message8600V2019.getAreaList().get(1).getLongitude());

        assertEquals(666, message8600V2019.getAreaList().get(0).getRadius());
        assertEquals(666, message8600V2019.getAreaList().get(1).getRadius());

        assertEquals(startTime, message8600V2019.getAreaList().get(0).getStartTime());
        assertNull(message8600V2019.getAreaList().get(1).getStartTime());

        assertEquals(endTime, message8600V2019.getAreaList().get(0).getEndTime());
        assertNull(message8600V2019.getAreaList().get(1).getEndTime());

        assertEquals(111, message8600V2019.getAreaList().get(0).getTopSpeed());
        assertEquals(111, message8600V2019.getAreaList().get(1).getTopSpeed());

        assertEquals(10, message8600V2019.getAreaList().get(0).getDurationOfOverSpeed());
        assertEquals(10, message8600V2019.getAreaList().get(1).getDurationOfOverSpeed());

        assertEquals(33, message8600V2019.getAreaList().get(0).getTopSpeedAtNight());
        assertEquals(33, message8600V2019.getAreaList().get(1).getTopSpeedAtNight());

        assertEquals("line-2", message8600V2019.getAreaList().get(0).getAreaName());
        assertEquals("line-2", message8600V2019.getAreaList().get(1).getAreaName());
    }

    public BuiltinMessage8600V2019 create8600V2019(long areaId, String areaName) {
        final BuiltinMessage8600V2019 entity = new BuiltinMessage8600V2019();
        entity.setType((short) 1);
        entity.setAreaList(List.of(
                new BuiltinMessage8600V2019.CircularArea().setAreaId(areaId)
                        .setAreaProps(XtreamBitOperator.mutable(0)
                                // 第 0 位为 1
                                .set(0)
                                // 第 1 位为 1
                                .set(1)
                                // 第 14 位为 1
                                .set(14)
                                .intValue()
                        )
                        .setLatitude(31000562)
                        .setLongitude(121451372)
                        .setRadius(666)
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setTopSpeed(111)
                        .setDurationOfOverSpeed((short) 10)
                        .setTopSpeedAtNight(33)
                        .setAreaNameLength(areaName.getBytes(XtreamConstants.CHARSET_GBK).length)
                        .setAreaName(areaName),
                new BuiltinMessage8600V2019.CircularArea().setAreaId(areaId)
                        .setAreaProps(XtreamBitOperator.mutable(0)
                                // 第 1 位为 1
                                .set(1)
                                .intValue()
                        )
                        .setLatitude(31000562)
                        .setLongitude(121451372)
                        .setRadius(666)
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setTopSpeed(111)
                        .setDurationOfOverSpeed((short) 10)
                        .setTopSpeedAtNight(33)
                        .setAreaNameLength(areaName.getBytes(XtreamConstants.CHARSET_GBK).length)
                        .setAreaName(areaName)
        ));
        entity.setAreaCount((short) entity.getAreaList().size());
        return entity;
    }
}

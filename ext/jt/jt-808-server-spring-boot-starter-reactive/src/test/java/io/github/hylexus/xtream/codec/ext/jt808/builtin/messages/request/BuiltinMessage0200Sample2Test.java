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
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.*;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0200Sample2Test extends BaseCodecTest {
    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void test() {
        codec(Jt808ProtocolVersion.VERSION_2011, terminalId2011, createEntity(), (expected, actual, hexString) -> {
            doCompare(expected, actual);
        }, false);

        codec(Jt808ProtocolVersion.VERSION_2013, terminalId2013, createEntity(), (expected, actual, hexString) -> {
            doCompare(expected, actual);
        }, false);

        codec(Jt808ProtocolVersion.VERSION_2019, terminalId2019, createEntity(), (expected, actual, hexString) -> {
            doCompare(expected, actual);
        }, true);
    }

    private static void doCompare(BuiltinMessage0200Sample2 expected, BuiltinMessage0200Sample2 actual) {
        assertEquals(expected.getAlarmFlag(), actual.getAlarmFlag());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getLatitude(), actual.getLatitude());
        assertEquals(expected.getLongitude(), actual.getLongitude());
        assertEquals(expected.getAltitude(), actual.getAltitude());
        assertEquals(expected.getSpeed(), actual.getSpeed());
        assertEquals(expected.getDirection(), actual.getDirection());
        assertEquals(expected.getTime(), actual.getTime());
        final Map<Short, Object> expectedExtraItems = expected.getExtraItems();
        final Map<Short, Object> actualExtraItems = actual.getExtraItems();
        assertEquals(expectedExtraItems.size(), actualExtraItems.size());
        for (Map.Entry<Short, Object> entry : expectedExtraItems.entrySet()) {
            final Short key = entry.getKey();
            switch (key) {
                case 0x12 -> {
                    final LocationItem0x12 e = (LocationItem0x12) entry.getValue();
                    final LocationItem0x12 a = (LocationItem0x12) actualExtraItems.get(key);
                    assertEquals(e.getLocationType(), a.getLocationType());
                    assertEquals(e.getAreaId(), a.getAreaId());
                    assertEquals(e.getDirection(), a.getDirection());
                }
                case 0x13 -> {
                    final LocationItem0x13 e = (LocationItem0x13) entry.getValue();
                    final LocationItem0x13 a = (LocationItem0x13) actualExtraItems.get(key);
                    assertEquals(e.getLineId(), a.getLineId());
                    assertEquals(e.getLineDrivenTime(), a.getLineDrivenTime());
                    assertEquals(e.getResult(), a.getResult());
                }
                case 0x64 -> {
                    final LocationItem0x64 e = (LocationItem0x64) entry.getValue();
                    final LocationItem0x64 a = (LocationItem0x64) actualExtraItems.get(key);
                    doCompareItem64(e, a);
                }
                default -> assertEquals(entry.getValue(), actualExtraItems.get(key));
            }
        }
    }

    private static void doCompareItem64(LocationItem0x64 e, LocationItem0x64 a) {
        assertEquals(e.getAlarmId(), a.getAlarmId());
        assertEquals(e.getStatus(), a.getStatus());
        assertEquals(e.getAlarmType(), a.getAlarmType());
        assertEquals(e.getAlarmLevel(), a.getAlarmLevel());
        assertEquals(e.getSpeedOfFrontObject(), a.getSpeedOfFrontObject());
        assertEquals(e.getDistanceToFrontObject(), a.getDistanceToFrontObject());
        assertEquals(e.getDeviationType(), a.getDeviationType());
        assertEquals(e.getRoadSignType(), a.getRoadSignType());
        assertEquals(e.getRoadSignData(), a.getRoadSignData());
        assertEquals(e.getSpeed(), a.getSpeed());
        assertEquals(e.getHeight(), a.getHeight());
        assertEquals(e.getLatitude(), a.getLatitude());
        assertEquals(e.getLongitude(), a.getLongitude());
        assertEquals(e.getDatetime(), a.getDatetime());
        assertEquals(e.getVehicleStatus(), a.getVehicleStatus());
        assertEquals(e.getAlarmIdentifier().getTerminalId(), a.getAlarmIdentifier().getTerminalId());
        assertEquals(e.getAlarmIdentifier().getTime(), a.getAlarmIdentifier().getTime());
        assertEquals(e.getAlarmIdentifier().getSequence(), a.getAlarmIdentifier().getSequence());
        assertEquals(e.getAlarmIdentifier().getAttachmentCount(), a.getAlarmIdentifier().getAttachmentCount());
        assertEquals(e.getAlarmIdentifier().getReserved(), a.getAlarmIdentifier().getReserved());
    }

    private BuiltinMessage0200Sample2 createEntity() {
        final BuiltinMessage0200Sample2 entity = new BuiltinMessage0200Sample2();
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
        extraItems.put((short) 0x11, new LocationItem0x11((short) 1, 666L));
        extraItems.put((short) 0x25, 777L);
        extraItems.put((short) 0x30, (short) 33);
        extraItems.put((short) 0x12, new LocationItem0x12().setLocationType((short) 1).setAreaId(0).setDirection((short) 1));
        extraItems.put((short) 0x13, new LocationItem0x13().setLineId(1L).setLineDrivenTime(0).setResult((short) 0));
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
        entity.setExtraItems(extraItems);
        return entity;
    }

}

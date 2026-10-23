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
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.BuiltinMessage0200Support.AlarmFlag;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.BuiltinMessage0200Support.StatusBit;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.LocationItem0x64;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author opencode (AI)
 */
class BuiltinMessage0200Test extends BaseCodecTest {

    final LocalDateTime time = LocalDateTime.of(2014, 10, 21, 19, 51, 9);

    @Test
    void test1() {
        final BuiltinMessage0200 entity = createEntity1();
        super.codec(Jt808ProtocolVersion.VERSION_2019, super.terminalId2019, entity, (expected, actual, hexString) -> {
            // System.out.println(hexString);
            // System.out.println(expected);
            // System.out.println(actual);
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
            assertEquals(expectedExtraItems.keySet(), actualExtraItems.keySet());
            assertEquals(expectedExtraItems.get((short) 0x01), actualExtraItems.get((short) 0x01));
            assertEquals(expectedExtraItems.get((short) 0x02), actualExtraItems.get((short) 0x02));
            assertEquals(expectedExtraItems.get((short) 0x03), actualExtraItems.get((short) 0x03));
            assertEquals(expectedExtraItems.get((short) 0x04), actualExtraItems.get((short) 0x04));
            assertArrayEquals((byte[]) expectedExtraItems.get((short) 0x05), (byte[]) actualExtraItems.get((short) 0x05));
            assertEquals(expectedExtraItems.get((short) 0x06), actualExtraItems.get((short) 0x06));
            final BuiltinMessage0200.Item0x11 sourceItem11 = (BuiltinMessage0200.Item0x11) expectedExtraItems.get((short) 0x11);
            final BuiltinMessage0200.Item0x11 targetItem11 = (BuiltinMessage0200.Item0x11) actualExtraItems.get((short) 0x11);
            if (sourceItem11.locationType() == 0) {
                assertEquals((short) 0, targetItem11.locationType());
                assertNull(targetItem11.locationId());
            } else {
                assertEquals(expectedExtraItems.get((short) 0x11), actualExtraItems.get((short) 0x11));
            }
            assertEquals(expectedExtraItems.get((short) 0x12), actualExtraItems.get((short) 0x12));
            assertEquals(expectedExtraItems.get((short) 0x13), actualExtraItems.get((short) 0x13));
            assertEquals(expectedExtraItems.get((short) 0x25), actualExtraItems.get((short) 0x25));
            assertEquals(expectedExtraItems.get((short) 0x2A), actualExtraItems.get((short) 0x2A));
            assertEquals(expectedExtraItems.get((short) 0x2B), actualExtraItems.get((short) 0x2B));
            assertEquals(expectedExtraItems.get((short) 0x30), actualExtraItems.get((short) 0x30));
            assertEquals(expectedExtraItems.get((short) 0x31), actualExtraItems.get((short) 0x31));

            compareItem64(expectedExtraItems, actualExtraItems);

            // ===== @DerivedField 断言 =====
            assertEquals(Set.of(
                    AlarmFlag.EMERGENCY, AlarmFlag.OVERSPEED, AlarmFlag.DANGEROUS_DRIVING,
                    AlarmFlag.GNSS_FAULT, AlarmFlag.GNSS_ANTENNA_CUT, AlarmFlag.GNSS_ANTENNA_SHORT
            ), actual.getAlarmFlags());
            assertEquals("紧急报警 | 超速报警 | 危险驾驶行为报警 | GNSS 模块故障 | GNSS 天线未接或被剪断 | GNSS 天线短路",
                    actual.getAlarmDescription());
            assertEquals(Set.of(
                    StatusBit.GPS_LOCKED, StatusBit.SOUTH_LATITUDE, StatusBit.WEST_LONGITUDE,
                    StatusBit.STOPPED, StatusBit.FCW, StatusBit.LDW,
                    StatusBit.CARGO_EMPTY
            ), actual.getStatusFlags());

        }, true);
    }

    @Test
    void test2() {
        final BuiltinMessage0200 entity = createEntity1();
        entity.setAlarmFlag(XtreamBitOperator.immutable()
                .set(AlarmFlag.EMERGENCY.bitOffset())
                .set(AlarmFlag.FATIGUE_DRIVING.bitOffset())
                .set(AlarmFlag.ROLLOVER_WARNING.bitOffset())
                .value()
        );
        entity.setStatus(XtreamBitOperator.immutable()
                .set(StatusBit.ACC_ON.bitOffset())
                .set(StatusBit.LDW.bitOffset())
                .setRange(8, 2)
                .value()
        );

        super.codec(Jt808ProtocolVersion.VERSION_2019, super.terminalId2019, entity, (expected, actual, hexString) -> {
            assertEquals(Set.of(
                    AlarmFlag.EMERGENCY, AlarmFlag.FATIGUE_DRIVING, AlarmFlag.ROLLOVER_WARNING
            ), actual.getAlarmFlags());
            assertEquals("紧急报警 | 疲劳驾驶报警 | 侧翻预警", actual.getAlarmDescription());

            assertEquals(Set.of(
                    StatusBit.ACC_ON, StatusBit.LDW, StatusBit.CARGO_FULL_LOADED
            ), actual.getStatusFlags());
        }, true);
    }

    @Test
    void test3() {
        final BuiltinMessage0200 entity = createEntity1();
        entity.setAlarmFlag(0);
        entity.setStatus(0);
        entity.setAlarmFlags(Set.of(AlarmFlag.EMERGENCY, AlarmFlag.OVERSPEED));
        entity.setStatusFlags(Set.of(StatusBit.ACC_ON, StatusBit.GPS_LOCKED));

        super.codec(Jt808ProtocolVersion.VERSION_2019, super.terminalId2019, entity, (expected, actual, hexString) -> {
            final long expectedAlarmFlag = XtreamBitOperator.immutable()
                    .set(AlarmFlag.EMERGENCY.bitOffset())
                    .set(AlarmFlag.OVERSPEED.bitOffset())
                    .value();
            assertEquals(expectedAlarmFlag, actual.getAlarmFlag());
            assertEquals(Set.of(AlarmFlag.EMERGENCY, AlarmFlag.OVERSPEED), actual.getAlarmFlags());

            final int expectedStatus = (int) XtreamBitOperator.immutable()
                    .set(StatusBit.ACC_ON.bitOffset())
                    .set(StatusBit.GPS_LOCKED.bitOffset())
                    .value();
            assertEquals(expectedStatus, actual.getStatus());
            // bits 8~9 为 0b00，对应 CARGO_EMPTY（空车）
            assertEquals(Set.of(StatusBit.ACC_ON, StatusBit.GPS_LOCKED, StatusBit.CARGO_EMPTY), actual.getStatusFlags());
        }, true);
    }

    @Test
    void testLocationTypeZeroOmitsLocationId() {
        final BuiltinMessage0200 entity = new BuiltinMessage0200()
                .setAlarmFlag(0)
                .setStatus(0)
                .setLatitude(31000562)
                .setLongitude(121451372)
                .setAltitude(666)
                .setSpeed(78)
                .setDirection(0)
                .setTime(time)
                .setExtraItems(Map.of((short) 0x11, new BuiltinMessage0200.Item0x11((short) 0, 123L)));
        final String expectedHex = "7e0200401f01000000000139123443290000000000000000000001d907f2073d336c029a004e0000141021195109110100c27e";
        assertEquals(expectedHex, encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019));

        final BuiltinMessage0200 actual = decodeAsEntity(BuiltinMessage0200.class, expectedHex);
        assertEquals(0, actual.getAlarmFlag());
        assertEquals(0, actual.getStatus());
        assertEquals(31000562, actual.getLatitude());
        assertEquals(121451372, actual.getLongitude());
        assertEquals(666, actual.getAltitude());
        assertEquals(78, actual.getSpeed());
        assertEquals(0, actual.getDirection());
        assertEquals(time, actual.getTime());
        assertEquals(Set.of((short) 0x11), actual.getExtraItems().keySet());
        final BuiltinMessage0200.Item0x11 item = (BuiltinMessage0200.Item0x11) actual.getExtraItems().get((short) 0x11);
        assertEquals(0, item.locationType());
        assertNull(item.locationId());
    }

    private static void compareItem64(Map<Short, Object> expectedExtraItems, Map<Short, Object> actualExtraItems) {
        final LocationItem0x64 expectedItem64 = (LocationItem0x64) expectedExtraItems.get((short) 0x64);
        final LocationItem0x64 actualItem64 = (LocationItem0x64) actualExtraItems.get((short) 0x64);
        assertEquals(expectedItem64.getAlarmId(), actualItem64.getAlarmId());
        assertEquals(expectedItem64.getStatus(), actualItem64.getStatus());
        assertEquals(expectedItem64.getAlarmType(), actualItem64.getAlarmType());
        assertEquals(expectedItem64.getAlarmLevel(), actualItem64.getAlarmLevel());
        assertEquals(expectedItem64.getSpeedOfFrontObject(), actualItem64.getSpeedOfFrontObject());
        assertEquals(expectedItem64.getDistanceToFrontObject(), actualItem64.getDistanceToFrontObject());
        assertEquals(expectedItem64.getDeviationType(), actualItem64.getDeviationType());
        assertEquals(expectedItem64.getRoadSignType(), actualItem64.getRoadSignType());
        assertEquals(expectedItem64.getRoadSignData(), actualItem64.getRoadSignData());
        assertEquals(expectedItem64.getSpeed(), actualItem64.getSpeed());
        assertEquals(expectedItem64.getHeight(), actualItem64.getHeight());
        assertEquals(expectedItem64.getLatitude(), actualItem64.getLatitude());
        assertEquals(expectedItem64.getLongitude(), actualItem64.getLongitude());
        assertEquals(expectedItem64.getDatetime(), actualItem64.getDatetime());
        assertEquals(expectedItem64.getVehicleStatus(), actualItem64.getVehicleStatus());
        assertEquals(expectedItem64.getAlarmIdentifier().getTerminalId(), actualItem64.getAlarmIdentifier().getTerminalId());
        assertEquals(expectedItem64.getAlarmIdentifier().getTime(), actualItem64.getAlarmIdentifier().getTime());
        assertEquals(expectedItem64.getAlarmIdentifier().getSequence(), actualItem64.getAlarmIdentifier().getSequence());
        assertEquals(expectedItem64.getAlarmIdentifier().getAttachmentCount(), actualItem64.getAlarmIdentifier().getAttachmentCount());
        assertEquals(expectedItem64.getAlarmIdentifier().getReserved(), actualItem64.getAlarmIdentifier().getReserved());
    }

    private BuiltinMessage0200 createEntity1() {
        final BuiltinMessage0200 entity = new BuiltinMessage0200();
        entity.setAlarmFlag(XtreamBitOperator.immutable()
                .set(AlarmFlag.EMERGENCY.bitOffset())
                .set(AlarmFlag.OVERSPEED.bitOffset())
                .set(AlarmFlag.DANGEROUS_DRIVING.bitOffset())
                .set(AlarmFlag.GNSS_FAULT.bitOffset())
                .set(AlarmFlag.GNSS_ANTENNA_CUT.bitOffset())
                .set(AlarmFlag.GNSS_ANTENNA_SHORT.bitOffset())
                .value()
        );
        entity.setStatus(XtreamBitOperator.immutable()
                .set(StatusBit.GPS_LOCKED.bitOffset())
                .set(StatusBit.SOUTH_LATITUDE.bitOffset())
                .set(StatusBit.WEST_LONGITUDE.bitOffset())
                .set(StatusBit.STOPPED.bitOffset())
                .set(StatusBit.FCW.bitOffset())
                .set(StatusBit.LDW.bitOffset())
                .value()
        );
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
        extraItems.put((short) 0x05, new byte[]{1, 2, 3, 4, 5});
        // extraItems.put((short) 0x06, -32767);
        extraItems.put((short) 0x06, 32767);
        extraItems.put((short) 0x11, new BuiltinMessage0200.Item0x11((short) 1, 222L));
        extraItems.put((short) 0x12, new BuiltinMessage0200.Item0x12((short) 3, 55, (short) 1));
        extraItems.put((short) 0x13, new BuiltinMessage0200.Item0x13((short) 5, 22, (short) 1));
        extraItems.put((short) 0x25, 777L);
        extraItems.put((short) 0x2A, 12);
        extraItems.put((short) 0x2B, Integer.MIN_VALUE);
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
        entity.setExtraItems(extraItems);
        return entity;
    }

}

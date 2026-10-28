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
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8602V2019;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8604V2019;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8606V2019;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 查询区域及线路应答的固定报文与嵌套边界测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage0608Test extends BaseCodecTest {
    final LocalDateTime startTime = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    final LocalDateTime endTime = startTime.plusHours(2);

    @Test
    void testAdjacentRectangularMessages() {
        final BuiltinMessage8602V2019 first = new BuiltinMessage8602V2019()
                .setType((short) 1)
                .setAreaCount((short) 1)
                .setAreaList(List.of(
                        new BuiltinMessage8602V2019.RectangularArea()
                                .setAreaId(1)
                                .setLeftTopLatitude(1)
                                .setLeftTopLongitude(2)
                                .setRightBottomLatitude(3)
                                .setRightBottomLongitude(4)
                                .setAreaNameLength(2)
                                .setAreaName("甲")
                ));
        final BuiltinMessage8602V2019 last = new BuiltinMessage8602V2019()
                .setType((short) 2)
                .setAreaCount((short) 1)
                .setAreaList(List.of(
                        new BuiltinMessage8602V2019.RectangularArea()
                                .setAreaId(2)
                                .setLeftTopLatitude(5)
                                .setLeftTopLongitude(6)
                                .setRightBottomLatitude(7)
                                .setRightBottomLongitude(8)
                                .setAreaNameLength(2)
                                .setAreaName("乙")
                ));
        final String hex = "02000000020101000000010000000000010000000200000003000000040002bcd70201000000020000000000050000000600000007000000080002d2d2";
        final BuiltinMessage0608 decoded = verifyNestedMessages((short) 2, List.of(first, last), hex);
        for (int i = 0; i < 2; i++) {
            final BuiltinMessage8602V2019 item = (BuiltinMessage8602V2019) decoded.getDataList().get(i);
            assertEquals(i + 1, item.getType());
            assertEquals(1, item.getAreaCount());
            assertEquals(1, item.getAreaList().size());
            final BuiltinMessage8602V2019.RectangularArea area = item.getAreaList().getFirst();
            assertEquals(i + 1, area.getAreaId());
            assertEquals(0, area.getAreaProps());
            assertEquals(i * 4 + 1, area.getLeftTopLatitude());
            assertEquals(i * 4 + 2, area.getLeftTopLongitude());
            assertEquals(i * 4 + 3, area.getRightBottomLatitude());
            assertEquals(i * 4 + 4, area.getRightBottomLongitude());
            assertEquals(2, area.getAreaNameLength());
            assertEquals(i == 0 ? "甲" : "乙", area.getAreaName());
            assertNull(area.getStartTime());
            assertNull(area.getEndTime());
        }
    }

    @Test
    void testAdjacentPolygonMessages() {
        final BuiltinMessage8604V2019 first = new BuiltinMessage8604V2019().setAreaId(1).setPointCount(1)
                .setPointList(List.of(new BuiltinMessage8604V2019.Point(1, 2))).setAreaNameLength(2).setAreaName("甲");
        final BuiltinMessage8604V2019 last = new BuiltinMessage8604V2019().setAreaId(2).setPointCount(1)
                .setPointList(List.of(new BuiltinMessage8604V2019.Point(3, 4))).setAreaNameLength(2).setAreaName("乙");
        final String hex = "0300000002000000010000000100000001000000020002bcd7000000020000000100000003000000040002d2d2";
        final BuiltinMessage0608 decoded = verifyNestedMessages((short) 3, List.of(first, last), hex);
        for (int i = 0; i < 2; i++) {
            final BuiltinMessage8604V2019 item = (BuiltinMessage8604V2019) decoded.getDataList().get(i);
            assertEquals(i + 1, item.getAreaId());
            assertEquals(0, item.getAreaProps());
            assertEquals(1, item.getPointCount());
            assertEquals(1, item.getPointList().size());
            assertEquals(i * 2 + 1, item.getPointList().getFirst().getLatitude());
            assertEquals(i * 2 + 2, item.getPointList().getFirst().getLongitude());
            assertEquals(2, item.getAreaNameLength());
            assertEquals(i == 0 ? "甲" : "乙", item.getAreaName());
            assertNull(item.getStartTime());
            assertNull(item.getEndTime());
        }
    }

    @Test
    void testAdjacentRouteMessages() {
        final BuiltinMessage8606V2019 first = new BuiltinMessage8606V2019().setRouteId(1).setCount(1)
                .setItemList(List.of(new BuiltinMessage8606V2019.Item().setId(11).setRouteId(12)
                        .setLatitude(13).setLongitude(14).setRouteWidth((short) 2).setAreaNameLength(2).setAreaName("甲")));
        final BuiltinMessage8606V2019 last = new BuiltinMessage8606V2019().setRouteId(2).setCount(1)
                .setItemList(List.of(new BuiltinMessage8606V2019.Item().setId(21).setRouteId(22)
                        .setLatitude(23).setLongitude(24).setRouteWidth((short) 3).setAreaNameLength(2).setAreaName("乙")));
        final String hex = "040000000200000001000000010000000b0000000c0000000d0000000e02000002bcd700000002000000010000001500000016000000170000001803000002d2d2";
        final BuiltinMessage0608 decoded = verifyNestedMessages((short) 4, List.of(first, last), hex);
        for (int i = 0; i < 2; i++) {
            final BuiltinMessage8606V2019 item = (BuiltinMessage8606V2019) decoded.getDataList().get(i);
            assertEquals(i + 1, item.getRouteId());
            assertEquals(0, item.getRouteProps());
            assertEquals(1, item.getCount());
            assertEquals(1, item.getItemList().size());
            assertNull(item.getStartTime());
            assertNull(item.getEndTime());
            final BuiltinMessage8606V2019.Item point = item.getItemList().getFirst();
            assertEquals(i * 10 + 11, point.getId());
            assertEquals(i * 10 + 12, point.getRouteId());
            assertEquals(i * 10 + 13, point.getLatitude());
            assertEquals(i * 10 + 14, point.getLongitude());
            assertEquals(i + 2, point.getRouteWidth());
            assertEquals(0, point.getRouteProps());
            assertEquals(2, point.getAreaNameLength());
            assertEquals(i == 0 ? "甲" : "乙", point.getAreaName());
            assertNull(point.getLongDriveThreshold());
            assertNull(point.getShortDriveThreshold());
            assertNull(point.getMaxSpeedLimit());
            assertNull(point.getSpeedingDuration());
        }
    }

    private BuiltinMessage0608 verifyNestedMessages(short type, List<Object> items, String hex) {
        final BuiltinMessage0608 message = new BuiltinMessage0608().setType(type).setCount(2).setDataList(items);
        assertEquals(hex, encodeBody(message));
        final BuiltinMessage0608 decoded = decodeBody(BuiltinMessage0608.class, hex);
        assertEquals(type, decoded.getType());
        assertEquals(2, decoded.getCount());
        assertEquals(2, decoded.getDataList().size());
        return decoded;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testEmptyDataList(int type) {
        final String hex = "0" + type + "00000000";
        assertEquals(hex, encodeBody(new BuiltinMessage0608().setType((short) type).setCount(0).setDataList(List.of())));
        final BuiltinMessage0608 decoded = decodeBody(BuiltinMessage0608.class, hex);
        assertEquals(type, decoded.getType());
        assertEquals(0, decoded.getCount());
        assertEquals(List.of(), decoded.getDataList());
    }

    @Test
    void testUnsupportedType() {
        assertThrows(UnsupportedOperationException.class, () -> decodeBody(BuiltinMessage0608.class, "0500000001ff"));
    }

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

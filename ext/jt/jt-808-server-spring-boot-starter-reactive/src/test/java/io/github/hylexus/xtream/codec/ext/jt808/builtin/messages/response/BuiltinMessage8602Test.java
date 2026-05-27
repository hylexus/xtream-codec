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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.common.utils.XtreamBitOperator;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuiltinMessage8602Test extends BaseCodecTest {

    final LocalDateTime startTime = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    final LocalDateTime endTime = startTime.plusHours(2);

    @Test
    void testEncode() {
        final BuiltinMessage8602 entity = new BuiltinMessage8602();
        entity.setType((short) 1);
        entity.setAreaList(List.of(
                new BuiltinMessage8602.RectangularArea()
                        .setAreaId(111L)
                        .setAreaProps(XtreamBitOperator.mutable(0)
                                // 第 0 位为 1
                                .set(0)
                                // 第 1 位为 1
                                .set(1)
                                // 第 14 位为 1
                                .set(14)
                                .intValue()
                        )
                        .setLeftTopLatitude(31000562)
                        .setLeftTopLongitude(121451372)
                        .setRightBottomLatitude(31000563)
                        .setRightBottomLongitude(121451373)
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setTopSpeed(111)
                        .setDurationOfOverSpeed((short) 10),
                new BuiltinMessage8602.RectangularArea().setAreaId(222L)
                        .setAreaProps(XtreamBitOperator.mutable(0)
                                // 第 1 位为 1
                                .set(1)
                                .intValue()
                        )
                        .setLeftTopLatitude(31000562)
                        .setLeftTopLongitude(121451372)
                        .setRightBottomLatitude(31000563)
                        .setRightBottomLongitude(121451373)
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setTopSpeed(111)
                        .setDurationOfOverSpeed((short) 10)
        ));
        entity.setAreaCount((short) entity.getAreaList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e86020040013912344323000001020000006f400301d907f2073d336c01d907f3073d336d241026183956241026203956006f0a000000de000201d907f2073d336c01d907f3073d336d006f0a717e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "86020040013912344323000001020000006f400301d907f2073d336c01d907f3073d336d241026183956241026203956006f0a000000de000201d907f2073d336c01d907f3073d336d006f0a71";
        final BuiltinMessage8602 entity = decodeAsEntity(BuiltinMessage8602.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(2, entity.getAreaList().size());

        final BuiltinMessage8602.RectangularArea first = entity.getAreaList().getFirst();
        assertEquals(111L, first.getAreaId());
        assertEquals(XtreamBitOperator.mutable(0).set(0).set(1).set(14).intValue(), first.getAreaProps());
        assertEquals(31000562, first.getLeftTopLatitude());
        assertEquals(121451372, first.getLeftTopLongitude());
        assertEquals(31000563, first.getRightBottomLatitude());
        assertEquals(121451373, first.getRightBottomLongitude());
        assertEquals(111, first.getTopSpeed());
        assertEquals(10, first.getDurationOfOverSpeed());

        final BuiltinMessage8602.RectangularArea second = entity.getAreaList().get(1);
        assertEquals(222L, second.getAreaId());
        assertEquals(XtreamBitOperator.mutable(0).set(1).intValue(), second.getAreaProps());
        assertEquals(31000562, second.getLeftTopLatitude());
        assertEquals(121451372, second.getLeftTopLongitude());
        assertEquals(31000563, second.getRightBottomLatitude());
        assertEquals(121451373, second.getRightBottomLongitude());

        assertNull(second.getStartTime());
        assertNull(second.getEndTime());

        assertEquals(111, second.getTopSpeed());
        assertEquals(10, second.getDurationOfOverSpeed());
    }
}

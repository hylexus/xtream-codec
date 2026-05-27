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

class BuiltinMessage8604Test extends BaseCodecTest {

    final LocalDateTime startTime = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    final LocalDateTime endTime = startTime.plusHours(2);

    @Test
    void testEncode() {
        final BuiltinMessage8604 entity = new BuiltinMessage8604();
        entity.setAreaId(111);
        entity.setAreaProps(XtreamBitOperator.mutable(0)
                // 第 0 位为 1
                .set(0)
                // 第 1 位为 1
                .set(1)
                // 第 14 位为 1
                .set(14)
                .intValue()
        );
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setTopSpeed(111);
        entity.setDurationOfOverSpeed((short) 10);
        entity.setPointList(
                List.of(
                        new BuiltinMessage8604.Point(31000562L, 121451372L),
                        new BuiltinMessage8604.Point(31000563L, 121451373L),
                        new BuiltinMessage8604.Point(31000564L, 121451374L)
                )
        );
        entity.setPointCount(entity.getPointList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e8604002f01391234432300000000006f4003241026183956241026203956006f0a000301d907f2073d336c01d907f3073d336d01d907f4073d336eed7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8604002f01391234432300000000006f4003241026183956241026203956006f0a000301d907f2073d336c01d907f3073d336d01d907f4073d336eed";
        final BuiltinMessage8604 entity = decodeAsEntity(BuiltinMessage8604.class, hex);
        assertEquals(111, entity.getAreaId());
        assertEquals(XtreamBitOperator.mutable(0).set(0).set(1).set(14).intValue(), entity.getAreaProps());
        assertEquals(startTime, entity.getStartTime());
        assertEquals(endTime, entity.getEndTime());
        assertEquals(111, entity.getTopSpeed());
        assertEquals(10, entity.getDurationOfOverSpeed());
        assertEquals(3, entity.getPointList().size());
        assertEquals(3, entity.getPointCount());
        assertEquals(31000562L, entity.getPointList().get(0).getLatitude());
        assertEquals(121451372L, entity.getPointList().get(0).getLongitude());
        assertEquals(31000563L, entity.getPointList().get(1).getLatitude());
        assertEquals(121451373L, entity.getPointList().get(1).getLongitude());
        assertEquals(31000564L, entity.getPointList().get(2).getLatitude());
        assertEquals(121451374L, entity.getPointList().get(2).getLongitude());
    }
}

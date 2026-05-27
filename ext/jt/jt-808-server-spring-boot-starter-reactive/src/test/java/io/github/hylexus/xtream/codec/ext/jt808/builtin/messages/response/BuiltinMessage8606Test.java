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

class BuiltinMessage8606Test extends BaseCodecTest {

    final LocalDateTime startTime = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    final LocalDateTime endTime = startTime.plusHours(2);

    @Test
    void testEncode() {
        final BuiltinMessage8606 entity = new BuiltinMessage8606();
        entity.setRouteId(666);
        entity.setRouteProps(XtreamBitOperator.mutable()
                // 第 0 位为 1
                .set(0)
                // 第 1 位为 1
                .set(1)
                .intValue()
        );
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setItemList(List.of(
                new BuiltinMessage8606.Item()
                        .setId(111)
                        .setRouteId(1111)
                        .setLatitude(31000561)
                        .setLongitude(121451371)
                        .setRouteWidth((short) 20)
                        .setRouteProps(XtreamBitOperator.mutable().set(0).set(1).shortValue())
                        .setLongDriveThreshold(9999)
                        .setShortDriveThreshold(1111)
                        .setMaxSpeedLimit(234)
                        .setSpeedingDuration((short) 6),
                new BuiltinMessage8606.Item()
                        .setId(222)
                        .setRouteId(2222)
                        .setLatitude(31000563)
                        .setLongitude(121451373)
                        .setRouteWidth((short) 20)
                        .setRouteProps(XtreamBitOperator.mutable().reset(0).reset(1).shortValue())
                        .setLongDriveThreshold(678)
                        .setShortDriveThreshold(321)
                        .setMaxSpeedLimit(333)
                        .setSpeedingDuration((short) 66)

        ));
        entity.setCount(entity.getItemList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e8606003f01391234432300000000029a000324102618395624102620395600020000006f0000045701d907f1073d336b1403270f045700ea06000000de000008ae01d907f3073d336d1400b47e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8606003f01391234432300000000029a000324102618395624102620395600020000006f0000045701d907f1073d336b1403270f045700ea06000000de000008ae01d907f3073d336d1400b4";
        final BuiltinMessage8606 entity = decodeAsEntity(BuiltinMessage8606.class, hex);
        assertEquals(666, entity.getRouteId());
        assertEquals(XtreamBitOperator.mutable().set(0).set(1).intValue(), entity.getRouteProps());
        assertEquals(startTime, entity.getStartTime());
        assertEquals(endTime, entity.getEndTime());
        assertEquals(2, entity.getItemList().size());

        final BuiltinMessage8606.Item first = entity.getItemList().get(0);
        assertEquals(111, first.getId());
        assertEquals(1111, first.getRouteId());
        assertEquals(31000561, first.getLatitude());
        assertEquals(121451371, first.getLongitude());
        assertEquals((short) 20, first.getRouteWidth());
        assertEquals(XtreamBitOperator.mutable().set(0).set(1).shortValue(), first.getRouteProps());
        assertEquals(9999, first.getLongDriveThreshold());
        assertEquals(1111, first.getShortDriveThreshold());
        assertEquals(234, first.getMaxSpeedLimit());
        assertEquals((short) 6, first.getSpeedingDuration());

        final BuiltinMessage8606.Item second = entity.getItemList().get(1);
        assertEquals(222, second.getId());
        assertEquals(2222, second.getRouteId());
        assertEquals(31000563, second.getLatitude());
        assertEquals(121451373, second.getLongitude());
        assertEquals((short) 20, second.getRouteWidth());
        assertEquals(XtreamBitOperator.mutable().reset(0).reset(1).shortValue(), second.getRouteProps());
        assertNull(second.getLongDriveThreshold());
        assertNull(second.getShortDriveThreshold());
        assertNull(second.getMaxSpeedLimit());
        assertNull(second.getSpeedingDuration());

    }
}

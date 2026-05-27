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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8203Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8203 entity = new BuiltinMessage8203();
        entity.setFlowId(321);
        entity.setAlarmType(XtreamBitOperator.immutable(0)
                // bi[28] - 1：确认车辆非法位移报警；
                .set(28)
                // bi[21] - 1：确认进出路线报警
                .set(21)
                .longValue()
        );
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8203400601000000000139123443290000014110200000c27e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8203400601000000000139123443290000014110200000c2";
        final BuiltinMessage8203 entity = decodeAsEntity(BuiltinMessage8203.class, hex);

        assertEquals(321, entity.getFlowId());
        final long alarmType = entity.getAlarmType();
        final XtreamBitOperator operator = XtreamBitOperator.immutable(alarmType);
        assertEquals(1, operator.get(28));
        assertEquals(1, operator.get(21));
    }
}

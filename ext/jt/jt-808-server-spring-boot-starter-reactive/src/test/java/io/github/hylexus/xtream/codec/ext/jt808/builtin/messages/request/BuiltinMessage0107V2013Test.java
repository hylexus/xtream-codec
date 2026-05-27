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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinMessage0107V2013Test extends BaseCodecTest {
    @Test
    void testEncode() {
        final BuiltinMessage0107V2013 entity = new BuiltinMessage0107V2013()
                .setType(XtreamBitOperator.mutable(0L)
                        // bit1，0：不适用危险品车辆，1：适用危险品车辆
                        .set(1)
                        // bit6，0：不支持硬盘录像，1：支持硬盘录像
                        .set(6)
                        .shortValue()
                )
                .setManufacturerId("12345")
                .setTerminalType("terminalType12345678")
                .setTerminalId("7654321")
                .setIccid("11112233445566778899")
                // .setHardwareVersionLength((short) 9)
                .setHardwareVersion("hd-v1.2.3")
                .setFirmwareVersion("fm-v3.2.1");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013, 0x0107);
        assertEquals("7e010700400139123443230000004231323334357465726d696e616c54797065313233343536373837363534333231111122334455667788990968642d76312e322e3309666d2d76332e322e31487e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0107V2013 entity = decodeAsEntity(BuiltinMessage0107V2013.class, "010700400139123443230000004231323334357465726d696e616c54797065313233343536373837363534333231111122334455667788990968642d76312e322e3309666d2d76332e322e3148");

        assertNotNull(entity);
        final XtreamBitOperator type = XtreamBitOperator.immutable(entity.getType());
        // 第 1 位为 1
        assertEquals(1, type.get(1));
        // 第 6 位为 1
        assertEquals(1, type.get(6));
        // 其他位都为 0
        assertEquals(0, type.get(7));

        assertEquals("12345", entity.getManufacturerId());
        assertEquals("terminalType12345678", entity.getTerminalType());
        assertEquals("7654321", entity.getTerminalId());
        assertEquals("11112233445566778899", entity.getIccid());
        // assertEquals(9, entity.getHardwareVersionLength());
        assertEquals("hd-v1.2.3", entity.getHardwareVersion());
        assertEquals("fm-v3.2.1", entity.getFirmwareVersion());
    }
}

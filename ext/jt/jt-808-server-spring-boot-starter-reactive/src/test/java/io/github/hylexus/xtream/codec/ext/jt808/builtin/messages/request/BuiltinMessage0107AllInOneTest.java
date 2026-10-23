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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuiltinMessage0107AllInOneTest extends BaseCodecTest {

    @Test
    void testV2013() {
        final BuiltinMessage0107AllInOne sourceEntity = createEntityV2013();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        final String expectedHex = "7e01070042013912344323000000013132333435747970653031323334353637383930313233343569643132333435678905432167890543210a6877762d3132333435360a6677762d363534333231207e";
        assertEquals(expectedHex, hex);
        final BuiltinMessage0107AllInOne decoded = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0107AllInOne.class, expectedHex);
        doCompareV2013(sourceEntity, decoded);
    }

    private void doCompareV2013(BuiltinMessage0107AllInOne sourceEntity, BuiltinMessage0107AllInOne decodedEntity) {
        assertEquals(sourceEntity.getType(), decodedEntity.getType());
        assertEquals(sourceEntity.getManufacturerId(), decodedEntity.getManufacturerId());
        assertEquals(sourceEntity.getTerminalType(), decodedEntity.getTerminalType());
        assertEquals(sourceEntity.getTerminalId(), decodedEntity.getTerminalId());
        assertEquals(sourceEntity.getIccid(), decodedEntity.getIccid());
        assertEquals(sourceEntity.getHardwareVersion(), decodedEntity.getHardwareVersion());
        assertEquals(sourceEntity.getFirmwareVersion(), decodedEntity.getFirmwareVersion());
        assertNull(decodedEntity.getGnssModelProperty());
        assertNull(decodedEntity.getCommunicationModelProperty());
    }

    @Test
    void testV2019() {
        final BuiltinMessage0107AllInOne sourceEntity = createEntityV2019();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        final String expectedHex = "7e010740650100000000013912344329000000013132333435747970653031323334353637383930313233343536373839303132333435696431323334353637383930313233343536373839303132333435363738678905432167890543210a6877762d3132333435360a6677762d3635343332310102777e";
        assertEquals(expectedHex, hex);
        final BuiltinMessage0107AllInOne decoded = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0107AllInOne.class, expectedHex);
        doCompareV2019(sourceEntity, decoded);
    }

    private void doCompareV2019(BuiltinMessage0107AllInOne sourceEntity, BuiltinMessage0107AllInOne decodedEntity) {
        assertEquals(sourceEntity.getType(), decodedEntity.getType());
        assertEquals(sourceEntity.getManufacturerId(), decodedEntity.getManufacturerId());
        assertEquals(sourceEntity.getTerminalType(), decodedEntity.getTerminalType());
        assertEquals(sourceEntity.getTerminalId(), decodedEntity.getTerminalId());
        assertEquals(sourceEntity.getIccid(), decodedEntity.getIccid());
        assertEquals(sourceEntity.getHardwareVersion(), decodedEntity.getHardwareVersion());
        assertEquals(sourceEntity.getFirmwareVersion(), decodedEntity.getFirmwareVersion());
        assertEquals(sourceEntity.getGnssModelProperty(), decodedEntity.getGnssModelProperty());
        assertEquals(sourceEntity.getCommunicationModelProperty(), decodedEntity.getCommunicationModelProperty());
    }

    private BuiltinMessage0107AllInOne createEntityV2013() {
        return new BuiltinMessage0107AllInOne()
                .setType((short) 1)
                .setManufacturerId("12345")
                .setTerminalType("type0123456789012345")
                .setTerminalId("id12345")
                .setIccid("67890543216789054321")
                .setHardwareVersion("hwv-123456")
                .setFirmwareVersion("fwv-654321")
                .setGnssModelProperty((short) 1)
                .setCommunicationModelProperty((short) 2);
    }

    private BuiltinMessage0107AllInOne createEntityV2019() {
        return new BuiltinMessage0107AllInOne()
                .setType((short) 1)
                .setManufacturerId("12345")
                .setTerminalType("type01234567890123456789012345")
                .setTerminalId("id1234567890123456789012345678")
                .setIccid("67890543216789054321")
                .setHardwareVersion("hwv-123456")
                .setFirmwareVersion("fwv-654321")
                .setGnssModelProperty((short) 1)
                .setCommunicationModelProperty((short) 2);
    }

}

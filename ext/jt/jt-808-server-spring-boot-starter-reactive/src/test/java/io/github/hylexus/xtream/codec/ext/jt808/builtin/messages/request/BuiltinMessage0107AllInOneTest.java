/*
 * Copyright 2024 the original author or authors.
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
        final BuiltinMessage0107AllInOne sourceEntity = createEntityV2011();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);

        final BuiltinMessage0107AllInOne decodedEntity1 = this.decodeAsEntity(BuiltinMessage0107AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity1);

        final BuiltinMessage0107AllInOne decodedEntity2 = this.decodeAsEntity(BuiltinMessage0107AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity2);
    }

    private void doCompareV2011(BuiltinMessage0107AllInOne sourceEntity, BuiltinMessage0107AllInOne decodedEntity) {
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

        final BuiltinMessage0107AllInOne decodedEntity1 = this.decodeAsEntity(BuiltinMessage0107AllInOne.class, hex);
        doCompareV2019(sourceEntity, decodedEntity1);

        final BuiltinMessage0107AllInOne decodedEntity2 = this.decodeAsEntity(BuiltinMessage0107AllInOne.class, hex);
        doCompareV2019(sourceEntity, decodedEntity2);
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

    private BuiltinMessage0107AllInOne createEntityV2011() {
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
                .setManufacturerId("12345678901")
                .setTerminalType("type01234567890123456789012345")
                .setTerminalId("id1234567890123456789012345678")
                .setIccid("67890543216789054321")
                .setHardwareVersion("hwv-123456")
                .setFirmwareVersion("fwv-654321")
                .setGnssModelProperty((short) 1)
                .setCommunicationModelProperty((short) 2);
    }

}

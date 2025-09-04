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

class BuiltinMessage0100AllInOneTest extends BaseCodecTest {

    @Test
    void testV2011() {
        final BuiltinMessage0100AllInOne sourceEntity = createEntityV2011();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2011, terminalId2011);
        final BuiltinMessage0100AllInOne entity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0100AllInOne.class, hex);
        doCompare(entity1, entity1);

        final BuiltinMessage0100AllInOne entity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0100AllInOne.class, hex);
        doCompare(sourceEntity, entity2);
    }

    @Test
    void testV2013() {
        final BuiltinMessage0100AllInOne sourceEntity = createEntityV2013();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        final BuiltinMessage0100AllInOne entity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0100AllInOne.class, hex);
        doCompare(entity1, entity1);

        final BuiltinMessage0100AllInOne entity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0100AllInOne.class, hex);
        doCompare(sourceEntity, entity2);
    }

    @Test
    void testV2019() {
        final BuiltinMessage0100AllInOne sourceEntity = createEntityV2019();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        final BuiltinMessage0100AllInOne entity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0100AllInOne.class, hex);
        doCompare(entity1, entity1);

        final BuiltinMessage0100AllInOne entity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0100AllInOne.class, hex);
        doCompare(sourceEntity, entity2);
    }

    private void doCompare(BuiltinMessage0100AllInOne entity1, BuiltinMessage0100AllInOne entity2) {
        assertEquals(entity1.getProvinceId(), entity2.getProvinceId());
        assertEquals(entity1.getCityId(), entity2.getCityId());
        assertEquals(entity1.getManufacturerId(), entity2.getManufacturerId());
        assertEquals(entity1.getTerminalType(), entity2.getTerminalType());
        assertEquals(entity1.getTerminalId(), entity2.getTerminalId());
        assertEquals(entity1.getColor(), entity2.getColor());
        assertEquals(entity1.getCarIdentifier(), entity2.getCarIdentifier());
    }

    private static BuiltinMessage0100AllInOne createEntityV2011() {
        return new BuiltinMessage0100AllInOne()
                .setProvinceId(2011)
                .setCityId(20111)
                .setManufacturerId("abcde")
                .setTerminalType("type1234")
                .setTerminalId("1234567")
                .setColor((short) 1)
                .setCarIdentifier("沪A-v2011");
    }

    private BuiltinMessage0100AllInOne createEntityV2013() {
        return new BuiltinMessage0100AllInOne()
                .setProvinceId(2013)
                .setCityId(20133)
                .setManufacturerId("abcde")
                .setTerminalType("12345678901234567890")
                .setTerminalId("1234567")
                .setColor((short) 2)
                .setCarIdentifier("沪A-v2013");
    }

    private BuiltinMessage0100AllInOne createEntityV2019() {
        return new BuiltinMessage0100AllInOne()
                .setProvinceId(2019)
                .setCityId(20199)
                .setManufacturerId("abcdefghijk")
                .setTerminalType("123456789012345678901234567890")
                .setTerminalId("terminalId12345678901234567890")
                .setColor((short) 3)
                .setCarIdentifier("沪A-v2019");
    }

}

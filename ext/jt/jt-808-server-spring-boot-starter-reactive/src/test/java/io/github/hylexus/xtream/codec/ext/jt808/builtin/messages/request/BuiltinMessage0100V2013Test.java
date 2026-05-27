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

class BuiltinMessage0100V2013Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0100V2013 entity = new BuiltinMessage0100V2013()
                .setProvinceId(11)
                .setCityId(2)
                .setManufacturerId("id321")
                .setTerminalType("type1234567890123456")
                .setTerminalId("id12345")
                .setColor((short) 1)
                .setCarIdentifier("甘J-123459");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e0100002f0139123443230000000b0002696433323174797065313233343536373839303132333435366964313233343501b8ca4a2d3132333435395a7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0100002f0139123443230000000b0002696433323174797065313233343536373839303132333435366964313233343501b8ca4a2d3132333435395a";
        final BuiltinMessage0100V2013 entity = decodeAsEntity(BuiltinMessage0100V2013.class, hex);
        assertEquals(11, entity.getProvinceId());
        assertEquals(2, entity.getCityId());
        assertEquals("id321", entity.getManufacturerId());
        assertEquals("type1234567890123456", entity.getTerminalType());
        assertEquals("id12345", entity.getTerminalId());
        assertEquals(1, entity.getColor());
        assertEquals("甘J-123459", entity.getCarIdentifier());
    }
}

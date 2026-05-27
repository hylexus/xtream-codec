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

class BuiltinMessage0100V2019Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0100V2019 entity = new BuiltinMessage0100V2019()
                .setProvinceId(11)
                .setCityId(2)
                .setManufacturerId("id123456789")
                .setTerminalType("type12345678901234567890123456")
                .setTerminalId("id1234567890123456789012345678")
                .setColor((short) 1)
                .setCarIdentifier("甘J-123459");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e0100405601000000000139123443290000000b0002696431323334353637383974797065313233343536373839303132333435363738393031323334353669643132333435363738393031323334353637383930313233343536373801b8ca4a2d313233343539517e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0100405601000000000139123443290000000b0002696431323334353637383974797065313233343536373839303132333435363738393031323334353669643132333435363738393031323334353637383930313233343536373801b8ca4a2d31323334353951";
        final BuiltinMessage0100V2019 entity = decodeAsEntity(BuiltinMessage0100V2019.class, hex);
        assertEquals(11, entity.getProvinceId());
        assertEquals(2, entity.getCityId());
        assertEquals("id123456789", entity.getManufacturerId());
        assertEquals("type12345678901234567890123456", entity.getTerminalType());
        assertEquals("id1234567890123456789012345678", entity.getTerminalId());
        assertEquals((short) 1, entity.getColor());
        assertEquals("甘J-123459", entity.getCarIdentifier());
    }

}

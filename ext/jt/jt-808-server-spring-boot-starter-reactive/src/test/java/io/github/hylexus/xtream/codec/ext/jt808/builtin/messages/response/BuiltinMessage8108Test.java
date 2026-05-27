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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuiltinMessage8108Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8108 entity = new BuiltinMessage8108();
        entity.setType((short) 52);
        entity.setManufacturerId("12345");
        entity.setVersion("v-1.2.3");
        entity.setData(new byte[]{1, 2, 3, 4});

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e81080016013912344323000034313233343507762d312e322e330000000401020304887e", hex);
    }


    @Test
    void testDecode() {
        final BuiltinMessage8108 entity = decodeAsEntity(BuiltinMessage8108.class, "81080016013912344323000034313233343507762d312e322e33000000040102030488");

        assertNotNull(entity);
        assertEquals(52, entity.getType());
        assertEquals("12345", entity.getManufacturerId());
        assertEquals("v-1.2.3", entity.getVersion());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, entity.getData());
    }
}

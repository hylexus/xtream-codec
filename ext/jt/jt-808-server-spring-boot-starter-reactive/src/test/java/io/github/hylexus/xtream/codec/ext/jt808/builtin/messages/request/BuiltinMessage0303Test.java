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

class BuiltinMessage0303Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0303 entity = new BuiltinMessage0303();
        entity.setType((short) 1);
        entity.setFlag((short) 1);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0303);
        assertEquals("7e03034002010000000001391234432900000101377e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0303400201000000000139123443290000010137";
        final BuiltinMessage0303 entity = decodeAsEntity(BuiltinMessage0303.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(1, entity.getFlag());
    }
}

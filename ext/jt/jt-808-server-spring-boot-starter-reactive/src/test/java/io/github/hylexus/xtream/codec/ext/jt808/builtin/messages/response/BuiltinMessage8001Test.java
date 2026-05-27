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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8001Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8001 entity = new BuiltinMessage8001()
                .setClientFlowId(111)
                .setClientMessageId(0x0200)
                .setResult((short) 0);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8001400501000000000139123443290000006f020000dc7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8001400501000000000139123443290000006f020000dc";
        final BuiltinMessage8001 entity = decodeAsEntity(BuiltinMessage8001.class, hex);
        assertEquals(111, entity.getClientFlowId());
        assertEquals(0x0200, entity.getClientMessageId());
        assertEquals((short) 0, entity.getResult());
    }
}

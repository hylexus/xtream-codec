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

class BuiltinMessage0302Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0302 entity = new BuiltinMessage0302();
        entity.setFlowId(123);
        entity.setAnswerId((short) 1);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013, 0x0302);
        assertEquals("7e030200030139123443230000007b01067e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "030200030139123443230000007b0106";
        final BuiltinMessage0302 entity = decodeAsEntity(BuiltinMessage0302.class, hex);
        assertEquals(123, entity.getFlowId());
        assertEquals(1, entity.getAnswerId());
    }
}

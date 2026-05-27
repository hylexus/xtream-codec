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

class BuiltinMessage8400Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8400 entity = new BuiltinMessage8400();
        entity.setFlag((short) 0);
        entity.setPhoneNumber("13900001111");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8400400c01000000000139123443290000003133393030303031313131867e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8400400c0100000000013912344329000000313339303030303131313186";
        final BuiltinMessage8400 entity = decodeAsEntity(BuiltinMessage8400.class, hex);
        assertEquals(0, entity.getFlag());
        assertEquals("13900001111", entity.getPhoneNumber());
    }
}

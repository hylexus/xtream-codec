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

class BuiltinMessage8500Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8500 entity = new BuiltinMessage8500();
        entity.setType((short) 1);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e85000001013912344323000001fb7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "85000001013912344323000001fb";
        final BuiltinMessage8500 entity = decodeAsEntity(BuiltinMessage8500.class, hex);
        assertEquals(1, entity.getType());
    }

}

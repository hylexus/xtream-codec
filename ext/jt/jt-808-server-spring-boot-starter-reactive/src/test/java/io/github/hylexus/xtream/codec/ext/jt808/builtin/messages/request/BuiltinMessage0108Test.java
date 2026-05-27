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

class BuiltinMessage0108Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0108 entity = new BuiltinMessage0108();
        entity.setType((short) 12);
        entity.setResult((short) 0);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0108);
        assertEquals("7e01084002010000000001391234432900000c00327e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0108 entity = decodeAsEntity(BuiltinMessage0108.class, "01084002010000000001391234432900000c0032");
        assertEquals(12, entity.getType());
        assertEquals(0, entity.getResult());
    }
}

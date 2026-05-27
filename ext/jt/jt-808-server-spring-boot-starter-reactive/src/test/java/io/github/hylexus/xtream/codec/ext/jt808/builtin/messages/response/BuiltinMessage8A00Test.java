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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8A00Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8A00 entity = new BuiltinMessage8A00()
                .setE(17)
                .setN(createArray());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8a004084010000000001391234432900000000001106060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606062a7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8a004084010000000001391234432900000000001106060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606060606062a";
        final BuiltinMessage8A00 entity = decodeAsEntity(BuiltinMessage8A00.class, hex);
        assertEquals(17, entity.getE());
        assertArrayEquals(createArray(), entity.getN());
    }

    byte[] createArray() {
        final byte[] n = new byte[128];
        Arrays.fill(n, (byte) 6);
        return n;
    }
}

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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0A00Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0A00 entity = new BuiltinMessage0A00()
                .setE(18L)
                .setN(createArray());
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e0a00408401000000000139123443290000000000120707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707a97e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0a00408401000000000139123443290000000000120707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707070707a9";
        final BuiltinMessage0A00 entity = decodeAsEntity(BuiltinMessage0A00.class, hex);
        assertEquals(18L, entity.getE());
        assertArrayEquals(createArray(), entity.getN());
    }

    byte[] createArray() {
        final byte[] n = new byte[128];
        Arrays.fill(n, (byte) 7);
        return n;
    }
}

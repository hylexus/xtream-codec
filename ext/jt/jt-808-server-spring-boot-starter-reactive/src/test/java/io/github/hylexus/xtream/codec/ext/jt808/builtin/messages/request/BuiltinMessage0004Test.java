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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinMessage0004Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0004 entity = new BuiltinMessage0004();

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0004);
        assertEquals("7e0004400001000000000139123443290000317e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "000440000100000000013912344329000031";
        final Jt808Request jt808Request = decodeAsRequest(hex);
        final BuiltinMessage0004 entity = decodeAsEntity(BuiltinMessage0004.class, hex);
        assertNotNull(entity);
        assertEquals(0x0004, jt808Request.header().messageId());
    }

}

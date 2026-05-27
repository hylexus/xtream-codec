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

import static org.junit.jupiter.api.Assertions.*;

class BuiltinMessage0003Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0003 entity = new BuiltinMessage0003();

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0003);
        assertEquals("7e0003400001000000000139123443290000367e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "000340000100000000013912344329000036";
        final Jt808Request jt808Request = decodeAsRequest(hex);
        final BuiltinMessage0003 entity = decodeAsEntity(BuiltinMessage0003.class, hex);
        assertNotNull(entity);
        assertEquals(0x0003, jt808Request.header().messageId());
    }

}

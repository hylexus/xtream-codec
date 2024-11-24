/*
 * Copyright 2024 the original author or authors.
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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinMessage8107Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8107 entity = new BuiltinMessage8107();

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8107400001000000000139123443290000b37e", hex);
    }

    @Test
    void testDecode() {
        final Jt808Request jt808Request = decodeAsRequest("8107400001000000000139123443290000b3");
        assertEquals(0x8107, jt808Request.header().messageId());
        final BuiltinMessage8107 entity = decodeAsEntity(BuiltinMessage8107.class, "8100400d01000000000139123443290000006f006f6b2e2e6f6b2e2e6f6bd2");
        assertNotNull(entity);
        jt808Request.release();
    }
}

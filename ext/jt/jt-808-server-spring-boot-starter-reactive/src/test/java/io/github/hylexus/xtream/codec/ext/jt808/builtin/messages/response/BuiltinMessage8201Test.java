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
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinMessage8201Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8201 entity = new BuiltinMessage8201();

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8201400001000000000139123443290000b67e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage8201 entity = decodeAsEntity(BuiltinMessage8201.class, "8201400001000000000139123443290000b6");
        assertNotNull(entity);

        final Jt808Request jt808Request = decodeAsRequest("8201400001000000000139123443290000b6");
        assertEquals(terminalId2019, jt808Request.header().terminalId());
        jt808Request.release();
    }
}

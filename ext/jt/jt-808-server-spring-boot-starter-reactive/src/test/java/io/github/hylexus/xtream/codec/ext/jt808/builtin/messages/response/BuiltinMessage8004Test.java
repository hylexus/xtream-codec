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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8004Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final LocalDateTime serverSideDateTime = LocalDateTime.of(2024, 10, 3, 15, 51, 6);
        final BuiltinMessage8004 entity = new BuiltinMessage8004()
                .setServerSideDateTime(serverSideDateTime);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x8004);
        assertEquals("7e8004400601000000000139123443290000241003155106c27e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage8004 entity = decodeAsEntity(BuiltinMessage8004.class, "8004400601000000000139123443290000241003155106c2");

        final LocalDateTime serverSideDateTime = LocalDateTime.of(2024, 10, 3, 15, 51, 6);
        assertEquals(serverSideDateTime, entity.getServerSideDateTime());
    }

}

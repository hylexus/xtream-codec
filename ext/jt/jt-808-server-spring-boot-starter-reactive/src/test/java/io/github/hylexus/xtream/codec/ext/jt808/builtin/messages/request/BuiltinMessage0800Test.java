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

class BuiltinMessage0800Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0800 entity = new BuiltinMessage0800();
        entity.setMultimediaDataID(111)
                .setMultimediaType((short) 0)
                .setMultimediaFormatCode((short) 0)
                .setEventItemCode((short) 7)
                .setChannelId((short) 123);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e08004008010000000001391234432900000000006f0000077b267e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "08004008010000000001391234432900000000006f0000077b26";
        final BuiltinMessage0800 entity = decodeAsEntity(BuiltinMessage0800.class, hex);
        assertEquals(111, entity.getMultimediaDataID());
        assertEquals(0, entity.getMultimediaType());
        assertEquals(0, entity.getMultimediaFormatCode());
        assertEquals(7, entity.getEventItemCode());
        assertEquals(123, entity.getChannelId());
    }
}

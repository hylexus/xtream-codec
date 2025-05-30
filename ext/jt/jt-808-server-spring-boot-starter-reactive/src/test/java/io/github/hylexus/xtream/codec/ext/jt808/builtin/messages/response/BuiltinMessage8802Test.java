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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8802Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8802 entity = new BuiltinMessage8802()
                .setMultimediaType((short) 2)
                .setChannelId((short) 3)
                .setEventItemCode((short) 0)
                .setStartTime(LocalDateTime.of(2021, 4, 12, 14, 30, 3))
                .setEndTime(LocalDateTime.of(2021, 4, 12, 14, 30, 4));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8802400f01000000000139123443290000020300210412143003210412143004b67e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8802400f01000000000139123443290000020300210412143003210412143004b6";
        final BuiltinMessage8802 entity = decodeAsEntity(BuiltinMessage8802.class, hex);
        assertEquals(2, entity.getMultimediaType());
        assertEquals(3, entity.getChannelId());
        assertEquals(0, entity.getEventItemCode());
        assertEquals(LocalDateTime.of(2021, 4, 12, 14, 30, 3), entity.getStartTime());
        assertEquals(LocalDateTime.of(2021, 4, 12, 14, 30, 4), entity.getEndTime());
    }

}

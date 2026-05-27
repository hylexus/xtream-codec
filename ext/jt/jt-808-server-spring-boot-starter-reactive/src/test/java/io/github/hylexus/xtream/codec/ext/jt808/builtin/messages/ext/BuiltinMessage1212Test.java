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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage1212Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage1212 entity = new BuiltinMessage1212()
                .setFileName("02_64_6401_4_5urdvdmh1pzyl9hcdy6x8h5218ghixn7.mp4")
                .setFileType((short) 2)
                .setFileSize(238196);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x1212);
        assertEquals("7e12124037010000000001391234432900003130325f36345f363430315f345f3575726476646d6831707a796c39686364793678386835323138676869786e372e6d7034020003a274c37e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage1212 entity = decodeAsEntity(BuiltinMessage1212.class, "12124037010000000001391234432900003130325f36345f363430315f345f3575726476646d6831707a796c39686364793678386835323138676869786e372e6d7034020003a274c3");
        assertEquals("02_64_6401_4_5urdvdmh1pzyl9hcdy6x8h5218ghixn7.mp4", entity.getFileName());
        assertEquals((short) 2, entity.getFileType());
        assertEquals(238196L, entity.getFileSize());
    }
}

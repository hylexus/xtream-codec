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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8804Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8804 entity = new BuiltinMessage8804()
                .setRecordingCommand((short) 0x01)
                .setRecordingDuration(10)
                .setSaveFlag((short) 0)
                .setAudioSampleRate((short) 3);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e880440050100000000013912344329000001000a0003b47e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "880440050100000000013912344329000001000a0003b4";
        final BuiltinMessage8804 entity = decodeAsEntity(BuiltinMessage8804.class, hex);
        assertEquals(1, entity.getRecordingCommand());
        assertEquals(10, entity.getRecordingDuration());
        assertEquals(0, entity.getSaveFlag());
        assertEquals(3, entity.getAudioSampleRate());
    }
}

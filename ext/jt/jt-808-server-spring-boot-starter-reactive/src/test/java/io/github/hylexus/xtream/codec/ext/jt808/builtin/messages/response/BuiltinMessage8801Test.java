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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8801Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8801 entity = new BuiltinMessage8801()
                .setChanelId((short) 22)
                .setCommand(0)
                .setSaveFlag((short) 1)
                .setResolution((short) 0xff)
                .setQuality((short) 3)
                .setBrightness((short) 199)
                .setContrastRate((short) 100)
                .setSaturation((short) 99)
                .setChroma((short) 111);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8801400c01000000000139123443290000160000000001ff03c764636ff47e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8801400c01000000000139123443290000160000000001ff03c764636ff4";
        final BuiltinMessage8801 entity = decodeAsEntity(BuiltinMessage8801.class, hex);
        assertEquals(22, entity.getChanelId());
        assertEquals(0, entity.getCommand());
        assertEquals(1, entity.getSaveFlag());
        assertEquals(0xff, entity.getResolution());
        assertEquals(3, entity.getQuality());
        assertEquals(199, entity.getBrightness());
        assertEquals(100, entity.getContrastRate());
        assertEquals(99, entity.getSaturation());
        assertEquals(111, entity.getChroma());
    }
}

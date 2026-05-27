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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.jt1078;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage9102Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage9102 entity = new BuiltinMessage9102()
                .setChannelNumber((short) 2)
                .setCommand((short) 0)
                .setMediaTypeToClose((byte) 0)
                .setStreamType((byte) 1);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e910240040100000000013912344329000002000001a17e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "910240040100000000013912344329000002000001a1";
        final BuiltinMessage9102 entity = decodeAsEntity(BuiltinMessage9102.class, hex);
        assertEquals(2, entity.getChannelNumber());
        assertEquals(0, entity.getCommand());
        assertEquals(0, entity.getMediaTypeToClose());
        assertEquals(1, entity.getStreamType());
    }

}

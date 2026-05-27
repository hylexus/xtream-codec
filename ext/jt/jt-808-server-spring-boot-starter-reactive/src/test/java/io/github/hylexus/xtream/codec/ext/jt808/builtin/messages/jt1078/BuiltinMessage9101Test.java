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

class BuiltinMessage9101Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage9101 entity = new BuiltinMessage9101()
                .setServerIp("192.168.0.1")
                .setServerPortTcp(7856)
                .setServerPortUdp(7963)
                .setChannelNumber((short) 2)
                .setDataType((short) 1)
                .setStreamType((short) 0);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e91014013010000000001391234432900000b3139322e3136382e302e311eb01f1b0201003e7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "91014013010000000001391234432900000b3139322e3136382e302e311eb01f1b0201003e";
        final BuiltinMessage9101 entity = decodeAsEntity(BuiltinMessage9101.class, hex);
        assertEquals("192.168.0.1", entity.getServerIp());
        assertEquals(7856, entity.getServerPortTcp());
        assertEquals(7963, entity.getServerPortUdp());
        assertEquals(2, entity.getChannelNumber());
        assertEquals(1, entity.getDataType());
        assertEquals(0, entity.getStreamType());
    }

}

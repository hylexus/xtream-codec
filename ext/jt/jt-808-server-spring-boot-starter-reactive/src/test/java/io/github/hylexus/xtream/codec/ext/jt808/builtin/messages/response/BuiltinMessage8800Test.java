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

import static org.junit.jupiter.api.Assertions.*;

class BuiltinMessage8800Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8800 entity = new BuiltinMessage8800()
                .setMultimediaId(11)
                .setRetransmittedPackageCount((short) 2)
                .setRetransmittedPackageIdList(new byte[]{1, 2, 3, 4});

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e88004009010000000001391234432900000000000b0201020304b97e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "88004009010000000001391234432900000000000b0201020304b9";
        final BuiltinMessage8800 entity = decodeAsEntity(BuiltinMessage8800.class, hex);
        assertEquals(11, entity.getMultimediaId());
        assertEquals(2, entity.getRetransmittedPackageCount());
        assertNotNull(entity.getRetransmittedPackageIdList());
        assertEquals(4, entity.getRetransmittedPackageIdList().length);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, entity.getRetransmittedPackageIdList());
    }
}

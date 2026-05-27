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

import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0805Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0805 entity = new BuiltinMessage0805()
                .setFlowId(111)
                .setResult((short) 0)
                .setMultimediaIdCount(2)
                .setMultimediaIdList(List.of(
                        new DwordWrapper(1L),
                        new DwordWrapper(2L)
                ));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e0805400d01000000000139123443290000006f00000200000001000000025b7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0805400d01000000000139123443290000006f00000200000001000000025b";
        final BuiltinMessage0805 entity = decodeAsEntity(BuiltinMessage0805.class, hex);
        assertEquals(111, entity.getFlowId());
        assertEquals(0, entity.getResult());
        assertEquals(2, entity.getMultimediaIdCount());
        assertEquals(1, entity.getMultimediaIdList().getFirst().asDword());
        assertEquals(2, entity.getMultimediaIdList().getLast().asDword());
    }
}

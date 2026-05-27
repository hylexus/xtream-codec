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

import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8605Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8605 entity = new BuiltinMessage8605();
        entity.setAreaIdList(List.of(
                new DwordWrapper(111L),
                new DwordWrapper(222L),
                new DwordWrapper(333L)
        ));
        entity.setAreaCount((short) entity.getAreaIdList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8605400d01000000000139123443290000030000006f000000de0000014d457e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8605400d01000000000139123443290000030000006f000000de0000014d45";
        final BuiltinMessage8605 entity = decodeAsEntity(BuiltinMessage8605.class, hex);
        assertEquals(3, entity.getAreaIdList().size());
        assertEquals(3, entity.getAreaCount());
        assertEquals(111L, entity.getAreaIdList().get(0).asDword());
        assertEquals(222L, entity.getAreaIdList().get(1).asDword());
        assertEquals(333L, entity.getAreaIdList().get(2).asDword());
    }

}

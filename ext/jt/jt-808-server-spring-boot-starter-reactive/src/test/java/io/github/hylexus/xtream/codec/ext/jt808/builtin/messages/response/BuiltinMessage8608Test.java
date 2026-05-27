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

class BuiltinMessage8608Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8608 entity = new BuiltinMessage8608();
        entity.setType((short) 1);
        entity.setIdList(List.of(
                new DwordWrapper(111L),
                new DwordWrapper(222L),
                new DwordWrapper(333L)
        ));
        entity.setCount(entity.getIdList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e860840110100000000013912344329000001000000030000006f000000de0000014d557e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "860840110100000000013912344329000001000000030000006f000000de0000014d55";
        final BuiltinMessage8608 entity = decodeAsEntity(BuiltinMessage8608.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(3, entity.getCount());
        assertEquals(111L, entity.getIdList().get(0).asDword());
        assertEquals(222L, entity.getIdList().get(1).asDword());
        assertEquals(333L, entity.getIdList().get(2).asDword());
    }

}

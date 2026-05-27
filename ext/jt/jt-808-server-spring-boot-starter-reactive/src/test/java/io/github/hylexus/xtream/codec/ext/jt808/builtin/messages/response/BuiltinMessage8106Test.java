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

class BuiltinMessage8106Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8106 entity = new BuiltinMessage8106();
        entity.setParameterIdList(List.of(
                new DwordWrapper(0x0001L),
                new DwordWrapper(0x0002L)
        ));
        entity.setParameterCount((short) entity.getParameterIdList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8106400901000000000139123443290000020000000100000002ba7e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage8106 entity = decodeAsEntity(BuiltinMessage8106.class, "8106400901000000000139123443290000020000000100000002ba");
        assertEquals(2, entity.getParameterCount());
        assertEquals(entity.getParameterCount(), entity.getParameterIdList().size());
        assertEquals(0x0001L, entity.getParameterIdList().getFirst().asU32());
        assertEquals(0x0002L, entity.getParameterIdList().getLast().asU32());
    }

}

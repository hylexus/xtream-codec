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

import io.github.hylexus.xtream.codec.core.type.wrapper.WordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8003V2013Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8003V2013 entity = new BuiltinMessage8003V2013()
                .setOriginalMessageFlowId(111)
                .setPackageCount((byte) 3)
                .setPackageIdList(List.of(
                                new WordWrapper(2),
                                new WordWrapper(3),
                                new WordWrapper(4)
                        )
                );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013, 0x8003);
        System.out.println(hex);
        assertEquals("7e800300090139123443230000006f030002000300049d7e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage8003V2013 entity = decodeAsEntity(BuiltinMessage8003V2013.class, "800300090139123443230000006f030002000300049d");

        assertEquals(111, entity.getOriginalMessageFlowId());
        assertEquals((byte) 3, entity.getPackageCount());
        assertEquals(3, entity.getPackageIdList().size());
        assertEquals(2, entity.getPackageIdList().get(0).asWord());
        assertEquals(3, entity.getPackageIdList().get(1).asWord());
        assertEquals(4, entity.getPackageIdList().get(2).asWord());
    }

}

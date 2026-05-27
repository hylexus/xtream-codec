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

import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0104Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0104 entity = new BuiltinMessage0104();
        entity.setFlowId(666);
        entity.setParameterCount((short) 7);
        entity.setParameterItems(List.of(
                new BuiltinMessage0104.ParameterItem(0x0001, ByteArrayContainer.ofU32(111)),
                new BuiltinMessage0104.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900001111")),
                new BuiltinMessage0104.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900002222")),
                new BuiltinMessage0104.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900003333")),
                new BuiltinMessage0104.ParameterItem(0x0081, ByteArrayContainer.ofU16(62)),
                new BuiltinMessage0104.ParameterItem(0x0082, ByteArrayContainer.ofU16(103)),
                new BuiltinMessage0104.ParameterItem(0x0084, ByteArrayContainer.ofU8(1))
        ));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0104);
        assertEquals("7e0104405001000000000139123443290000029a0700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013b7e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0104 entity = decodeAsEntity(BuiltinMessage0104.class, "0104405001000000000139123443290000029a0700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013b");

        assertEquals(666, entity.getFlowId());
        assertEquals(7, entity.getParameterCount());
        final List<BuiltinMessage0104.ParameterItem> parameterItems = entity.getParameterItems();
        assertEquals(111, parameterItems.getFirst().getParameterValue().asU32());
        assertEquals("13900001111", parameterItems.get(1).getParameterValue().asStringGbk());
        assertEquals("13900002222", parameterItems.get(2).getParameterValue().asStringGbk());
        assertEquals("13900003333", parameterItems.get(3).getParameterValue().asStringGbk());
        assertEquals(62, parameterItems.get(4).getParameterValue().asU16());
        assertEquals(103, parameterItems.get(5).getParameterValue().asU16());
        assertEquals(1, parameterItems.get(6).getParameterValue().asU8());
    }
}

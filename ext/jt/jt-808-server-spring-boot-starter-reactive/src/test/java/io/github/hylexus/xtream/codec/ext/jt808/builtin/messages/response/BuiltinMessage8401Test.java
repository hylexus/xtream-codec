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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8401Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8401 entity = new BuiltinMessage8401();
        entity.setType((short) 1);
        entity.setItemList(List.of(
                new BuiltinMessage8401.Item()
                        .setFlag((short) 3)
                        .setPhoneNumber("13911112222")
                        .setContacts("张三"),
                new BuiltinMessage8401.Item()
                        .setFlag((short) 2)
                        .setPhoneNumber("13911113333")
                        .setContacts("张三疯子"),
                new BuiltinMessage8401.Item()
                        .setFlag((short) 1)
                        .setPhoneNumber("13911116666")
                        .setContacts("李四")
        ));
        entity.setCount((short) entity.getItemList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8401403c010000000001391234432900000103030b313339313131313232323204d5c5c8fd020b313339313131313333333308d5c5c8fdb7e8d7d3010b313339313131313636363604c0eecbc4cc7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8401403c010000000001391234432900000103030b313339313131313232323204d5c5c8fd020b313339313131313333333308d5c5c8fdb7e8d7d3010b313339313131313636363604c0eecbc4cc";
        final BuiltinMessage8401 entity = decodeAsEntity(BuiltinMessage8401.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(3, entity.getCount());
        assertEquals(3, entity.getItemList().size());

        assertEquals(3, entity.getItemList().getFirst().getFlag());
        assertEquals("13911112222", entity.getItemList().getFirst().getPhoneNumber());
        assertEquals("张三", entity.getItemList().getFirst().getContacts());

        assertEquals(2, entity.getItemList().get(1).getFlag());
        assertEquals("13911113333", entity.getItemList().get(1).getPhoneNumber());
        assertEquals("张三疯子", entity.getItemList().get(1).getContacts());

        assertEquals(1, entity.getItemList().get(2).getFlag());
        assertEquals("13911116666", entity.getItemList().get(2).getPhoneNumber());
        assertEquals("李四", entity.getItemList().get(2).getContacts());
    }
}

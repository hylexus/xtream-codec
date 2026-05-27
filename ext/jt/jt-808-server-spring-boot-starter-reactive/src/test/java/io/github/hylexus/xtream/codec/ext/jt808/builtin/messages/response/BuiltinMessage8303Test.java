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

class BuiltinMessage8303Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8303 entity = new BuiltinMessage8303();
        entity.setType((short) 1);
        entity.setItemList(List.of(
                new BuiltinMessage8303.Item((short) 0x01, "测试 1"),
                new BuiltinMessage8303.Item((short) 0x02, "测试 2"),
                new BuiltinMessage8303.Item((short) 0x03, "测试 3")
        ));
        entity.setItemCount((short) entity.getItemList().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8303401d010000000001391234432900000103010006b2e2cad42031020006b2e2cad42032030006b2e2cad42033f27e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8303401d010000000001391234432900000103010006b2e2cad42031020006b2e2cad42032030006b2e2cad42033f2";
        final BuiltinMessage8303 entity = decodeAsEntity(BuiltinMessage8303.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(3, entity.getItemCount());
        assertEquals(3, entity.getItemList().size());
        assertEquals("测试 1", entity.getItemList().get(0).getContent());
        assertEquals("测试 2", entity.getItemList().get(1).getContent());
        assertEquals("测试 3", entity.getItemList().get(2).getContent());
    }

}

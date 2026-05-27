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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8300V2019Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8300V2019 entity = new BuiltinMessage8300V2019();
        entity.setIdentifier((short) 1);
        entity.setTextType((short) 1);
        entity.setText("HelloWorld 汉字 GBK 编码");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8300401a01000000000139123443290000010148656c6c6f576f726c6420babad7d62047424b20b1e0c2ebbb7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8300401a01000000000139123443290000010148656c6c6f576f726c6420babad7d62047424b20b1e0c2ebbb";
        final BuiltinMessage8300V2019 entity = decodeAsEntity(BuiltinMessage8300V2019.class, hex);
        assertEquals(1, entity.getIdentifier());
        assertEquals(1, entity.getTextType());
        assertEquals("HelloWorld 汉字 GBK 编码", entity.getText());
    }

}

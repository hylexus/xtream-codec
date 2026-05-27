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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8304Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8304 entity = new BuiltinMessage8304();
        entity.setType((short) 1);
        entity.setContent("测试...");
        entity.setLength(entity.getContent().getBytes(XtreamConstants.CHARSET_GBK).length);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e8304000a0139123443230000010007b2e2cad42e2e2e957e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8304000a0139123443230000010007b2e2cad42e2e2e95";
        final BuiltinMessage8304 entity = decodeAsEntity(BuiltinMessage8304.class, hex);
        assertEquals(1, entity.getType());
        assertEquals("测试...", entity.getContent());
        assertEquals(7, entity.getLength());
    }
}

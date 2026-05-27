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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0102V2013Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0102V2013 entity = new BuiltinMessage0102V2013();
        entity.setAuthenticationCode("这是鉴权码111");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013, 0x0102);
        assertEquals("7e0102000d0139123443230000d5e2cac7bcf8c8a8c2eb313131767e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0102V2013 entity = decodeAsEntity(BuiltinMessage0102V2013.class, "0102000d0139123443230000d5e2cac7bcf8c8a8c2eb31313176");
        assertEquals("这是鉴权码111", entity.getAuthenticationCode());
    }
}

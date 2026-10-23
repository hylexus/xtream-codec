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
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 终端鉴权（2011）的编解码测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage0102V2011Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0102V2011 entity = new BuiltinMessage0102V2011()
                .setAuthenticationCode("这是鉴权码111");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2011, terminalId2011, 0x0102);
        assertEquals("7e0102000d0139123443210000d5e2cac7bcf8c8a8c2eb313131747e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0102000d0139123443210000d5e2cac7bcf8c8a8c2eb31313174";
        final BuiltinMessage0102V2011 entity = decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0102V2011.class, hex);
        assertEquals("这是鉴权码111", entity.getAuthenticationCode());
    }

    @Test
    void testEscapedAuthenticationCode() {
        final BuiltinMessage0102V2011 entity = new BuiltinMessage0102V2011()
                .setAuthenticationCode("auth~}2011");
        final String expectedHex = "7e0102000a0139123443210000617574687d027d01323031317c7e";
        assertEquals(expectedHex, encode(entity, Jt808ProtocolVersion.VERSION_2011, terminalId2011, 0x0102));

        final BuiltinMessage0102V2011 decoded = decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0102V2011.class, expectedHex);
        assertEquals("auth~}2011", decoded.getAuthenticationCode());
    }

    @Test
    void testEmptyAuthenticationCode() {
        final BuiltinMessage0102V2011 entity = new BuiltinMessage0102V2011()
                .setAuthenticationCode("");
        final String expectedHex = "7e0102000001391234432100007f7e";
        assertEquals(expectedHex, encode(entity, Jt808ProtocolVersion.VERSION_2011, terminalId2011, 0x0102));

        final BuiltinMessage0102V2011 decoded = decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0102V2011.class, expectedHex);
        // 无剩余字节的字段按现有解码约定返回 null。
        assertNull(decoded.getAuthenticationCode());
    }
}

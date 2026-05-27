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

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0102V2019Test extends BaseCodecTest {

    @Test
    void testEncode() {

        final BuiltinMessage0102V2019 entity = new BuiltinMessage0102V2019();
        final String authCode = "这是鉴权码111";
        entity.setAuthenticationCode(authCode);
        entity.setImei("111111111111111");
        final byte[] bytes = XtreamBytes.appendSuffixIfNecessary(
                "1.2.3010".getBytes(XtreamConstants.CHARSET_GBK),
                20,
                (byte) 0x00
        );
        entity.setSoftwareVersion(new String(bytes, XtreamConstants.CHARSET_GBK));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0102);
        assertEquals("7e01024031010000000001391234432900000dd5e2cac7bcf8c8a8c2eb313131313131313131313131313131313131312e322e333031300000000000000000000000003c7e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0102V2019 entity = decodeAsEntity(BuiltinMessage0102V2019.class, "01024031010000000001391234432900000dd5e2cac7bcf8c8a8c2eb313131313131313131313131313131313131312e322e333031300000000000000000000000003c");

        final String authCode = "这是鉴权码111";
        assertEquals(authCode, entity.getAuthenticationCode());
        assertEquals("111111111111111", entity.getImei());
        assertEquals("1.2.3010", entity.getFormatedSoftwareVersion());
    }
}

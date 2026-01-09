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

package io.github.hylexus.xtream.debug.codec.core.demo01;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.debug.codec.core.BaseEntityCodecTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JtStyleDebugEntity01NestedTest extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final JtStyleDebugEntity01Nested originalEntity = this.createEntity();
        final String hexString = this.encodeAsHexString(originalEntity);

        final JtStyleDebugEntity01Nested entity = this.doDecode(JtStyleDebugEntity01Nested.class, hexString);

        assertEquals(originalEntity.getHeader().getMagicNumber(), entity.getHeader().getMagicNumber());
        assertEquals(originalEntity.getHeader().getMajorVersion(), entity.getHeader().getMajorVersion());
        assertEquals(originalEntity.getHeader().getMinorVersion(), entity.getHeader().getMinorVersion());
        assertEquals(originalEntity.getHeader().getMsgType(), entity.getHeader().getMsgType());
        assertEquals(originalEntity.getMsgBodyLength(), entity.getMsgBodyLength());
        assertEquals(originalEntity.getBody().getUsernameLength(), entity.getBody().getUsernameLength());
        assertEquals(originalEntity.getBody().getUsername(), entity.getBody().getUsername());
        assertEquals(originalEntity.getBody().getPasswordLength(), entity.getBody().getPasswordLength());
        assertEquals(originalEntity.getBody().getPassword(), entity.getBody().getPassword());
        assertEquals(originalEntity.getBody().getBirthday(), entity.getBody().getBirthday());
        assertEquals(originalEntity.getBody().getPhoneNumber(), entity.getBody().getPhoneNumber());
        assertEquals(originalEntity.getBody().getAge(), entity.getBody().getAge());
        assertEquals(originalEntity.getBody().getStatus(), entity.getBody().getStatus());
    }

    JtStyleDebugEntity01Nested createEntity() {
        final JtStyleDebugEntity01Nested entity = new JtStyleDebugEntity01Nested();
        final JtStyleDebugEntity01Nested.Body body = new JtStyleDebugEntity01Nested.Body();
        final JtStyleDebugEntity01Nested.Header header = new JtStyleDebugEntity01Nested.Header();

        entity.setBody(body);
        int msgBodyLength = 0;
        final String username = "张三丰 UTF-8 编码";
        body.setUsername(username);
        body.setUsernameLength(username.getBytes(StandardCharsets.UTF_8).length);
        msgBodyLength += 2 + body.getUsernameLength();

        final String password = "password-密码-BGK 编码";
        body.setPassword(password);
        body.setPasswordLength(password.getBytes(XtreamConstants.CHARSET_GBK).length);
        msgBodyLength += 2 + body.getPasswordLength();

        body.setBirthday("20240210");
        msgBodyLength += 8;

        body.setPhoneNumber("013900001111");
        msgBodyLength += 6;

        body.setAge(9999);
        msgBodyLength += 2;

        body.setStatus(288);
        msgBodyLength += 2;

        entity.setMsgBodyLength(msgBodyLength);

        entity.setHeader(header);
        header.setMagicNumber(0x80901234L);
        header.setMajorVersion((short) 2);
        header.setMinorVersion((short) 3);
        header.setMsgType(0x0007);
        return entity;
    }
}

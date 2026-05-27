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

package io.github.hylexus.xtream.debug.codec.core.demo01;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.debug.codec.core.BaseEntityCodecTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RustStyleDebugEntity01FlattenTest extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final RustStyleDebugEntity01Flatten originalEntity = this.createEntity();
        final String hexString = this.encodeAsHexString(originalEntity);

        final RustStyleDebugEntity01Flatten decodedEntity = this.doDecode(RustStyleDebugEntity01Flatten.class, hexString);
        assertEquals(originalEntity.getMagicNumber(), decodedEntity.getMagicNumber());
        assertEquals(originalEntity.getMajorVersion(), decodedEntity.getMajorVersion());
        assertEquals(originalEntity.getMinorVersion(), decodedEntity.getMinorVersion());
        assertEquals(originalEntity.getMsgType(), decodedEntity.getMsgType());
        assertEquals(originalEntity.getMsgBodyLength(), decodedEntity.getMsgBodyLength());

        assertEquals(originalEntity.getUsernameLength(), decodedEntity.getUsernameLength());
        assertEquals(originalEntity.getUsername(), decodedEntity.getUsername());
        assertEquals(originalEntity.getPasswordLength(), decodedEntity.getPasswordLength());
        assertEquals(originalEntity.getPassword(), decodedEntity.getPassword());
        assertEquals(originalEntity.getBirthday(), decodedEntity.getBirthday());
        assertEquals(originalEntity.getPhoneNumber(), decodedEntity.getPhoneNumber());
        assertEquals(originalEntity.getAge(), decodedEntity.getAge());
        assertEquals(originalEntity.getStatus(), decodedEntity.getStatus());
    }

    RustStyleDebugEntity01Flatten createEntity() {
        final RustStyleDebugEntity01Flatten entity = new RustStyleDebugEntity01Flatten();
        // region header
        entity.setMagicNumber(0x80901234);
        entity.setMajorVersion((short) 1);
        entity.setMinorVersion((short) 2);
        entity.setMsgType(0x0007);
        // endregion header

        // region body
        int msgBodyLength = 0;

        final String username = "xtream-codec.用户名";
        entity.setUsername(username);
        entity.setUsernameLength(username.getBytes(StandardCharsets.UTF_8).length);
        msgBodyLength += entity.getUsernameLength() + 2;

        final String password = "xtream-codec.密码";
        entity.setPassword(password);
        entity.setPasswordLength(password.getBytes(XtreamConstants.CHARSET_GBK).length);
        msgBodyLength += entity.getPasswordLength() + 2;

        entity.setBirthday("20210203");
        msgBodyLength += 8;

        entity.setPhoneNumber("013911112222");
        msgBodyLength += 6;

        entity.setAge(9999);
        msgBodyLength += 2;

        entity.setStatus((short) -100);
        msgBodyLength += 2;

        entity.setMsgBodyLength(msgBodyLength);
        // endregion body
        return entity;
    }
}

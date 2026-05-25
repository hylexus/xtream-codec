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

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.debug.codec.core.BaseEntityCodecTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RustStyleDebugEntity01NestedSimpleTest extends BaseEntityCodecTest {
    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final RustStyleDebugEntity01NestedSimple originalEntity = this.createEntity();
        final String hexString = this.encodeAsHexString(originalEntity);

        final RustStyleDebugEntity01NestedSimple entity = this.doDecode(RustStyleDebugEntity01NestedSimple.class, hexString);

        assertEquals(originalEntity.getHeader().getMagicNumber(), entity.getHeader().getMagicNumber());
        assertEquals(originalEntity.getHeader().getMajorVersion(), entity.getHeader().getMajorVersion());
        assertEquals(originalEntity.getHeader().getMinorVersion(), entity.getHeader().getMinorVersion());
        assertEquals(originalEntity.getHeader().getMsgType(), entity.getHeader().getMsgType());
        assertEquals(originalEntity.getBody().getUsername(), entity.getBody().getUsername());
        assertEquals(originalEntity.getBody().getPassword(), entity.getBody().getPassword());
        assertEquals(originalEntity.getBody().getBirthday(), entity.getBody().getBirthday());
        assertEquals(originalEntity.getBody().getPhoneNumber(), entity.getBody().getPhoneNumber());
        assertEquals(originalEntity.getBody().getAge(), entity.getBody().getAge());
        assertEquals(originalEntity.getBody().getStatus(), entity.getBody().getStatus());
    }

    RustStyleDebugEntity01NestedSimple createEntity() {
        final RustStyleDebugEntity01NestedSimple entity = new RustStyleDebugEntity01NestedSimple();
        final RustStyleDebugEntity01NestedSimple.Body body = new RustStyleDebugEntity01NestedSimple.Body();
        final RustStyleDebugEntity01NestedSimple.Header header = new RustStyleDebugEntity01NestedSimple.Header();

        entity.setBody(body);
        body.setUsername("张三丰 UTF-8 编码");
        body.setPassword("password-密码-BGK 编码");
        body.setBirthday("20240210");
        body.setPhoneNumber("013900001111");
        body.setAge(9999);
        body.setStatus((short) -100);

        entity.setHeader(header);
        header.setMagicNumber(0x80901234);
        header.setMajorVersion((short) 2);
        header.setMinorVersion((short) 3);
        header.setMsgType(0x0007);
        return entity;
    }
}

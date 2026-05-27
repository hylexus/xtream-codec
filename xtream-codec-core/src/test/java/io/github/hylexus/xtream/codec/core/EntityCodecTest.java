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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityCodecTest extends BaseEntityCodecTest {

    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity()
                .setId(100L)
                .setName("无名氏")
                .setAge(1024)
                .setAddress("保密");
    }

    @Test
    void testVersion2() {
        doCodecTest(2, userEntity, (source, hexString, decoded) -> {
            doCompare(source, decoded);
            assertEquals("0000006406cedec3fbcacf040006e4bf9de5af86", hexString);
            assertTrue(hexString.contains(hexStringForGbk(userEntity.getName())));
        }, false);

        doCodecTest(2, userEntity, (source, hexString, decoded) -> {
            doCompare(source, decoded);
            assertTrue(hexString.contains(hexStringForGbk(userEntity.getName())));
            assertEquals("0000006406cedec3fbcacf040006e4bf9de5af86", hexString);
        }, true);
    }

    @Test
    void testVersion1() {
        doCodecTest(1, userEntity, (source, hexString, decoded) -> {
            doCompare(userEntity, decoded);
            assertTrue(hexString.contains(hexStringForGbk(userEntity.getName())));
            assertEquals("0000006406cedec3fbcacf040006e4bf9de5af86", hexString);
        }, false);
        doCodecTest(1, userEntity, (source, hexString, decoded) -> {
            doCompare(userEntity, decoded);
            assertTrue(hexString.contains(hexStringForGbk(userEntity.getName())));
            assertEquals("0000006406cedec3fbcacf040006e4bf9de5af86", hexString);
        }, true);
    }

    @Test
    void testVersionDefault() {
        doCodecTest(XtreamField.ALL_VERSION, userEntity, (source, hexString, decoded) -> {
            doCompare(source, decoded);
            assertTrue(hexString.contains(hexStringForUtf8(userEntity.getName())));
            assertEquals("0000006409e697a0e5908de6b08f040006e4bf9de5af86", hexString);
        }, false);

        doCodecTest(XtreamField.ALL_VERSION, userEntity, (source, hexString, decoded) -> {
            doCompare(source, decoded);
            assertTrue(hexString.contains(hexStringForUtf8(userEntity.getName())));
            assertEquals("0000006409e697a0e5908de6b08f040006e4bf9de5af86", hexString);
        }, true);
    }

    private void doCompare(UserEntity expected, UserEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAge(), actual.getAge());
        assertEquals(expected.getAddress(), actual.getAddress());
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class UserEntity {

        @Preset.RustStyle.u32(desc = "用户ID(32位无符号数1)")
        private Long id;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "用户名(UTF-8)")
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "用户名(GBK)", version = {1, 2}, charset = XtreamConstants.CHARSET_NAME_GBK)
        private String name;

        @Preset.RustStyle.u16(desc = "年龄(16位无符号数)")
        private Integer age;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "地址")
        private String address;

    }
}

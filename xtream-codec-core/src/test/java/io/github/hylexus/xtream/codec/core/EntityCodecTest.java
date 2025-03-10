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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityCodecTest {

    EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void testEncode() {
        final UserEntity userEntity = new UserEntity()
                .setId(100L)
                .setName("无名氏")
                .setAge(1024)
                .setAddress("保密");
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            this.entityCodec.encode(userEntity, buffer);
            final String hexString = FormatUtils.toHexString(buffer);
            assertEquals("0000006409e697a0e5908de6b08f040006e4bf9de5af86", hexString);
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    @Test
    void testDecode() {
        final ByteBuf buffer = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, "0000006409e697a0e5908de6b08f040006e4bf9de5af86");
        try {
            final UserEntity entity = this.entityCodec.decode(UserEntity.class, buffer);
            assertEquals(100L, entity.getId());
            assertEquals("无名氏", entity.getName());
            assertEquals(1024, entity.getAge());
            assertEquals("保密", entity.getAddress());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class UserEntity {

        @Preset.RustStyle.u32(desc = "用户ID(32位无符号数)")
        private Long id;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "用户名")
        private String name;

        @Preset.RustStyle.u16(desc = "年龄(16位无符号数)")
        private Integer age;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "地址")
        private String address;

    }
}

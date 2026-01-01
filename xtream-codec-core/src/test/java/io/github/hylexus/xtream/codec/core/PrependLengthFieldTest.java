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
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class PrependLengthFieldTest {

    private final EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void testWithoutPrependLengthField() {
        final TestEntityWithoutPrependLengthField rawEntity = new TestEntityWithoutPrependLengthField()
                .setId(1L)
                .setUsername("张三")
                .setPassword("<PASSWORD>")
                .setAge(1L);

        // 这里 ”不得不“ 手动计算长度
        rawEntity.setUsernameLength((short) rawEntity.getUsername().getBytes(StandardCharsets.UTF_8).length);
        rawEntity.setPasswordLength((short) rawEntity.getPassword().getBytes(StandardCharsets.UTF_8).length);

        ByteBuf encoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf decoderBuffer = null;
        try {
            this.entityCodec.encode(rawEntity, encoderBuffer);
            final String encodedHexString = FormatUtils.toHexString(encoderBuffer);
            decoderBuffer = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, encodedHexString);
            final TestEntityWithoutPrependLengthField decodedEntity = this.entityCodec.decode(TestEntityWithoutPrependLengthField.class, decoderBuffer);
            Assertions.assertNotNull(decodedEntity);
            Assertions.assertEquals(rawEntity.getId(), decodedEntity.getId());
            Assertions.assertEquals(rawEntity.getUsernameLength(), decodedEntity.getUsernameLength());
            Assertions.assertEquals(rawEntity.getUsername(), decodedEntity.getUsername());
            Assertions.assertEquals(rawEntity.getPasswordLength(), decodedEntity.getPasswordLength());
            Assertions.assertEquals(rawEntity.getPassword(), decodedEntity.getPassword());

        } finally {
            encoderBuffer.release();
            if (decoderBuffer != null) {
                decoderBuffer.release();
                Assertions.assertEquals(0, decoderBuffer.refCnt());
            }
            Assertions.assertEquals(0, encoderBuffer.refCnt());
        }
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class TestEntityWithoutPrependLengthField {
        @Preset.RustStyle.u32
        private Long id;

        // 先解码出 username 的长度字段
        @Preset.RustStyle.u8
        private Short usernameLength;

        // @Preset.RustStyle.str(lengthExpression = "getUsernameLength()")
        @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getUsernameLength()", mvel = "self.getUsernameLength()", aviator = "self.usernameLength"))
        private String username;

        // 先解码出 password 的长度字段
        @Preset.RustStyle.u8
        private Short passwordLength;

        // @Preset.RustStyle.str(lengthExpression = "getPasswordLength()")
        @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getPasswordLength()", mvel = "self.getPasswordLength()", aviator = "self.passwordLength"))
        private String password;

        @Preset.RustStyle.u32
        private Long age;

        public Short getUsernameLength() {
            return usernameLength;
        }

        public Short getPasswordLength() {
            return passwordLength;
        }
    }

    @Test
    void testWithPrependLengthField() {
        final TestEntityWithPrependLengthField rawEntity = new TestEntityWithPrependLengthField()
                .setId(1L)
                .setUsername("张三")
                .setPassword("<PASSWORD>")
                .setAge(0L);

        // 这里 不用 手动计算长度（前置长度字段已经自动处理了）
        // rawEntity.setUsernameLength((short) rawEntity.getUsername().getBytes(StandardCharsets.UTF_8).length);
        // rawEntity.setPasswordLength((short) rawEntity.getPassword().getBytes(StandardCharsets.UTF_8).length);

        ByteBuf encoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf decoderBuffer = null;
        try {
            this.entityCodec.encode(rawEntity, encoderBuffer);
            final String encodedHexString = FormatUtils.toHexString(encoderBuffer);
            decoderBuffer = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, encodedHexString);
            final TestEntityWithPrependLengthField decodedEntity = this.entityCodec.decode(TestEntityWithPrependLengthField.class, decoderBuffer);
            Assertions.assertNotNull(decodedEntity);
            Assertions.assertEquals(rawEntity.getId(), decodedEntity.getId());
            Assertions.assertEquals(rawEntity.getUsername(), decodedEntity.getUsername());
            Assertions.assertEquals(rawEntity.getPassword(), decodedEntity.getPassword());
            Assertions.assertEquals(rawEntity.getAge(), decodedEntity.getAge());
        } finally {
            encoderBuffer.release();
            Assertions.assertEquals(0, encoderBuffer.refCnt());
            if (decoderBuffer != null) {
                decoderBuffer.release();
                Assertions.assertEquals(0, decoderBuffer.refCnt());
            }
        }
    }


    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class TestEntityWithPrependLengthField {
        @Preset.RustStyle.u32
        private Long id;

        // username 的长度字段被 prependLengthFieldType = PrependLengthFieldType.u8 自动处理了
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
        private String username;

        // password 的长度字段被 prependLengthFieldType = PrependLengthFieldType.u8 自动处理了
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
        private String password;

        @Preset.RustStyle.u32
        private Long age;
    }
}

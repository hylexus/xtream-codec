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

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.annotation.Padding;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringFieldPaddingTest {

    EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void testPaddingUtf8() {
        final TestUtf8StringPaddingEntity rawEntity = new TestUtf8StringPaddingEntity()
                .setId(123L)
                // 15 bytes (UTF8)
                .setStrFiled1("哈哈@1a!😁@")
                // 15 bytes (UTF8)
                .setStrFiled2("呵呵@2a!😁@");
        final ByteBuf encoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        final ByteBuf decoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            this.entityCodec.encode(rawEntity, encoderBuffer);
            final String hexString = FormatUtils.toHexString(encoderBuffer);
            XtreamBytes.byteBufFromHexString(decoderBuffer, hexString);
            final TestUtf8StringPaddingEntity decodedEntity = this.entityCodec.decode(TestUtf8StringPaddingEntity.class, decoderBuffer);
            assertNotNull(decodedEntity);
            assertEquals(rawEntity.getId(), decodedEntity.getId());
            // 左填充 5 个 "1"(即 0x31，也即十进制的 49)
            assertEquals("1".repeat(5) + rawEntity.getStrFiled1(), decodedEntity.getStrFiled1());
            // 右填充 5 个 "1"(即 0x31，也即十进制的 49)
            assertEquals(rawEntity.getStrFiled2() + "1".repeat(5), decodedEntity.getStrFiled2());
        } finally {
            encoderBuffer.release();
            assertEquals(0, encoderBuffer.refCnt());
            decoderBuffer.release();
            assertEquals(0, decoderBuffer.refCnt());
        }
    }


    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class TestUtf8StringPaddingEntity {

        @Preset.RustStyle.u32(desc = "用户ID(32位无符号数)")
        private Long id;

        @Preset.RustStyle.str(
                prependLengthFieldType = PrependLengthFieldType.u8,
                desc = "strFiled1...",
                // 如果编码后长度不够 20 字节，头部(左边)填充0x31(49)直到长度为20字节
                paddingLeft = @Padding(minEncodedLength = 20, paddingElement = 49)
        )
        private String strFiled1;

        @Preset.RustStyle.str(
                prependLengthFieldType = PrependLengthFieldType.u8,
                desc = "strFiled2...",
                // 如果编码后长度不够 20 字节，尾部(右边)用填充0x31(49)直到长度为20字节
                paddingRight = @Padding(minEncodedLength = 20, paddingElement = 49)
        )
        private String strFiled2;

    }

    @Test
    void testPaddingGbk() {
        final TestGbkStringPaddingEntity rawEntity = new TestGbkStringPaddingEntity()
                .setId(123L)
                // 15 bytes (UTF8)
                .setStrFiled1("哈哈@1a!！@繁體")
                // 15 bytes (UTF8)
                .setStrFiled2("哈哈@1a!！@繁體");
        final ByteBuf encoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        final ByteBuf decoderBuffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            this.entityCodec.encode(rawEntity, encoderBuffer);
            final String hexString = FormatUtils.toHexString(encoderBuffer);
            XtreamBytes.byteBufFromHexString(decoderBuffer, hexString);
            final TestGbkStringPaddingEntity decodedEntity = this.entityCodec.decode(TestGbkStringPaddingEntity.class, decoderBuffer);
            assertNotNull(decodedEntity);
            assertEquals(rawEntity.getId(), decodedEntity.getId());
            // 左填充 5 个 "1"(即 0x31，也即十进制的 49)
            assertEquals("1".repeat(5) + rawEntity.getStrFiled1(), decodedEntity.getStrFiled1());
            // 右填充 5 个 "1"(即 0x31，也即十进制的 49)
            assertEquals(rawEntity.getStrFiled2() + "1".repeat(5), decodedEntity.getStrFiled2());
        } finally {
            encoderBuffer.release();
            assertEquals(0, encoderBuffer.refCnt());
            decoderBuffer.release();
            assertEquals(0, decoderBuffer.refCnt());
        }
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class TestGbkStringPaddingEntity {

        @Preset.RustStyle.u32(desc = "用户ID(32位无符号数)")
        private Long id;

        @Preset.JtStyle.Str(
                prependLengthFieldType = PrependLengthFieldType.u8,
                desc = "strFiled1...",
                // 如果编码后长度不够 20 字节，头部(左边)填充0x31(49)直到长度为20字节
                paddingLeft = @Padding(minEncodedLength = 20, paddingElement = 49)
        )
        private String strFiled1;

        @Preset.JtStyle.Str(
                prependLengthFieldType = PrependLengthFieldType.u8,
                desc = "strFiled2...",
                // 如果编码后长度不够 20 字节，尾部(右边)用填充0x31(49)直到长度为20字节
                paddingRight = @Padding(minEncodedLength = 20, paddingElement = 49)
        )
        private String strFiled2;

    }

}

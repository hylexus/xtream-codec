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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteArrayContainerTest {

    @Test
    void test() {
        doWithByteBuf(byteBuf -> {
            final byte v = byteBuf.writeByte(Short.MAX_VALUE + 1).readByte();
            assertEquals(v, ByteArrayContainer.ofI8(Short.MAX_VALUE + 1).asI8());
        });
        doWithByteBuf(byteBuf -> {
            final short v = byteBuf.writeByte(Short.MAX_VALUE + 1).readUnsignedByte();
            assertEquals(v, ByteArrayContainer.ofU8(Short.MAX_VALUE + 1).asU8());
            assertEquals(v, ByteArrayContainer.ofU8(Short.MAX_VALUE + 1).asByte());
        });

        doWithByteBuf(byteBuf -> {
            final short v = byteBuf.writeShort(Short.MAX_VALUE + 1).readShort();
            assertEquals(v, ByteArrayContainer.ofI16(Short.MAX_VALUE + 1).asI16());
        });
        doWithByteBuf(byteBuf -> {
            final int v = byteBuf.writeShort(Short.MAX_VALUE + 10).readUnsignedShort();
            assertEquals(v, ByteArrayContainer.ofU16(Short.MAX_VALUE + 10).asU16());
            assertEquals(v, ByteArrayContainer.ofU16(Short.MAX_VALUE + 10).asWord());
        });

        doWithByteBuf(byteBuf -> {
            final int v2 = byteBuf.writeInt(Integer.MAX_VALUE - 1).readInt();
            assertEquals(v2, ByteArrayContainer.ofI32(Integer.MAX_VALUE - 1).asI32());
        });
        doWithByteBuf(byteBuf -> {
            final long v2 = byteBuf.writeInt(Integer.MAX_VALUE - 10).readUnsignedInt();
            assertEquals(v2, ByteArrayContainer.ofU32(Integer.MAX_VALUE - 10).asU32());
            assertEquals(v2, ByteArrayContainer.ofU32(Integer.MAX_VALUE - 10).asDword());
        });

        assertEquals("1234567890", ByteArrayContainer.ofBcd("1234567890").asBcd());

        final String utf8 = ByteArrayContainer.ofString("HelloWorld UTF 编码", StandardCharsets.UTF_8).asString(StandardCharsets.UTF_8);
        assertEquals("HelloWorld UTF 编码", utf8);
        final String utf81 = ByteArrayContainer.ofString("HelloWorld UTF 编码").asString(StandardCharsets.UTF_8);
        assertEquals("HelloWorld UTF 编码", utf81);
        final String utf82 = ByteArrayContainer.ofString("HelloWorld UTF 编码", StandardCharsets.UTF_8).asString();
        assertEquals("HelloWorld UTF 编码", utf82);

        final Charset gbk = XtreamConstants.CHARSET_GBK;
        assertEquals("HelloWorld GBK 编码", ByteArrayContainer.ofString("HelloWorld GBK 编码", gbk).asString(gbk));
    }

    void doWithByteBuf(Consumer<ByteBuf> bufConsumer) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            bufConsumer.accept(buffer);
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}

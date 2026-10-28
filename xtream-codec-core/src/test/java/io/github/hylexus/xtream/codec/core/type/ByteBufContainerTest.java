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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ByteBufContainerTest {

    static Stream<Arguments> factories() {
        return Stream.of(
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofBytes(new byte[]{1, 2}), "0102"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI8(-1), "ff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU8(255), "ff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI16(-2), "fffe"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU16(65535), "ffff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofI32(Integer.MIN_VALUE), "80000000"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofU32(-1), "ffffffff"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofBcd("12345"), "012345"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofString("中"), "e4b8ad"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofString("A", StandardCharsets.UTF_16BE), "0041"),
                Arguments.of((Supplier<ByteBufContainer>) () -> ByteBufContainer.ofStringGbk("中"), "d6d0")
        );
    }

    @ParameterizedTest
    @MethodSource("factories")
    void shouldCreateExpectedPayloadAndReleaseIt(Supplier<ByteBufContainer> factory, String expected) {
        final ByteBuf payload = factory.get().payload();
        try {
            assertEquals(expected, ByteBufUtil.hexDump(payload));
        } finally {
            payload.release();
        }
        assertEquals(0, payload.refCnt());
    }

    @Test
    void shouldExposeSignedAndUnsignedViewsWithoutConsumingPayload() {
        final ByteBuf payload = Unpooled.wrappedBuffer(new byte[]{-1, -1, -1, -1});
        final ByteBufContainer container = ByteBufContainer.ofBytes(payload);
        try {
            assertSame(payload, container.payload());
            assertEquals(4, container.length());
            assertArrayEquals(new byte[]{-1, -1, -1, -1}, container.asBytes());
            assertEquals(-1, container.asI8());
            assertEquals(255, container.asU8());
            assertEquals(255, container.asByte());
            assertEquals(-1, container.asI16());
            assertEquals(65535, container.asU16());
            assertEquals(65535, container.asWord());
            assertEquals(-1, container.asI32());
            assertEquals(4294967295L, container.asU32());
            assertEquals(4294967295L, container.asDword());
            assertEquals(0, payload.readerIndex());
        } finally {
            container.release();
        }
        assertEquals(0, payload.refCnt());
    }

    @Test
    void shouldDecodeTextAndBcdWithoutConsumingPayload() {
        final ByteBuf utf8 = Unpooled.wrappedBuffer(new byte[]{(byte) 0xe4, (byte) 0xb8, (byte) 0xad});
        final ByteBuf gbk = Unpooled.wrappedBuffer(new byte[]{(byte) 0xd6, (byte) 0xd0});
        final ByteBuf bcd = Unpooled.wrappedBuffer(new byte[]{0x12, 0x34});
        try {
            assertEquals("中", ByteBufContainer.ofBytes(utf8).asString());
            assertEquals("中", ByteBufContainer.ofBytes(utf8).asStringUtf8());
            assertEquals("中", ByteBufContainer.ofBytes(gbk).asStringGbk());
            assertEquals("中", ByteBufContainer.ofBytes(gbk).asString(XtreamConstants.CHARSET_GBK));
            assertEquals("1234", ByteBufContainer.ofBytes(bcd).asBcd());
            assertEquals(0, utf8.readerIndex());
            assertEquals(0, gbk.readerIndex());
            assertEquals(0, bcd.readerIndex());
        } finally {
            utf8.release();
            gbk.release();
            bcd.release();
        }
    }

    @Test
    void shouldWriteOnlyReadablePayloadAndConsumeIt() {
        final ByteBuf payload = Unpooled.wrappedBuffer(new byte[]{99, 1, 2});
        final ByteBuf output = Unpooled.buffer().writeByte(7);
        final ByteBufContainer container = ByteBufContainer.ofBytes(payload);
        try {
            payload.readByte();
            container.writeTo(output);
            assertEquals("070102", ByteBufUtil.hexDump(output));
            assertEquals(0, container.length());
        } finally {
            container.release();
            output.release();
        }
    }
}

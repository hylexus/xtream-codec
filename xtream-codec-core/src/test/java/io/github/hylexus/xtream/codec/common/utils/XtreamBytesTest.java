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

package io.github.hylexus.xtream.codec.common.utils;

import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.type.PaddingConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XtreamBytesTest {

    @Test
    void shouldDecodeMixedCaseHexAndAppendToExistingBuffer() {
        assertArrayEquals(new byte[]{0, 10, (byte) 0xbc, (byte) 0xff}, XtreamBytes.decodeHex("000aBcFF"));
        assertArrayEquals(new byte[0], XtreamBytes.decodeHex(""));
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            assertSame(buffer, XtreamBytes.byteBufFromHexString(buffer, "  000aBcFF  "));
            assertSame(buffer, XtreamBytes.writeHexString(buffer, " aB "));
            assertEquals("55000abcffab", ByteBufUtil.hexDump(buffer));
            assertEquals(0, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldLeaveBufferUnchangedForEmptyHex(String hex) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(1);
        try {
            assertSame(buffer, XtreamBytes.byteBufFromHexString(buffer, hex));
            assertSame(buffer, XtreamBytes.writeHexString(buffer, hex));
            assertEquals("01", ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "123", "g0", "0g"})
    void shouldRejectMalformedHex(String hex) {
        assertThrows(IllegalArgumentException.class, () -> XtreamBytes.decodeHex(hex));
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(IllegalArgumentException.class, () -> XtreamBytes.byteBufFromHexString(buffer, hex));
            assertThrows(IllegalArgumentException.class, () -> XtreamBytes.writeHexString(buffer, hex));
            assertEquals(0, buffer.writerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldReportInvalidHexCharacterPosition() {
        assertThrows(IllegalArgumentException.class, () -> XtreamBytes.decodeHex((char[]) null));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> XtreamBytes.decodeHex("00x0"))
                .getMessage().contains("位置: 2"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> XtreamBytes.decodeHex("000x"))
                .getMessage().contains("位置: 3"));
    }

    @Test
    void shouldDistinguishPeekingFromConsumingBytes() {
        final ByteBuf buffer = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, "ff12345678");
        try {
            assertEquals("FF12345678", XtreamBytes.encodeHex(buffer));
            assertEquals("FF12345678", XtreamBytes.encodeHex(buffer, 0, 5));
            assertEquals(0x1234, XtreamBytes.getWord(buffer, 1));
            assertEquals("1234", XtreamBytes.getBcd(buffer, 1, 2));
            assertEquals(0, buffer.readerIndex());
            assertEquals(255, XtreamBytes.readU8(buffer));
            assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, XtreamBytes.getBytes(buffer));
            assertEquals(1, buffer.readerIndex());
            assertEquals(0x1234, XtreamBytes.readU16(buffer));
            assertArrayEquals(new byte[]{0x56, 0x78}, XtreamBytes.readBytes(buffer, 2));
            assertEquals(5, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldWriteAndReadWordsBcdAndStrings() {
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertSame(buffer, XtreamBytes.writeWord(buffer, 0xffff));
            assertSame(buffer, XtreamBytes.writeBcd(buffer, "1234"));
            assertEquals("ffff1234", ByteBufUtil.hexDump(buffer));
            assertEquals(65535, XtreamBytes.readU16(buffer));
            assertEquals("1234", XtreamBytes.readBcd(buffer, 2));
            buffer.clear().writeCharSequence("AB", StandardCharsets.US_ASCII);
            assertEquals("AB", XtreamBytes.getString(buffer, 2, StandardCharsets.US_ASCII));
            assertEquals(0, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "NONE, 4, 551234", ", 4, 551234",
            "LEFT, 1, 551234", "LEFT, 2, 551234", "LEFT, 4, 557e7e1234",
            "RIGHT, 1, 551234", "RIGHT, 2, 551234", "RIGHT, 4, 5512347e7e"
    })
    void shouldPadHexAndBcdByEncodedByteLength(PaddingType type, int length, String expected) {
        final PaddingConfig padding = new PaddingConfig(type, (byte) 0x7e, length);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0x55);
            XtreamBytes.writeHexString(buffer, "1234", padding);
            assertEquals(expected, ByteBufUtil.hexDump(buffer));
            buffer.clear().writeByte(0x55);
            XtreamBytes.writeBcd8421(buffer, "1234", padding);
            assertEquals(expected, ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "NONE, 5, 55e4b8ad", ", 5, 55e4b8ad",
            "LEFT, 2, 55e4b8ad", "LEFT, 3, 55e4b8ad", "LEFT, 5, 557e7ee4b8ad",
            "RIGHT, 2, 55e4b8ad", "RIGHT, 3, 55e4b8ad", "RIGHT, 5, 55e4b8ad7e7e"
    })
    void shouldPadMultibyteTextWithoutOverwritingExistingOutput(PaddingType type, int length, String expected) {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0x55);
        try {
            XtreamBytes.writeCharSequence(buffer, "中", StandardCharsets.UTF_8,
                    new PaddingConfig(type, (byte) 0x7e, length));
            assertEquals(expected, ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldPadArraysOnlyWhenNecessaryAndTrimTrailingBytes() {
        final byte[] bytes = {1, 2};
        assertSame(bytes, XtreamBytes.prefixZero(bytes, 1));
        assertSame(bytes, XtreamBytes.prefixZero(bytes, 2));
        assertArrayEquals(new byte[]{0, 0, 1, 2}, XtreamBytes.prefixZero(bytes, 4));
        assertSame(bytes, XtreamBytes.appendSuffixIfNecessary(bytes, 1, (byte) 9));
        assertSame(bytes, XtreamBytes.appendSuffixIfNecessary(bytes, 2, (byte) 9));
        assertArrayEquals(new byte[]{1, 2, 9, 9}, XtreamBytes.appendSuffixIfNecessary(bytes, 4, (byte) 9));
        assertEquals("a0b", XtreamBytes.trimTailing("a0b00", (byte) '0'));
        assertEquals("", XtreamBytes.trimTailing("000", (byte) '0'));
        assertEquals("", XtreamBytes.trimTailing("", (byte) '0'));
        assertEquals("abc", XtreamBytes.trimTailing("abc", (byte) '0'));
        assertEquals(3, XtreamBytes.gbkByteCount("中A"));
    }

    @Test
    void shouldReleaseCollectionsSafelyAndRespectRetainedReferences() {
        final ByteBuf first = Unpooled.buffer();
        final ByteBuf retained = Unpooled.buffer().retain();
        try {
            XtreamBytes.releaseBuf(null);
            XtreamBytes.releaseBufList(null);
            XtreamBytes.releaseBufList(List.of());
            XtreamBytes.releaseBufList(Arrays.asList(first, null, retained));
            assertEquals(0, first.refCnt());
            assertEquals(1, retained.refCnt());
            XtreamBytes.releaseBuf(first);
            XtreamBytes.releaseBuf(retained);
            assertEquals(0, retained.refCnt());
        } finally {
            XtreamBytes.releaseBuf(first);
            XtreamBytes.releaseBuf(retained);
        }
    }
}

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BitStreamReaderTest {

    @Test
    void shouldReadMostSignificantBitsFirstAcrossByteBoundaries() {
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) 0xb3, 0x5a, (byte) 0xff});
        try (BitStreamReader reader = BitStreamReader.fromByteBuf(buffer)) {
            assertEquals(1, reader.readU1());
            assertEquals(1, reader.readU2());
            assertEquals(4, reader.readU3());
            assertEquals(0x35, reader.readU(6));
            assertEquals(0xaf, reader.readU8());
            assertEquals(15, reader.readBit(4));
            assertEquals(0, buffer.readerIndex());
        }
        assertEquals(0, buffer.refCnt());
    }

    @Test
    void shouldSupportFixedWidthConvenienceMethods() {
        try (BitStreamReader reader = BitStreamReader.fromHexString("ffffffff")) {
            assertEquals(31, reader.readU5());
            assertEquals(63, reader.readU6());
            assertEquals(65535, reader.readU16());
            assertEquals(31, reader.readBit(5));
        }
    }

    @Test
    void shouldHandleZeroBitsAndFullIntegerWidth() {
        try (BitStreamReader reader = BitStreamReader.fromHexString("80000001")) {
            assertEquals(0, reader.readBit(0));
            assertEquals(0x80000001, reader.readBit(32));
        }
    }

    @ParameterizedTest
    @CsvSource({"80, 0", "40, 1", "60, 2", "20, 3", "28, 4", "18, 11"})
    void shouldDecodeUnsignedExponentialGolombNumbers(String hex, int expected) {
        try (BitStreamReader reader = BitStreamReader.fromHexString(hex)) {
            assertEquals(expected, reader.readUe());
        }
    }

    @ParameterizedTest
    @CsvSource({"80, 0", "40, 1", "60, -1", "20, 2", "28, -2", "18, 6"})
    void shouldDecodeSignedExponentialGolombNumbers(String hex, int expected) {
        try (BitStreamReader reader = BitStreamReader.fromHexString(hex)) {
            assertEquals(expected, reader.readSe());
        }
    }

    @Test
    void shouldAllowRepeatedRelease() {
        final ByteBuf buffer = Unpooled.buffer().writeByte(0);
        final BitStreamReader reader = BitStreamReader.fromByteBuf(buffer);
        try {
            reader.release();
            reader.close();
            assertEquals(0, buffer.refCnt());
        } finally {
            XtreamBytes.releaseBuf(buffer);
        }
    }
}

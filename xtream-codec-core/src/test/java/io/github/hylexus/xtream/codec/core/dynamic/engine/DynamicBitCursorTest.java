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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证批量字节读取与共享字节位段读取保持同一位映射语义。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicBitCursorTest {
    @Test
    void shouldReadIntegersAcrossByteOrdersAndBitOffsets() {
        final byte[] data = new byte[]{0, (byte) 0x96, 0x21, (byte) 0xe5, 0x43,
                (byte) 0xb8, 0x09, (byte) 0xfe, 0x75, 0x36, (byte) 0xa1};
        for (final boolean little : new boolean[]{false, true}) {
            for (final boolean lsb : new boolean[]{false, true}) {
                for (final int offset : new int[]{0, 1, 3, 7}) {
                    for (int width = 1; width <= 64; width++) {
                        if (little && width % 8 != 0) {
                            continue;
                        }
                        final ByteBuf buffer = Unpooled.wrappedBuffer(data);
                        try {
                            buffer.readerIndex(1);
                            final DynamicBitReader cursor = new DynamicBitReader(buffer, DynamicLimits.DEFAULT);
                            if (offset != 0) {
                                cursor.readInteger(offset, false, lsb, cursor.limit(), "$.prefix");
                            }
                            final BigInteger actual = cursor.readInteger(width, little, lsb, cursor.limit(), "$.value");
                            assertEquals(referenceValue(data, 1, offset, width, little, lsb), actual,
                                    "offset=" + offset + ", width=" + width + ", little=" + little + ", lsb=" + lsb);
                            assertEquals(offset + width, cursor.position());
                            assertEquals(1, buffer.readerIndex());
                        } finally {
                            buffer.release();
                        }
                    }
                }
            }
        }
    }

    @Test
    void shouldWriteIntegersAcrossByteOrdersAndBitOffsets() {
        final BigInteger source = new BigInteger("fedcba9876543210", 16);
        for (final boolean little : new boolean[]{false, true}) {
            for (final boolean lsb : new boolean[]{false, true}) {
                for (final int offset : new int[]{0, 1, 3, 7}) {
                    for (int width = 1; width <= 64; width++) {
                        if (little && width % 8 != 0) {
                            continue;
                        }
                        final BigInteger value = source.and(BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE));
                        final ByteBuf buffer = Unpooled.buffer();
                        try {
                            buffer.writeByte(0x7e);
                            final DynamicBitWriter writer = new DynamicBitWriter(buffer, DynamicLimits.DEFAULT);
                            if (offset != 0) {
                                writer.writeInteger(BigInteger.ONE.shiftLeft(offset).subtract(BigInteger.ONE),
                                        offset, false, lsb, writer.limit(), "$.prefix");
                            }
                            writer.writeInteger(value, width, little, lsb, writer.limit(), "$.value");
                            final byte[] actual = new byte[buffer.readableBytes()];
                            buffer.getBytes(0, actual);
                            assertArrayEquals(referenceBytes(value, offset, width, little, lsb), actual,
                                    "offset=" + offset + ", width=" + width + ", little=" + little + ", lsb=" + lsb);
                            assertEquals(offset + width, writer.position());
                        } finally {
                            buffer.release();
                        }
                    }
                }
            }
        }
    }

    @Test
    void shouldPatchUnalignedFieldWithoutChangingNeighboringBits() {
        for (final boolean lsb : new boolean[]{false, true}) {
            final ByteBuf buffer = Unpooled.buffer();
            try {
                final DynamicBitWriter writer = new DynamicBitWriter(buffer, DynamicLimits.DEFAULT);
                writer.writeInteger(BigInteger.valueOf(5), 3, false, lsb, writer.limit(), "$.prefix");
                writer.writeInteger(BigInteger.ZERO, 10, false, lsb, writer.limit(), "$.length");
                writer.writeInteger(BigInteger.valueOf(3), 3, false, lsb, writer.limit(), "$.suffix");
                writer.patchInteger(3, BigInteger.valueOf(0x155), 10, false, lsb, "$.length");
                assertEquals(16, writer.position());

                final DynamicBitReader reader = new DynamicBitReader(buffer, DynamicLimits.DEFAULT);
                assertEquals(BigInteger.valueOf(5), reader.readInteger(3, false, lsb, reader.limit(), "$.prefix"));
                assertEquals(BigInteger.valueOf(0x155), reader.readInteger(10, false, lsb, reader.limit(), "$.length"));
                assertEquals(BigInteger.valueOf(3), reader.readInteger(3, false, lsb, reader.limit(), "$.suffix"));
                assertEquals(16, reader.position());
            } finally {
                buffer.release();
            }
        }
    }

    private static byte[] referenceBytes(BigInteger value, int offset, int width, boolean little, boolean lsb) {
        final byte[] bytes = new byte[1 + (offset + width + 7) / 8];
        bytes[0] = 0x7e;
        for (int index = 0; index < offset + width; index++) {
            final int fieldIndex = index - offset;
            final int valueBit = little
                    ? fieldIndex / 8 * 8 + (lsb ? fieldIndex % 8 : 7 - fieldIndex % 8)
                    : lsb ? fieldIndex : width - 1 - fieldIndex;
            if (index < offset || value.testBit(valueBit)) {
                bytes[1 + index / 8] |= (byte) (1 << (lsb ? index % 8 : 7 - index % 8));
            }
        }
        return bytes;
    }

    private static BigInteger referenceValue(byte[] data, int start, int offset, int width,
                                             boolean little, boolean lsb) {
        // 独立按原始 bit 规则构造期望值，覆盖完整字节与跨字节位段的相同物理布局。
        BigInteger result = BigInteger.ZERO;
        for (int index = 0; index < width; index++) {
            final int position = offset + index;
            final int octet = Byte.toUnsignedInt(data[start + position / 8]);
            final int physicalBit = lsb ? position % 8 : 7 - position % 8;
            if (((octet >>> physicalBit) & 1) != 0) {
                final int valueBit = little ? index / 8 * 8 + (lsb ? index % 8 : 7 - index % 8)
                        : lsb ? index : width - 1 - index;
                result = result.setBit(valueBit);
            }
        }
        return result;
    }
}

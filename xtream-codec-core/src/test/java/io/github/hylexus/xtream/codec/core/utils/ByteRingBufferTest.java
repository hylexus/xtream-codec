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

package io.github.hylexus.xtream.codec.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ByteRingBufferTest {

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 3, 6, Integer.MAX_VALUE})
    void shouldRejectCapacityThatIsNotAPositivePowerOfTwo(int capacity) {
        assertTrue(ByteRingBuffer.isInvalidCapacity(capacity));
        assertThrows(IllegalArgumentException.class, () -> new ByteRingBuffer(capacity));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 1024})
    void shouldAcceptPowerOfTwoAndStartEmpty(int capacity) {
        final ByteRingBuffer buffer = new ByteRingBuffer(capacity);
        assertFalse(ByteRingBuffer.isInvalidCapacity(capacity));
        assertEquals(0, buffer.readableBytes());
        assertEquals(0, buffer.readerIndex());
        assertEquals(0, buffer.writerIndex());
        assertArrayEquals(new byte[0], buffer.peek(0, 0));
    }

    @Test
    void shouldPreserveOrderAcrossWraparoundAndUseAbsoluteIndices() {
        final ByteRingBuffer buffer = new ByteRingBuffer(4);
        buffer.writeBytes(new byte[]{10, 20, 30});
        buffer.discard(2);
        buffer.writeBytes(new byte[]{40, 50, 60});

        assertEquals(2, buffer.readerIndex());
        assertEquals(6, buffer.writerIndex());
        assertEquals(4, buffer.readableBytes());
        assertArrayEquals(new byte[]{30, 40, 50, 60}, buffer.peek(0, 4));
        assertArrayEquals(new byte[]{40, 50}, buffer.peek(1, 2));
        assertEquals(30, buffer.getByte(2));
        assertEquals(60, buffer.getByte(5));
        assertEquals(4, buffer.readableBytes());

        buffer.discard(4);
        buffer.writeBytes(new byte[]{70});
        assertEquals(70, buffer.getByte(6));
        assertArrayEquals(new byte[]{70}, buffer.peek(0, 1));
    }

    @Test
    void shouldCopyOnlyReadableBytesWithoutConsumingSource() {
        final ByteRingBuffer buffer = new ByteRingBuffer(4);
        final ByteBuf source = Unpooled.wrappedBuffer(new byte[]{99, 1, 2, 3});
        try {
            source.readByte();
            buffer.writeBytes(source);
            assertArrayEquals(new byte[]{1, 2, 3}, buffer.peek(0, 3));
            assertEquals(1, source.readerIndex());
            assertEquals(4, source.writerIndex());
        } finally {
            source.release();
        }
    }

    @Test
    void shouldRejectOverflowWithoutChangingBufferedBytes() {
        final ByteRingBuffer buffer = new ByteRingBuffer(2);
        buffer.writeBytes(new byte[]{1});
        assertThrows(IllegalStateException.class, () -> buffer.writeBytes(new byte[]{2, 3}));
        final ByteBuf source = Unpooled.wrappedBuffer(new byte[]{2, 3});
        try {
            assertThrows(IllegalStateException.class, () -> buffer.writeBytes(source));
            assertEquals(0, source.readerIndex());
            assertEquals(1, buffer.writerIndex());
            assertArrayEquals(new byte[]{1}, buffer.peek(0, 1));
            buffer.writeBytes(new byte[0]);
            buffer.writeBytes(new byte[]{2});
            assertArrayEquals(new byte[]{1, 2}, buffer.peek(0, 2));
        } finally {
            source.release();
        }
    }

    @Test
    void shouldRejectReadsOutsideReadableRange() {
        final ByteRingBuffer buffer = new ByteRingBuffer(4);
        buffer.writeBytes(new byte[]{1, 2, 3});
        buffer.discard(1);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getByte(0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.getByte(3));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.peek(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.peek(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.peek(1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.discard(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.discard(3));
        assertArrayEquals(new byte[0], buffer.peek(2, 0));
        buffer.discard(0);
        assertEquals(1, buffer.readerIndex());
        assertEquals(2, buffer.readableBytes());
    }
}

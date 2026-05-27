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

public class ByteRingBuffer {
    private final byte[] buffer;
    private final int capacity;
    private final int mask;
    private int readerIndex = 0;
    private int writerIndex = 0;

    public static boolean isInvalidCapacity(int capacity) {
        return capacity <= 0 || (capacity & (capacity - 1)) != 0;
    }

    public ByteRingBuffer(int capacity) {
        if (isInvalidCapacity(capacity)) {
            throw new IllegalArgumentException("Capacity must be a power of 2 and > 0");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new byte[capacity];
    }

    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    public void writeBytes(ByteBuf byteBuf) {
        final int length = byteBuf.readableBytes();
        if (length > this.capacity - readableBytes()) {
            throw new IllegalStateException("Buffer overflow");
        }
        byteBuf.forEachByte(b -> {
            buffer[writerIndex & mask] = b;
            writerIndex++;
            return true;
        });
    }

    public void writeBytes(byte[] src) {
        if (src.length > capacity - readableBytes()) {
            throw new IllegalStateException("Buffer overflow");
        }
        for (byte b : src) {
            buffer[writerIndex & mask] = b;
            writerIndex++;
        }
    }

    public byte getByte(int absoluteIndex) {
        if (absoluteIndex < readerIndex || absoluteIndex >= writerIndex) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[absoluteIndex & mask];
    }

    public byte[] peek(int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        byte[] dst = new byte[length];
        int startPos = (readerIndex + offset) & mask;
        int firstPart = Math.min(length, capacity - startPos);
        System.arraycopy(buffer, startPos, dst, 0, firstPart);
        if (length > firstPart) {
            System.arraycopy(buffer, 0, dst, firstPart, length - firstPart);
        }
        return dst;
    }

    public void discard(int length) {
        if (length < 0 || length > readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        readerIndex += length;
    }

    public int readerIndex() {
        return readerIndex;
    }

    public int writerIndex() {
        return writerIndex;
    }

}

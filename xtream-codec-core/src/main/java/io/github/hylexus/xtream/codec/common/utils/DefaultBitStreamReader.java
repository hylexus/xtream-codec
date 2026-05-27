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

public class DefaultBitStreamReader implements BitStreamReader {

    private final ByteBuf buffer;

    // [0, byteBuf.readableBytes()]
    private int byteOffset;

    // [0-7]
    private int bitOffset;

    public DefaultBitStreamReader(ByteBuf buffer) {
        this.buffer = buffer;
        this.byteOffset = 0;
        this.bitOffset = 7;
    }

    @Override
    public int readBit() {
        final int result = (buffer.getByte(byteOffset) >> (bitOffset--)) & 0b01;
        if (bitOffset == -1) {
            byteOffset++;
            bitOffset = 7;
        }
        return result;
    }

    @Override
    public int readBit(int len) {
        // Assertions.range(len, 1, Integer.SIZE, "len >= 1 && len < " + Integer.SIZE);
        int result = 0;
        for (int i = 0; i < len; i++) {
            result |= (readBit() << (len - i - 1));
        }
        return result;
    }

    @Override
    public void release() {
        if (this.buffer != null) {
            XtreamBytes.releaseBuf(this.buffer);
        }
    }
}

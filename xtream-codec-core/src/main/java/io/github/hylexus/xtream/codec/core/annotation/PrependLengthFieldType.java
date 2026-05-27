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

package io.github.hylexus.xtream.codec.core.annotation;

import io.netty.buffer.ByteBuf;

public enum PrependLengthFieldType {
    none(-1) {
        @Override
        public int readFrom(ByteBuf buffer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeTo(ByteBuf buffer, int value) {
            throw new UnsupportedOperationException();
        }
    },
    u8(1) {
        @Override
        public int readFrom(ByteBuf buffer) {
            return buffer.readUnsignedByte();
        }

        @Override
        public void writeTo(ByteBuf buffer, int value) {
            buffer.writeByte(value);
        }
    },
    u16(2) {
        @Override
        public int readFrom(ByteBuf buffer) {
            return buffer.readUnsignedShort();
        }

        @Override
        public void writeTo(ByteBuf buffer, int value) {
            buffer.writeShort(value);
        }
    },
    u32(4) {
        @Override
        public int readFrom(ByteBuf buffer) {
            return (int) buffer.readUnsignedInt();
        }

        @Override
        public void writeTo(ByteBuf buffer, int value) {
            buffer.writeInt(value);
        }
    },
    ;
    final int byteCounts;

    PrependLengthFieldType(int byteCounts) {
        this.byteCounts = byteCounts;
    }

    public abstract int readFrom(ByteBuf buffer);

    public abstract void writeTo(ByteBuf buffer, int value);

    @SuppressWarnings("lombok")
    public int getByteCounts() {
        return byteCounts;
    }

    public static PrependLengthFieldType from(int byteCounts) {
        return switch (byteCounts) {
            case 1 -> PrependLengthFieldType.u8;
            case 2 -> PrependLengthFieldType.u16;
            case 4 -> PrependLengthFieldType.u32;
            default -> PrependLengthFieldType.none;
        };
    }
}

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

import java.math.BigInteger;

/**
 * 单次解码使用的位读取器。整数与字节读取只推进逻辑位置，不立即移动 readerIndex。
 * 整个报文解码成功后由执行器调用 {@link #commit()}，失败时保留调用方原来的读取位置。
 * 借用调用方的 ByteBuf，其释放仍由调用方负责。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicBitReader extends DynamicBitCursor {
    private final ByteBuf buffer;

    DynamicBitReader(ByteBuf buffer, DynamicLimits limits) {
        super(buffer.readerIndex(), 8L * buffer.readableBytes(), "输入不足或超过容器边界");
        this.buffer = buffer;
        if (buffer.readableBytes() > limits.maxInputBytes()) {
            throw new DynamicCodecException("$", 0, "输入超过资源上限");
        }
    }

    BigInteger readInteger(int bits, boolean little, boolean lsb, long boundary, String path) {
        final long value = readLong(bits, little, lsb, boundary, path);
        // u64 的最高位为 1 时，long 会显示为负数：先清除符号位，以非负值构造 BigInteger，再恢复第 63 位。
        // 例如 long 的全 1 位模式（-1）转为无符号值 2^64 - 1，而不是对负数直接转换。
        return bits == 64 && value < 0
                ? BigInteger.valueOf(value & Long.MAX_VALUE).setBit(63)
                : BigInteger.valueOf(value);
    }

    long readLong(int bits, boolean little, boolean lsb, long boundary, String path) {
        bitOrder(lsb, path);
        checkBoundary(bits, boundary, path);
        if (position % 8 == 0 && bits % 8 == 0) {
            // 整字节路径须与逐段拼接一致：小端或 lsb0 都先处理数值低字节，故选择 little || lsb。
            return readWholeBytes(bits / 8, little || lsb);
        }
        return readBitChunks(bits, little, lsb);
    }

    private long readWholeBytes(int count, boolean leastSignificantByteFirst) {
        final int index = start + (int) (position / 8);
        position += 8L * count;
        // Netty 直接读取常见整数宽度；5～7 字节的非标准宽度按协议字节序组合。
        // getLong 的负值仍保留完整的 64 位模式；有符号解释或 u64 转换由调用方完成。
        return switch (count) {
            case 1 -> buffer.getUnsignedByte(index);
            case 2 -> leastSignificantByteFirst ? buffer.getUnsignedShortLE(index) : buffer.getUnsignedShort(index);
            case 3 -> leastSignificantByteFirst ? buffer.getUnsignedMediumLE(index) : buffer.getUnsignedMedium(index);
            case 4 -> leastSignificantByteFirst ? buffer.getUnsignedIntLE(index) : buffer.getUnsignedInt(index);
            case 8 -> leastSignificantByteFirst ? buffer.getLongLE(index) : buffer.getLong(index);
            default -> {
                long result = 0;
                for (int offset = 0; offset < count; offset++) {
                    final int byteIndex = leastSignificantByteFirst ? count - 1 - offset : offset;
                    // 按数值的高字节到低字节拼接：左移 8 位为新字节腾出低位，再用 OR 合并其 0..255 值。
                    // 例如先读 0x12，再读 0x34，得到 (0x12 << 8) | 0x34 = 0x1234。
                    result = (result << 8) | buffer.getUnsignedByte(index + byteIndex);
                }
                yield result;
            }
        };
    }

    private long readBitChunks(int bits, boolean little, boolean lsb) {
        // 每次取当前字节内的一段；小端字段还要在数值字节边界处分段。
        long result = 0;
        for (int consumed = 0; consumed < bits; ) {
            final int index = start + (int) (position / 8);
            final int byteOffset = (int) (position % 8);
            final int count = Math.min(
                    bits - consumed,
                    Math.min(8 - byteOffset, little ? (8 - (consumed % 8)) : 8)
            );
            // lsb0 从字节低位开始取，msb0 从高位开始取；这里计算位段最低位在物理字节中的位置。
            final int physicalShift = lsb ? byteOffset : 8 - byteOffset - count;
            // (1 << count) - 1 生成低 count 位全 1 的掩码，例如 count=3 时为 0b111。
            final int mask = (1 << count) - 1;
            // 无符号右移把目标段移到低位，再用 AND 丢弃同一字节中的其他位。
            final int chunk = (buffer.getUnsignedByte(index) >>> physicalShift) & mask;
            // 将提取段移到数值中的对应位置，再用 OR 拼接；各段占用不同的数值位。
            // 先转 long 再左移，避免 int 的 32 位移位规则截断高位。
            result |= (long) chunk << valueShift(consumed, count, bits, little, lsb);
            consumed += count;
            position += count;
        }
        return result;
    }

    byte[] readBytes(int size, long boundary, String path) {
        checkAligned(path);
        checkBoundary(8L * size, boundary, path);
        final byte[] bytes = new byte[size];
        buffer.getBytes(start + (int) (position / 8), bytes);
        position += 8L * size;
        return bytes;
    }

    /**
     * 解码全部成功后才提交读取位置。
     */
    void commit() {
        buffer.readerIndex(start + consumedBytes());
    }
}

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
 * 单次编码使用的位写入器。短位字段共享字节，字节字段按完整字节写入。
 * 长度回填只覆盖预留范围，完成后恢复后续字段的写入位置。
 * 借用调用方的 ByteBuf，其释放仍由调用方负责。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicBitWriter extends DynamicBitCursor {
    private final ByteBuf buffer;

    DynamicBitWriter(ByteBuf buffer, DynamicLimits limits) {
        super(buffer.writerIndex(), 8L * limits.maxOutputBytes(), "输出超过边界或资源上限");
        this.buffer = buffer;
    }

    void writeInteger(BigInteger value, int bits, boolean little, boolean lsb, long boundary, String path) {
        // 上层已检查位宽；longValue 保留低 64 位原始模式，u64 最高位为 1 时即使表现为负数也可写出。
        writeLong(value.longValue(), bits, little, lsb, boundary, path);
    }

    void writeLong(long value, int bits, boolean little, boolean lsb, long boundary, String path) {
        bitOrder(lsb, path);
        checkBoundary(bits, boundary, path);
        if (position % 8 == 0 && bits % 8 == 0) {
            // 整字节路径须与逐段提取一致：小端或 lsb0 都先处理数值低字节，故选择 little || lsb。
            writeWholeBytes(value, bits / 8, little || lsb);
        } else {
            writeBitChunks(value, bits, little, lsb);
        }
    }

    void patchInteger(long at, BigInteger value, int bits, boolean little, boolean lsb, String path) {
        // 回填暂时进入先前字段的位置和位序，结束后恢复当前共享字节的完整游标状态。
        final long saved = position;
        final boolean savedLsb = lastLsb;
        try {
            position = at;
            lastLsb = lsb;
            writeInteger(value, bits, little, lsb, limit(), path);
        } finally {
            position = saved;
            lastLsb = savedLsb;
        }
    }

    void writeBytes(byte[] bytes, long boundary, String path) {
        checkAligned(path);
        checkBoundary(8L * bytes.length, boundary, path);
        buffer.ensureWritable(bytes.length);
        buffer.setBytes(start + (int) (position / 8), bytes);
        position += 8L * bytes.length;
        buffer.writerIndex(start + (int) (position / 8));
    }

    private void writeWholeBytes(long value, int count, boolean leastSignificantByteFirst) {
        final int index = start + (int) (position / 8);
        final int end = index + count;
        buffer.ensureWritable(Math.max(0, end - buffer.writerIndex()));
        switch (count) {
            case 1 -> buffer.setByte(index, (int) value);
            case 2 -> {
                if (leastSignificantByteFirst) {
                    buffer.setShortLE(index, (int) value);
                } else {
                    buffer.setShort(index, (int) value);
                }
            }
            case 4 -> {
                if (leastSignificantByteFirst) {
                    buffer.setIntLE(index, (int) value);
                } else {
                    buffer.setInt(index, (int) value);
                }
            }
            case 8 -> {
                if (leastSignificantByteFirst) {
                    buffer.setLongLE(index, value);
                } else {
                    buffer.setLong(index, value);
                }
            }
            default -> {
                for (int offset = 0; offset < count; offset++) {
                    final int valueByte = leastSignificantByteFirst ? offset : count - 1 - offset;
                    // >>> 将指定数值字节移到低 8 位且始终补零；setByte 只写低 8 位，原值为负也不改变位模式。
                    buffer.setByte(index + offset, (int) (value >>> (8 * valueByte)));
                }
            }
        }
        position += 8L * count;
        buffer.writerIndex(Math.max(buffer.writerIndex(), end));
    }

    private void writeBitChunks(long value, int bits, boolean little, boolean lsb) {
        // 每段只更新一次当前字节；已有字节保留其他字段占用的位。
        for (int consumed = 0; consumed < bits; ) {
            final int index = start + (int) (position / 8);
            final int byteOffset = (int) (position % 8);
            final int count = Math.min(bits - consumed, Math.min(8 - byteOffset,
                    little ? 8 - consumed % 8 : 8));
            // lsb0 从字节低位向高位写，msb0 从高位向低位写；偏移指向当前段的物理最低位。
            final int physicalShift = lsb ? byteOffset : 8 - byteOffset - count;
            // 先生成 count 个低位 1，再移到目标段；例如 count=3、physicalShift=2 时掩码为 0b00011100。
            final int mask = ((1 << count) - 1) << physicalShift;
            // >>> 将要写入的数值段移到低位；暂时保留的其他位会在写入时被 mask 去掉。
            final int chunk = (int) (value >>> valueShift(consumed, count, bits, little, lsb));
            if (index == buffer.writerIndex()) {
                // 首次写入共享字节时清零，后续字段只覆盖自己的位段。
                buffer.writeByte(0);
            }
            // 旧字节 & ~mask 只清空目标段；新段左移后 & mask 只保留目标段，最后 OR 合并，避免覆盖相邻字段。
            buffer.setByte(index, (buffer.getUnsignedByte(index) & ~mask) | ((chunk << physicalShift) & mask));
            consumed += count;
            position += count;
        }
    }
}

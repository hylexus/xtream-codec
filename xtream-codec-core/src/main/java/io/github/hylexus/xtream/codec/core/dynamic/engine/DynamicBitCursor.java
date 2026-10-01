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

/**
 * 位游标的公共位置规则：bit 偏移、边界、对齐和共享字节内的位序约束。
 * 具体读取由 {@link DynamicBitReader} 完成，写入及长度回填由 {@link DynamicBitWriter} 完成。
 * 此类不访问 ByteBuf，也不保存编解码方向。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
abstract class DynamicBitCursor {
    protected final int start;
    private final long limit;
    private final String boundaryError;
    protected long position;
    // 同一字节内切换位序会让前后字段对物理 bit 的解释互相冲突。
    protected boolean lastLsb;

    DynamicBitCursor(int start, long limit, String boundaryError) {
        this.start = start;
        this.limit = limit;
        this.boundaryError = boundaryError;
    }

    long position() {
        return position;
    }

    long limit() {
        return limit;
    }

    void checkBoundary(long bits, long boundary, String path) {
        // 先减后比较，避免 position + bits 在不可信长度下溢出。
        if (bits < 0 || position > Math.min(limit, boundary) - bits) {
            throw new DynamicCodecException(path, position, boundaryError);
        }
    }

    void checkAligned(String path) {
        if (position % 8 != 0) {
            throw new DynamicCodecException(path, position, "整字节字段之前需要显式 align");
        }
    }

    int consumedBytes() {
        // 对不足整字节的尾部向上取整，供 ByteBuf 索引和响应指标使用。
        return (int) ((position + 7) / 8);
    }

    final void bitOrder(boolean lsb, String path) {
        if (position % 8 != 0 && lastLsb != lsb) {
            throw new DynamicCodecException(path, position, "共享字节内不能切换 bitOrder");
        }
        lastLsb = lsb;
    }

    /**
     * 计算当前物理位段对应的数值最低位偏移，供读取时左移拼接、写入时右移提取。
     *
     * @param consumed  已经处理的字段位数
     * @param chunkBits 当前段位数，已限制为不跨物理字节；小端字段也不跨数值字节
     * @param bits      字段总位宽
     * @param little    是否按小端排列数值字节
     * @param lsb       是否按 lsb0 遍历字节内的位段
     * @return 当前段在数值中的最低位下标
     */
    static int valueShift(int consumed, int chunkBits, int bits, boolean little, boolean lsb) {
        if (little) {
            // consumed / 8 * 8 定位低字节在前的数值字节；余数定位该字节内已处理的位数。
            // lsb0 直接使用余数，msb0 则从字节高端扣除已处理位数和当前段宽度。
            // 例如 consumed=8、chunkBits=3 时，lsb0 偏移为 8，msb0 偏移为 8 + 5 = 13。
            return consumed / 8 * 8 + (lsb ? consumed % 8 : 8 - consumed % 8 - chunkBits);
        }
        // 非小端字段：lsb0 从整个数值的低位向上拼接，msb0 从总位宽的高端向下拼接。
        // 例如 16 位字段的首个 8 位段，lsb0 偏移为 0，msb0 偏移为 16 - 0 - 8 = 8。
        return lsb ? consumed : bits - consumed - chunkBits;
    }
}

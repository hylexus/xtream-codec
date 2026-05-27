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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.impl;

import io.github.hylexus.xtream.codec.core.utils.ByteRingBuffer;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264Decoder;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264Nalu;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264NaluHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public class DefaultH264Decoder implements H264Decoder {

    private final ByteRingBuffer ringBuffer;

    public DefaultH264Decoder(int capacity) {
        this.ringBuffer = new ByteRingBuffer(capacity);
    }

    @Override
    public List<H264Nalu> decode(ByteBuf input) {
        this.ringBuffer.writeBytes(input);

        int readerIndex = ringBuffer.readerIndex();
        final int writerIndex = ringBuffer.writerIndex();

        final List<H264Nalu> nalus = new ArrayList<>();

        // 当前扫描位置
        int i = readerIndex;

        // 上一个 StartCode 的位置
        int lastStartCodePos = -1;
        int lastStartCodeLength = 0;

        while (i < writerIndex - 3) {
            final int startCodeLength = getStartCodeLength(i);

            if (startCodeLength > 0) {
                if (lastStartCodePos != -1) {
                    // 提取上一个 NALU（不含当前 StartCode）
                    final int naluStart = lastStartCodePos + lastStartCodeLength;
                    final int naluEnd = i;

                    if (naluEnd > naluStart) {
                        final byte[] naluData = ringBuffer.peek(naluStart - readerIndex, naluEnd - naluStart);
                        nalus.add(createH264Nalu(Unpooled.wrappedBuffer(naluData)));

                        // 移动 readerIndex 到当前 StartCode 前
                        ringBuffer.discard(naluEnd - readerIndex);
                        readerIndex = ringBuffer.readerIndex(); // 更新 readerIndex
                        i = readerIndex; // 重置扫描起点
                    }
                }

                // 记录当前 StartCode 信息
                lastStartCodePos = i;
                lastStartCodeLength = startCodeLength;

                i += startCodeLength; // 跳过 StartCode
            } else {
                i++;
            }
        }

        return nalus;
    }

    /**
     * 检查当前位置是否是 StartCode
     */
    private int getStartCodeLength(int pos) {
        final byte b0 = ringBuffer.getByte(pos);
        final byte b1 = ringBuffer.getByte(pos + 1);
        final byte b2 = ringBuffer.getByte(pos + 2);

        // 0x00,0x00,0x01
        if (b0 == 0 && b1 == 0 && b2 == 1) {
            return 3;
        }

        if (pos + 3 < ringBuffer.writerIndex()) {
            final byte b3 = ringBuffer.getByte(pos + 3);
            // 0x00,0x00,0x00,0x01
            if (b0 == 0 && b1 == 0 && b2 == 0 && b3 == 1) {
                return 4;
            }
        }

        return 0;
    }

    protected H264Nalu createH264Nalu(ByteBuf data) {
        final H264NaluHeader h264NaluHeader = H264NaluHeader.of(data.getByte(0));
        return new DefaultH264Nalu(
                h264NaluHeader,
                data.slice(0, data.readableBytes())
        );
    }

}

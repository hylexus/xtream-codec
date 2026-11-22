/*
 * Copyright [2019] [matrixy]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Symphonia
 * Copyright (c) 2019-2022 The Project Symphonia Developers.
 * Modifications Copyright 2024-present the xtream-codec authors.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.adpcm;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioFormatOptions;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.AbstractJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;


/**
 * IMA(Interactive Multimedia Association) ADPCM 解码算法实现。
 * <p>
 * 从以下来源适配为 JT1078 Java 解码器；Symphonia 派生文件适用 MPL-2.0。
 * 许可、修改范围及尚待核实的来源见仓库 THIRD-PARTY-SOURCE-NOTICES.txt。
 * 对应 Java 源码随模块 JAR 的 META-INF/third-party-sources/adpcm/ 分发。
 * <ol>
 *     <li><a href="https://github.com/pdeljanov/Symphonia/blob/b157bb58826a2035405702f481b6fdcbd922cbf3/symphonia-codec-adpcm/src/codec_ima.rs#L65">symphonia-codec-adpcm/src/codec_ima.rs#L65(Rust 版本的音频库)</a></li>
 *     <li><a href="https://gitee.com/matrixy/jtt1078-video-server/blob/flv/src/main/java/cn/org/hentai/jtt1078/codec/ADPCMCodec.java#L181">cn.org.hentai.jtt1078.codec.ADPCMCodec</a></li>
 * </ol>
 * Microchip 应用笔记保留为历史技术参考；其原 C 示例和未调用的移植方法已移除。
 *
 * @see <a href="https://wiki.multimedia.cx/index.php/IMA_ADPCM">https://wiki.multimedia.cx/index.php/IMA_ADPCM</a>
 * @see <a href="https://github.com/pdeljanov/Symphonia.git">https://github.com/pdeljanov/Symphonia.git(Rust 版本的音频库)</a>
 * @see <a href="https://www.cs.columbia.edu/~hgs/audio/dvi/IMA_ADPCM.pdf">https://www.cs.columbia.edu/~hgs/audio/dvi/IMA_ADPCM.pdf</a>
 * @see <a href="https://ww1.microchip.com/downloads/en/AppNotes/00643b.pdf">https://ww1.microchip.com/downloads/en/AppNotes/00643b.pdf</a>
 * @see <a href="https://www.hentai.org.cn/article?id=8">https://www.hentai.org.cn/article?id=8</a>
 * @author Codex (AI)
 */
public class AdpcmImaJt1078AudioCodec extends AbstractJt1078AudioCodec {

    // 不要深究这两个 TABLE 的含义(模数转换): 经验值 + 统计学 + 听觉实验 + 压缩效率 + 调优(人工微调) + ... 得出的 “最佳实践”
    private static final int[] IMA_INDEX_TABLE = {
            -1, -1, -1, -1, 2, 4, 6, 8,
            -1, -1, -1, -1, 2, 4, 6, 8
    };

    private static final int[] IMA_STEP_TABLE = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };
    // private static final Logger log = LoggerFactory.getLogger(AdpcmImaJt1078AudioCodec.class);
    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    public AdpcmImaJt1078AudioCodec() {
    }

    @Override
    protected AudioPackage doDecodeAsPcm(ByteBuf stream, @Nullable AudioFormatOptions options) {
        final AdpcmImaBlockStatus status = this.readPreamble(stream);

        final ByteBuf buffer = this.allocator.buffer();
        try {
            // buffer.writeShortLE(status.predictor);
            while (stream.isReadable()) {
                final short nibbles = stream.readUnsignedByte();
                buffer.writeShortLE(this.expandNibble(status, (byte) ((nibbles >> 4) & 0x0F)));
                buffer.writeShortLE(this.expandNibble(status, (byte) (nibbles & 0x0F)));
            }
        } catch (Throwable e) {
            XtreamBytes.releaseBuf(buffer);
            throw e;
        }

        return new AudioPackage(BuiltinAudioFormatOptions.PCM_S16_LE_MONO, buffer);
    }

    // 前 4 个字节
    private AdpcmImaBlockStatus readPreamble(ByteBuf stream) {
        // 2 bytes
        final short predictor = stream.readShortLE();
        // 1 byte
        final byte stepIndex = stream.readByte();
        if (stepIndex > 88) {
            throw new IllegalStateException("adpcm (ima): invalid step index : " + stepIndex);
        }
        // reserved 1 byte
        stream.readByte();
        return new AdpcmImaBlockStatus(predictor, stepIndex);
    }

    /**
     * 参考 Rust 音频库的算法：<a href="https://github.com/pdeljanov/Symphonia/blob/b157bb58826a2035405702f481b6fdcbd922cbf3/symphonia-codec-adpcm/src/codec_ima.rs#L65">symphonia-codec-adpcm/src/codec_ima.rs#L65</a>
     *
     * @see <a href="https://github.com/pdeljanov/Symphonia/blob/b157bb58826a2035405702f481b6fdcbd922cbf3/symphonia-codec-adpcm/src/codec_ima.rs#L65">https://github.com/pdeljanov/Symphonia/blob/b157bb58826a2035405702f481b6fdcbd922cbf3/symphonia-codec-adpcm/src/codec_ima.rs#L65</a>
     */
    private short expandNibble(AdpcmImaBlockStatus status, short nibble) {
        final int step = IMA_STEP_TABLE[status.stepIndex];

        final boolean sign = (nibble & 0x08) > 0;
        final int delta = (nibble & 0x07);
        final int diff = ((2 * delta + 1) * step) >> 3;
        final int predictor = sign ? status.predictor - diff : status.predictor + diff;

        status.predictor = this.ensurePredictorRange(predictor);
        status.stepIndex = this.ensureIndexCapacity(status.stepIndex + IMA_INDEX_TABLE[nibble]);

        return status.predictor;
    }

    /**
     * 确保索引在 {@link #IMA_STEP_TABLE} 的索引范围内。
     *
     * @return index ∈ [0, {@link #IMA_STEP_TABLE IMA_STEP_TABLE.length - 1}] 即 [0, 88]
     */
    byte ensureIndexCapacity(int index) {
        if (index < 0) {
            return 0;
        } else {
            return (byte) Math.min(index, 88);
        }
    }

    short ensurePredictorRange(int predictor) {
        return (short) predictor;
    }

    static class AdpcmImaBlockStatus {
        short predictor;
        byte stepIndex;

        public AdpcmImaBlockStatus(short predictor, byte stepIndex) {
            this.predictor = predictor;
            this.stepIndex = stepIndex;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AdpcmImaBlockStatus.class.getSimpleName() + "[", "]")
                    .add("predictor=" + predictor)
                    .add("stepIndex=" + stepIndex)
                    .toString();
        }
    }

}

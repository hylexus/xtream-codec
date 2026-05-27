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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio;

public interface AudioFormatOptions {
    /**
     * 获取音频的采样率（单位：Hz）
     * <p>
     * 示例：
     * - 8000 Hz：常用于语音通信
     * - 44100 Hz：CD 音质标准
     *
     * @return 音频采样率
     */
    int sampleRate();

    /**
     * 获取音频样本的位深（单位：bit）
     * <p>
     * 示例：
     * - 16 bit：常见于 PCM 编码
     * - 8 bit：常见于 G.711 编码
     * <p>
     * 注意：这是解码后的 PCM 数据精度，非压缩格式可能与此值一致。
     *
     * @return 每个音频样本的位数
     */
    int bitDepth();

    /**
     * 获取音频的声道数量
     * <p>
     * 示例：
     * - 1：单声道（Mono）
     * - 2：立体声（Stereo）
     *
     * @return 声道数
     */
    int channelCount();

    /**
     * 获取压缩编码中每个采样点所占用的比特数（单位：bit）
     * <p>
     * 示例：
     * - G.726：2 ~ 5 bits/sample
     * - G.711：8 bits/sample
     * - PCM：与 bitDepth 相同（无压缩）
     *
     * @return 每个采样点在压缩数据中的比特数
     */
    int encodedBitsPerSample();

    /**
     * 计算音频比特率（单位：bps / bit per second）
     * <p>
     * 默认实现适用于未压缩的 PCM 格式，公式为：
     * <pre>
     * bitRate = sampleRate() * bitDepth() * channelCount()
     * </pre>
     * <p>
     * 对于压缩格式（如 G.726），建议覆盖此方法以使用 encodedBitsPerSample 计算实际比特率。
     *
     * @return 音频比特率（每秒比特数）
     */
    default int bitRate() {
        return sampleRate() * bitDepth() * channelCount();
    }

    default int estimateDecodedPcmSize(int compressedSizeInBytes) {
        // return compressedSizeInBytes * bitDepth() * channelCount() / encodedBitsPerSample();

        // 避免溢出
        final long totalBits = (long) compressedSizeInBytes * 8;
        final int numSamples = (int) (totalBits / encodedBitsPerSample());
        // 向上取整字节
        return (numSamples * bitDepth() * channelCount() + 7) / 8;
    }

    default boolean isAac() {
        return false;
    }

    default boolean isPcm() {
        return false;
    }

}

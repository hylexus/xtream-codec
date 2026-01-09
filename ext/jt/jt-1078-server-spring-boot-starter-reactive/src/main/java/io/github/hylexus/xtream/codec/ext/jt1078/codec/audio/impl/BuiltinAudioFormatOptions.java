/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl;


import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioFormatOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum BuiltinAudioFormatOptions implements AudioFormatOptions {

    /**
     * 静音--忽略音频数据
     */
    SILENCE(AudioFamily.SILENCE, 0, 0, 0, 0),
    /**
     * G.726 编码，每个采样点使用 2 比特（16 kbps），16-bit PCM 解码后数据，单声道
     */
    G726_S16_LE_MONO(AudioFamily.G726, 8_000, 2, 16, 1),
    /**
     * G.726 编码，每个采样点使用 3 比特（24 kbps），16-bit PCM 解码后数据，单声道
     */
    G726_S24_LE_MONO(AudioFamily.G726, 8_000, 3, 16, 1),
    /**
     * G.726 编码，每个采样点使用 4 比特（32 kbps），16-bit PCM 解码后数据，单声道
     */
    G726_S32_LE_MONO(AudioFamily.G726, 8_000, 4, 16, 1),
    /**
     * G.726 编码，每个采样点使用 5 比特（40 kbps），16-bit PCM 解码后数据，单声道
     */
    G726_S40_LE_MONO(AudioFamily.G726, 8_000, 5, 16, 1),
    /**
     * ADPCM IMA 单声道
     */
    ADPCM_IMA_MONO(AudioFamily.ADPCM, 8_000, 8, 16, 1),
    /**
     * G711_A 单声道
     */
    G711_A_MONO(AudioFamily.G711, 8_000, 8, 8, 1),
    /**
     * G711_U 单声道
     */
    G711_U_MONO(AudioFamily.G711, 8_000, 8, 8, 1),

    /**
     * PCM 有符号数 16bit 小端 单声道
     */
    PCM_S16_LE_MONO(AudioFamily.PCM, 8_000, 16, 16, 1),
    /**
     * PCM 有符号数 24bit 小端 单声道
     */
    PCM_S24_LE_MONO(AudioFamily.PCM, 8_000, 24, 24, 1),
    /**
     * PCM 有符号数 32bit 小端 单声道
     */
    PCM_S32_LE_MONO(AudioFamily.PCM, 8_000, 32, 32, 1),
    /**
     * PCM 有符号数 40bit 小端 单声道
     */
    PCM_S40_LE_MONO(AudioFamily.PCM, 8_000, 40, 40, 1),
    /**
     * AAC 编码，采样率 44100Hz，比特率 128kbps，位深度 16bit，双声道
     */
    AAC_STEREO(AudioFamily.AAC, 44100, 128, 16, 2),
    ;

    public enum AudioFamily {
        SILENCE(-1),
        G726(0),
        ADPCM(1),
        G711(2),
        PCM(3),
        AAC(4),
        ;
        private final int familyCode;

        AudioFamily(int familyCode) {
            this.familyCode = familyCode;
        }

        public int familyCode() {
            return familyCode;
        }
    }

    private final AudioFamily audioFamily;
    private final int sampleRate;
    private final int encodedBitsPerSample;
    private final int bitDepth;
    private final int channelCount;

    BuiltinAudioFormatOptions(AudioFamily familyCode, int sampleRate, int encodedBitsPerSample, int bitDepth, int channelCount) {
        this.audioFamily = familyCode;
        this.sampleRate = sampleRate;
        this.encodedBitsPerSample = encodedBitsPerSample;
        this.bitDepth = bitDepth;
        this.channelCount = channelCount;
    }

    @Override
    public int sampleRate() {
        return sampleRate;
    }

    @Override
    public int bitDepth() {
        return bitDepth;
    }

    @Override
    public int channelCount() {
        return channelCount;
    }

    @Override
    public int encodedBitsPerSample() {
        return encodedBitsPerSample;
    }

    @Override
    public int bitRate() {
        if (isG726()) {
            return sampleRate * encodedBitsPerSample * channelCount;
        } else {
            return sampleRate * bitDepth * channelCount;
        }
    }

    public AudioFamily getAudioFamily() {
        return audioFamily;
    }

    public boolean isG726() {
        return audioFamily == AudioFamily.G726;
    }

    public boolean isAdpcm() {
        return audioFamily == AudioFamily.ADPCM;
    }

    public boolean isG711() {
        return audioFamily == AudioFamily.G711;
    }

    @Override
    public boolean isPcm() {
        return audioFamily == AudioFamily.PCM;
    }

    @Override
    public boolean isAac() {
        return this.audioFamily == AudioFamily.AAC;
    }

    /**
     * 估算解码后 PCM 数据的字节数。
     *
     * @param compressedSizeInBits 压缩数据大小（单位：bit）
     * @return 解码后的 PCM 字节数
     */
    public int estimateDecodedPcmByteCount(int compressedSizeInBits) {
        final int numSamples = compressedSizeInBits / encodedBitsPerSample;
        // bitDepth() 是每个采样点的位数（如 16），除以 8 得到字节
        return (numSamples * bitDepth()) / 8;
    }

    /**
     * 估算压缩后数据的比特数。
     *
     * @param pcmSizeInBits PCM 数据大小（单位：bit）
     * @return 压缩后的比特数
     */
    public int estimateCompressedBitCount(int pcmSizeInBits) {
        int numSamples = pcmSizeInBits / bitDepth();
        return numSamples * encodedBitsPerSample;
    }

    public String toFfPlayCommand(String fileName) {
        return "ffplay " + toFfmpegArgs() + " -i " + fileName;
    }

    public String toFfmpegArgs() {
        return switch (audioFamily) {
            case G726 -> String.format(
                    "-f g726 -ar %d -ac %d -code_size %d",
                    sampleRate, channelCount, encodedBitsPerSample
            );
            case PCM -> String.format("-f s%dle -ar %d -ac %d", bitDepth, sampleRate, channelCount);
            case AAC -> String.format("-f aac -ar %d -ac %d -b:a %dk", sampleRate, channelCount, encodedBitsPerSample);
            case null, default -> throw new IllegalStateException("AudioFamily is not supported");
        };
    }

    @Override
    public String toString() {
        return "BuiltinAudioFormatOptions{"
               + this.name() + ": family=" + audioFamily
               + ", sampleRate=" + sampleRate
               + ", encodedBitsPerSample=" + encodedBitsPerSample
               + ", bitDepth=" + bitDepth
               + ", channelCount=" + channelCount
               + ", bitRate=" + bitRate()
               + '}';
    }

    private static final Map<String, BuiltinAudioFormatOptions> NAME_CACHE = new HashMap<>(values().length);

    static {
        for (final BuiltinAudioFormatOptions value : values()) {
            NAME_CACHE.put(value.name(), value);
        }
    }

    public static Optional<BuiltinAudioFormatOptions> parseFrom(String name) {
        return Optional.ofNullable(NAME_CACHE.get(name));
    }
}

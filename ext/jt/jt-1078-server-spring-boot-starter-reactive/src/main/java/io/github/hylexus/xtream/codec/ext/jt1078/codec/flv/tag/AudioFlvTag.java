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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag;

import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl.DefaultAudioFlvFlvTagHeader;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public interface AudioFlvTag {


    /**
     * 非 {@link AudioSoundFormat#AAC} 类型: 1 byte
     * <p>
     * {@link AudioSoundFormat#AAC} 类型: 2 byte
     */
    interface AudioFlvTagHeader {

        /**
         * bit[4,7]
         *
         * @return 编码类型
         */
        AudioSoundFormat soundFormat();


        /**
         * bit[2,3]
         * <p>
         * AAC始终为 3
         * <p>
         * 0 = 5.5-kHz
         * 1 = 11-kHz
         * 2 = 22-kHz
         * 3 = 44-kHz
         *
         * @return 采样率
         */
        AudioSoundRate soundRate();

        /**
         * bit[1]
         * <p>
         * 0 = snd8Bit
         * <p>
         * 1 = snd16Bit
         *
         * @return 采样精度
         */
        AudioSoundSize soundSize();

        /**
         * bit[0]
         * <p>
         * 0 = sndMono 单声道
         * <p>
         * 1 = sndStereo 立体声，双声道
         * <p>
         * 对于AAC总是1
         *
         * @return 声道数
         */
        AudioSoundType soundType();

        /**
         * 只有 AAC 格式才有该字段
         */
        @Nullable AudioAacPacketType aacPacketType();

        byte @Nullable [] aacSequenceHeader();

        default int writeTo(ByteBuf byteBuf) {
            final int writerIndex = byteBuf.writerIndex();
            final int v = (this.soundType().getValue() & 0b1)
                          | ((this.soundSize().getValue() & 0b1) << 1)
                          | ((this.soundRate().getValue() & 0b11) << 2)
                          | (this.soundFormat().getValue() << 4);

            byteBuf.writeByte(v);

            // 只有 AAC 才有该属性
            final AudioAacPacketType aacPacketType = this.aacPacketType();
            if (aacPacketType != null) {
                byteBuf.writeByte(aacPacketType.getValue());
                if (aacPacketType == AudioAacPacketType.AAC_SEQ_HEADER) {
                    byteBuf.writeBytes(Objects.requireNonNull(this.aacSequenceHeader(), "AAC"));
                }
            }
            return byteBuf.writerIndex() - writerIndex;
        }
    }

    @Getter
    enum AudioSoundType {
        MONO((byte) 0),
        STEREO((byte) 1);
        private final byte value;

        AudioSoundType(byte value) {
            this.value = value;
        }
    }

    @Getter
    enum AudioSoundSize {
        SND_BIT_8((byte) 0),
        SND_BIT_16((byte) 1);
        private final byte value;

        AudioSoundSize(byte value) {
            this.value = value;
        }
    }

    @Getter
    enum AudioSoundRate {
        // 5.5-kHz
        RATE_5_5_KHZ((byte) 0),
        // 11-kHz
        RATE_11_KHZ((byte) 1),
        // 22-kHz
        RATE_22_KHZ((byte) 2),
        // 44-kHz
        RATE_44_KHZ((byte) 3),
        ;
        private final byte value;

        AudioSoundRate(byte value) {
            this.value = value;
        }
    }

    @Getter
    enum AudioSoundFormat {
        LINEAR_PCM_PLATFORM_ENDIAN((byte) 0),
        ADPCM((byte) 1),
        MP3((byte) 2),
        LINEAR_PCM_LITTLE_ENDIAN((byte) 3),
        NELLYMOSER_16_KHZ_MONO((byte) 4),
        NELLYMOSER_8_KHZ_MONO((byte) 5),
        NELLYMOSER_6((byte) 6),
        G_711_A_LAW_LOGARITHMIC_PCM((byte) 7),
        G_711_MU_LAW_LOGARITHMIC_PCM((byte) 8),
        AAC((byte) 10),
        SPEEX((byte) 11),
        MP3_8_KHZ((byte) 14),
        DEVICE_SPECIFIC_SOUND((byte) 15),
        // TODO 补充其他格式
        ;
        private final byte value;

        AudioSoundFormat(byte value) {
            this.value = value;
        }
    }

    @Getter
    enum AudioAacPacketType {
        // AAC sequence header
        AAC_SEQ_HEADER((byte) 0),
        // AAC raw
        AAC_RAW((byte) 1),
        ;
        private final byte value;

        AudioAacPacketType(byte value) {
            this.value = value;
        }
    }

    /**
     * 备注：下面注释是从 <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a> 网页源码复制过来的。
     * <p>
     * Note: The following comments were copied from the source code of the webpage <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a>.
     *
     * <ul>
     * <li>0: Null</li>
     * <li>1: <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> Main</li>
     * <li>2: <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> LC (Low Complexity)</li>
     * <li>3: <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> SSR (Scalable Sample Rate)</li>
     * <li>4: <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> LTP (Long Term Prediction)</li>
     * <li>5: SBR (<a href="/index.php/Spectral_Band_Replication" title="Spectral Band Replication">Spectral Band Replication</a>)</li>
     * <li>6: <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> Scalable</li>
     * <li>7: <a href="/index.php/TwinVQ" title="TwinVQ">TwinVQ</a></li>
     * <li>8: <a href="/index.php?title=CELP&amp;action=edit&amp;redlink=1" class="new" title="CELP (page does not exist)">CELP</a> (Code Excited Linear Prediction)</li>
     * <li>9: HXVC (Harmonic Vector eXcitation Coding)</li>
     * <li>10: Reserved</li>
     * <li>11: Reserved</li>
     * <li>12: TTSI (Text-To-Speech Interface)</li>
     * <li>13: Main Synthesis</li>
     * <li>14: Wavetable Synthesis</li>
     * <li>15: General MIDI</li>
     * <li>16: Algorithmic Synthesis and Audio Effects</li>
     * <li>17: ER (Error Resilient) <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> LC</li>
     * <li>18: Reserved</li>
     * <li>19: ER <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> LTP</li>
     * <li>20: ER <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> Scalable</li>
     * <li>21: ER <a href="/index.php/TwinVQ" title="TwinVQ">TwinVQ</a></li>
     * <li>22: ER <a href="/index.php/BSAC" class="mw-redirect" title="BSAC">BSAC</a> (Bit-Sliced Arithmetic Coding)</li>
     * <li>23: ER <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> LD (Low Delay)</li>
     * <li>24: ER <a href="/index.php?title=CELP&amp;action=edit&amp;redlink=1" class="new" title="CELP (page does not exist)">CELP</a></li>
     * <li>25: ER HVXC</li>
     * <li>26: ER HILN (Harmonic and Individual Lines plus Noise)</li>
     * <li>27: ER Parametric</li>
     * <li>28: SSC (SinuSoidal Coding)</li>
     * <li>29: PS (<a href="/index.php?title=Parametric_Stereo&amp;action=edit&amp;redlink=1" class="new" title="Parametric Stereo (page does not exist)">Parametric Stereo</a>)</li>
     * <li>30: <a href="/index.php/MPEG_Surround" class="mw-redirect" title="MPEG Surround">MPEG Surround</a></li>
     * <li>31: (Escape value)</li>
     * <li>32: <a href="/index.php?title=MP1&amp;action=edit&amp;redlink=1" class="new" title="MP1 (page does not exist)">Layer-1</a></li>
     * <li>33: <a href="/index.php/MP2" title="MP2">Layer-2</a></li>
     * <li>34: <a href="/index.php/MP3" title="MP3">Layer-3</a></li>
     * <li>35: DST (Direct Stream Transfer)</li>
     * <li>36: <a href="/index.php/MPEG-4_ALS" class="mw-redirect" title="MPEG-4 ALS">ALS</a> (Audio Lossless)</li>
     * <li>37: <a href="/index.php/MPEG-4_SLS" title="MPEG-4 SLS">SLS</a> (Scalable LosslesS)</li>
     * <li>38: <a href="/index.php/MPEG-4_SLS" title="MPEG-4 SLS">SLS</a> non-core</li>
     * <li>39: ER <a href="/index.php/AAC" class="mw-redirect" title="AAC">AAC</a> ELD (Enhanced Low Delay)</li>
     * <li>40: SMR (Symbolic Music Representation) Simple</li>
     * <li>41: SMR Main</li>
     * <li>42: <a href="/index.php?title=USAC&amp;action=edit&amp;redlink=1" class="new" title="USAC (page does not exist)">USAC</a> (Unified Speech and Audio Coding) (no <a href="/index.php/SBR" class="mw-redirect" title="SBR">SBR</a>)</li>
     * <li>43: SAOC (Spatial Audio Object Coding)</li>
     * <li>44: LD <a href="/index.php/MPEG_Surround" class="mw-redirect" title="MPEG Surround">MPEG Surround</a></li>
     * <li>45: <a href="/index.php?title=USAC&amp;action=edit&amp;redlink=1" class="new" title="USAC (page does not exist)">USAC</a></li>
     * </ul>
     */
    enum AacSeqHeaderObjectType {
        NULL((byte) 0, "Null"),
        TYPE_1_AAC_MAIN((byte) 1, "AAC Main"),
        TYPE_2_AAC_LC((byte) 2, "AAC LC (Low Complexity)"),
        TYPE_3_AAC_SSR((byte) 3, "AAC SSR (Scalable Sample Rate)"),
        TYPE_4_AAC_LTP((byte) 4, "AAC LTP (Long Term Prediction)"),
        TYPE_5_AAC_SBR((byte) 5, "SBR"),
        TYPE_6_AAC_SCALABLE((byte) 6, "AAC Scalable"),
        TYPE_7_AAC_TWIN_VQ((byte) 7, "TwinVQ"),
        TYPE_8_AAC_CELP((byte) 8, "CELP (Code Excited Line Prediction)"),
        TYPE_9_AAC_HVXC((byte) 9, "HVXC"),
        TYPE_10_AAC_RESERVED((byte) 10, "Reserved"),
        TYPE_11_AAC_RESERVED((byte) 11, "Reserved"),
        TYPE_12_AAC_TTSI((byte) 12, "TTSI (Text-To-Speech Interface)"),
        TYPE_13_AAC_MAIN_SYNTHESIS((byte) 13, "Main Synthesis"),
        TYPE_14_AAC_WAVETABLE_SYNTHESIS((byte) 14, "Wavetable Synthesis"),
        TYPE_15_AAC_GENERAL_MIDI((byte) 15, "General MIDI"),
        TYPE_16_AAC_ALGORITHMIC_SYNTHESIS_AND_AUDIO_EFFECTS((byte) 16, "Algorithmic Synthesis and Audio Effects"),
        TYPE_17_ER((byte) 17, "ER (Error Resilient) AAC LC"),
        TYPE_18_AAC_RESERVED((byte) 18, "Reserved"),
        TYPE_19_ER_AAC_LTP((byte) 19, "ER AAC LTP"),
        TYPE_20_ER_AAC_SCALABLE((byte) 20, "ER AAC Scalable"),
        TYPE_21_ER_TWIN_VQ((byte) 21, "ER TwinVQ"),
        TYPE_22_ER_BSAC((byte) 22, "ER BSAC (Bit-Sliced Arithmetic Coding)"),
        TYPE_23_ER_AAC_LD((byte) 23, "ER AAC LD (Low Delay)"),
        TYPE_24_ER_CELP((byte) 24, "ER CELP"),
        TYPE_25_ER_HVXC((byte) 25, "ER HVXC"),
        TYPE_26_ER_HILN((byte) 26, "ER HILN (Harmonic and Individual Lines plus Noise)"),
        TYPE_27_ER_PARAMETRIC((byte) 27, "ER Parametric"),
        TYPE_28_SSC((byte) 28, "SSC (SinuSoidal Coding)"),
        TYPE_29_PS((byte) 29, "PS (Parametric Stereo)"),
        TYPE_30_MPEG_SURROUND((byte) 30, "MPEG Surround"),
        TYPE_31_ESCAPE((byte) 31, "Escape value"),
        TYPE_32_LAYER_1((byte) 32, "Layer-1"),
        TYPE_33_LAYER_2((byte) 33, "Layer-2"),
        TYPE_34_LAYER_3((byte) 34, "Layer-3"),
        TYPE_35_DST((byte) 35, "DST (Direct Stream Transfer)"),
        TYPE_36_ALS((byte) 36, "Audio Lossless"),
        TYPE_37_SLS((byte) 37, "SLS (Scalable LosslesS)"),
        TYPE_38_SLS_NON_CORE((byte) 38, "SLS non-core"),
        TYPE_39_ER_AAC_ELD((byte) 39, "ER AAC ELD (Enhanced Low Delay)"),
        TYPE_40_SMR_SIMPLE((byte) 40, "SMR (Symbolic Music Representation) Simpl"),
        TYPE_41_SMR_MAIN((byte) 41, "SMR (Symbolic Music Representation) Main"),
        TYPE_42_USAC((byte) 42, "USAC (Unified Speech and Audio Coding) (no SBR)"),
        TYPE_43_SAOC((byte) 43, "SAOC (Spatial Audio Object Coding)"),
        TYPE_44_LD_MPEG_SURROUND((byte) 44, "LD MPEG Surround"),
        TYPE_45_USAC((byte) 45, "USAC"),
        ;
        private final byte value;
        private final String desc;

        AacSeqHeaderObjectType(byte value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public byte value() {
            return value;
        }

        public String desc() {
            return desc;
        }
    }

    /**
     * 备注：下面注释是从 <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a> 网页源码复制过来的。
     * <p>
     * Note: The following comments were copied from the source code of the webpage <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a>.
     *
     * <ul>
     * <li>0: 96000 Hz</li>
     * <li>1: 88200 Hz</li>
     * <li>2: 64000 Hz</li>
     * <li>3: 48000 Hz</li>
     * <li>4: 44100 Hz</li>
     * <li>5: 32000 Hz</li>
     * <li>6: 24000 Hz</li>
     * <li>7: 22050 Hz</li>
     * <li>8: 16000 Hz</li>
     * <li>9: 12000 Hz</li>
     * <li>10: 11025 Hz</li>
     * <li>11: 8000 Hz</li>
     * <li>12: 7350 Hz</li>
     * <li>13: Reserved</li>
     * <li>14: Reserved</li>
     * <li>15: frequency is written explictly</li>
     * </ul>
     */
    enum AacSeqHeaderSampleRate {
        RATE_0_96000_HZ((byte) 0, "96000 Hz"),
        RATE_1_88200_HZ((byte) 1, "88200 Hz"),
        RATE_2_64000_HZ((byte) 2, "64000 Hz"),
        RATE_3_48000_HZ((byte) 3, "48000 Hz"),
        RATE_4_44100_HZ((byte) 4, "44100 Hz"),
        RATE_5_32000_HZ((byte) 5, "32000 Hz"),
        RATE_6_24000_HZ((byte) 6, "24000 Hz"),
        RATE_7_22050_HZ((byte) 7, "22050 Hz"),
        RATE_8_16000_HZ((byte) 8, "16000 Hz"),
        RATE_9_12000_HZ((byte) 9, "12000 Hz"),
        RATE_10_11025_HZ((byte) 10, "11025 Hz"),
        RATE_11_8000_HZ((byte) 11, "8000 Hz"),
        RATE_12_7350_HZ((byte) 12, "7350 Hz"),
        RATE_13_RESERVED((byte) 13, "Reserved"),
        RATE_14_RESERVED((byte) 14, "Reserved"),
        RATE_15_EXPLICITLY_SPECIFIED((byte) 15, "frequency is written explicitly"),
        ;
        private final byte value;
        private final String desc;

        AacSeqHeaderSampleRate(byte value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public byte value() {
            return value;
        }

        public String desc() {
            return desc;
        }
    }

    /**
     * 备注：下面注释是从 <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a> 网页源码复制过来的。
     * <p>
     * Note: The following comments were copied from the source code of the webpage <a href="https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio">https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio</a>.
     *
     * <ul>
     * <li>0: Defined in AOT Specific Config</li>
     * <li>1: 1 channel: front-center</li>
     * <li>2: 2 channels: front-left, front-right</li>
     * <li>3: 3 channels: front-center, front-left, front-right</li>
     * <li>4: 4 channels: front-center, front-left, front-right, back-center</li>
     * <li>5: 5 channels: front-center, front-left, front-right, back-left, back-right</li>
     * <li>6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel</li>
     * <li>7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel</li>
     * <li>8-15: Reserved</li>
     * </ul>
     */
    enum AacSeqHeaderChannelConfig {
        CONFIG_0_DEFINED_IN_AOT_SPECIFIC_CONFIG((byte) 0, "Defined in AOT Specific Config"),
        CONFIG_1_1_CHANNEL((byte) 1, "1 channel: front-center"),
        CONFIG_2_2_CHANNELS((byte) 2, "2 channels: front-left, front-right"),
        CONFIG_3_3_CHANNELS((byte) 3, "3 channels: front-center, front-left, front-right"),
        CONFIG_4_4_CHANNELS((byte) 4, "4 channels: front-center, front-left, front-right, back-center"),
        CONFIG_5_5_CHANNELS((byte) 5, "5 channels: front-center, front-left, front-right, back-left, back-right"),
        CONFIG_6_6_CHANNELS((byte) 6, "6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel"),
        CONFIG_7_8_CHANNELS((byte) 7, "8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel"),
        ;
        private final byte value;
        private final String desc;

        AacSeqHeaderChannelConfig(byte value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public byte value() {
            return value;
        }

        public String desc() {
            return desc;
        }
    }

    interface AudioFlvTagHeaderBuilder {
        AudioFlvTagHeaderBuilder soundFormat(AudioSoundFormat soundFormat);

        /**
         * @param soundRate 采样率
         */
        AudioFlvTagHeaderBuilder soundRate(AudioSoundRate soundRate);

        /**
         * @param soundSize 采样精度(位深)
         */
        AudioFlvTagHeaderBuilder soundSize(AudioSoundSize soundSize);

        /**
         * @param soundType 声道类型
         */
        AudioFlvTagHeaderBuilder soundType(AudioSoundType soundType);

        AudioFlvTagHeaderBuilder aacPacketType(@Nullable AudioAacPacketType aacPacketType);

        AudioFlvTagHeaderBuilder aacSequenceHeader(byte[] aacSequenceHeader);

        AudioFlvTagHeader build();
    }

    static AudioFlvTagHeaderBuilder newTagHeaderBuilder() {
        return new DefaultAudioFlvFlvTagHeader();
    }
}

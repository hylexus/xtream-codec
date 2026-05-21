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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * NALU(Network Abstraction Layer Unit)
 */
public class H264NaluHeader {

    /**
     * bit[7] F 禁止位
     * <p>
     * 正常情况下为 0,
     * 某些情况下，如果 NALU 发生了丢失数据的情况可以将该字段置为 1，以便接收方纠错或直接丢掉该 NALU
     */
    private final byte forbiddenBit;

    /**
     * bit[5,6] NRI 表示该 NALU 的重要性(可以作为该 NALU 是否可以被丢弃的标识)
     * <p>
     * 取值范围: [0,3]
     * <p>
     * 0: DISPOSABLE;
     * 1: LOW;
     * 2: HIGH;
     * 3: HIGHEST
     */
    private final byte nalRefIdc;

    /**
     * bit[0,4] TYPE
     * <p>
     * {@link NaluType#SPS SPS}和 {@link NaluType#PPS PPS} 中存放了解码所需的各种参数信息，是H.264解码的前置条件
     * <li>这种数据应该在码流数据的最前面</li>
     * <li>解码(播放)时要先把这两种类型的数据传递给解码(播放)器</li>
     *
     * @see NaluType#SPS
     * @see NaluType#PPS
     * @see NaluType#SEI
     * @see NaluType#IDR
     */
    private final byte typeValue;

    public H264NaluHeader(int value) {
        this.forbiddenBit = (byte) ((value >> 7) & 0b1);
        this.nalRefIdc = (byte) ((value >> 5) & 0b11);
        this.typeValue = (byte) ((value) & 0b11111);
    }

    public byte forbiddenBit() {
        return this.forbiddenBit;
    }

    public byte nalRefIdc() {
        return this.nalRefIdc;
    }

    public byte nir() {
        return this.nalRefIdc();
    }


    public byte typeValue() {
        return this.typeValue;
    }

    public Optional<NaluType> type() {
        return NaluType.findByCode(typeValue());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", H264NaluHeader.class.getSimpleName() + "[", "]")
                .add("F=" + forbiddenBit)
                .add("NRI=" + nir())
                .add("TYPE=" + type().orElse(null))
                .toString();
    }

    public static H264NaluHeader of(int value) {
        return new H264NaluHeader(value);
    }

    public enum NaluType {
        /**
         * 未指定
         */
        UNKNOWN((byte) 0),
        /**
         * slice_layer_without_partitioning_rbsp()
         * <p>
         * 一般都是除了 {@link #IDR} 之外的其他视频数据, 有可能是一个 I 帧
         *
         * @see #IDR
         */
        SLICE((byte) 1),
        /**
         * slice_data_partition_a_layer_rbsp()
         */
        SDP_A((byte) 2),
        /**
         * slice_data_partition_b_layer_rbsp()
         */
        SDP_B((byte) 3),
        /**
         * slice_data_partition_c_layer_rbsp()
         */
        SDP_C((byte) 4),
        /**
         * Instantaneous decoding refresh
         * <p>
         * IDR 一定是 I 帧，反过来不成立
         *
         * @see #SLICE
         */
        IDR((byte) 5),
        /**
         * Supplemental enhancement information
         * <p>
         * 自定义信息(对应的 NRI 一般是 0)
         */
        SEI((byte) 6),
        /**
         * Sequence Parameter Set
         */
        SPS((byte) 7),
        /**
         * Picture Parameter Set
         */
        PPS((byte) 8),
        /**
         * Access unit delimiter
         */
        AUD((byte) 9),
        /**
         * End of sequence RBSP
         */
        END_OF_SEQ((byte) 10),
        /**
         * End of stream RBSP
         */
        END_OF_STREAM((byte) 11),
        /**
         * Filler data RBSP
         */
        FILTER_DATA((byte) 12),
        /**
         * seq_parameter_set_extension_rbsp()
         */
        SPS_EXT((byte) 13),
        // 14~18: 保留
        /**
         * slice_layer_without_partitioning_rbsp()
         */
        SLICE_19((byte) 19),
        // 20~23: 保留
        // 24~31: 未指定
        ;
        private final byte value;

        private static final Map<Byte, NaluType> mapping = new HashMap<>(values().length);

        static {
            for (final NaluType value : values()) {
                mapping.put(value.value, value);
            }
        }

        NaluType(byte value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.name() + "(" + this.value + ")";
        }

        public static Optional<NaluType> findByCode(byte code) {
            return Optional.ofNullable(mapping.get(code));
        }

        public byte getValue() {
            return value;
        }
    }
}

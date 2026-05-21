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

import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl.DefaultVideoFlvFlvTagHeader;
import io.netty.buffer.ByteBuf;

import java.util.Optional;

public interface VideoFlvTag {
    /**
     * 非 H.264 : 1 byte
     * <p>
     * H.264: 5 bytes({@link #avcPacketType()} + {@link #compositionTime()})
     */
    interface VideoFlvTagHeader {

        default int length() {
            if (codecId() == VideoCodecId.AVC) {
                return 5;
            }
            return 1;
        }

        /**
         * 4 bits
         *
         * @return 帧类型
         */
        VideoFrameType frameType();

        /**
         * 4 bits
         *
         * @return 编码 ID
         */
        VideoCodecId codecId();

        /**
         * 8 bits
         * <p>
         * 只有 H.264 才有该属性
         */
        Optional<VideoAvcPacketType> avcPacketType();

        /**
         * 24 bits
         * <p>
         * 只有 H.264 才有该属性
         *
         * @return {@link #avcPacketType()} == 1 ({@link VideoAvcPacketType#AVC_NALU}) 时为cts，否则为零
         */
        Optional<Integer> compositionTime();

        static VideoFlvTagHeader createAvcNaluHeader(VideoFrameType frameType, int compositionTime) {
            return newBuilder().frameType(frameType)
                    .codecId(VideoCodecId.AVC)
                    .avcPacketType(VideoAvcPacketType.AVC_NALU)
                    .compositionTime(compositionTime)
                    .build();
        }

        static VideoFlvTagHeader createAvcEndSequenceHeader() {
            return newBuilder().frameType(VideoFrameType.KEY_FRAME)
                    .codecId(VideoCodecId.AVC)
                    .avcPacketType(VideoAvcPacketType.AVC_END_OF_SEQ)
                    .compositionTime(0)
                    .build();
        }

        static VideoFlvTagHeader createAvcSequenceHeader() {
            return newBuilder().frameType(VideoFrameType.KEY_FRAME)
                    .codecId(VideoCodecId.AVC)
                    .avcPacketType(VideoAvcPacketType.AVC_SEQ_HEADER)
                    .compositionTime(0)
                    .build();
        }

        static VideoFlvTagHeaderBuilder newBuilder() {
            return new DefaultVideoFlvFlvTagHeader();
        }

        default int writeTo(ByteBuf byteBuf) {
            // 1 byte
            byteBuf.writeByte(this.frameType().getValue() << 4 | (this.codecId().getValue() & 0xF));
            // 只有 H.264 才有该属性
            // 1 byte
            avcPacketType().ifPresent(avcPacketType -> byteBuf.writeByte(avcPacketType.getValue()));
            // 只有 H.264 才有该属性
            // 3 bytes
            compositionTime().ifPresent(time -> byteBuf.writeBytes(Numbers.intTo3Bytes(time)));
            return length();
        }
    }

    enum VideoCodecId {
        JPEG((byte) 1),
        SORENSON_H263((byte) 2),
        SCREEN_VIDE((byte) 3),
        VP6_WITH_ALPHA((byte) 5),
        SCREEN_VIDEO_V2((byte) 6),
        AVC((byte) 7),
        ;
        private final byte value;

        VideoCodecId(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    enum VideoFrameType {
        KEY_FRAME((byte) 1),
        INTER_FRAME((byte) 2),
        DISPOSABLE_INTER_FRAME((byte) 3),
        GENERATED_KEY_FRAME((byte) 4),
        VIDEO_INFO_OR_COMMAND_FRAME((byte) 5),
        ;
        private final byte value;

        VideoFrameType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    enum VideoAvcPacketType {
        AVC_SEQ_HEADER((byte) 0),
        AVC_NALU((byte) 1),
        AVC_END_OF_SEQ((byte) 2),
        ;
        private final byte value;

        VideoAvcPacketType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    interface VideoFlvTagHeaderBuilder {
        VideoFlvTagHeaderBuilder frameType(VideoFrameType frameType);

        VideoFlvTagHeaderBuilder codecId(VideoCodecId codecId);

        VideoFlvTagHeaderBuilder avcPacketType(VideoAvcPacketType avcPacketType);

        VideoFlvTagHeaderBuilder compositionTime(int compositionTime);

        VideoFlvTagHeader build();
    }

}

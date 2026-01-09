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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl;


import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.FlvEncoder;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.FlvTagHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.Amf;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.AudioFlvTag;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.ScriptFlvTag;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.VideoFlvTag;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.*;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.impl.DefaultH264Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author hylexus
 * @see <a href="https://rtmp.veriskope.com/pdf/video_file_format_spec_v10.pdf">video_file_format_spec_v10.pdf</a>
 * @see <a href="https://www.jianshu.com/p/916899d4833b">30分钟学会FLV</a>
 * @see <a href="https://www.jianshu.com/p/07657d85617e">FLV视频封装格式详细解析</a>
 * @see <a href="https://www.cnblogs.com/chyingp/p/flv-getting-started.html">FLV协议5分钟入门浅析</a>
 * @see <a href="https://www.cnblogs.com/CoderTian/p/8278369.html">FLV文件格式解析</a>
 * @see <a href="https://gitee.com/ldming/JT1078">https://gitee.com/ldming/JT1078</a>
 * @see <a href="https://gitee.com/matrixy/jtt1078-video-server">https://gitee.com/matrixy/jtt1078-video-server</a>
 * @see <a href="https://sample-videos.com/index.php#sample-flv-video">https://sample-videos.com/index.php#sample-flv-video</a>
 */
@SuppressWarnings("NullAway")
public class DefaultFlvEncoder implements FlvEncoder {

    private static final Logger log = LoggerFactory.getLogger(DefaultFlvEncoder.class);
    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    private final H264Decoder h264Decoder;
    private final SpsDecoder spsDecoder = SpsDecoder.DEFAULT;

    private ByteBuf spsFrame;
    private ByteBuf ppsFrame;

    private ByteBuf flvBasicFrame;
    private ByteBuf lastIFrame;

    public DefaultFlvEncoder(int ringBufferCapacity) {
        this.h264Decoder = new DefaultH264Decoder(ringBufferCapacity);
    }

    @Override
    public ByteBuf getFlvBasicFrame() {
        return flvBasicFrame;
    }

    @Override
    public ByteBuf getLastIFrame() {
        return lastIFrame;
    }

    @Override
    @SuppressWarnings("MixedMutabilityReturnType")
    public List<ByteBuf> encodeVideoTag(long timestamp, ByteBuf naluStream) {
        final List<H264Nalu> h264NaluList;
        try {
            h264NaluList = this.h264Decoder.decode(naluStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        final List<ByteBuf> list = new ArrayList<>(h264NaluList.size());
        for (final H264Nalu nalu : h264NaluList) {
            try {
                final Optional<ByteBuf> data = this.doEncode(nalu, timestamp);
                data.ifPresent(list::add);
            } finally {
                nalu.close();
            }
        }
        return list;
    }

    @Override
    public ByteBuf encodeAudioTag(int dts, AudioFlvTag.AudioFlvTagHeader audioTagHeader, ByteBuf payload) {
        final ByteBuf buffer = this.allocator.buffer();
        try {
            // 1. tagHeader : 11 bytes
            final int outerHeaderSize = FlvTagHeader.newAudioTagHeader(0, dts).writeTo(buffer);

            // 2.1 tagData[audioTagHeader] : 1 bytes / 2 bytes(AAC)
            final int audioHeaderSize = audioTagHeader.writeTo(buffer);

            // 2.2 tagData[audioTagData]
            final int l = payload.readableBytes();
            buffer.writeBytes(payload);

            // 2.3 给 3 字节的 dataSize 赋值(之前的 0 只是个占位符)
            final int tagDataSize = audioHeaderSize + l;
            buffer.setBytes(1, Numbers.intTo3Bytes(tagDataSize));

            // 3. previous tag size: 11 + tagDataSize
            buffer.writeInt(outerHeaderSize + tagDataSize);
            return buffer;
        } catch (Throwable throwable) {
            buffer.release();
            throw throwable;
        }
    }

    private Optional<ByteBuf> doEncode(H264Nalu nalu, long timestamp) {
        final Optional<H264NaluHeader.NaluType> optional = nalu.header().type();
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        final H264NaluHeader.NaluType naluType = optional.get();
        // 丢一个 NALU 用户不会感知
        // 丢一帧可能花屏
        // 丢 IDR 解码器可能直接挂掉
        // type in [1,2,3,4,5,7,8]
        if (naluType.getValue() <= 0 || naluType.getValue() > 8 || naluType.getValue() == 6) {
            return Optional.empty();
        }

        if (naluType == H264NaluHeader.NaluType.SPS && this.spsFrame == null) {
            this.spsFrame = this.allocator.buffer();
            final ByteBuf rbsp = nalu.rbsp();
            this.spsFrame.writeBytes(rbsp.slice(), rbsp.readableBytes());
        }

        if (naluType == H264NaluHeader.NaluType.PPS && this.ppsFrame == null) {
            this.ppsFrame = this.allocator.buffer();
            final ByteBuf rbsp = nalu.rbsp().slice();
            this.ppsFrame.writeBytes(rbsp, rbsp.readableBytes());
        }

        if (this.ppsFrame != null && this.spsFrame != null && this.flvBasicFrame == null) {
            this.flvBasicFrame = this.createFlvBasicFrame(timestamp);
        }

        if (this.flvBasicFrame == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.createFlvFrame(timestamp, nalu, naluType));
    }

    private ByteBuf createFlvFrame(long timestamp, H264Nalu nalu, H264NaluHeader.NaluType naluType) {
        if (naluType == H264NaluHeader.NaluType.SPS || naluType == H264NaluHeader.NaluType.PPS || naluType == H264NaluHeader.NaluType.SEI) {
            return null;
        }

        final ByteBuf buffer = this.allocator.buffer();
        try {
            // 1. tagHeader : 11 bytes
            // log.info("V-dts: {}", timestamp);
            final int outerHeaderSize = FlvTagHeader.newVideoTagHeader(0, (int) timestamp).writeTo(buffer);

            // 2.1 tagData[videoTagHeader] : 5 bytes
            final VideoFlvTag.VideoFrameType frameType = naluType == H264NaluHeader.NaluType.IDR
                    ? VideoFlvTag.VideoFrameType.KEY_FRAME
                    : VideoFlvTag.VideoFrameType.INTER_FRAME;

            final int videoHeaderSize = VideoFlvTag.VideoFlvTagHeader.createAvcNaluHeader(frameType, 0).writeTo(buffer);

            // 2.2 tagData[videoTagData]
            final int naluLength = nalu.rbsp().readableBytes();
            buffer.writeInt(naluLength);
            buffer.writeBytes(nalu.rbsp(), naluLength);

            // 2.3 给 3 字节的 dataSize 赋值(之前的 0 只是个占位符)
            // +4: `naluLength` 本身的长度 4 bytes
            final int tagDataSize = videoHeaderSize + naluLength + 4;
            buffer.setBytes(1, Numbers.intTo3Bytes(tagDataSize));

            // 3. previous tag size: 11 + tagDataSize
            buffer.writeInt(outerHeaderSize + tagDataSize);
            if (naluType == H264NaluHeader.NaluType.IDR) {
                if (this.lastIFrame != null) {
                    this.lastIFrame.release();
                }
                this.lastIFrame = buffer.copy();
            }
            return buffer;
        } catch (Throwable throwable) {
            buffer.release();
            throw throwable;
        }
    }

    private ByteBuf createFlvBasicFrame(long timestamp) {
        final Sps sps = this.spsDecoder.decodeSps(this.spsFrame);
        final ByteBuf buffer = this.allocator.buffer();
        this.writeScriptTag(buffer, sps);
        this.writeFirstVideoTag(buffer, sps, timestamp);
        return buffer;
    }

    private void writeScriptTag(ByteBuf buffer, Sps sps) {
        final int writerIndex = buffer.writerIndex();
        // 1. tagHeader: 11 bytes
        final int outerHeaderSize = FlvTagHeader.newScriptTagHeader(0, 0).writeTo(buffer);
        // 2.1 tagData: scriptTag 没有 header 直接就是数据
        final int tagDataSize = ScriptFlvTag.of(this.generateMetadata(sps)).writeTo(buffer);

        // 2.2 给 3 字节的 dataSize 赋值(之前的 0 只是个占位符)
        buffer.setBytes(writerIndex + 1, Numbers.intTo3Bytes(tagDataSize));
        // 3. previous tag size: 11 + tagDataSize
        buffer.writeInt(outerHeaderSize + tagDataSize);
    }

    private List<Amf> generateMetadata(Sps sps) {
        return List.of(
                Amf.ofString("onMetaData"),
                Amf.ofEcmaArray(List.of(
                        Amf.ofPair("videocodecid", Amf.ofNumber(VideoFlvTag.VideoCodecId.AVC.getValue() * 1.0)),
                        Amf.ofPair("width", Amf.ofNumber(sps.getWidth() * 1.0D)),
                        Amf.ofPair("height", Amf.ofNumber(sps.getHeight() * 1.0D))
                ))
        );
    }

    private void writeFirstVideoTag(ByteBuf buffer, Sps sps, long timestamp) {
        final int writerIndex = buffer.writerIndex();
        // 1. tagHeader: 11 bytes
        final int outerHeaderSize = FlvTagHeader.newVideoTagHeader(0, (int) timestamp).writeTo(buffer);
        // 2.1 tagData[videoTagHeader] : 5 bytes
        // 4bits(frameType) + 4bits(codecId) + 8bits(avcPacketType) + compositionTime(24bits) = 5 bytes
        final int videoHeaderSize = VideoFlvTag.VideoFlvTagHeader.createAvcSequenceHeader().writeTo(buffer);
        // 2.2 tagData[videoTagData]
        final int videoBodySize = AvcDecoderConfigurationRecord.writeTo(buffer, this.spsFrame, this.ppsFrame, sps);
        // 2.3 给 3 字节的 dataSize 赋值(之前的 0 只是个占位符)
        final int tagDataSize = videoHeaderSize + videoBodySize;
        buffer.setBytes(writerIndex + 1, Numbers.intTo3Bytes(tagDataSize));
        // 3. previous tag size: 11 + tagDataSize
        buffer.writeInt(outerHeaderSize + tagDataSize);
    }

    @Override
    public void close() {
        XtreamBytes.releaseBuf(this.flvBasicFrame);
        XtreamBytes.releaseBuf(this.spsFrame);
        XtreamBytes.releaseBuf(this.ppsFrame);
        XtreamBytes.releaseBuf(this.lastIFrame);
    }

}

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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264;

import io.github.hylexus.xtream.codec.ext.jt1078.codec.AbstractAudioVideoCodecTest;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.impl.DefaultH264Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

class H264DecoderTest extends AbstractAudioVideoCodecTest {

    private static final Logger log = LoggerFactory.getLogger(H264DecoderTest.class);

    /**
     * `demo_video_176x144_baseline.h264` 视频来源: <a href="https://www.bilibili.com/video/BV1nG4y1u7HT">https://www.bilibili.com/video/BV1nG4y1u7HT</a>
     * <pre>{@code
     * ffplay -f h264 demo_video_176x144_baseline.h264
     * }</pre>
     *
     * @see <a href="https://www.bilibili.com/video/BV1nG4y1u7HT">https://www.bilibili.com/video/BV1nG4y1u7HT</a>
     */
    @org.junit.jupiter.api.Test
    void testDecodeH264RawStream() {
        final SpsDecoder spsDecoder = SpsDecoder.DEFAULT;
        final DefaultH264Decoder h264Decoder = new DefaultH264Decoder(1 << 16);
        final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        try (final InputStream inputStream = getInputStream("mock-data/h264/demo_video_176x144_baseline.h264")) {
            Assertions.assertNotNull(inputStream);
            final byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                final ByteBuf byteBuf = allocator.buffer().writeBytes(buffer, 0, length);
                try {
                    final List<H264Nalu> naluList = h264Decoder.decode(byteBuf);
                    if (naluList.isEmpty()) {
                        continue;
                    }
                    for (final H264Nalu nalu : naluList) {
                        System.out.println(nalu);
                        if (nalu.header().type().orElseThrow() == H264NaluHeader.NaluType.SPS) {
                            final Sps sps = spsDecoder.decodeSps(nalu.rbsp());
                            System.out.println("\tSPS: " + sps);
                        }
                    }
                    System.out.println("..".repeat(100));
                } finally {
                    byteBuf.release();
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}

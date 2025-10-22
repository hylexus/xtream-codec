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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.mp3;

import de.sciss.jump3r.lowlevel.LameEncoder;
import de.sciss.jump3r.mp3.Lame;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioFormatOptions;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.PcmToMp3Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;

/**
 * 这个实现类也是从 <a href="https://gitee.com/matrixy/jtt1078-video-server">https://gitee.com/matrixy/jtt1078-video-server</a> 复制过来之后修改的。
 *
 * @see <a href="https://gitee.com/matrixy/jtt1078-video-server/blob/flv/src/main/java/cn/org/hentai/jtt1078/codec/MP3Encoder.java#L12">https://gitee.com/matrixy/jtt1078-video-server/blob/flv/src/main/java/cn/org/hentai/jtt1078/codec/MP3Encoder.java#L12</a>
 * @see <a href="https://sourceforge.net/projects/jump3r/">https://sourceforge.net/projects/jump3r/</a>
 * @see <a href="https://pure-java-mp3-encoder.blogspot.com/">https://pure-java-mp3-encoder.blogspot.com/</a>
 */
@SuppressWarnings("NullAway")
public class DefaultPcmToMp3Encoder implements PcmToMp3Encoder {

    private static final Logger log = LoggerFactory.getLogger(DefaultPcmToMp3Encoder.class);
    private volatile @Nullable LameEncoder lameEncoder;
    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    private byte @Nullable [] buffer;

    public DefaultPcmToMp3Encoder() {
    }

    private void initLameEncoderIfNecessary(AudioFormatOptions sourceOptions) {
        if (this.lameEncoder == null) {
            synchronized (this) {
                if (this.lameEncoder == null) {
                    this.lameEncoder = this.createLameEncoder(sourceOptions);
                    this.buffer = new byte[this.lameEncoder.getPCMBufferSize()];
                }
            }
        }
    }

    private LameEncoder createLameEncoder(AudioFormatOptions sourceOptions) {
        final int sourceFrameSize = sourceOptions.bitDepth() / 8 * sourceOptions.channelCount();
        return new LameEncoder(
                new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceOptions.sampleRate(), sourceOptions.bitDepth(), sourceOptions.channelCount(), sourceFrameSize <= 0 ? -1 : sourceFrameSize, -1, false),
                sourceOptions.bitRate() / 1000,
                LameEncoder.CHANNEL_MODE_MONO,
                Lame.MEDIUM, // Lame.MEDIUM, LameEncoder.QUALITY_MIDDLE
                false
        );
    }

    @Nonnull
    @Override
    public AudioPackage encode(AudioPackage audioPackage) {
        if (audioPackage.isEmpty()) {
            return AudioPackage.empty();
        }
        this.initLameEncoderIfNecessary(audioPackage.options());

        final ByteBuf stream = audioPackage.payload();
        final byte[] pcm = XtreamBytes.getBytes(stream);

        int offset = 0;
        int length = Math.min(this.lameEncoder.getPCMBufferSize(), pcm.length);
        int processed;
        final ByteBuf mp3Stream = this.allocator.buffer();
        try {
            while ((processed = this.lameEncoder.encodeBuffer(pcm, offset, length, buffer)) > 0) {
                mp3Stream.writeBytes(buffer, 0, processed);
                offset += length;
                length = Math.min(buffer.length, pcm.length - offset);
                // mp3Stream.writeBytes(buffer, 0, processed);
            }
            // processed = this.lameEncoder.encodeFinish(buffer);
            // if (processed > 0) {
            //     mp3Stream.writeBytes(buffer, 0, processed);
            // }
        } catch (Throwable e) {
            XtreamBytes.releaseBuf(mp3Stream);
            throw new RuntimeException(e);
        }

        if (mp3Stream.readableBytes() <= 0) {
            XtreamBytes.releaseBuf(mp3Stream);
            return AudioPackage.empty();
        }

        return new AudioPackage(null, mp3Stream);
    }

    @Override
    public void close() {
        if (this.lameEncoder != null) {
            this.lameEncoder.close();
            log.info("{} closed", this);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.g711;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.AbstractAudioVideoCodecTest;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * <pre>{@code
 * # 单元测试用到的 shell 命令如下
 *
 * # 截取音频片段
 * ffmpeg -i lchs.mp3 -ss 00:01:13 -to 00:02:57 -c copy lchs-slice.mp3
 *
 * # .mp3 --> G711-ALaw
 * ffmpeg -i lchs-slice.mp3 -ar 8000 -ac 1 -f alaw -acodec pcm_alaw lchs-slice.g711a-8k
 * ffplay -f alaw -ar 8000 -i lchs-slice.g711a-8k
 * }</pre>
 */
class G711ALawJt1078AudioCodecTest extends AbstractAudioVideoCodecTest {

    /// ```sh
    /// ffplay -f s16le -ar 8000 -i lchs-slice.g711a-8k.pcm
    ///```
    @Test
    void test() throws Exception {
        final String inputFile = "mock-data/g711/lchs-slice.g711a-8k";
        final String outputFile = "mock-data/output/g711/lchs-slice.g711a-8k.pcm";
        deleteFileIfExists(outputFile);

        final G711ALawJt1078AudioCodec codec = new G711ALawJt1078AudioCodec();
        try (
                final InputStream inputStream = getInputStream(inputFile);
                final OutputStream fileOutputStream = getMockDataOutputFileStream(outputFile)
        ) {
            Objects.requireNonNull(inputStream);

            final byte[] buffer = new byte[256];
            int readSize;
            while ((readSize = inputStream.read(buffer)) != -1) {
                final AudioPackage audioPackage = codec.toPcm(new AudioPackage(BuiltinAudioFormatOptions.G711_A_MONO, Unpooled.wrappedBuffer(buffer, 0, readSize)));
                fileOutputStream.write(XtreamBytes.getBytes(audioPackage.payload()));
                audioPackage.close();
            }
        }
    }
}

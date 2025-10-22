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
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.Jt1078AudioCodec;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public abstract class AbstractJt1078AudioCodec implements Jt1078AudioCodec {

    @Override
    public AudioPackage toPcm(AudioPackage source) {
        if (source.isEmpty()) {
            return AudioPackage.empty();
        }

        final ByteBuf maybeChanged = this.beforeAudioConvert(source.payload());
        return this.doDecodeAsPcm(maybeChanged, source.options());
    }

    protected abstract AudioPackage doDecodeAsPcm(ByteBuf stream, @Nullable AudioFormatOptions options);

    @SuppressWarnings("checkstyle:indentation")
    protected ByteBuf beforeAudioConvert(ByteBuf stream) {
        // 参考: https://www.hentai.org.cn/article?id=8
        // 参考: https://www.hentai.org.cn/article?id=8
        // 参考: https://www.hentai.org.cn/article?id=8
        // 音频数据体通常会带上"海思"头，它的形式如 00 01 XX 00
        // XX表示后续字节数的一半，如果碰到符合这个规则的前四个字节，直接去掉就可以了
        if (stream.getByte(0) == 0
            && stream.getByte(1) == 1
            && stream.getByte(2) == (stream.readableBytes() - 4) >>> 1
            && stream.getByte(3) == 0) {
            stream.readerIndex(stream.readerIndex() + 4);
        }
        return stream;
    }

}

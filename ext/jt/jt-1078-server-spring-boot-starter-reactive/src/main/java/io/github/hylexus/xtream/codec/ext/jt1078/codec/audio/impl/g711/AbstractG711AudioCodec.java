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

import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioFormatOptions;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.AbstractJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;

/**
 * 当前类是从 <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a> 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a>.
 *
 * @author hylexus
 */
public abstract class AbstractG711AudioCodec extends AbstractJt1078AudioCodec {

    protected final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    @Override
    protected AudioPackage doDecodeAsPcm(ByteBuf stream, @Nullable AudioFormatOptions options) {
        final int readableBytes = stream.readableBytes();
        final int length = readableBytes * 2;
        final ByteBuf buffer = this.allocator.buffer(length, length);
        while (stream.isReadable()) {
            final short value = this.decodeOne(stream.readByte());
            buffer.writeByte(value & 0xff);
            buffer.writeByte((value >> 8) & 0xff);
        }
        return new AudioPackage(BuiltinAudioFormatOptions.PCM_S16_LE_MONO, buffer);
    }

    protected abstract short decodeOne(byte input);

}

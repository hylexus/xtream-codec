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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.aac;

import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.Jt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.netty.buffer.ByteBuf;
import jakarta.annotation.Nonnull;

/**
 * 这个实现来自 <a href="https://gitee.com/hui_hui_zhou/open-source-repository">sky/jt1078</a> 的作者 sky
 */
public class AacJt1078AudioCodec implements Jt1078AudioCodec {

    @Nonnull
    @Override
    public AudioPackage toPcm(AudioPackage source) {
        final ByteBuf inputData = source.payload();
        if (inputData.getByte(0) == (byte) 0xff && inputData.getByte(1) == (byte) 0xF1) {
            // 去掉ADTS头
            inputData.skipBytes(7);
        }
        return AudioPackage.of(BuiltinAudioFormatOptions.AAC_STEREO, inputData.retain());
    }

}

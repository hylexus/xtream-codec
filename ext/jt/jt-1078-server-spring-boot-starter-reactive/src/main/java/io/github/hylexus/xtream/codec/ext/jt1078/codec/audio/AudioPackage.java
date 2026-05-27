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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public record AudioPackage(@Nullable AudioFormatOptions options, ByteBuf payload, int payloadSize) {
    private static final ByteBuf EMPTY_PAYLOAD = Unpooled.buffer(0);
    private static final AudioPackage EMPTY_AUDIO_PACKAGE = new AudioPackage(BuiltinAudioFormatOptions.SILENCE, EMPTY_PAYLOAD, 0);

    @SuppressWarnings("redundent")
    public AudioPackage {
    }

    public static AudioPackage of(AudioFormatOptions options, ByteBuf payload) {
        return new AudioPackage(options, payload);
    }

    public AudioPackage(@Nullable AudioFormatOptions options, @Nullable ByteBuf payload) {
        this(options, payload != null ? payload : EMPTY_PAYLOAD, payload != null ? payload.readableBytes() : 0);
    }

    @SuppressWarnings("ReferenceEquality")
    public boolean isEmpty() {
        return this == EMPTY_AUDIO_PACKAGE || this.payloadSize == 0 || this.payload.readableBytes() == 0;
    }

    /**
     * 浅复制
     */
    public AudioPackage shallowCopy() {
        if (this.isEmpty()) {
            return EMPTY_AUDIO_PACKAGE;
        }
        return new AudioPackage(this.options, this.payload.slice());
    }

    public static AudioPackage empty() {
        return EMPTY_AUDIO_PACKAGE;
    }

    public void close() {
        XtreamBytes.releaseBuf(this.payload);
    }
}

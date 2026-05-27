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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.Jt1078AudioCodec;
import jakarta.annotation.Nonnull;

public class SilenceJt1078AudioCodec implements Jt1078AudioCodec {
    public static final SilenceJt1078AudioCodec INSTANCE = new SilenceJt1078AudioCodec();

    public SilenceJt1078AudioCodec() {
    }

    @Nonnull
    @Override
    public AudioPackage toPcm(AudioPackage source) {
        return AudioPackage.empty();
    }

}

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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264Nalu;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.h264.H264NaluHeader;
import io.netty.buffer.ByteBuf;

import java.util.StringJoiner;

public class DefaultH264Nalu implements H264Nalu {

    private final H264NaluHeader header;
    private final ByteBuf data;

    public DefaultH264Nalu(H264NaluHeader header, ByteBuf data) {
        this.header = header;
        this.data = data;
    }

    @Override
    public H264NaluHeader header() {
        return this.header;
    }

    @Override
    public ByteBuf rbsp() {
        return this.data;
    }

    final String formatRbsp() {
        final ByteBuf payload = this.data;
        if (payload == null) {
            return "<NULL>";
        }
        final int readableBytes = payload.readableBytes();
        if (readableBytes <= 0) {
            return "<EMPTY>";
        }
        return "ByteBuf[readableBytes=" + readableBytes + ", data=[" + FormatUtils.toString(payload, 0, Math.min(5, readableBytes), ",") + "...] ]";
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultH264Nalu.class.getSimpleName() + "[", "]")
                .add("header=" + header)
                .add("rbsp=" + formatRbsp())
                .toString();
    }

}

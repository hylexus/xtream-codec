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


import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.VideoFlvTag;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("NullAway")
public class DefaultVideoFlvFlvTagHeader implements VideoFlvTag.VideoFlvTagHeader, VideoFlvTag.VideoFlvTagHeaderBuilder {
    private VideoFlvTag.VideoFrameType frameType;
    private VideoFlvTag.VideoCodecId codecId;
    private VideoFlvTag.VideoAvcPacketType avcPacketType;
    private Integer compositionTime;

    public DefaultVideoFlvFlvTagHeader() {
    }

    @Override
    public VideoFlvTag.VideoFrameType frameType() {
        return this.frameType;
    }

    @Override
    public VideoFlvTag.VideoFlvTagHeaderBuilder frameType(VideoFlvTag.VideoFrameType frameType) {
        this.frameType = frameType;
        return this;
    }

    @Override
    public VideoFlvTag.VideoCodecId codecId() {
        return this.codecId;
    }

    @Override
    public VideoFlvTag.VideoFlvTagHeaderBuilder codecId(VideoFlvTag.VideoCodecId codecId) {
        this.codecId = codecId;
        return this;
    }

    @Override
    public Optional<VideoFlvTag.VideoAvcPacketType> avcPacketType() {
        return Optional.ofNullable(this.avcPacketType);
    }

    @Override
    public VideoFlvTag.VideoFlvTagHeaderBuilder avcPacketType(VideoFlvTag.VideoAvcPacketType avcPacketType) {
        this.avcPacketType = avcPacketType;
        return this;
    }

    @Override
    public Optional<Integer> compositionTime() {
        return Optional.ofNullable(this.compositionTime);
    }

    @Override
    public VideoFlvTag.VideoFlvTagHeaderBuilder compositionTime(int compositionTime) {
        this.compositionTime = compositionTime;
        return this;
    }

    @Override
    public VideoFlvTag.VideoFlvTagHeader build() {
        requireNonNull(this.frameType, "frameType is null");
        requireNonNull(this.codecId, "codecId is null");
        return this;
    }

}

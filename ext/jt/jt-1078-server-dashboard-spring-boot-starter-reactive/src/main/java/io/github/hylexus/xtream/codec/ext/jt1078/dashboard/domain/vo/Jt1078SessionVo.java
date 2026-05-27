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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.StringJoiner;

public class Jt1078SessionVo {
    private String id;
    private int simLength;
    private String convertedSim;
    private String rawSim;
    private XtreamInbound.Type protocolType;
    private @Nullable Jt1078PayloadType audioType;
    private @Nullable Jt1078PayloadType videoType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant creationTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant lastCommunicateTime;

    public String getId() {
        return id;
    }

    public Jt1078SessionVo setId(String id) {
        this.id = id;
        return this;
    }

    public int getSimLength() {
        return simLength;
    }

    public Jt1078SessionVo setSimLength(int simLength) {
        this.simLength = simLength;
        return this;
    }

    public String getConvertedSim() {
        return convertedSim;
    }

    public Jt1078SessionVo setConvertedSim(String convertedSim) {
        this.convertedSim = convertedSim;
        return this;
    }

    public String getRawSim() {
        return rawSim;
    }

    public Jt1078SessionVo setRawSim(String rawSim) {
        this.rawSim = rawSim;
        return this;
    }

    public XtreamInbound.Type getProtocolType() {
        return protocolType;
    }

    public Jt1078SessionVo setProtocolType(XtreamInbound.Type protocolType) {
        this.protocolType = protocolType;
        return this;
    }

    public @Nullable Jt1078PayloadType getAudioType() {
        return audioType;
    }

    public Jt1078SessionVo setAudioType(@Nullable Jt1078PayloadType audioType) {
        this.audioType = audioType;
        return this;
    }

    public @Nullable Jt1078PayloadType getVideoType() {
        return videoType;
    }

    public Jt1078SessionVo setVideoType(@Nullable Jt1078PayloadType videoType) {
        this.videoType = videoType;
        return this;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Jt1078SessionVo setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Instant getLastCommunicateTime() {
        return lastCommunicateTime;
    }

    public Jt1078SessionVo setLastCommunicateTime(Instant lastCommunicateTime) {
        this.lastCommunicateTime = lastCommunicateTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt1078SessionVo.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("simLength=" + simLength)
                .add("convertedSim='" + convertedSim + "'")
                .add("rawSim='" + rawSim + "'")
                .add("protocolType=" + protocolType)
                .add("audioType=" + audioType)
                .add("videoType=" + videoType)
                .add("creationTime=" + creationTime)
                .add("lastCommunicateTime=" + lastCommunicateTime)
                .toString();
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto;

import io.github.hylexus.xtream.codec.base.web.domain.dto.PageableDto;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;

import java.util.StringJoiner;


public class Jt1078SessionQueryDto extends PageableDto {
    private String sim;
    private XtreamInbound.Type protocolType;
    private Jt1078PayloadType audioType;
    private Jt1078PayloadType videoType;

    public String getSim() {
        return sim;
    }

    public Jt1078SessionQueryDto setSim(String sim) {
        this.sim = sim;
        return this;
    }

    public XtreamInbound.Type getProtocolType() {
        return protocolType;
    }

    public Jt1078SessionQueryDto setProtocolType(XtreamInbound.Type protocolType) {
        this.protocolType = protocolType;
        return this;
    }

    public Jt1078PayloadType getAudioType() {
        return audioType;
    }

    public Jt1078SessionQueryDto setAudioType(Jt1078PayloadType audioType) {
        this.audioType = audioType;
        return this;
    }

    public Jt1078PayloadType getVideoType() {
        return videoType;
    }

    public Jt1078SessionQueryDto setVideoType(Jt1078PayloadType videoType) {
        this.videoType = videoType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt1078SessionQueryDto.class.getSimpleName() + "[", "]")
                .add("sim='" + sim + "'")
                .add("protocolType=" + protocolType)
                .add("audioType=" + audioType)
                .add("videoType=" + videoType)
                .toString();
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;

import java.time.LocalDateTime;
import java.util.Map;

public interface Jt1078SubscriberDescriptor {
    @JsonProperty("id")
    String id();

    @JsonProperty("convertedSim")
    String convertedSim();

    @JsonProperty("rawSim")
    String rawSim();

    @JsonProperty("channel")
    short channel();

    @JsonProperty("rawAudioType")
    Jt1078PayloadType rawAudioType();

    @JsonProperty("rawVideoType")
    Jt1078PayloadType rawVideoType();

    @JsonProperty("convertedAudioType")
    String convertedAudioType();

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    LocalDateTime createdAt();

    @JsonProperty("desc")
    default String desc() {
        return "Jt1078Subscriber";
    }

    @JsonProperty("metadata")
    Map<String, Object> metadata();

}

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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@SuperBuilder
@NoArgsConstructor
@SuppressWarnings("NullAway")
public class Jt1078SubscriberCreator {
    protected String convertedSim;
    protected String rawSim;
    protected short channelNumber;

    @Builder.Default
    protected boolean hasAudio = true;

    @Builder.Default
    protected boolean hasVideo = true;

    protected Duration timeout;
    protected String desc;
    protected Map<String, Object> metadata;

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt1078SubscriberCreator.class.getSimpleName() + "[", "]")
                .add("convertedSim='" + convertedSim + "'")
                .add("rawSim='" + rawSim + "'")
                .add("channelNumber=" + channelNumber)
                .add("hasAudio=" + hasAudio)
                .add("hasVideo=" + hasVideo)
                .add("timeout=" + timeout)
                .add("desc='" + desc + "'")
                .add("metadata=" + metadata)
                .toString();
    }
}

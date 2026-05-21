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

import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

@SuppressWarnings({"NullAway", "NotNullFieldNotInitialized"})
public class Jt1078SubscriberCreator {
    protected String convertedSim;
    protected String rawSim;
    protected short channelNumber;
    protected boolean hasAudio = true;
    protected boolean hasVideo = true;
    protected Duration timeout;
    protected String desc;
    protected Map<String, Object> metadata;

    public Jt1078SubscriberCreator() {
    }

    protected Jt1078SubscriberCreator(Jt1078SubscriberCreatorBuilder<?, ?> b) {
        this.convertedSim = b.convertedSim;
        this.rawSim = b.rawSim;
        this.channelNumber = b.channelNumber;
        this.hasAudio = b.hasAudio;
        this.hasVideo = b.hasVideo;
        this.timeout = b.timeout;
        this.desc = b.desc;
        this.metadata = b.metadata;
    }

    public String convertedSim() {
        return convertedSim;
    }

    public Jt1078SubscriberCreator convertedSim(String convertedSim) {
        this.convertedSim = convertedSim;
        return this;
    }

    public String rawSim() {
        return rawSim;
    }

    public Jt1078SubscriberCreator rawSim(String rawSim) {
        this.rawSim = rawSim;
        return this;
    }

    public short channelNumber() {
        return channelNumber;
    }

    public Jt1078SubscriberCreator channelNumber(short channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    public boolean hasAudio() {
        return hasAudio;
    }

    public Jt1078SubscriberCreator hasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
        return this;
    }

    public boolean hasVideo() {
        return hasVideo;
    }

    public Jt1078SubscriberCreator hasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
        return this;
    }

    public Duration timeout() {
        return timeout;
    }

    public Jt1078SubscriberCreator timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public String desc() {
        return desc;
    }

    public Jt1078SubscriberCreator desc(String desc) {
        this.desc = desc;
        return this;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public Jt1078SubscriberCreator metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public static Jt1078SubscriberCreatorBuilder<?, ?> builder() {
        return new Jt1078SubscriberCreatorBuilder<>();
    }

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

    @SuppressWarnings("NotNullFieldNotInitialized")
    public static class Jt1078SubscriberCreatorBuilder<C extends Jt1078SubscriberCreator, B extends Jt1078SubscriberCreatorBuilder<C, B>> {
        protected String convertedSim;
        protected String rawSim;
        protected short channelNumber;
        protected boolean hasAudio = true;
        protected boolean hasVideo = true;
        protected Duration timeout;
        protected String desc;
        protected Map<String, Object> metadata;

        public Jt1078SubscriberCreatorBuilder() {
        }

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B convertedSim(String convertedSim) {
            this.convertedSim = convertedSim;
            return self();
        }

        public B rawSim(String rawSim) {
            this.rawSim = rawSim;
            return self();
        }

        public B channelNumber(short channelNumber) {
            this.channelNumber = channelNumber;
            return self();
        }

        public B hasAudio(boolean hasAudio) {
            this.hasAudio = hasAudio;
            return self();
        }

        public B hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return self();
        }

        public B timeout(Duration timeout) {
            this.timeout = timeout;
            return self();
        }

        public B desc(String desc) {
            this.desc = desc;
            return self();
        }

        public B metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return self();
        }

        public C build() {
            return createInstance();
        }

        @SuppressWarnings("unchecked")
        protected C createInstance() {
            return (C) new Jt1078SubscriberCreator(this);
        }
    }
}

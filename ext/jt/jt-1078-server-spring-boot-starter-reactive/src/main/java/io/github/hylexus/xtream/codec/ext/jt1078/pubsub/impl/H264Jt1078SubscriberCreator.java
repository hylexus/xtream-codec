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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl;

import io.github.hylexus.xtream.codec.core.utils.ByteRingBuffer;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078SubscriberCreator;

import java.util.StringJoiner;

public class H264Jt1078SubscriberCreator extends Jt1078SubscriberCreator {

    private H264Meta h264Meta = new H264Meta(1 << 18);

    public H264Jt1078SubscriberCreator() {
    }

    protected H264Jt1078SubscriberCreator(H264Jt1078SubscriberCreatorBuilder<?, ?> b) {
        super(b);
        this.h264Meta = b.h264Meta;
    }

    public H264Meta h264Meta() {
        return h264Meta;
    }

    public H264Jt1078SubscriberCreator h264Meta(H264Meta h264Meta) {
        this.h264Meta = h264Meta;
        return this;
    }

    public static H264Jt1078SubscriberCreatorBuilder<?, ?> builder() {
        return new H264Jt1078SubscriberCreatorBuilder<>();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", H264Jt1078SubscriberCreator.class.getSimpleName() + "[", "]")
                .add("convertedSim='" + convertedSim() + "'")
                .add("rawSim='" + rawSim() + "'")
                .add("channelNumber=" + channelNumber())
                .add("hasAudio=" + hasAudio())
                .add("hasVideo=" + hasVideo())
                .add("h264Meta=" + h264Meta)
                .add("timeout=" + timeout())
                .add("desc='" + desc() + "'")
                .add("metadata=" + metadata())
                .toString();
    }

    public record H264Meta(int naluDecoderRingBufferSize) {

        public H264Meta {
            if (ByteRingBuffer.isInvalidCapacity(naluDecoderRingBufferSize)) {
                throw new IllegalArgumentException("naluDecoderRingBufferSize must be a power of 2");
            }
        }
    }

    public static class H264Jt1078SubscriberCreatorBuilder<C extends H264Jt1078SubscriberCreator, B extends H264Jt1078SubscriberCreatorBuilder<C, B>>
            extends Jt1078SubscriberCreatorBuilder<C, B> {
        private H264Meta h264Meta = new H264Meta(1 << 18);

        public H264Jt1078SubscriberCreatorBuilder() {
        }

        public B h264Meta(H264Meta h264Meta) {
            this.h264Meta = h264Meta;
            return self();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected C createInstance() {
            return (C) new H264Jt1078SubscriberCreator(this);
        }
    }
}

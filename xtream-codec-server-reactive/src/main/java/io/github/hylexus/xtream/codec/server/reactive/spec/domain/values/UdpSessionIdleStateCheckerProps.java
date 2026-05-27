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

package io.github.hylexus.xtream.codec.server.reactive.spec.domain.values;


import java.time.Duration;
import java.util.StringJoiner;

@SuppressWarnings("all")
public class UdpSessionIdleStateCheckerProps {
    private Duration maxIdleTime = Duration.ofMinutes(20);
    private Duration checkInterval = Duration.ofSeconds(10);
    private Duration checkBackoffTime = Duration.ofSeconds(10);

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    public UdpSessionIdleStateCheckerProps setMaxIdleTime(Duration maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public UdpSessionIdleStateCheckerProps setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }

    public Duration getCheckBackoffTime() {
        return checkBackoffTime;
    }

    public UdpSessionIdleStateCheckerProps setCheckBackoffTime(Duration checkBackoffTime) {
        this.checkBackoffTime = checkBackoffTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UdpSessionIdleStateCheckerProps.class.getSimpleName() + "[", "]")
                .add("maxIdleTime=" + maxIdleTime)
                .add("checkInterval=" + checkInterval)
                .add("checkBackoffTime=" + checkBackoffTime)
                .toString();
    }
}

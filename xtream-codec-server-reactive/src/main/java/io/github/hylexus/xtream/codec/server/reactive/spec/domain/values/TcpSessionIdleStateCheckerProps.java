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

package io.github.hylexus.xtream.codec.server.reactive.spec.domain.values;

import io.netty.handler.timeout.IdleStateHandler;

import java.time.Duration;
import java.util.StringJoiner;

/**
 * @see IdleStateHandler
 */
public class TcpSessionIdleStateCheckerProps {
    /**
     * @see IdleStateHandler#getReaderIdleTimeInMillis()
     */
    Duration readerIdleTime = Duration.ZERO;

    /**
     * @see IdleStateHandler#getWriterIdleTimeInMillis()
     */
    Duration writerIdleTime = Duration.ZERO;

    /**
     * @see IdleStateHandler#getAllIdleTimeInMillis()
     */
    Duration allIdleTime = Duration.ofMinutes(20);

    public Duration getReaderIdleTime() {
        return readerIdleTime;
    }

    public TcpSessionIdleStateCheckerProps setReaderIdleTime(Duration readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
        return this;
    }

    public Duration getWriterIdleTime() {
        return writerIdleTime;
    }

    public TcpSessionIdleStateCheckerProps setWriterIdleTime(Duration writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
        return this;
    }

    public Duration getAllIdleTime() {
        return allIdleTime;
    }

    public TcpSessionIdleStateCheckerProps setAllIdleTime(Duration allIdleTime) {
        this.allIdleTime = allIdleTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TcpSessionIdleStateCheckerProps.class.getSimpleName() + "[", "]")
                .add("readerIdleTime=" + readerIdleTime)
                .add("writerIdleTime=" + writerIdleTime)
                .add("allIdleTime=" + allIdleTime)
                .toString();
    }

}

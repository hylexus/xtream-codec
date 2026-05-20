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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.StringJoiner;
import java.util.concurrent.atomic.LongAdder;

@SuppressWarnings("NullAway")
public final class XtreamServerErrorInfo {

    public XtreamServerErrorInfo() {
    }

    private LongAdder count = new LongAdder();
    private Serializable details;

    private @Nullable String lastErrorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant lastErrorTime;

    public void incrementCount() {
        this.count.increment();
    }

    public LongAdder getCount() {
        return count;
    }

    public XtreamServerErrorInfo setCount(LongAdder count) {
        this.count = count;
        return this;
    }

    public Serializable getDetails() {
        return details;
    }

    public XtreamServerErrorInfo setDetails(Serializable details) {
        this.details = details;
        return this;
    }

    public @Nullable String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public XtreamServerErrorInfo setLastErrorMessage(@Nullable String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
        return this;
    }

    public Instant getLastErrorTime() {
        return lastErrorTime;
    }

    public XtreamServerErrorInfo setLastErrorTime(Instant lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", XtreamServerErrorInfo.class.getSimpleName() + "[", "]")
                .add("count=" + count)
                .add("details=" + details)
                .add("lastErrorMessage='" + lastErrorMessage + "'")
                .add("lastErrorTime=" + lastErrorTime)
                .toString();
    }
}

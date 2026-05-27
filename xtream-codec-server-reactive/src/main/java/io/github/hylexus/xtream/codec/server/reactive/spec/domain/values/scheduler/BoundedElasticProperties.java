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

package io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.scheduler;

import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.StringJoiner;

public class BoundedElasticProperties {
    private String threadNamePrefix = "x-bounded-elastic";
    private boolean daemon = true;
    private boolean rejectBlockingTask = true;

    private int threadCapacity = Math.max(16, Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE);
    private int queuedTaskCapacity = Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
    private Duration ttl = Duration.ofMinutes(1);

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public BoundedElasticProperties setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        return this;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public BoundedElasticProperties setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public boolean isRejectBlockingTask() {
        return rejectBlockingTask;
    }

    public BoundedElasticProperties setRejectBlockingTask(boolean rejectBlockingTask) {
        this.rejectBlockingTask = rejectBlockingTask;
        return this;
    }

    public int getThreadCapacity() {
        return threadCapacity;
    }

    public BoundedElasticProperties setThreadCapacity(int threadCapacity) {
        this.threadCapacity = threadCapacity;
        return this;
    }

    public int getQueuedTaskCapacity() {
        return queuedTaskCapacity;
    }

    public BoundedElasticProperties setQueuedTaskCapacity(int queuedTaskCapacity) {
        this.queuedTaskCapacity = queuedTaskCapacity;
        return this;
    }

    public Duration getTtl() {
        return ttl;
    }

    public BoundedElasticProperties setTtl(Duration ttl) {
        this.ttl = ttl;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BoundedElasticProperties.class.getSimpleName() + "[", "]")
                .add("threadNamePrefix='" + threadNamePrefix + "'")
                .add("daemon=" + daemon)
                .add("rejectBlockingTask=" + rejectBlockingTask)
                .add("threadCapacity=" + threadCapacity)
                .add("queuedTaskCapacity=" + queuedTaskCapacity)
                .add("ttl=" + ttl)
                .toString();
    }

}

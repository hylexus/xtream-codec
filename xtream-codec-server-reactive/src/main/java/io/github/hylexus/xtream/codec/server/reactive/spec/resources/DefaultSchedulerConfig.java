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

package io.github.hylexus.xtream.codec.server.reactive.spec.resources;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public record DefaultSchedulerConfig(
        String name,
        boolean virtualThread,
        boolean rejectBlocking,
        boolean metricsEnabled,
        String metricsPrefix,
        @Nullable String remark,
        Map<String, Serializable> metadata) implements XtreamSchedulerRegistry.SchedulerConfig {

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultSchedulerConfig.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("metricsEnabled=" + metricsEnabled)
                .add("metricsPrefix='" + metricsPrefix + "'")
                .add("metadata=" + metadata)
                .toString();
    }

    public static class DefaultSchedulerConfigBuilder implements XtreamSchedulerRegistry.SchedulerConfigBuilder {
        private @Nullable String name;
        private boolean virtualThread;
        private boolean rejectBlocking;
        private boolean metricsEnabled;
        private @Nullable String metricsPrefix;
        private @Nullable String remark;
        private @Nullable Map<String, Serializable> metadata;

        @Override
        public DefaultSchedulerConfigBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public XtreamSchedulerRegistry.SchedulerConfigBuilder virtualThread(boolean virtualThread) {
            this.virtualThread = virtualThread;
            return this;
        }

        @Override
        public XtreamSchedulerRegistry.SchedulerConfigBuilder rejectBlocking(boolean rejectBlocking) {
            this.rejectBlocking = rejectBlocking;
            return this;
        }

        @Override
        public DefaultSchedulerConfigBuilder metricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
            return this;
        }

        @Override
        public DefaultSchedulerConfigBuilder metricsPrefix(String metricsPrefix) {
            this.metricsPrefix = metricsPrefix;
            return this;
        }

        @Override
        public XtreamSchedulerRegistry.SchedulerConfigBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        @Override
        public DefaultSchedulerConfigBuilder metadata(Map<String, Serializable> metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public XtreamSchedulerRegistry.SchedulerConfig build() {
            if (this.metricsEnabled) {
                if (!StringUtils.hasText(this.metricsPrefix)) {
                    throw new IllegalArgumentException("metricsPrefix cannot be empty when metricsEnabled is true");
                }
            }
            return new DefaultSchedulerConfig(
                    Objects.requireNonNull(name),
                    virtualThread,
                    rejectBlocking,
                    metricsEnabled,
                    Objects.requireNonNullElse(metricsPrefix, ""),
                    remark,
                    Objects.requireNonNullElse(metadata, new LinkedHashMap<>())
            );
        }
    }
}

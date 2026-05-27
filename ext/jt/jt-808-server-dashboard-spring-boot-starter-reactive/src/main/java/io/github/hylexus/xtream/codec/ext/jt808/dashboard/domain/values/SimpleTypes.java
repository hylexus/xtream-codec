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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

public final class SimpleTypes {
    private SimpleTypes() {
    }

    public record RequestInfo(LongAdder total, ConcurrentMap<String, RequestDetail> details) {
        public void incrementTotal() {
            this.total.increment();
        }
    }

    public record RequestDetail(int messageId, LongAdder count, String desc) {
        public void incrementCount() {
            this.count.increment();
        }

        // for jackson
        @SuppressWarnings("unused")
        public String getMessageIdAsHexString() {
            return "0x" + FormatUtils.toHexString(messageId, 4);
        }
    }

    @SuppressWarnings("all")
    public static final class SessionInfo {
        private long max;
        private long current;

        public SessionInfo() {
            this.max = 0;
            this.current = 0;
        }

        public void setMax(long value) {
            this.max = Math.max(this.max, value);
        }

        public long getMax() {
            return max;
        }

        public long getCurrent() {
            return current;
        }

        public SessionInfo setCurrent(long current) {
            this.current = current;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SessionInfo.class.getSimpleName() + "[", "]")
                    .add("max=" + max)
                    .add("current=" + current)
                    .toString();
        }
    }

    public record SimpleJvmThreadMetrics(int peak, int daemon, int live, long started, Map<String, Long> states) {
    }

    public record Vendor(String name, String version) {
    }

    public record Runtime(String name, String version) {
    }

    public record Jvm(String name, String vendor, String version) {
    }

    public record OsInfo(String name, String version, String arch) {
        public static final OsInfo INSTANCE;

        static {
            INSTANCE = new OsInfo(
                    System.getProperty("os.name", "unknown"),
                    System.getProperty("os.version", "unknown"),
                    System.getProperty("os.arch", "unknown")
            );
        }
    }

    public record JavaInfo(
            String version,
            Vendor vendor,
            Runtime runtime,
            Jvm jvm) {
        public static final JavaInfo INSTANCE;

        static {
            INSTANCE = new JavaInfo(
                    System.getProperty("java.version", "unknown"),
                    new Vendor(
                            System.getProperty("java.vendor", "unknown"),
                            System.getProperty("java.vendor.version", "unknown")
                    ),
                    new Runtime(
                            System.getProperty("java.runtime.name", "unknown"),
                            System.getProperty("java.runtime.version", "unknown")
                    ),
                    new Jvm(
                            System.getProperty("java.vm.name", "unknown"),
                            System.getProperty("java.vm.vendor", "unknown"),
                            System.getProperty("java.vm.version", "unknown")
                    )
            );
        }
    }

    public record TimeSeries<T>(
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
            LocalDateTime time,
            T value) {

        public static <T> TimeSeries<T> of(LocalDateTime time, T value) {
            return new TimeSeries<>(time, value);
        }
    }

    public record DependencyInfo(String name, String version) {
    }

    public record Jt808EntityClassMetadata(
            Class<?> targetClass,
            int messageId,
            byte encryptionType,
            int maxPackageSize,
            byte reversedBit15InHeader,
            String desc
    ) {
    }

    public record CodecDebugOptions(String defaultTerminalId, List<Jt808EntityClassMetadata> classMetadata) {
    }

}

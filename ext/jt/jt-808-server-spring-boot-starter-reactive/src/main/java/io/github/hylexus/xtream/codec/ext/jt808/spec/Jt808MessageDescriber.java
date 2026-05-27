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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.core.tracker.BaseSpan;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.RootSpan;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author hylexus
 */
public class Jt808MessageDescriber {
    protected Jt808ProtocolVersion version;
    protected String terminalId;
    protected Integer messageId;
    protected int maxPackageSize = 1024;
    protected byte reversedBit15InHeader = 0;
    protected byte encryptionType = 0b000;
    protected int flowId = -1;
    protected @Nullable Jt808FlowIdGenerator flowIdGenerator;
    protected @Nullable List<Tracker> trackers;
    protected boolean withTracker;

    public Jt808MessageDescriber(Integer messageId, Jt808ProtocolVersion version, String terminalId) {
        this.messageId = messageId;
        this.version = version;
        this.terminalId = terminalId;
    }

    /**
     * 仅调试用。会对性能有一定影响。
     */
    public Jt808MessageDescriber enableTracker() {
        this.withTracker = true;
        if (this.trackers == null) {
            this.trackers = new ArrayList<>();
        }
        return this;
    }

    public @Nullable List<Tracker> trackers() {
        return this.trackers;
    }

    public Jt808ProtocolVersion version() {
        return version;
    }

    public Jt808MessageDescriber version(Jt808ProtocolVersion version) {
        this.version = requireNonNull(version);
        return this;
    }

    public String terminalId() {
        return terminalId;
    }

    public Jt808MessageDescriber terminalId(String terminalId) {
        this.terminalId = requireNonNull(terminalId);
        return this;
    }

    public Integer messageId() {
        return messageId;
    }

    public Jt808MessageDescriber messageId(Integer messageId) {
        this.messageId = requireNonNull(messageId);
        return this;
    }

    public int maxPackageSize() {
        return maxPackageSize;
    }

    public Jt808MessageDescriber maxPackageSize(int maxPackageSize) {
        this.maxPackageSize = maxPackageSize;
        return this;
    }

    public byte reversedBit15InHeader() {
        return reversedBit15InHeader;
    }

    public Jt808MessageDescriber reversedBit15InHeader(byte reversedBit15InHeader) {
        this.reversedBit15InHeader = reversedBit15InHeader;
        return this;
    }

    public byte encryptionType() {
        return encryptionType;
    }

    public Jt808MessageDescriber encryptionType(byte encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    public int flowId() {
        return flowId;
    }

    public Jt808MessageDescriber flowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public @Nullable Jt808FlowIdGenerator flowIdGenerator() {
        return this.flowIdGenerator;
    }

    public Jt808MessageDescriber flowIdGenerator(Jt808FlowIdGenerator flowIdGenerator) {
        this.flowIdGenerator = flowIdGenerator;
        return this;
    }

    public Jt808MessageDescriber check() {
        if (this.maxPackageSize() <= 0) {
            throw new IllegalArgumentException("maxPackageSize() is invalid");
        }
        return this;
    }

    public void visitTracker() {
        this.visitTracker((currentPackageNo, totalPackageNo, level, span) -> {
            // ...
            System.out.println("#" + currentPackageNo + "/" + totalPackageNo + "\t".repeat(level) + span);
        });
    }

    public void visitTracker(Jt808ResponseEncoderTrackerVisitor visitor) {
        final List<Tracker> trackers = requireNonNull(this.trackers);
        final int totalPackage = trackers.size();
        for (int i = 0; i < totalPackage; i++) {
            final Tracker tracker = trackers.get(i);
            final int packageNo = i;
            CodecTracker.visitTracker(0, tracker.getDetails(), (level, span) -> {
                visitor.visit(packageNo + 1, totalPackage, level, span);
            });
        }
    }

    @FunctionalInterface
    public interface Jt808ResponseEncoderTrackerVisitor {
        void visit(int currentPackageNo, int totalPackageNo, int level, BaseSpan span);
    }

    @SuppressWarnings("NullAway")
    public static class Tracker {
        private String rawHexString;
        private String escapedHexString;
        private RootSpan details;

        public Tracker() {
        }

        public String getRawHexString() {
            return rawHexString;
        }

        public Tracker setRawHexString(String rawHexString) {
            this.rawHexString = rawHexString;
            return this;
        }

        public String getEscapedHexString() {
            return escapedHexString;
        }

        public Tracker setEscapedHexString(String escapedHexString) {
            this.escapedHexString = escapedHexString;
            return this;
        }

        public RootSpan getDetails() {
            return details;
        }

        public Tracker setDetails(RootSpan details) {
            this.details = details;
            return this;
        }
    }

}

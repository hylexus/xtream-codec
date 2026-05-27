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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import io.github.hylexus.xtream.codec.core.tracker.RootSpan;

import java.util.List;

public class DecodedMessageVo {
    private Single single;
    private Multiple multiple;

    public DecodedMessageVo(Multiple multiple) {
        this.multiple = multiple;
    }

    public DecodedMessageVo(Single single) {
        this.single = single;
    }

    public Single getSingle() {
        return single;
    }

    public DecodedMessageVo setSingle(Single single) {
        this.single = single;
        return this;
    }

    public Multiple getMultiple() {
        return multiple;
    }

    public DecodedMessageVo setMultiple(Multiple multiple) {
        this.multiple = multiple;
        return this;
    }

    public static class Single {
        private String rawHexString;
        private String escapedHexString;
        private RootSpan details;

        public String getRawHexString() {
            return rawHexString;
        }

        public Single setRawHexString(String rawHexString) {
            this.rawHexString = rawHexString;
            return this;
        }

        public String getEscapedHexString() {
            return escapedHexString;
        }

        public Single setEscapedHexString(String escapedHexString) {
            this.escapedHexString = escapedHexString;
            return this;
        }

        public RootSpan getDetails() {
            return details;
        }

        public Single setDetails(RootSpan details) {
            this.details = details;
            return this;
        }
    }

    public record SubPackageMetadata(Jt808MessageHeaderMetadata header, String bodyHexString) {
    }

    public static class Multiple {
        private List<SubPackageMetadata> subPackageMetadata;
        private String mergedHexString;
        private RootSpan details;

        public List<SubPackageMetadata> getSubPackageMetadata() {
            return subPackageMetadata;
        }

        public Multiple setSubPackageMetadata(List<SubPackageMetadata> subPackageMetadata) {
            this.subPackageMetadata = subPackageMetadata;
            return this;
        }

        public String getMergedHexString() {
            return mergedHexString;
        }

        public Multiple setMergedHexString(String mergedHexString) {
            this.mergedHexString = mergedHexString;
            return this;
        }

        public RootSpan getDetails() {
            return details;
        }

        public Multiple setDetails(RootSpan details) {
            this.details = details;
            return this;
        }
    }
}

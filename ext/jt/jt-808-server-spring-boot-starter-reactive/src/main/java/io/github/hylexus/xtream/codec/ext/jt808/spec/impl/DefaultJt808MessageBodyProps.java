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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

/**
 * @author hylexus
 */
public class DefaultJt808MessageBodyProps implements Jt808RequestHeader.Jt808MessageBodyProps {

    private final int intValue;

    public DefaultJt808MessageBodyProps(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public int intValue() {
        return intValue;
    }

    @Override
    public String toString() {
        return "MessageBodyProps{"
               + "intValue=" + intValue
               + ", messageBodyLength=" + messageBodyLength()
               + ", hasSubPackage=" + hasSubPackage()
               + ", encryptionType=" + encryptionType()
               + '}';
    }

    public static class DefaultJt808MessageBodyPropsBuilder implements Jt808RequestHeader.Jt808MessageBodyPropsBuilder {
        private int intValue;

        public DefaultJt808MessageBodyPropsBuilder() {
        }

        public DefaultJt808MessageBodyPropsBuilder(int intValue) {
            this.intValue = intValue;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder messageBodyLength(int messageBodyLength) {
            this.intValue = XtreamUtils.setBitRange(messageBodyLength, 10, intValue, 0);
            return this;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder encryptionType(int encryptionType) {
            this.intValue = XtreamUtils.setBitRange(encryptionType, 3, intValue, 10);
            return this;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder hasSubPackage(int subPackageIdentifier) {
            this.intValue = XtreamUtils.setBitRange(subPackageIdentifier, 1, intValue, 13);
            return this;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder hasSubPackage(boolean hasSubPackage) {
            return this.hasSubPackage(hasSubPackage ? 1 : 0);
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder versionIdentifier(int versionIdentifier) {
            this.intValue = XtreamUtils.setBitRange(versionIdentifier, 1, intValue, 14);
            return this;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder versionIdentifier(Jt808ProtocolVersion version) {
            return this.versionIdentifier(version.versionBit());
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyPropsBuilder reversedBit15(int reversedBit15) {
            this.intValue = XtreamUtils.setBitRange(reversedBit15, 1, intValue, 15);
            return this;
        }

        @Override
        public Jt808RequestHeader.Jt808MessageBodyProps build() {
            return new DefaultJt808MessageBodyProps(intValue);
        }
    }
}

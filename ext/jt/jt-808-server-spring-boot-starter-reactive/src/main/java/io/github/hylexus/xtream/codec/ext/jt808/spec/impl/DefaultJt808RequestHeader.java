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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

/**
 * @author hylexus
 */
public class DefaultJt808RequestHeader implements Jt808RequestHeader {

    private Jt808ProtocolVersion version;

    // byte[0-1] 消息ID
    private int messageId;

    // byte[2-3] 消息体属性
    private Jt808MessageBodyProps messageBodyProps;

    // byte[4-9] 终端Id
    private String terminalId;

    // byte[10-11] 流水号
    private int flowId;

    private Jt808SubPackageProps subPackageProps;

    @SuppressWarnings("NullAway.Init")
    public DefaultJt808RequestHeader() {
    }

    public DefaultJt808RequestHeader(
            Jt808ProtocolVersion version, int messageId, Jt808MessageBodyProps messageBodyProps, String terminalId, int flowId, Jt808SubPackageProps subPackageProps) {
        this.version = version;
        this.messageId = messageId;
        this.messageBodyProps = messageBodyProps;
        this.terminalId = terminalId;
        this.flowId = flowId;
        this.subPackageProps = subPackageProps;
    }

    public DefaultJt808RequestHeader version(Jt808ProtocolVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public Jt808ProtocolVersion version() {
        return version;
    }

    public DefaultJt808RequestHeader messageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    @Override
    public int messageId() {
        return messageId;
    }

    public DefaultJt808RequestHeader subPackageProps(Jt808SubPackageProps subPackageProps) {
        this.subPackageProps = subPackageProps;
        return this;
    }

    public DefaultJt808RequestHeader messageBodyProps(Jt808MessageBodyProps messageBodyProps) {
        this.messageBodyProps = messageBodyProps;
        return this;
    }

    @Override
    public Jt808MessageBodyProps messageBodyProps() {
        return messageBodyProps;
    }

    public DefaultJt808RequestHeader terminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    @Override
    public String terminalId() {
        return terminalId;
    }

    public DefaultJt808RequestHeader flowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    @Override
    public int flowId() {
        return flowId;
    }

    @Override
    public Jt808SubPackageProps subPackage() {
        return this.subPackageProps;
    }

    @Override
    public String toString() {
        return "HeaderSpec{"
               + "version=" + version
               + ", terminalId='" + terminalId + '\''
               + ", messageId=" + messageId + "(0x" + FormatUtils.toHexString(messageId, 4) + ")"
               + ", flowId=" + flowId
               + ", messageBodyProps=" + messageBodyProps
               + '}';
    }

    public static class DefaultJt808MessageHeaderBuilder implements Jt808MessageHeaderBuilder {

        private Jt808ProtocolVersion version;
        private int messageId;
        private Jt808MessageBodyProps messageBodyProps;
        private String terminalId;
        private int flowId;
        private Jt808SubPackageProps subPackage;

        @SuppressWarnings("NullAway.Init")
        public DefaultJt808MessageHeaderBuilder() {
        }

        public DefaultJt808MessageHeaderBuilder(Jt808RequestHeader header) {
            this.messageId = header.messageId();
            this.messageBodyProps = header.messageBodyProps();
            this.terminalId = header.terminalId();
            this.flowId = header.flowId();
            this.version = header.version();
            this.subPackage = header.subPackage();
        }

        @Override
        public Jt808MessageHeaderBuilder version(Jt808ProtocolVersion version) {
            this.version = version;
            return this;
        }

        @Override
        public Jt808MessageHeaderBuilder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        @Override
        public Jt808MessageHeaderBuilder messageBodyProps(Jt808MessageBodyProps messageBodyProps) {
            this.messageBodyProps = messageBodyProps;
            return this;
        }

        @Override
        public Jt808MessageHeaderBuilder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        @Override
        public Jt808MessageHeaderBuilder flowId(int flowId) {
            this.flowId = flowId;
            return this;
        }

        @Override
        public Jt808MessageHeaderBuilder subPackage(Jt808SubPackageProps subPackage) {
            this.subPackage = subPackage;
            return this;
        }

        @Override
        public Jt808RequestHeader build() {
            return new DefaultJt808RequestHeader(version, messageId, messageBodyProps, terminalId, flowId, subPackage);
        }
    }

}

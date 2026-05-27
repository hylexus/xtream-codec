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

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

public class Jt808MessageHeaderMetadata {
    private int messageId;
    private BodyProps bodyProps;
    private String terminalId;
    private int flowId;
    private Jt808SubPackageProps subPackageProps;
    private Jt808ProtocolVersion protocolVersion;

    public Jt808MessageHeaderMetadata() {
    }

    public Jt808ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public Jt808MessageHeaderMetadata setProtocolVersion(Jt808ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public int getMessageId() {
        return messageId;
    }

    public Jt808MessageHeaderMetadata setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    public BodyProps getBodyProps() {
        return bodyProps;
    }

    public Jt808MessageHeaderMetadata setBodyProps(BodyProps bodyProps) {
        this.bodyProps = bodyProps;
        return this;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public Jt808MessageHeaderMetadata setTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    public int getFlowId() {
        return flowId;
    }

    public Jt808MessageHeaderMetadata setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public Jt808SubPackageProps getSubPackageProps() {
        return subPackageProps;
    }

    public Jt808MessageHeaderMetadata setSubPackageProps(Jt808SubPackageProps subPackageProps) {
        this.subPackageProps = subPackageProps;
        return this;
    }

    public record BodyProps(
            int intValue,
            int messageBodyLength,
            int encryptionType,
            boolean hasSubPackage,
            int versionIdentifier,
            int reversedBit15) {

        public BodyProps(Jt808RequestHeader.Jt808MessageBodyProps props) {
            this(props.intValue(), props.messageBodyLength(), props.encryptionType(), props.hasSubPackage(), props.versionIdentifier(), props.reversedBit15());
        }
    }

    public record Jt808SubPackageProps(int totalSubPackageCount, int currentPackageNo) {
    }
}

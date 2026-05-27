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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto;

import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class EncodeMessageDto {
    public static final String DATA_FIELD_BODY_CLASS_NAME = DataField.class.getSimpleName();

    @NotNull(message = "version is null")
    private Jt808ProtocolVersion version = Jt808ProtocolVersion.VERSION_2019;

    @NotNull(message = "terminalId is null")
    @NotEmpty(message = "terminalId is empty")
    private String terminalId = "00000000013912344329";

    @NotNull(message = "messageId is null")
    private Integer messageId;

    @NotNull(message = "flowId is null")
    private Integer flowId = -1;

    private byte reversedBit15InHeader = 0;

    @Min(value = 25, message = "maxPackageSize > 25")
    private int maxPackageSize = 1024;

    private byte encryptionType = 0b000;

    @NotNull(message = "bodyClass is null")
    private String bodyClass;

    @NotNull(message = "bodyData is null")
    private Object bodyData;

    public byte getEncryptionType() {
        return encryptionType;
    }

    public EncodeMessageDto setEncryptionType(byte encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    public byte getReversedBit15InHeader() {
        return reversedBit15InHeader;
    }

    public EncodeMessageDto setReversedBit15InHeader(byte reversedBit15InHeader) {
        this.reversedBit15InHeader = reversedBit15InHeader;
        return this;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public EncodeMessageDto setMessageId(Integer messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getBodyClass() {
        return bodyClass;
    }

    public EncodeMessageDto setBodyClass(String bodyClass) {
        this.bodyClass = bodyClass;
        return this;
    }

    public int getFlowId() {
        return flowId;
    }

    public EncodeMessageDto setFlowId(Integer flowId) {
        this.flowId = flowId;
        return this;
    }

    public Object getBodyData() {
        return bodyData;
    }

    public EncodeMessageDto setBodyData(Object bodyData) {
        this.bodyData = bodyData;
        return this;
    }

    public int getMaxPackageSize() {
        return maxPackageSize;
    }

    public EncodeMessageDto setMaxPackageSize(int maxPackageSize) {
        this.maxPackageSize = maxPackageSize;
        return this;
    }

    public Jt808ProtocolVersion getVersion() {
        return version;
    }

    public EncodeMessageDto setVersion(Jt808ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public EncodeMessageDto setTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }
}

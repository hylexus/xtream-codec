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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.hylexus.xtream.codec.core.RuntimeTypeSupplier;
import io.github.hylexus.xtream.codec.core.tracker.RootSpan;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

public class Jt808DashboardDebugMessage implements RuntimeTypeSupplier {

    @JsonIgnore
    private Class<?> bodyType;

    // 消息头
    @Preset.JtStyle.Object(desc = "消息头")
    private Header header;

    @Preset.JtStyle.RuntimeType(lengthExpression = "header.msgBodyLength()", desc = "消息体")
    private Object body;

    private RootSpan bodyDetails;

    // 校验码
    @Preset.JtStyle.Byte(desc = "校验码")
    private short checkSum;

    public RootSpan getBodyDetails() {
        return bodyDetails;
    }

    public Jt808DashboardDebugMessage setBodyDetails(RootSpan bodyDetails) {
        this.bodyDetails = bodyDetails;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public Jt808DashboardDebugMessage setHeader(Header header) {
        this.header = header;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public Jt808DashboardDebugMessage setBody(Object body) {
        this.body = body;
        return this;
    }

    public short getCheckSum() {
        return checkSum;
    }

    public Jt808DashboardDebugMessage setCheckSum(short checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    @Override
    public Class<?> getRuntimeType(String name) {
        return this.bodyType;
    }

    public Jt808DashboardDebugMessage setBodyType(Class<?> bodyType) {
        this.bodyType = bodyType;
        return this;
    }

    public static class Header {

        public boolean hasVersionField() {
            return Jt808RequestHeader.Jt808MessageBodyProps.from(this.messageBodyProps).versionIdentifier() == 1;
        }

        @Preset.JtStyle.Word(desc = "消息ID")
        private int messageId;

        // byte[2-4)    消息体属性 word(16)
        @Preset.JtStyle.Word(desc = "消息体属性")
        private int messageBodyProps;

        // byte[4]     协议版本号
        @Preset.JtStyle.Byte(condition = "hasVersionField()", desc = "协议版本号(V2019+)")
        private short protocolVersion;

        // byte[5-15)    终端手机号或设备ID bcd[10]
        @Preset.JtStyle.Bcd(lengthExpression = "hasVersionField() ? 10 : 6", desc = "终端手机号或设备ID")
        private String terminalId;

        // byte[15-17)    消息流水号 word(16)
        @Preset.JtStyle.Word(desc = "消息流水号")
        private int serialNo;

        // byte[17-21)    消息包封装项
        @Preset.JtStyle.Object(condition = "hasSubPackage()", desc = "消息包封装项")
        private SubPackageProps subPackageProps;

        // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
        public int msgBodyLength() {
            return messageBodyProps & 0x3ff;
        }

        // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
        public boolean hasSubPackage() {
            // return ((msgBodyProperty & 0x2000) >> 13) == 1;
            return (messageBodyProps & 0x2000) > 0;
        }

        public int getMessageId() {
            return messageId;
        }

        public Header setMessageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public int getMessageBodyProps() {
            return messageBodyProps;
        }

        public Header setMessageBodyProps(int messageBodyProps) {
            this.messageBodyProps = messageBodyProps;
            return this;
        }

        public short getProtocolVersion() {
            return protocolVersion;
        }

        public Header setProtocolVersion(short protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public String getTerminalId() {
            return terminalId;
        }

        public Header setTerminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public int getSerialNo() {
            return serialNo;
        }

        public Header setSerialNo(int serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public SubPackageProps getSubPackageProps() {
            return subPackageProps;
        }

        public Header setSubPackageProps(SubPackageProps subPackageProps) {
            this.subPackageProps = subPackageProps;
            return this;
        }
    }

    public static class SubPackageProps {
        @Preset.JtStyle.Word(desc = "消息总包数")
        private int totalSubPackageCount;

        @Preset.JtStyle.Word(desc = "包序号")
        private int currentPackageNo;

        public int getTotalSubPackageCount() {
            return totalSubPackageCount;
        }

        public SubPackageProps setTotalSubPackageCount(int totalSubPackageCount) {
            this.totalSubPackageCount = totalSubPackageCount;
            return this;
        }

        public int getCurrentPackageNo() {
            return currentPackageNo;
        }

        public SubPackageProps setCurrentPackageNo(int currentPackageNo) {
            this.currentPackageNo = currentPackageNo;
            return this;
        }
    }
}

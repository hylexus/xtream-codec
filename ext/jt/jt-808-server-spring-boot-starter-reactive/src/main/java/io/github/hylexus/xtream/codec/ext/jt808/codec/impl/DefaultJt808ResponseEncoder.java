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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.tracker.RootSpan;
import io.github.hylexus.xtream.codec.core.tracker.VirtualEntitySpan;
import io.github.hylexus.xtream.codec.core.tracker.VirtualFieldSpan;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.*;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808SubPackageProps;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class DefaultJt808ResponseEncoder implements Jt808ResponseEncoder {
    private static final Logger log = LoggerFactory.getLogger(DefaultJt808ResponseEncoder.class);
    protected final ByteBufAllocator allocator;
    protected final Jt808FlowIdGenerator flowIdGenerator;
    protected final EntityCodec entityCodec;
    protected final Jt808BytesProcessor messageProcessor;
    protected final Jt808MessageEncryptionHandler encryptionHandler;

    public DefaultJt808ResponseEncoder(ByteBufAllocator allocator, Jt808FlowIdGenerator flowIdGenerator, EntityCodec entityCodec, Jt808BytesProcessor messageProcessor, Jt808MessageEncryptionHandler encryptionHandler) {
        this.allocator = allocator;
        this.flowIdGenerator = flowIdGenerator;
        this.entityCodec = entityCodec;
        this.messageProcessor = messageProcessor;
        this.encryptionHandler = encryptionHandler;
    }

    @Override
    public ByteBuf encode(Object body, Jt808ProtocolVersion version, String terminalId, int flowId, Jt808ResponseBody annotation) {
        final Jt808MessageDescriber describer = this.createDescriber(version, terminalId, flowId, annotation);
        return this.encode(body, describer);
    }

    @Override
    public ByteBuf encode(Object body, Jt808ProtocolVersion version, String terminalId, Jt808ResponseBody annotation) {
        final Jt808MessageDescriber describer = this.createDescriber(version, terminalId, -1, annotation);
        return this.encode(body, describer);
    }

    @Override
    public ByteBuf encode(Object body, Jt808MessageDescriber describer) {
        final CodecTracker tracker = describer.trackers() != null ? new CodecTracker() : null;
        if (body instanceof ByteBuf byteBuf) {
            return this.doBuild(describer, byteBuf, tracker);
        }
        final ByteBuf bodyBuf = this.encodeBody(describer, body, tracker);
        return this.doBuild(describer, bodyBuf, tracker);
    }

    protected Jt808MessageDescriber createDescriber(Jt808ProtocolVersion version, String terminalId, int flowId, Jt808ResponseBody annotation) {
        final Jt808MessageDescriber describer = new Jt808MessageDescriber(annotation.messageId(), version, terminalId)
                .maxPackageSize(annotation.maxPackageSize())
                .reversedBit15InHeader(annotation.reversedBit15InHeader())
                .encryptionType(annotation.encryptionType());
        if (flowId >= 0) {
            describer.flowId(flowId);
        }
        return describer;
    }

    private Jt808FlowIdGenerator getFlowIdGenerator(Jt808MessageDescriber describer) {
        return describer.flowIdGenerator() == null
                ? this.flowIdGenerator
                : describer.flowIdGenerator();
    }

    protected ByteBuf doBuild(Jt808MessageDescriber describer, ByteBuf body, @Nullable CodecTracker tracker) {
        final int maxPackageSize = describer.check().maxPackageSize();
        final int messageBodyLength = body.readableBytes();
        final Jt808ProtocolVersion version = requireNonNull(describer.version(), "version() is null");
        final int estimatedPackageSize = Jt808RequestHeader.messageBodyStartIndex(version, false) + messageBodyLength + 3;
        if (estimatedPackageSize <= maxPackageSize) {
            if (describer.flowId() < 0) {
                describer.flowId(this.getFlowIdGenerator(describer).nextFlowId());
            }
            return this.buildPackage(tracker, describer, body, 0, 0, describer.flowId());
        }

        final int subPackageBodySize = maxPackageSize - Jt808RequestHeader.messageBodyStartIndex(version, true) - 3;
        final int subPackageCount = messageBodyLength % subPackageBodySize == 0
                ? messageBodyLength / subPackageBodySize
                : messageBodyLength / subPackageBodySize + 1;

        final CompositeByteBuf allResponseBytes = allocator.compositeBuffer(subPackageCount);
        final int[] flowIds = this.getFlowIdGenerator(describer).flowIds(subPackageCount);
        for (int i = 0; i < subPackageCount; i++) {
            final int offset = i * subPackageBodySize;
            final int length = (i == subPackageCount - 1)
                    ? Math.min(subPackageBodySize, messageBodyLength - offset)
                    : subPackageBodySize;
            final ByteBuf bodyData = body.retainedSlice(offset, length);
            final CompositeByteBuf subPackage = this.buildPackage(tracker, describer, bodyData, subPackageCount, i + 1, flowIds[i]);
            allResponseBytes.addComponents(true, subPackage);
        }
        XtreamBytes.releaseBuf(body);
        return allResponseBytes;
    }

    private CompositeByteBuf buildPackage(@Nullable CodecTracker tracker, Jt808MessageDescriber describer, ByteBuf body, int totalSubPackageCount, int currentPackageNo, int flowId) {
        // @see https://github.com/hylexus/jt-framework/issues/82
        body = this.encryptionHandler.encryptResponseBody(describer, body);

        final ByteBuf headerBuf = allocator.buffer();
        final Jt808RequestHeader jt808RequestHeader = this.encodeMessageHeader(describer, body, totalSubPackageCount > 0, totalSubPackageCount, currentPackageNo, flowId);
        jt808RequestHeader.encode(headerBuf);
        final CompositeByteBuf compositeByteBuf = allocator.compositeBuffer()
                .addComponent(true, headerBuf)
                .addComponent(true, body);

        final byte checkSum = this.messageProcessor.calculateCheckSum(compositeByteBuf);

        compositeByteBuf.writeByte(checkSum);
        compositeByteBuf.resetReaderIndex();
        if (log.isDebugEnabled()) {
            log.debug("- <<<<<<<<<<<<<<< ({}--{}) {}/{}: 7E{}7E",
                    FormatUtils.toHexString(describer.messageId(), 4),
                    compositeByteBuf.readableBytes() + 2,
                    Math.max(currentPackageNo, 1), Math.max(totalSubPackageCount, 1),
                    FormatUtils.toHexString(compositeByteBuf)
            );
        }
        final boolean needTracker = tracker != null;
        if (needTracker) {
            this.updateTracker(describer, totalSubPackageCount, currentPackageNo, tracker, compositeByteBuf, jt808RequestHeader, body, checkSum);
        }
        final ByteBuf escaped;
        try {
            escaped = this.messageProcessor.doEscapeForSend(compositeByteBuf);
            if (needTracker) {
                final Jt808MessageDescriber.Tracker last = describer.trackers().getLast();
                last.setEscapedHexString("7e" + FormatUtils.toHexString(escaped) + "7e");
                final RootSpan details = last.getDetails();
                details.setHexString(
                        details.getChildren().get(0).getHexString()
                        + details.getChildren().get(1).getHexString()
                        + details.getChildren().get(2).getHexString()
                );
            }
        } catch (Throwable e) {
            XtreamBytes.releaseBuf(compositeByteBuf);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("+ <<<<<<<<<<<<<<< ({}--{}) {}/{}: 7E{}7E",
                    FormatUtils.toHexString(describer.messageId(), 4),
                    escaped.readableBytes() + 2,
                    Math.max(currentPackageNo, 1), Math.max(totalSubPackageCount, 1),
                    FormatUtils.toHexString(escaped)
            );
        }
        return allocator.compositeBuffer()
                .addComponent(true, allocator.buffer().writeByte(JtProtocolConstant.PACKAGE_DELIMITER))
                .addComponent(true, escaped)
                .addComponent(true, allocator.buffer().writeByte(JtProtocolConstant.PACKAGE_DELIMITER));
    }

    private void updateTracker(
            Jt808MessageDescriber describer, int totalSubPackageCount, int currentPackageNo, CodecTracker tracker,
            ByteBuf message, Jt808RequestHeader jt808RequestHeader, ByteBuf body, byte checkSum) {

        final Jt808MessageDescriber.Tracker responseTracker = new Jt808MessageDescriber.Tracker();
        describer.trackers().add(responseTracker);
        responseTracker.setRawHexString("7e" + FormatUtils.toHexString(message) + "7e");

        final Header header = new Header(jt808RequestHeader);
        final CodecTracker headerTracker = new CodecTracker();
        final ByteBuf tempHeaderBuffer = allocator.buffer();
        try {
            this.entityCodec.encode(describer.version().versionIdentifier(), header, tempHeaderBuffer, headerTracker);
        } finally {
            XtreamBytes.releaseBuf(tempHeaderBuffer);
        }

        final RootSpan details = new RootSpan().setEntityClass("VirtualEntity");
        responseTracker.setDetails(details);

        // 1. header
        details.getChildren().add(new VirtualEntitySpan(headerTracker.getRootSpan(), "header", "消息头"));

        // 2. body
        if (totalSubPackageCount == 0 && currentPackageNo == 0) {
            details.getChildren().add(new VirtualEntitySpan(tracker.getRootSpan(), "body", "消息体"));
        } else {
            details.getChildren().add(new VirtualFieldSpan("body", "消息体", "ByteBuf", body).setHexString(FormatUtils.toHexString(body)));
        }

        // 3. checkSum
        details.getChildren().add(new VirtualFieldSpan("checkSum", "校验码", "java.lang.Byte", checkSum).setHexString(FormatUtils.toHexString(checkSum, 2)));
    }

    private ByteBuf encodeBody(Jt808MessageDescriber describer, Object entity, @Nullable CodecTracker tracker) {
        if (entity instanceof ByteBuf byteBuf) {
            return byteBuf;
        }
        final ByteBuf buffer = allocator.buffer();
        try {
            final int version = describer.version().versionIdentifier();
            this.entityCodec.encode(version, entity, buffer, tracker);
        } catch (Throwable e) {
            XtreamBytes.releaseBuf(buffer);
            throw e;
        }
        return buffer;
    }

    private Jt808RequestHeader encodeMessageHeader(Jt808MessageDescriber response, ByteBuf body, boolean hasSubPackage, int totalSubPkgCount, int currentSubPkgNo, int flowId) {
        return Jt808RequestHeader.newBuilder()
                .version(response.version())
                .messageId(response.messageId())
                .messageBodyProps(Jt808RequestHeader.Jt808MessageBodyProps.newBuilder()
                        .messageBodyLength(body.readableBytes())
                        .encryptionType(response.encryptionType())
                        .hasSubPackage(hasSubPackage)
                        .versionIdentifier(response.version())
                        .reversedBit15(response.reversedBit15InHeader())
                        .build()
                )
                .subPackage(new DefaultJt808SubPackageProps(totalSubPkgCount, currentSubPkgNo))
                .terminalId(response.terminalId())
                .flowId(flowId)
                .build();
    }

    public static class Header {

        public Header(Jt808RequestHeader header) {
            this.messageId = header.messageId();
            this.messageBodyProps = header.messageBodyProps().intValue();
            this.protocolVersion = header.version().versionBit();
            this.terminalId = header.terminalId();
            this.serialNo = header.flowId();
            final Jt808RequestHeader.Jt808SubPackageProps subPackage = header.subPackage();
            if (subPackage != null) {
                this.subPackageProps = new SubPackageProps()
                        .setTotalSubPackageCount(subPackage.totalSubPackageCount())
                        .setCurrentPackageNo(subPackage.currentPackageNo());
            }
        }

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

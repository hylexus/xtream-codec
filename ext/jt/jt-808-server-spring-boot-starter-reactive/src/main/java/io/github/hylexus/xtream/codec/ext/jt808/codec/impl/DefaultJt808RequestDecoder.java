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
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ProtocolVersionDetector;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.exception.Jt808MessageDecodeException;
import io.github.hylexus.xtream.codec.ext.jt808.spec.*;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808MessageBodyProps;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808SubPackageProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.NettyInbound;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public class DefaultJt808RequestDecoder implements Jt808RequestDecoder {

    private static final Logger log = LoggerFactory.getLogger(DefaultJt808RequestDecoder.class);
    protected final Jt808BytesProcessor jt808MessageProcessor;
    protected final Jt808RequestCombiner requestCombiner;
    protected final Jt808MessageEncryptionHandler encryptionHandler;
    private final Jt808ProtocolVersionDetector protocolVersionDetector;

    public DefaultJt808RequestDecoder(Jt808BytesProcessor jt808MessageProcessor, Jt808ProtocolVersionDetector protocolVersionDetector, Jt808MessageEncryptionHandler encryptionHandler, Jt808RequestCombiner requestCombiner) {
        this.jt808MessageProcessor = jt808MessageProcessor;
        this.requestCombiner = requestCombiner;
        this.encryptionHandler = encryptionHandler;
        this.protocolVersionDetector = protocolVersionDetector;
    }

    public DefaultJt808RequestDecoder(Jt808BytesProcessor jt808MessageProcessor, Jt808MessageEncryptionHandler encryptionHandler, Jt808RequestCombiner requestCombiner) {
        this(jt808MessageProcessor, Jt808ProtocolVersionDetector.DEFAULT, encryptionHandler, requestCombiner);
    }

    @Override
    public Jt808Request decode(Jt808ServerType serverType, String requestId, ByteBufAllocator allocator, NettyInbound nettyInbound, XtreamRequest.Type requestType, ByteBuf payload, Channel channel, InetSocketAddress remoteAddress) {
        if (log.isDebugEnabled()) {
            log.debug("- >>>>>>>>>>>>>>> : 7E{}7E", FormatUtils.toHexString(payload));
        }
        final ByteBuf escaped = this.jt808MessageProcessor.doEscapeForReceive(payload);

        try {
            if (log.isDebugEnabled()) {
                log.debug("+ >>>>>>>>>>>>>>> : 7E{}7E", FormatUtils.toHexString(escaped));
            }

            final Jt808RequestHeader header = this.parseMessageHeaderSpec(escaped);
            final int messageBodyStartIndex = Jt808RequestHeader.messageBodyStartIndex(header.version(), header.messageBodyProps().hasSubPackage());
            final byte originalCheckSum = escaped.getByte(escaped.readableBytes() - 1);
            final byte calculatedCheckSum = this.jt808MessageProcessor.calculateCheckSum(escaped.slice(0, escaped.readableBytes() - 1));
            final ByteBuf body = escaped.slice(messageBodyStartIndex, header.messageBodyLength());
            final String traceId = this.requestCombiner.getTraceId(nettyInbound, header);

            // 消息解密
            // @see https://github.com/hylexus/jt-framework/issues/82
            final ByteBuf newBody = this.encryptionHandler.decryptRequestBody(header, body);
            final int newBodyLength = newBody.readableBytes();
            final Jt808RequestHeader newHeader;
            if (newBodyLength == body.readableBytes()) {
                newHeader = header;
            } else {
                // 重新计算消息体属性(长度发生变化)
                final Jt808RequestHeader.Jt808MessageBodyProps newMessageBodyProps = header.messageBodyProps().mutate().messageBodyLength(newBodyLength).build();
                newHeader = header.mutate().messageBodyProps(newMessageBodyProps).build();
            }
            final byte version = header.version().versionIdentifier();
            return new DefaultJt808Request(
                    version,
                    serverType,
                    requestId,
                    traceId,
                    allocator,
                    nettyInbound,
                    requestType,
                    newBody,
                    channel,
                    remoteAddress,
                    newHeader,
                    originalCheckSum,
                    calculatedCheckSum
            );
        } catch (Throwable e) {
            XtreamBytes.releaseBuf(escaped);
            throw e;
        }
    }

    @Override
    public Jt808RequestHeader decodeHeader(ByteBuf buffer) {
        return this.parseMessageHeaderSpec(buffer.slice());
    }

    protected Jt808RequestHeader parseMessageHeaderSpec(ByteBuf byteBuf) {
        // 1. bytes[0-1] WORD
        final int messageId = XtreamBytes.getWord(byteBuf, 0);
        // bytes[2-3] WORD 消息体属性
        final int messageBodyPropsIntValue = XtreamBytes.getWord(byteBuf, 2);
        final Jt808RequestHeader.Jt808MessageBodyProps messageBodyProps = new DefaultJt808MessageBodyProps(messageBodyPropsIntValue);

        final Jt808ProtocolVersion version = this.protocolVersionDetector.detectVersion(messageId, messageBodyProps, byteBuf);
        if (version.versionBit() == 1) {
            return this.parseHeaderGreatThanOrEqualsV2019(version, messageBodyProps, byteBuf);
        } else if (version == Jt808ProtocolVersion.VERSION_2013 || version == Jt808ProtocolVersion.VERSION_2011) {
            return this.parseHeaderLessThanOrEqualsV2013(version, messageBodyProps, byteBuf);
        }

        throw new Jt808MessageDecodeException("未知版本: " + version);
    }

    protected Jt808RequestHeader parseHeaderGreatThanOrEqualsV2019(
            Jt808ProtocolVersion version, Jt808RequestHeader.Jt808MessageBodyProps messageBodyProps, ByteBuf byteBuf) {

        final DefaultJt808RequestHeader headerSpec = new DefaultJt808RequestHeader();
        headerSpec.version(version);
        // 1. bytes[0-1] WORD
        final int messageId = XtreamBytes.getWord(byteBuf, 0);
        headerSpec.messageId(messageId);
        // 2. bytes[2-3] WORD
        headerSpec.messageBodyProps(messageBodyProps);

        // 3. bytes[4] WORD 协议版本号
        // final byte version = byteBuf.getByte(4);
        // assert version == 1; // V2019
        // 4. byte[5-14]   终端手机号或设备ID bcd[10]
        headerSpec.terminalId(XtreamBytes.getBcd(byteBuf, 5, 10));

        // 4. byte[15-16]     消息流水号
        headerSpec.flowId(XtreamBytes.getWord(byteBuf, 15));
        if (messageBodyProps.hasSubPackage()) {
            final DefaultJt808SubPackageProps subPackageProps = new DefaultJt808SubPackageProps();
            subPackageProps.totalSubPackageCount(XtreamBytes.getWord(byteBuf, 17));
            subPackageProps.currentPackageNo(XtreamBytes.getWord(byteBuf, 19));
            headerSpec.subPackageProps(subPackageProps);
        }
        return headerSpec;
    }

    protected Jt808RequestHeader parseHeaderLessThanOrEqualsV2013(
            Jt808ProtocolVersion version, Jt808RequestHeader.Jt808MessageBodyProps messageBodyProps, ByteBuf byteBuf) {

        final DefaultJt808RequestHeader headerSpec = new DefaultJt808RequestHeader();
        headerSpec.version(version);
        // 1. bytes[0-1] WORD
        final int messageId = XtreamBytes.getWord(byteBuf, 0);
        headerSpec.messageId(messageId);

        // 2. bytes[2-3] WORD
        headerSpec.messageBodyProps(messageBodyProps);

        // 3. byte[4-9]   终端手机号或设备ID bcd[6]
        headerSpec.terminalId(XtreamBytes.getBcd(byteBuf, 4, 6));

        // 4. byte[10-11]     消息流水号
        headerSpec.flowId(XtreamBytes.getWord(byteBuf, 10));
        if (messageBodyProps.hasSubPackage()) {
            final DefaultJt808SubPackageProps subPackageProps = new DefaultJt808SubPackageProps();
            subPackageProps.totalSubPackageCount(XtreamBytes.getWord(byteBuf, 12));
            subPackageProps.currentPackageNo(XtreamBytes.getWord(byteBuf, 14));
            headerSpec.subPackageProps(subPackageProps);
        }
        return headerSpec;
    }
}

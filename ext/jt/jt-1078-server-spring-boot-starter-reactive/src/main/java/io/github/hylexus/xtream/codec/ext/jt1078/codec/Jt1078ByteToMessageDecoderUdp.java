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

package io.github.hylexus.xtream.codec.ext.jt1078.codec;

import io.github.hylexus.xtream.codec.common.utils.XtreamByteReader;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.*;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;

public class Jt1078ByteToMessageDecoderUdp {
    private final ByteBufAllocator allocator;
    private final Jt1078SessionManager sessionManager;
    private final Jt1078SimConverter jt1078SimConverter;

    public Jt1078ByteToMessageDecoderUdp(ByteBufAllocator allocator, Jt1078SimConverter jt1078SimConverter, Jt1078SessionManager sessionManager) {
        this.allocator = allocator;
        this.sessionManager = sessionManager;
        this.jt1078SimConverter = jt1078SimConverter;
    }

    public Jt1078Request decode(NettyInbound nettyInbound, NettyOutbound nettyOutbound, DatagramPacket datagramPacket, XtreamNettyHandlerAdapter.InboundInfo inboundInfo) {
        final ByteBuf udpPayload = datagramPacket.content();
        if (udpPayload.readableBytes() <= 4) {
            throw new IllegalStateException("Parse JT/T 1078 stream-data error");
        }
        // 去掉30316364前缀
        final ByteBuf byteBuf = udpPayload.slice(4, udpPayload.readableBytes() - 4);
        return this.doDecode(nettyInbound, nettyOutbound, inboundInfo, byteBuf);
    }

    private Jt1078Request doDecode(NettyInbound nettyInbound, NettyOutbound nettyOutbound, XtreamNettyHandlerAdapter.InboundInfo inboundInfo, ByteBuf buffer) {
        final Jt1078Session session = this.sessionManager.getUdpSession(inboundInfo.remoteAddress());
        if (session != null) {
            final Jt1078Request request = this.decodeRequest(buffer, session.simLength(), nettyInbound, inboundInfo);
            this.updateSession(session, request);
            return request;
        }

        final String sessionId = this.sessionManager.sessionIdGenerator().generateUdpSessionId(inboundInfo.remoteAddress());
        final Jt1078Request requestWithSimLength6 = this.tryDecodeRequest(buffer, 6, nettyInbound, inboundInfo);
        if (requestWithSimLength6 != null) {
            final DefaultJt1078Session newSession = this.initSession(nettyOutbound, sessionId, requestWithSimLength6, inboundInfo.remoteAddress(), 6);
            this.updateSession(newSession, requestWithSimLength6);
            return requestWithSimLength6;
        }

        final Jt1078Request requestWithSimLength10 = this.tryDecodeRequest(buffer, 10, nettyInbound, inboundInfo);
        if (requestWithSimLength10 != null) {
            final DefaultJt1078Session newSession = this.initSession(nettyOutbound, sessionId, requestWithSimLength10, inboundInfo.remoteAddress(), 10);
            this.updateSession(newSession, requestWithSimLength10);
            return requestWithSimLength10;
        }

        throw new IllegalStateException("Parse JT/T 1078 stream-data error");
    }

    private void updateSession(Jt1078Session session, Jt1078Request request) {
        session.lastCommunicateTime(Instant.now());
        if (session.audioType() == null && request.header().payloadType().isAudio()) {
            session.audioType(request.header().payloadType());
        }
        if (session.videoType() == null && request.header().payloadType().isVideo()) {
            session.videoType(request.header().payloadType());
        }
    }

    private DefaultJt1078Session initSession(NettyOutbound nettyOutbound, String sessionId, Jt1078Request request, InetSocketAddress remoteAddress, int simLength) {
        final DefaultJt1078Session jt1078Session = new DefaultJt1078Session(
                this.allocator,
                nettyOutbound,
                this.sessionManager,
                sessionId,
                XtreamInbound.Type.UDP,
                simLength,
                request.header().rawSim(),
                request.header().convertedSim(),
                request.header().channelNumber(),
                remoteAddress
        );
        this.sessionManager.createSession(jt1078Session);
        return jt1078Session;
    }

    Jt1078Request tryDecodeRequest(ByteBuf in, int simLen, NettyInbound inbound, XtreamNettyHandlerAdapter.InboundInfo inboundInfo) {
        // 这里输入的报文没有 30316364 前缀
        // +5 = 1byte(V,P,X,CC) + 1byte(M,PT) + 2byte(sequenceNumber) + 1byte(channelNumber)
        // FIXME 重复代码
        // noinspection Duplicates 重复代码先不处理
        final int dataType = (in.getByte(simLen + 5) >> 4) & 0x0F;
        final int lengthFieldOffset = switch (dataType) {
            // 透传
            case 0x04 -> simLen + 6;
            // 音频
            case 0x03 -> simLen + 14;
            // 视频
            default -> simLen + 18;
        };
        final short messageBodyLength = in.getShort(lengthFieldOffset);
        final int totalPackageSize = messageBodyLength + lengthFieldOffset + 2;
        if (totalPackageSize == in.readableBytes()) {
            return this.decodeRequest(in, simLen, inbound, inboundInfo);
        }
        return null;
    }

    private Jt1078Request decodeRequest(ByteBuf in, int simLen, NettyInbound inbound, XtreamNettyHandlerAdapter.InboundInfo inboundInfo) {
        // FIXME 重复代码
        // noinspection Duplicates 重复代码先不处理
        final XtreamByteReader reader = XtreamByteReader.of(in);
        final DefaultJt1078RequestHeader headerBuilder = new DefaultJt1078RequestHeader();
        // V + P + X + CC
        reader.readU8(headerBuilder::offset4);
        // M + PT
        reader.readU8(headerBuilder::markerBitAndPayloadType);
        // 包序号
        reader.readU16(headerBuilder::sequenceNumber);
        // SIM
        headerBuilder.simLength(simLen);
        final String sim = reader.readBcd(simLen);
        // SIM-rawSim
        headerBuilder.rawSim(sim);
        final String convertedSim = this.jt1078SimConverter.convert(sim);
        // SIM-convertedSim
        headerBuilder.convertedSim(convertedSim);
        // 通道号
        reader.readU8(headerBuilder::channelNumber);
        // 数据类型 + 分包处理标记
        final short dataTypeAndSubPackageIdentifier = reader.readU8();
        headerBuilder.dataTypeAndSubPackageIdentifier(dataTypeAndSubPackageIdentifier);
        final byte dataType = Jt1078RequestHeader.dataTypeValue(dataTypeAndSubPackageIdentifier);
        final boolean hasTimestampField = Jt1078RequestHeader.hasTimestampField(dataType);
        final boolean hasFrameIntervalFields = Jt1078RequestHeader.hasFrameIntervalFields(dataType);
        if (hasTimestampField) {
            // 时间戳
            reader.readI64(headerBuilder::timestamp);
        }
        if (hasFrameIntervalFields) {
            // Last I Frame Interval
            reader.readU16(headerBuilder::lastIFrameInterval);
            // Last Frame Interval
            reader.readU16(headerBuilder::lastFrameInterval);
        }

        // 数据体长度字段
        final int bodyLength = reader.readU16();
        headerBuilder.msgBodyLength(bodyLength);
        final ByteBuf body = in.readSlice(bodyLength);

        final String requestId = UUID.randomUUID().toString().replace("-", "");
        return new DefaultJt1078Request(
                requestId,
                allocator,
                inbound,
                XtreamInbound.Type.TCP,
                inboundInfo.channel(),
                inboundInfo.remoteAddress(),
                headerBuilder,
                body
        );
    }
}

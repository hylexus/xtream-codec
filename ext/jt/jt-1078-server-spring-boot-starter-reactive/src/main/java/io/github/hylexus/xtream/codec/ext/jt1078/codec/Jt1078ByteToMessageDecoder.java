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
import io.github.hylexus.xtream.codec.ext.jt1078.extensions.handler.Jt1078ServerTcpHandlerAdapter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.*;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamNettyHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.Connection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 这个实现来自 <a href="https://gitee.com/hui_hui_zhou/open-source-repository">sky/jt1078</a> 的作者 sky
 *
 * @see <a href="https://gitee.com/hui_hui_zhou/open-source-repository">sky/jt1078</a>
 * @see Jt1078ServerTcpHandlerAdapter
 */
public class Jt1078ByteToMessageDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(Jt1078ByteToMessageDecoder.class);

    private final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    private final Jt1078SimConverter jt1078SimConverter;
    private final Connection connection;
    private final Jt1078SessionManager sessionManager;

    private int simLength = 0;
    private Jt1078Session session;
    final XtreamNettyHandlerAdapter.InboundInfo inboundInfo;

    public Jt1078ByteToMessageDecoder(Jt1078SimConverter jt1078SimConverter, Connection connection, Jt1078SessionManager sessionManager) {
        this.jt1078SimConverter = jt1078SimConverter;
        this.connection = connection;
        this.sessionManager = sessionManager;
        this.inboundInfo = XtreamNettyHandlerAdapter.InboundInfo.forTcp(connection.inbound());
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() <= 0) {
            in.release();
            return;
        }

        if (this.simLength == 0) {
            final Jt1078Request requestWithSimLength6 = this.tryDecodeRequest(in, 6);
            if (requestWithSimLength6 != null) {
                this.simLength = 6;
                this.initSession(requestWithSimLength6, this.simLength);
                this.emitDecodedRequest(out, requestWithSimLength6);
            } else {
                final Jt1078Request requestWithSimLength10 = this.tryDecodeRequest(in, 10);
                if (requestWithSimLength10 != null) {
                    this.simLength = 10;
                    this.initSession(requestWithSimLength10, this.simLength);
                    this.emitDecodedRequest(out, requestWithSimLength10);
                } else {
                    throw new IllegalStateException("Parse JT/T 1078 stream-data error");
                }
            }
        } else {
            final Jt1078Request packageInfo = this.decodeRequest(in, this.simLength);
            this.emitDecodedRequest(out, packageInfo);
        }
    }

    private void emitDecodedRequest(List<Object> out, Jt1078Request request) {
        out.add(request);
        this.session.lastCommunicateTime(Instant.now());
        if (this.session.audioType() == null && request.header().payloadType().isAudio()) {
            this.session.audioType(request.header().payloadType());
        }
        if (this.session.videoType() == null && request.header().payloadType().isVideo()) {
            this.session.videoType(request.header().payloadType());
        }
    }

    private void initSession(Jt1078Request request, int simLength) {
        final String sessionId = this.sessionManager.sessionIdGenerator().generateTcpSessionId(this.connection.channel());
        final DefaultJt1078Session jt1078Session = new DefaultJt1078Session(
                this.allocator, this.connection.outbound(), this.sessionManager, sessionId,
                XtreamInbound.Type.TCP,
                simLength,
                request.header().rawSim(),
                request.header().convertedSim(),
                request.header().channelNumber(),
                this.inboundInfo.remoteAddress()
        );
        this.session = jt1078Session;
        this.sessionManager.createSession(jt1078Session);
    }

    Jt1078Request tryDecodeRequest(ByteBuf in, int simLen) {
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
            return this.decodeRequest(in, simLen);
        }
        return null;
    }

    private Jt1078Request decodeRequest(ByteBuf in, int simLen) {
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
        final ByteBuf body = in.readRetainedSlice(bodyLength);

        final String requestId = UUID.randomUUID().toString().replace("-", "");
        return new DefaultJt1078Request(
                requestId,
                allocator,
                this.connection.inbound(),
                XtreamInbound.Type.TCP,
                this.inboundInfo.channel(),
                this.inboundInfo.remoteAddress(),
                headerBuilder,
                body
        );
    }
}

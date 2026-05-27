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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;


import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Optional;

/**
 * @author hylexus
 */
public interface Jt1078RequestHeader {

    /**
     * bytes[4,5)
     * <p>
     * V, P, X, CC
     */
    short offset4();

    /**
     * V: 2 bits; 固定为 2
     */
    @SuppressWarnings("checkstyle:methodname")
    default byte v() {
        return (byte) ((this.offset4() >> 6) & 0b11);
    }

    /**
     * P: 1 bit; 固定为 0
     */
    @SuppressWarnings("checkstyle:methodname")
    default byte p() {
        return (byte) ((this.offset4() >> 5) & 0b01);
    }

    /**
     * X: 1bit; RTP头是否需要扩展位，固定为 0
     */
    @SuppressWarnings("checkstyle:methodname")
    default byte x() {
        return (byte) ((this.offset4() >> 4) & 0b01);
    }

    /**
     * CC: 4 bits; 固定为 1
     */
    @SuppressWarnings("checkstyle:methodname")
    default byte cc() {
        return (byte) (this.offset4() & 0x0f);
    }

    /**
     * bytes[5,6)
     * <p>
     * M: 1bit; 标志位，确定是否是完整数据帧的边界
     */
    @SuppressWarnings("checkstyle:methodname")
    byte m();

    /**
     * bytes[5,6)
     * <p>
     * PT: 7bits; 负载类型
     */
    Jt1078PayloadType payloadType();

    /**
     * bytes[6,8)    包序号
     */
    int sequenceNumber();

    int simLength();

    /**
     * @return SIM 卡号
     * @see Jt1078SimConverter
     */
    String rawSim();

    /**
     * bytes[8,14) BCD[6] SIM卡号
     *
     * @see Jt1078SimConverter
     */
    String convertedSim();

    /**
     * bytes[14,15)    逻辑通道号
     */
    short channelNumber();

    /**
     * bytes[15,16)
     * <p>
     * 数据类型
     *
     * @param offset15 消息头中第15个字节的高四位
     * @return 高四位
     */
    static byte dataTypeValue(short offset15) {
        return (byte) ((offset15 >> 4) & 0x0f);
    }

    /**
     * 数据类型
     */
    Jt1078DataType dataType();

    /**
     * 分包处理标记
     *
     * @param offset15 消息头中第15个字节
     * @return 低四位
     */
    static byte subPackageIdentifierValue(short offset15) {
        return (byte) (offset15 & 0x0f);
    }

    /**
     * bytes[15,16)
     *
     * @return 分包处理标记
     */
    Jt1078SubPackageIdentifier subPackageIdentifier();


    static boolean hasFrameIntervalFields(byte dataType) {
        // 0000: 视频I帧
        // 0001: 视频P帧
        // 0010: 视频B帧
        // 0011: 音频帧
        // 0100: 透传数据
        return (dataType & 0b11) <= 0b10;
    }

    static boolean hasTimestampField(byte dataType) {
        return (dataType & 0x0f) != 0b0100;
    }

    /**
     * bytes[16,24)    BYTE[8] 时间戳
     *
     * @return 当 {@link #dataType()} == 0x0100 时返回 {@link Optional#empty()}
     */
    Optional<Long> timestamp();

    /**
     * bytes[24,26)    WORD    该帧与上一个关键帧之间的相对时间(ms)
     *
     * @return 当 {@link #dataType()} == 0x0100 || {@link #dataType()} == 0x0011 时返回 {@link Optional#empty()}
     * @see #dataTypeValue(short)
     * @see #dataType()
     * @see #hasFrameIntervalFields(byte)
     * @see #lastFrameInterval()
     */
    Optional<Integer> lastIFrameInterval();


    /**
     * bytes[24,26)    WORD    该帧与上一帧之间的相对时间(ms)
     *
     * @return 当 {@link #dataType()} == 0x0100 || {@link #dataType()} == 0x0011 时返回 {@link Optional#empty()}
     * @see #dataTypeValue(short)
     * @see #dataType()
     * @see #hasFrameIntervalFields(byte)
     * @see #lastIFrameInterval()
     */
    Optional<Integer> lastFrameInterval();

    /**
     * bytes[28,30)    WORD    数据体长度
     */
    int msgBodyLength();

    static byte generateOffset4() {
        return generateOffset4((byte) 2, (byte) 0, (byte) 0, (byte) 1);
    }

    static byte generateOffset4(byte v, byte p, byte x, byte cc) {
        return (byte)
                (
                        (byte) ((v & 0b11) << 6)
                        // P(1bit): 固定为 0
                        | (byte) ((p & 0b1) << 5)
                        // X(1bit): 固定为 0
                        | (byte) ((x & 0b1) << 4)
                        // CC(4bit): 固定为 1
                        | (byte) (cc & 0x0F)
                );
    }

    default ByteBuf encode(boolean withIdentifier) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        this.encode(withIdentifier, buffer);
        return buffer;
    }

    default void encode(boolean withIdentifier, ByteBuf buffer) {
        if (withIdentifier) {
            buffer.writeByte(0x30);
            buffer.writeByte(0x31);
            buffer.writeByte(0x63);
            buffer.writeByte(0x64);
        }
        buffer.writeByte(this.offset4());
        buffer.writeByte(this.m() << 7 | (this.payloadType().value() & 0b0111_1111));
        buffer.writeShort(this.sequenceNumber());
        XtreamBytes.writeBcd(buffer, this.rawSim());
        buffer.writeByte(this.channelNumber());
        buffer.writeByte((this.dataType().value() << 4) | (this.subPackageIdentifier().value() & 0x0F));
        this.timestamp().ifPresent(buffer::writeLong);
        this.lastIFrameInterval().ifPresent(buffer::writeShort);
        this.lastFrameInterval().ifPresent(buffer::writeShort);
        buffer.writeShort(this.msgBodyLength());
    }

}

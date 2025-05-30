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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;


import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.DefaultJt1078RequestHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Optional;

/**
 * @author hylexus
 */
public interface Jt1078RequestHeader {

    /**
     * bytes[4,5)
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
     */
    short offset5();

    /**
     * M: 1bit; 标志位，确定是否是完整数据帧的边界
     */
    @SuppressWarnings("checkstyle:methodname")
    default byte m() {
        return (byte) ((this.offset5() >> 7) & 0b01);
    }

    /**
     * PT: 7bits; 负载类型
     */
    default byte pt() {
        return (byte) (this.offset5() & 0b0111_1111);
    }

    /**
     * PT: 负载类型
     */
    Jt1078PayloadType payloadType();

    /**
     * bytes[6,8)    包序号
     */
    int offset6();

    default int sequenceNumber() {
        return offset6();
    }

    /**
     * @return SIM 卡号
     * @see Jt1078TerminalIdConverter
     */
    String offset8();

    /**
     * bytes[8,14)    BCD[6]    SIM卡号
     *
     * @see Jt1078TerminalIdConverter
     */
    default String sim() {
        return offset8();
    }

    /**
     * bytes[14,15)    逻辑通道号
     */
    short offset14();

    /**
     * bytes[14,15)    逻辑通道号
     */
    default short channelNumber() {
        return offset14();
    }

    /**
     * bytes[15,16)
     */
    short offset15();

    /**
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
    default byte dataTypeValue() {
        return dataTypeValue(this.offset15());
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
     * 分包处理标记
     *
     * @return 分包处理标记
     */
    default byte subPackageIdentifierValue() {
        return subPackageIdentifierValue(this.offset15());
    }

    /**
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

    static int msgLengthFieldIndex(boolean hasIdentifier, boolean hasTimestampField, boolean hasFrameIntervalFields) {
        int index = 28;
        if (!hasTimestampField) {
            // -8: timestamp
            index -= 8;
        }
        if (!hasFrameIntervalFields) {
            // -2: lastFrameInterval
            // -2: lastIFrameInterval
            index = index - 2 - 2;
        }
        if (!hasIdentifier) {
            // 0x30,0x31,0x63,0x64
            index -= 4;
        }
        return index;
    }

    Optional<Long> offset16();

    /**
     * bytes[16,24)    BYTE[8] 时间戳
     *
     * @return 当 {@link #dataType()} == 0x0100 时返回 {@link Optional#empty()}
     */
    default Optional<Long> timestamp() {
        return offset16();
    }

    Optional<Integer> offset24();

    /**
     * bytes[24,26)    WORD    该帧与上一个关键帧之间的相对时间(ms)
     *
     * @return 当 {@link #dataType()} == 0x0100 || {@link #dataType()} == 0x0011 时返回 {@link Optional#empty()}
     * @see #dataTypeValue(short)
     * @see #dataTypeValue()
     * @see #hasFrameIntervalFields(byte)
     * @see #lastFrameInterval()
     */
    default Optional<Integer> lastIFrameInterval() {
        return offset24();
    }

    Optional<Integer> offset26();

    /**
     * bytes[24,26)    WORD    该帧与上一帧之间的相对时间(ms)
     *
     * @return 当 {@link #dataType()} == 0x0100 || {@link #dataType()} == 0x0011 时返回 {@link Optional#empty()}
     * @see #dataTypeValue(short)
     * @see #dataType()
     * @see #hasFrameIntervalFields(byte)
     * @see #lastIFrameInterval()
     */
    default Optional<Integer> lastFrameInterval() {
        return offset26();
    }

    int offset28();

    /**
     * bytes[28,30)    WORD    数据体长度
     */
    default int msgBodyLength() {
        return offset28();
    }

    boolean isCombined();

    default Jt1078RequestHeaderBuilder mutate() {
        return newBuilder(this);
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
        buffer.writeByte(this.offset5());
        buffer.writeShort(this.offset6());
        XtreamBytes.writeBcd(buffer, this.offset8());
        buffer.writeByte(this.offset14());
        buffer.writeByte(this.offset15());
        this.offset16().ifPresent(buffer::writeLong);
        this.offset24().ifPresent(buffer::writeShort);
        this.offset26().ifPresent(buffer::writeShort);
        buffer.writeShort(this.offset28());
    }

    static Jt1078RequestHeaderBuilder newBuilder() {
        return new DefaultJt1078RequestHeader();
    }

    static Jt1078RequestHeaderBuilder newBuilder(Jt1078RequestHeader another) {
        return new DefaultJt1078RequestHeader(another);
    }

    interface Jt1078RequestHeaderBuilder {
        Jt1078RequestHeaderBuilder offset4(short value);

        Jt1078RequestHeaderBuilder offset5(short value);

        Jt1078RequestHeaderBuilder offset6(int value);

        Jt1078RequestHeaderBuilder offset8(String value);

        Jt1078RequestHeaderBuilder offset14(short value);

        Jt1078RequestHeaderBuilder offset15(short value);

        Jt1078RequestHeaderBuilder dataType(short value);

        default Jt1078RequestHeaderBuilder dataType(Jt1078DataType dataType) {
            return this.dataType(dataType.value());
        }

        Jt1078RequestHeaderBuilder subPackageIdentifier(short value);

        default Jt1078RequestHeaderBuilder subPackageIdentifier(Jt1078SubPackageIdentifier identifier) {
            return this.subPackageIdentifier(identifier.value());
        }

        Jt1078RequestHeaderBuilder offset16(Long value);

        Jt1078RequestHeaderBuilder offset24(Integer value);

        Jt1078RequestHeaderBuilder offset26(Integer value);

        Jt1078RequestHeaderBuilder offset28(int value);

        default Jt1078RequestHeaderBuilder msgBodyLength(int newLength) {
            return this.offset28(newLength);
        }

        Jt1078RequestHeaderBuilder isCombined(boolean isCombined);

        Jt1078RequestHeader build();
    }
}

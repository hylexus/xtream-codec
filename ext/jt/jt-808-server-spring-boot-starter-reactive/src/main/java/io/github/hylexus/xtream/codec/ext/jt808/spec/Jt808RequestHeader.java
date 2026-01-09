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

package io.github.hylexus.xtream.codec.ext.jt808.spec;


import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808MessageBodyProps;
import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808RequestHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author hylexus
 */
public interface Jt808RequestHeader {

    // region 请求头属性定义
    default int messageBodyStartIndex() {
        return Jt808RequestHeader.messageBodyStartIndex(version(), messageBodyProps().hasSubPackage());
    }

    static int messageBodyStartIndex(Jt808ProtocolVersion version, boolean hasSubPackage) {
        // 2011, 2013
        if (version.versionBit() == Jt808ProtocolVersion.VERSION_2013.versionBit()) {
            return hasSubPackage ? 16 : 12;
        }
        // 2019
        if (version.versionBit() == Jt808ProtocolVersion.VERSION_2019.versionBit()) {
            return hasSubPackage ? 21 : 17;
        }
        throw new IllegalStateException("未知版本, version=" + version);
    }

    static Jt808MessageHeaderBuilder newBuilder() {
        return new DefaultJt808RequestHeader.DefaultJt808MessageHeaderBuilder();
    }

    /**
     * byte[0-2) 消息ID word(16)
     */
    int messageId();

    /**
     * byte[2-4)    消息体属性 word(16)
     */
    Jt808MessageBodyProps messageBodyProps();

    /**
     * {@code v2013}: byte[4-10)    终端手机号或设备ID bcd[6]
     * <p>
     * {@code v2019}: byte[5-15)    终端手机号或设备ID bcd[10]
     */
    String terminalId();
    // endregion 请求头属性定义

    /**
     * {@code v2013}: byte[10-12) 消息流水号 word(16)
     * <p>
     * {@code v2019}: byte[15-17)    消息流水号 word(16)
     */
    int flowId();

    /**
     * {@code v2013}: byte[12-16)    消息包封装项
     * <p>
     * {@code v2019}: byte[17-21)    消息包封装项
     */
    Jt808SubPackageProps subPackage();

    // region 辅助方法定义
    Jt808ProtocolVersion version();

    default int messageBodyLength() {
        return messageBodyProps().messageBodyLength();
    }

    @Override
    String toString();

    default ByteBuf encode() {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        this.encode(buffer);
        return buffer;
    }

    default void encode(ByteBuf buffer) {
        final Jt808ProtocolVersion version = this.version();
        // bytes[0-2) 消息ID Word
        XtreamBytes.writeWord(buffer, this.messageId());

        // bytes[2-4) 消息体属性 Word
        final Jt808MessageBodyProps messageBodyProps = this.messageBodyProps();
        // final int bodyPropsForJt808 = Jt808RequestHeader.Jt808MessageBodyProps.create(
        //         messageBodyProps.messageBodyLength(), messageBodyProps.encryptionType(),
        //         messageBodyProps.hasSubPackage(), this.version(), messageBodyProps.reversedBit15()
        // );
        final int bodyPropsForJt808 = this.messageBodyProps().intValue();
        XtreamBytes.writeWord(buffer, bodyPropsForJt808);

        // bytes[4] 协议版本号 byte
        if (version == Jt808ProtocolVersion.VERSION_2019) {
            buffer.writeByte(this.version().versionBit());
        }

        // bytes[5-14) 终端手机号 BCD[10]
        // bytes[4-10) 终端手机号 BCD[6]
        XtreamBytes.writeBcd(buffer, this.terminalId());

        // bytes[15-17) 消息流水号  Word
        XtreamBytes.writeWord(buffer, this.flowId());

        // bytes[17-21) 消息包封装项
        // bytes[12-16) 消息包封装项
        if (messageBodyProps.hasSubPackage()) {
            // 消息总包包数
            XtreamBytes.writeWord(buffer, this.subPackage().totalSubPackageCount());
            // 包序号
            XtreamBytes.writeWord(buffer, this.subPackage().currentPackageNo());
        }
    }

    default Jt808MessageHeaderBuilder mutate() {
        return new DefaultJt808RequestHeader.DefaultJt808MessageHeaderBuilder(this);
    }
    // endregion 辅助方法定义

    interface Jt808MessageBodyProps {

        static Jt808MessageBodyPropsBuilder newBuilder() {
            return new DefaultJt808MessageBodyProps.DefaultJt808MessageBodyPropsBuilder();
        }

        static Jt808MessageBodyProps from(int value) {
            return new DefaultJt808MessageBodyProps(value);
        }

        static int create(int messageBodySize, int encryptionType, boolean isSubPackage, Jt808ProtocolVersion version, int reversedBit15) {
            // [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
            int props = (messageBodySize & 0x3FF)
                        // [10-12] 0001,1100,0000,0000(1C00)(加密类型)
                        | ((encryptionType << 10) & 0x1C00)
                        // [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
                        | (((isSubPackage ? 1 : 0) << 13) & 0x2000)
                        // [14_ ]  0100,0000,0000,0000(4000)(保留位)
                        | ((version.versionBit() << 14) & 0x4000)
                        // [15_ ]  1000,0000,0000,0000(8000)(保留位)
                        | ((reversedBit15 << 15) & 0x8000);
            return props & 0xFFFF;
        }

        int intValue();

        // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
        default int messageBodyLength() {
            return intValue() & 0x3ff;
        }

        /**
         * bit[10-12] 0001,1100,0000,0000(1C00)(加密类型)
         *
         * @see #dataEncryptionType()
         */
        default int encryptionType() {
            return (intValue() & 0x1c00) >> 10;
        }

        /**
         * @see #encryptionType()
         * @since 2.1.4
         */
        default Jt808MessageEncryptionType dataEncryptionType() {
            return Jt808MessageEncryptionType.fromIntValue(this.encryptionType());
        }

        // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
        default boolean hasSubPackage() {
            return ((intValue() & 0x2000) >> 13) == 1;
        }

        // bit[14] 0100,0000,0000,0000(4000)(版本标识)
        default int versionIdentifier() {
            return (intValue() & 0x4000) >> 14;
        }

        // bit[15] 1000,0000,0000,0000(8000)(保留位)
        default int reversedBit15() {
            return (intValue() & 0x8000) >> 15;
        }

        default Jt808MessageBodyPropsBuilder mutate() {
            return new DefaultJt808MessageBodyProps.DefaultJt808MessageBodyPropsBuilder(this.intValue());
        }
    }

    interface Jt808SubPackageProps {

        /**
         * byte[0-2) 消息包总数(word(16)) 该消息分包后的总包数
         */
        int totalSubPackageCount();

        /**
         * byte[2-4) 包序号(word(16))  从 1 开始
         */
        int currentPackageNo();
    }

    interface Jt808MessageBodyPropsBuilder {

        Jt808MessageBodyPropsBuilder messageBodyLength(int messageBodyLength);

        Jt808MessageBodyPropsBuilder encryptionType(int encryptionType);

        Jt808MessageBodyPropsBuilder hasSubPackage(int subPackageIdentifier);

        Jt808MessageBodyPropsBuilder hasSubPackage(boolean hasSubPackage);

        Jt808MessageBodyPropsBuilder versionIdentifier(int versionIdentifier);

        Jt808MessageBodyPropsBuilder versionIdentifier(Jt808ProtocolVersion version);

        Jt808MessageBodyPropsBuilder reversedBit15(int reversedBit15);

        Jt808MessageBodyProps build();
    }

    interface Jt808MessageHeaderBuilder {
        Jt808MessageHeaderBuilder version(Jt808ProtocolVersion version);

        Jt808MessageHeaderBuilder messageId(int messageId);

        Jt808MessageHeaderBuilder messageBodyProps(Jt808MessageBodyProps messageBodyProps);

        Jt808MessageHeaderBuilder terminalId(String terminalId);

        Jt808MessageHeaderBuilder flowId(int flowId);

        Jt808MessageHeaderBuilder subPackage(Jt808SubPackageProps subPackage);

        Jt808RequestHeader build();
    }
}

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

package io.github.hylexus.xtream.debug.codec.core.demo04.spec;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Jt808MsgHeader {
    /**
     * 消息ID word(16)
     */
    private int msgId;

    /**
     * 消息体属性 word(16)
     */
    private int msgBodyProperty;

    /**
     * 协议版本号(v2019)
     */
    private int msgVersionNumber;

    /**
     * 协议版本号(v2019)
     */
    private Jt808ProtocolVersion version;

    /**
     * 终端手机号或设备ID bcd[6](v2013,v2011) || bcd[10](v2019)
     */
    private String terminalPhoneNo;

    /**
     * 消息流水号 word(16)
     */
    private int msgSerialNo;

    /**
     * 消息包封装项: 消息包总数(word(16)) 该消息分包后得总包数
     */
    private int totalPackageCount = 1;
    /**
     * 消息包封装项: 包序号(word(16))  从 1 开始
     */
    private int currentPackageNo = 1;

    // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
    public boolean isSubPackage() {
        return ((msgBodyProperty & 0x2000) >> 13) == 1;
    }

    /**
     * 消息体属性:bit[14] 0100,0000,0000,0000(4000)(版本标识)
     * <ul>
     *     <li>v2019: 1</li>
     *     <li>v2013,v2011: 0</li>
     * </ul>
     */
    public int versionIdentifier() {
        return (msgBodyProperty & 0x4000) >> 14;
    }

    /**
     * 消息体属性:bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
     */
    public int msgBodyLength() {
        return msgBodyProperty & 0x3ff;
    }

    /**
     * 消息体起始位置
     */
    public int msgBodyStartIndex() {
        return msgBodyStartIndex(this.version, this.isSubPackage());
    }

    static int msgBodyStartIndex(Jt808ProtocolVersion version, boolean hasSubPackage) {
        return switch (version) {
            case VERSION_2019 -> hasSubPackage ? 21 : 17;
            case VERSION_2013, VERSION_2011 -> hasSubPackage ? 16 : 12;
            case null -> throw new NullPointerException("未知版本");
        };
    }

    /**
     * 构建消息体属性
     *
     * @param msgBodySize    消息体大小
     * @param encryptionType 加密类型
     * @param isSubPackage   是否是子包
     * @param reversedBit15  保留位
     * @param version        版本
     */
    public static int generateMsgBodyProps(int msgBodySize, int encryptionType, boolean isSubPackage, int reversedBit15, Jt808ProtocolVersion version) {
        // [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
        int props = (msgBodySize & 0x3FF)
                // [10-12] 0001,1100,0000,0000(1C00)(加密类型)
                | ((encryptionType << 10) & 0x1C00)
                // [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
                | (((isSubPackage ? 1 : 0) << 13) & 0x2000)
                // [14_ ]  0100,0000,0000,0000(4000)(保留位)
                | ((version.getVersionBit() << 14) & 0x4000)
                // [15_ ]  1000,0000,0000,0000(8000)(保留位)
                | ((reversedBit15 << 15) & 0x8000);
        return props & 0xFFFF;
    }

    @Override
    public String toString() {
        return "Jt808MsgHeader{"
                + "msgId=" + msgId
                + ", version=" + version
                + ", terminalPhoneNo='" + terminalPhoneNo + '\''
                + ", msgSerialNo=" + msgSerialNo
                + ", packageInfo=" + currentPackageNo + "/" + totalPackageCount
                + '}';
    }

}

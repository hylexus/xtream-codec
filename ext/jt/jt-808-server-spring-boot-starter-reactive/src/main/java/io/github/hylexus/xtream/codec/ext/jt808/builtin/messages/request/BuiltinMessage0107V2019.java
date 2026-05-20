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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 查询终端属性应答 0x0107
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0107, desc = "查询终端属性应答(2019)")
public class BuiltinMessage0107V2019 {

    /**
     * 终端类型
     * <li>bit0，0：不适用客运车辆，1：适用客运车辆</li>
     * <li>bit1，0：不适用危险品车辆，1：适用危险品车辆</li>
     * <li>bit2，0：不适用普通货运车辆，1：适用普通货运车辆</li>
     * <li>bit3，0：不适用出租车辆，1：适用出租车辆</li>
     * <li>bit6，0：不支持硬盘录像，1：支持硬盘录像</li>
     * <li>bit7，0：一体机，1：分体机</li>
     * <li>bit8，0：不适用挂车，1：适用挂车</li>
     */
    @Preset.JtStyle.Word(desc = "终端类型")
    private int type;

    @Preset.JtStyle.Bytes(length = 11, desc = "制造商ID(11)")
    private String manufacturerId;

    @Preset.JtStyle.Bytes(length = 30, desc = "终端型号(30)")
    private String terminalType;

    @Preset.JtStyle.Bytes(length = 30, desc = "终端ID(30)")
    private String terminalId;

    @Preset.JtStyle.Bcd(length = 10, desc = "终端SIM卡ICCID")
    private String iccid;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 终端硬件版本号长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "终端硬件版本号")
    private String hardwareVersion;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 终端固件版本号长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "终端固件版本号")
    private String firmwareVersion;

    @Preset.JtStyle.Byte(desc = "GNSS 模块属性")
    private short gnssModelProperty;

    @Preset.JtStyle.Byte(desc = "通信模块属性")
    private short communicationModelProperty;

    public int getType() {
        return type;
    }

    public BuiltinMessage0107V2019 setType(int type) {
        this.type = type;
        return this;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public BuiltinMessage0107V2019 setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
        return this;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public BuiltinMessage0107V2019 setTerminalType(String terminalType) {
        this.terminalType = terminalType;
        return this;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public BuiltinMessage0107V2019 setTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    public String getIccid() {
        return iccid;
    }

    public BuiltinMessage0107V2019 setIccid(String iccid) {
        this.iccid = iccid;
        return this;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public BuiltinMessage0107V2019 setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
        return this;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public BuiltinMessage0107V2019 setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
        return this;
    }

    public short getGnssModelProperty() {
        return gnssModelProperty;
    }

    public BuiltinMessage0107V2019 setGnssModelProperty(short gnssModelProperty) {
        this.gnssModelProperty = gnssModelProperty;
        return this;
    }

    public short getCommunicationModelProperty() {
        return communicationModelProperty;
    }

    public BuiltinMessage0107V2019 setCommunicationModelProperty(short communicationModelProperty) {
        this.communicationModelProperty = communicationModelProperty;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0107V2019.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("manufacturerId='" + manufacturerId + "'")
                .add("terminalType='" + terminalType + "'")
                .add("terminalId='" + terminalId + "'")
                .add("iccid='" + iccid + "'")
                .add("hardwareVersion='" + hardwareVersion + "'")
                .add("firmwareVersion='" + firmwareVersion + "'")
                .add("gnssModelProperty=" + gnssModelProperty)
                .add("communicationModelProperty=" + communicationModelProperty)
                .toString();
    }
}

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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 查询终端属性应答 0x0107
 *
 * @author hylexus
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
@Jt808ResponseBody(messageId = 0x0107, desc = "查询终端属性应答(2013)")
public class BuiltinMessage0107V2013 {

    /**
     * 终端类型
     * <li>bit0，0：不适用客运车辆，1：适用客运车辆</li>
     * <li>bit1，0：不适用危险品车辆，1：适用危险品车辆</li>
     * <li>bit2，0：不适用普通货运车辆，1：适用普通货运车辆</li>
     * <li>bit3，0：不适用出租车辆，1：适用出租车辆</li>
     * <li>bit6，0：不支持硬盘录像，1：支持硬盘录像</li>
     * <li>bit7，0：一体机，1：分体机</li>
     */
    @Preset.JtStyle.Word(desc = "终端类型")
    private short type;

    @Preset.JtStyle.Bytes(length = 5, desc = "制造商ID(5)")
    private String manufacturerId;

    @Preset.JtStyle.Bytes(length = 20, desc = "终端型号(20)")
    private String terminalType;

    @Preset.JtStyle.Bytes(length = 7, desc = "终端ID(7)")
    private String terminalId;

    @Preset.JtStyle.Bcd(length = 10, desc = "终端SIM卡ICCID")
    private String iccid;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 终端硬件版本号长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "终端硬件版本号")
    private String hardwareVersion;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 终端固件版本号长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "终端固件版本号")
    private String firmwareVersion;

}

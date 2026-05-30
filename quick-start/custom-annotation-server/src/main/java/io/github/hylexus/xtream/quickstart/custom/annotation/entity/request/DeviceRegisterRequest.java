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

package io.github.hylexus.xtream.quickstart.custom.annotation.entity.request;

import io.github.hylexus.xtream.quickstart.custom.annotation.entity.AbstractEntity;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// X-IoT 设备注册请求 (`msgType=0x14`) 消息体。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+-----------------------------------------------------+
/// | 偏移        | 长度   | 说明                                                |
/// +-------------+--------+-----------------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                         |
/// | 4           | 1      | msgType: u8, 固定 0x14 (设备注册)                    |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)                    |
/// | 7           | 1      | imeiLen: u8, 后续 imei 字符串的字节长度              |
/// | 8           | N      | imei: String(ASCII), 由 imeiLen 决定长度            |
/// | 8+N         | 1      | productKeyLen: u8, 后续 productKey 字符串的字节长度  |
/// | 9+N         | M      | productKey: String(ASCII), 由 productKeyLen 决定长度 |
/// +-------------+--------+-----------------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class DeviceRegisterRequest extends AbstractEntity {

    // region 消息体

    /// 设备 IMEI，ASCII 编码，前置 1 字节长度字段
    @Preset.RustStyle.str(charset = "ascii", prependLengthFieldLength = 1)
    private String imei;

    /// 产品密钥，ASCII 编码，前置 1 字节长度字段
    @Preset.RustStyle.str(charset = "ascii", prependLengthFieldLength = 1)
    private String productKey;
    // endregion

}

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

/// X-IoT 报警上报 (`msgType=0x15`) 消息体。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+----------------------------------------------+
/// | 偏移        | 长度   | 说明                                         |
/// +-------------+--------+----------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                  |
/// | 4           | 1      | msgType: u8, 固定 0x15 (报警上报)             |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)             |
/// | 7           | 2      | alarmType: u16 big-endian, 报警类型码         |
/// | 9           | 1      | descLen: u8, 后续 desc 字符串的 UTF-8 字节长度 |
/// | 10          | N      | desc: String(UTF-8), 由 descLen 决定长度      |
/// +-------------+--------+----------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class AlarmReport extends AbstractEntity {

    // region 消息体

    /// 报警类型，uint16 big-endian。如 1 = 通用报警，2 = 超温报警
    @Preset.RustStyle.u16
    private int alarmType;

    /// 报警描述，UTF-8 编码，前置 1 字节长度字段
    @Preset.RustStyle.str(prependLengthFieldLength = 1)
    private String desc;
    // endregion
}

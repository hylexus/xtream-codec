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

package io.github.hylexus.xtream.quickstart.custom.annotation.entity.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.quickstart.custom.annotation.entity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/// 服务器时间响应 (`msgType=0x11` 的响应，响应类型为 `0x81`)。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+-----------------------------------------------------+
/// | 偏移        | 长度   | 说明                                                |
/// +-------------+--------+-----------------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                         |
/// | 4           | 1      | msgType: u8, 固定 0x81 (时间查询响应)                 |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)，固定为 6           |
/// | 7           | 6      | serverTime: BCD[6], yyMMddHHmmss                    |
/// +-------------+--------+-----------------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class ServerTimeResponse extends AbstractEntity {

    // region 消息体 (6 bytes)
    // byte[7-13)  服务器时间 BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BcdDateTime
    private LocalDateTime serverTime;
    // endregion

    public ServerTimeResponse() {
        this.msgType = 0x81;
    }

    public ServerTimeResponse(LocalDateTime serverTime) {
        this();
        this.serverTime = serverTime;
        // BCD 时间固定 6 字节
        this.bodyLength = 6;
    }
}

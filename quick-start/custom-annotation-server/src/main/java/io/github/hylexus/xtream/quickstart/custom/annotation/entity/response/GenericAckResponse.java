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

/// 通用应答 (`msgType=0x80`)。
///
/// 用于对心跳(`0x10`)、温湿度上报(`0x12`)等消息的应答。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+-----------------------------------------------------+
/// | 偏移        | 长度   | 说明                                                |
/// +-------------+--------+-----------------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                         |
/// | 4           | 1      | msgType: u8, 固定 0x80 (通用应答)                    |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)，固定为 2           |
/// | 7           | 1      | ackMsgType: u8, 被应答的消息类型                      |
/// | 8           | 1      | result: u8, 0=成功 / 1=失败                          |
/// +-------------+--------+-----------------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class GenericAckResponse extends AbstractEntity {

    // region 消息体 (2 bytes)
    // byte[7]     被应答的消息类型 u8 — 例如 0x10(心跳) 或 0x12(温湿度)
    @Preset.RustStyle.u8
    private int ackMsgType;

    // byte[8]     应答结果 u8 — 0=成功 1=失败
    @Preset.RustStyle.u8
    private int result;
    // endregion

    public GenericAckResponse() {
        this.msgType = 0x80;
    }

    /// @param ackMsgType 被应答的消息类型 (如 0x10, 0x12)
    /// @param result     应答结果 (0=成功, 1=失败)
    public GenericAckResponse(int ackMsgType, int result) {
        this();
        this.ackMsgType = ackMsgType;
        this.result = result;
        this.bodyLength = 2;
    }
}

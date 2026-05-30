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

/// 注册应答 (`msgType=0x82`)。
///
/// 用于对设备注册(`0x14`)的应答。
///
/// ## 报文格式
///
/// ```
/// +-------------+--------+-----------------------------------------------------+
/// | 偏移        | 长度   | 说明                                                |
/// +-------------+--------+-----------------------------------------------------+
/// | 0           | 4      | magic: u32, 固定 0x12345678                         |
/// | 4           | 1      | msgType: u8, 固定 0x82 (注册应答)                    |
/// | 5           | 2      | bodyLength: u16, 消息体长度(大端)                     |
/// | 7           | 1      | result: u8, 0=注册成功 / 1=注册失败                   |
/// | 8           | 1      | replyMsgLen: u8, 后续回复消息的字节长度                |
/// | 9           | N      | replyMsg: String(ASCII), 注册结果描述                 |
/// +-------------+--------+-----------------------------------------------------+
/// ```
@Getter
@Setter
@ToString(callSuper = true)
public class RegisterAckResponse extends AbstractEntity {

    // region 消息体
    // byte[7]     注册结果 u8 — 0=成功 1=失败
    @Preset.RustStyle.u8
    private int result;

    // byte[8..)   回复描述 ASCII 字符串(前置 1 字节长度)
    @Preset.RustStyle.str(charset = "ascii", prependLengthFieldLength = 1)
    private String replyMsg;
    // endregion

    public RegisterAckResponse() {
        this.msgType = 0x82;
    }

    /// @param result   注册结果 (0=成功, 1=失败)
    /// @param replyMsg 回复描述 (ASCII)
    public RegisterAckResponse(int result, String replyMsg) {
        this();
        this.result = result;
        this.replyMsg = replyMsg;
        // body = result(1) + replyMsgLen(1) + replyMsg(N)
        this.bodyLength = 1 + 1 + replyMsg.length();
    }
}

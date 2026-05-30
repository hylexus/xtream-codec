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

package io.github.hylexus.xtream.quickstart.custom.annotation.entity;

import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;

/// X-IoT Demo 协议消息体基类。
///
/// 包含公共的 7 字节协议头：
///
/// ```
/// +------------+------+--------------------------------------------+
/// | magic      | u32  | big-endian, 固定 0x12345678               |
/// | msgType    | u8   | 消息类型                                     |
/// | bodyLength | u16  | big-endian, 消息体长度                      |
/// +------------+------+--------------------------------------------+
/// ```
@Getter
@Setter
public abstract class AbstractEntity {

    // region 协议头 (7 bytes)

    /// 魔数，u32 big-endian，固定 0x12345678
    @Preset.RustStyle.u32(order = -300)
    protected long magic = 0x12345678L;

    /// 消息类型，u8
    @Preset.RustStyle.u8(order = -200)
    protected int msgType;

    /// 消息体长度，u16 big-endian
    @Preset.RustStyle.u16(order = -100)
    protected int bodyLength;
    // endregion

}

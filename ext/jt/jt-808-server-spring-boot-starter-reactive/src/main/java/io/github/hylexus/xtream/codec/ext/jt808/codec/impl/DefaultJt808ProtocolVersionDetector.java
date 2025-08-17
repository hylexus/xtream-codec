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

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ProtocolVersionDetector;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.netty.buffer.ByteBuf;

public class DefaultJt808ProtocolVersionDetector implements Jt808ProtocolVersionDetector {

    @Override
    public Jt808ProtocolVersion detectVersion(int messageId, Jt808RequestHeader.Jt808MessageBodyProps messageBodyProps, ByteBuf byteBuf) {
        // 消息体属性中 第14位
        final int versionIdentifier = messageBodyProps.versionIdentifier();
        // 有版本标识
        if (versionIdentifier == 1) {
            final byte version = byteBuf.getByte(4);
            if (version == Jt808ProtocolVersion.VERSION_2019.versionBit()) {
                return Jt808ProtocolVersion.VERSION_2019;
            }
            // 目前只有 V2019 带版本标识，其他情况视为报文格式异常
            throw new IllegalArgumentException("Invalid version: " + messageBodyProps);
        }

        // 终端注册
        if (messageId == 0x0100) {
            if (messageBodyProps.messageBodyLength() > 37) {
                return Jt808ProtocolVersion.VERSION_2013;
            }
            return Jt808ProtocolVersion.VERSION_2011;
        }

        // if(终端鉴权) {
        // // 自定义实现建议:
        // // 要区分鉴权消息的版本类型，只靠协议格式是做不到的，只能从业务入手：比如给不同版本的终端分配不同长度的鉴权码，靠消息体长度来区分不同版本
        //     return ...;
        // }

        // 其他都视为 2013 版本
        return Jt808ProtocolVersion.VERSION_2013;
    }
}

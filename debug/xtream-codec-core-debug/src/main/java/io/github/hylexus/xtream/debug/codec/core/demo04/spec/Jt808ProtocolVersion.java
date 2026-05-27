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

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public enum Jt808ProtocolVersion {
    VERSION_2013("2013", (byte) 0),
    VERSION_2011("2011", (byte) 0),
    VERSION_2019("2019", (byte) 1),
    ;

    /**
     * 消息体属性中 第14位
     */
    private final byte versionBit;
    private final String shortDesc;

    Jt808ProtocolVersion(String shortDesc, byte versionBit) {
        this.shortDesc = shortDesc;
        this.versionBit = versionBit;
    }

    public static Jt808ProtocolVersion detectVersion(Jt808MsgHeader header, ByteBuf byteBuf) {
        // bit[14] 0100,0000,0000,0000(4000)(版本标识)
        // 版本标识为1 ==> v2019 才有的
        // 不为1 ==> v2013 或 v2011(v2013 和 v2011没法真正区分出来) ==> 这里默认为 v2013
        if (header.versionIdentifier() != 1) {
            return Jt808ProtocolVersion.VERSION_2013;
        }

        // byte[4] 协议版本号 (header.versionIdentifier() == 1时才有)
        final byte version = byteBuf.getByte(4);
        if (version == Jt808ProtocolVersion.VERSION_2019.getVersionBit()) {
            return Jt808ProtocolVersion.VERSION_2019;
        }
        return Jt808ProtocolVersion.VERSION_2013;
    }
}

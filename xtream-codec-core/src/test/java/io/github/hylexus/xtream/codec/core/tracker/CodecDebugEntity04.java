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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.core.RuntimeTypeSupplier;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CodecDebugEntity04 implements RuntimeTypeSupplier {

    public CodecDebugEntity04() {
    }

    private Class<?> runtimeClass;

    @Preset.JtStyle.Object(desc = "消息头")
    private Header header;

    @Preset.JtStyle.RuntimeType(lengthExpression = "header.msgBodyLength()",desc = "消息体")
    private Object body;

    @Preset.JtStyle.Byte(desc = "校验码")
    private short checkSum;

    @Override
    public Class<?> getRuntimeType(String name) {
        return this.runtimeClass;
    }

    public CodecDebugEntity04 setRuntimeClass(Class<?> runtimeClass) {
        this.runtimeClass = runtimeClass;
        return this;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Header {

        public boolean hasVersionField() {
            return (msgBodyProps & 0x4000) >> 14 == 1;
        }

        @Preset.JtStyle.Word
        private int msgId;

        // byte[2-4)    消息体属性 word(16)
        @Preset.JtStyle.Word
        private int msgBodyProps;

        // byte[4]     协议版本号
        @Preset.JtStyle.Byte(condition = "hasVersionField()", desc = "协议版本号(V2019+)")
        private short protocolVersion;

        // byte[5-15)    终端手机号或设备ID bcd[10]
        @Preset.JtStyle.Bcd(lengthExpression = "hasVersionField() ? 10: 6")
        private String terminalId;

        // byte[15-17)    消息流水号 word(16)
        @Preset.JtStyle.Word
        private int msgSerialNo;

        // byte[17-21)    消息包封装项
        @Preset.JtStyle.Dword(condition = "hasSubPackage()")
        private Long subPackageInfo;

        // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
        public int msgBodyLength() {
            return msgBodyProps & 0x3ff;
        }

        // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
        public boolean hasSubPackage() {
            // return ((msgBodyProperty & 0x2000) >> 13) == 1;
            return (msgBodyProps & 0x2000) > 0;
        }
    }
}

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

package io.github.hylexus.xtream.debug.codec.core.demo01;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.debug.codec.core.utilsforunittest.DebugEntity01NestedForJunitPurpose;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RustStyleDebugEntity01ForEncodeNested {

    // 整个 Header 封装到一个实体类中
    @Preset.RustStyle.struct
    private Header header;

    // 消息体长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgBodyLength;

    // 整个 Body 封装到一个实体类中
    @Preset.RustStyle.struct
    private Body body;

    // 下面是 Header 和 Body 实体类的声明
    @Data
    // 实现 DebugEntity01NestedHeader 仅仅是为了方便单元测试，没其他特殊意义
    public static class Header implements DebugEntity01NestedForJunitPurpose.DebugEntity01NestedHeader {
        // 固定为 0x80901234
        @Preset.RustStyle.i32
        private int magicNumber = 0x80901234;

        // 主版本号 无符号数 1字节
        @Preset.RustStyle.u8
        private short majorVersion;
        // 次版本号 无符号数 1字节

        @Preset.RustStyle.u8
        private short minorVersion;

        // 消息类型 无符号数 2字节
        @Preset.RustStyle.u16
        private int msgType;
    }


    @Data
    // 实现 DebugEntity01NestedBody 仅仅是为了方便单元测试，没其他特殊意义
    public static class Body implements DebugEntity01NestedForJunitPurpose.DebugEntity01NestedBody {
        // 下一个字段长度 无符号数 2字节
        @Preset.RustStyle.u16
        private int usernameLength;

        // 用户名 String, "UTF-8"
        @Preset.RustStyle.str
        private String username;

        // 下一个字段长度 无符号数 2字节
        @Preset.RustStyle.u16
        private int passwordLength;

        // 密码 String, "GBK"
        @Preset.RustStyle.str(charset = "GBK")
        private String password;

        // 生日 String[8], "yyyyMMdd", "UTF-8"
        @Preset.RustStyle.str
        private String birthday;

        // 手机号 BCD_8421[6] "GBK"
        @Preset.RustStyle.str(charset = "bcd_8421")
        private String phoneNumber;

        // 年龄 无符号数 2字节
        @Preset.RustStyle.u16
        private int age;

        // 状态 有符号数 2字节
        @Preset.RustStyle.i16
        private short status;
    }
}
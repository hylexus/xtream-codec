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

package io.github.hylexus.xtream.debug.codec.core.demo01;

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RustStyleDebugEntity01Nested {

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
    public static class Header {
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
    public static class Body {
        // 下一个字段长度 无符号数 2字节
        @Preset.RustStyle.u16
        private int usernameLength;

        // 用户名 String, "UTF-8"
        // @Preset.RustStyle.str(lengthExpression = "getUsernameLength()")
        @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getUsernameLength()", mvel = "self.getUsernameLength()", aviator = "self.usernameLength"))
        private String username;

        // 下一个字段长度 无符号数 2字节
        @Preset.RustStyle.u16
        private int passwordLength;

        // 密码 String, "GBK"
        // @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "getPasswordLength()")
        @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpressions = @Expression(spel = "getPasswordLength()", mvel = "self.getPasswordLength()", aviator = "self.passwordLength"))
        private String password;

        // 生日 String[8], "yyyyMMdd", "UTF-8"
        @Preset.RustStyle.str(length = 8)
        private String birthday;

        // 手机号 BCD_8421[6] "GBK"
        @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
        private String phoneNumber;

        // 年龄 无符号数 2字节
        @Preset.RustStyle.u16
        private int age;

        // 状态 有符号数 2字节
        @Preset.RustStyle.i16
        private short status;
    }
}

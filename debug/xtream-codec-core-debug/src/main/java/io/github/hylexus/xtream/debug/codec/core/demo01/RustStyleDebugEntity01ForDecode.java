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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.debug.codec.core.utilsforunittest.DebugEntity01ForJunitPurpose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
// 实现 DebugEntity01 仅仅是为了方便做单元测试，没其他特殊意义
public class RustStyleDebugEntity01ForDecode implements DebugEntity01ForJunitPurpose {
    //////////////////////// header
    // 固定为 0x80901234
    @Preset.RustStyle.i32
    private int magicNumber;

    // 主版本号 无符号数 1字节
    @Preset.RustStyle.u8
    private short majorVersion;
    // 次版本号 无符号数 1字节

    @Preset.RustStyle.u8
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgType;

    // 消息体长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgBodyLength;

    //////////////////////// body
    // 下一个字段(username)长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int usernameLength;

    // 用户名 String, "UTF-8"
    @Preset.RustStyle.str(lengthExpression = "getUsernameLength()")
    private String username;

    // 下一个字段(password)长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int passwordLength;

    // 密码 String, "GBK"
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "getPasswordLength()")
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8" 8字节
    @Preset.RustStyle.str(length = 8)
    private String birthday;

    // 手机号 BCD_8421[6] 6字节(12个数字)
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @Preset.RustStyle.u16
    private int age;

    // 状态 有符号数 2字节
    @Preset.RustStyle.i16
    private short status;
}

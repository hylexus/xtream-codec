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
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
// 这里使用了 prependLengthFieldType 和 prependLengthFieldLength 属性
// 从而省略了 usernameLength 和 passwordLength
public class JtStyleDebugEntity01FlattenSimple {
    //////////////////////// header
    // 固定为 0x80901234
    @Preset.JtStyle.Dword
    private long magicNumber = 0x80901234L;

    // 主版本号 无符号数 1字节
    @Preset.JtStyle.Byte
    private short majorVersion;
    // 次版本号 无符号数 1字节

    @Preset.JtStyle.Byte
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @Preset.JtStyle.Word
    private int msgType;

    // 消息体长度 无符号数 2字节
    @Preset.JtStyle.Word
    private int msgBodyLength;

    //////////////////////// body
    // 用户名 String, "UTF-8"
    // prependLengthFieldType: 前面自动添加一个u16类型字段作为该字段的长度字段
    @Preset.JtStyle.Str(charset = "UTF-8", prependLengthFieldType = PrependLengthFieldType.u16)
    private String username;

    // 密码 String, "GBK"
    // prependLengthFieldType: 前面自动添加一个u16类型(2字节)字段作为该字段的长度字段
    @Preset.JtStyle.Str(charset = XtreamConstants.CHARSET_NAME_GBK, prependLengthFieldLength = 2)
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8"
    @Preset.JtStyle.Str(charset = "UTF-8", length = 8)
    private String birthday;

    // 手机号 BCD_8421[6]
    @Preset.JtStyle.Bcd(length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @Preset.JtStyle.Word
    private int age;

    // 状态 无符号数 2字节
    @Preset.JtStyle.Word
    private int status;
}

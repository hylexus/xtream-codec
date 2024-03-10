/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.debug.codec.core.demo01;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.debug.codec.core.utilsforunittest.DebugEntity01;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
// 实现 DebugEntity01 仅仅是为了方便做单元测试，没其他特殊意义
public class RustStyleDebugEntity01ForEncode implements DebugEntity01 {
    //////////////////////// header
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

    // 消息体长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgBodyLength;

    //////////////////////// body
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

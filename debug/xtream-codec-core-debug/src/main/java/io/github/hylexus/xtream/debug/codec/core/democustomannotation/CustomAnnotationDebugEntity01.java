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

package io.github.hylexus.xtream.debug.codec.core.democustomannotation;

import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Setter
@Getter
@ToString
// 实现 DebugEntity01 仅仅是为了方便做单元测试，没其他特殊意义
public class CustomAnnotationDebugEntity01 {

    // 主版本号 无符号数 1字节
    @Preset.RustStyle.u8
    private short majorVersion = 2;
    // 次版本号 无符号数 1字节

    // 生日 String[8], "yyyyMMdd", "UTF-8" 8字节
    @MyDateType
    private LocalDate birthday;

    // ... 其他属性 ...
}

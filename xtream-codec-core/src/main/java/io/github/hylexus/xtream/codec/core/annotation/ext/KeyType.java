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

package io.github.hylexus.xtream.codec.core.annotation.ext;

import io.github.hylexus.xtream.codec.core.type.XtreamDataType;

public enum KeyType {
    i8(XtreamDataType.i8),
    u8(XtreamDataType.u8),
    i16(XtreamDataType.i16),
    u16(XtreamDataType.u16),
    i32(XtreamDataType.i32),
    u32(XtreamDataType.u32),
    i64(XtreamDataType.i64),
    str(XtreamDataType.string),
    str_gb_2312(XtreamDataType.string_gb_2312),
    str_gbk(XtreamDataType.string_gbk),
    str_utf8(XtreamDataType.string_utf8),
    ;

    private final XtreamDataType dataType;

    KeyType(XtreamDataType dataType) {
        this.dataType = dataType;
    }

    public XtreamDataType dataType() {
        return dataType;
    }
}

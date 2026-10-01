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

package io.github.hylexus.xtream.codec.core.dynamic.schema;

/**
 * 固定名称的字段类型。整数类型带有位宽，继续使用 {@code uN}/{@code iN} 字符串声明。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DynamicFieldType {
    FLOAT("float"),
    STRING("string"),
    BCD("bcd"),
    HEX("hex"),
    BYTES("bytes"),
    BITS("bits"),
    PADDING("padding"),
    ALIGN("align"),
    STRUCT("struct"),
    ARRAY("array"),
    DERIVED("derived"),
    TLV("tlv"),
    MAP("map"),
    PAIR("pair"),
    CHOICE("choice"),
    EXTENSION("extension"),
    ;

    private final String value;

    DynamicFieldType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

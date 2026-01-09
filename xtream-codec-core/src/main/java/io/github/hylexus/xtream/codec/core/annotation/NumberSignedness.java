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

package io.github.hylexus.xtream.codec.core.annotation;


/**
 * @author hylexus
 */
public enum NumberSignedness {
    /**
     * 有符号数字类型 (Signed)
     * 例如: i8, i16, i32, i64
     */
    SIGNED("Signed"),

    /**
     * 无符号数字类型 (Unsigned)
     * 例如: u8, u16, u32, u64
     */
    UNSIGNED("Unsigned"),
    /**
     * 未知符号类型 或 用于占位符
     */
    NONE("None");

    private final String value;

    NumberSignedness(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}

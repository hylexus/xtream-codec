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

package io.github.hylexus.xtream.codec.core.debugger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 调试请求的编解码模式；JSON 中使用 entity 或 dynamic。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DebugCodecMode {
    ENTITY("entity"), DYNAMIC("dynamic");

    private final String value;

    DebugCodecMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static DebugCodecMode from(String value) {
        return switch (value) {
            case "entity" -> ENTITY;
            case "dynamic" -> DYNAMIC;
            default -> throw new IllegalArgumentException("mode 必须为 entity 或 dynamic: " + value);
        };
    }
}


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
 * 调试请求的执行方向；JSON 中使用 encode 或 decode。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DebugCodecDirection {
    ENCODE("encode"), DECODE("decode");

    private final String value;

    DebugCodecDirection(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static DebugCodecDirection from(String value) {
        return switch (value) {
            case "encode" -> ENCODE;
            case "decode" -> DECODE;
            default -> throw new IllegalArgumentException("direction 必须为 encode 或 decode: " + value);
        };
    }
}


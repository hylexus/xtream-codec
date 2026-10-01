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
 * 固定字节边界内的填充方向，可用于 options.padSide。
 * LEFT 在值之前填充，RIGHT 在值之后填充；JSON 保持 left/right 字符串。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public enum DynamicPaddingSide {
    LEFT("left"), RIGHT("right");

    private final String value;

    DynamicPaddingSide(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

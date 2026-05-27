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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;

import java.util.Optional;
import java.util.StringJoiner;

public sealed interface FieldLength permits FieldLength.DefaultFieldLength {

    LengthFieldType type();

    // value 没有暴露 set 方法；
    // 使用方也没必要手动 setValue
    // 编解码器会负责设置 value
    int value();

    default Optional<Integer> getValue() {
        if (this.initialized()) {
            return Optional.of(this.value());
        }
        return Optional.empty();
    }

    boolean initialized();

    // 只有这个一个实现类
    final class DefaultFieldLength implements FieldLength {
        private final LengthFieldType type;
        private int value;
        private boolean initialized = false;

        public DefaultFieldLength(LengthFieldType type) {
            this.type = type;
        }

        @Override
        public int value() {
            return this.value;
        }

        public DefaultFieldLength setValue(int value) {
            this.value = value;
            this.initialized = true;
            return this;
        }

        @Override
        public boolean initialized() {
            return this.initialized;
        }

        @Override
        public LengthFieldType type() {
            return this.type;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", DefaultFieldLength.class.getSimpleName() + "[", "]")
                    .add("type=" + type)
                    .add("value=" + value)
                    .add("initialized=" + initialized)
                    .toString();
        }
    }
}

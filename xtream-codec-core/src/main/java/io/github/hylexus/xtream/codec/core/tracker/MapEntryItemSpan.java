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

package io.github.hylexus.xtream.codec.core.tracker;

import java.util.StringJoiner;

public class MapEntryItemSpan extends CodecSpan {
    public enum Type {
        KEY, VALUE, VALUE_LENGTH
    }

    private final Type type;

    public MapEntryItemSpan(BaseSpan parent, Type type) {
        super(parent);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MapEntryItemSpan.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("fieldCodec='" + fieldCodec + "'")
                .add("hexString='" + hexString + "'")
                .add("value=" + value)
                .toString();
    }
}

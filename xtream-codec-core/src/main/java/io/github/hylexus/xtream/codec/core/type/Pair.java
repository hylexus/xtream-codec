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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 键值对结构。
 * <p>
 * 和 {@link TLV} 的区别是 {@link Pair} 没有值长度字段。
 *
 * @author hylexus
 */
@NullMarked
@ApiStatus.Experimental
public record Pair(
        DataField.DictKey key,
        @Nullable Object value,
        @Nullable String remainingHex
) implements FieldCodecRegistry.AtomicDataType {

    @SuppressWarnings("unused")
    public Pair {
    }

    public Pair(DataField.DictKey key, @Nullable Object value) {
        this(key, value, null);
    }

    public static Pair of(DataField.DictKey key, Object value) {
        return new Pair(key, value);
    }

    public boolean hasError() {
        return remainingHex != null;
    }

}

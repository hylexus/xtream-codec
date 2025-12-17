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

/**
 * TLV（Tag-Length-Value）结构
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@NullMarked
@ApiStatus.Experimental
public record TLV(
        DataField.DictKey tag,
        FieldLength length,
        Object value
) implements FieldCodecRegistry.AtomicDataType {

    public TLV(DataField.DictKey tag, FieldLength.LengthType length, Object value) {
        this(tag, new FieldLength.DefaultFieldLength(length), value);
    }

    public static TLV of(DataField.DictKey tag, FieldLength.LengthType length, Object value) {
        return new TLV(tag, length, value);
    }

}

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

package io.github.hylexus.xtream.codec.core.impl.domain;

import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.PaddingType;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.core.annotation.ext.Key
 */
public record KeyMeta(
        int version,
        KeyType type,
        int sizeInBytes,
        String charset,
        PaddingType paddingType,
        byte paddingElement,
        FieldCodec<Object> codec
) {

    @SuppressWarnings("unused")
    public KeyMeta {
    }

    public DataField.DictKey readFrom(ByteBuf input) {
        return DataField.DictKey.deserialize(this.type(), input, this.sizeInBytes(), this.charset());
    }

    public DataField.DictKey readFromWithTracker(ByteBuf input, CodecTracker codecTracker, String fieldName) {
        return DataField.DictKey.deserializeWithTracker(codecTracker, this.type(), input, this.sizeInBytes(), this.charset(), fieldName);
    }
}

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

package io.github.hylexus.xtream.codec.core.type.wrapper;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

public class I16Wrapper implements DataWrapper<Short> {

    @Preset.RustStyle.i16
    private @Nullable Short value;

    public I16Wrapper() {
    }

    public I16Wrapper(@Nullable Short value) {
        this.value = value;
    }

    @Override
    public void writeTo(ByteBuf output) {
        if (value == null) {
            return;
        }
        output.writeShort(value);
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public byte @Nullable [] asBytes() {
        if (value == null) {
            return null;
        }
        return new byte[]{
                (byte) (requireNonNull(value) >>> 8),
                value.byteValue()
        };
    }

    @Override
    public byte asI8() {
        return requireNonNull(value).byteValue();
    }

    @Override
    public short asI16() {
        return requireNonNull(value);
    }

    @Override
    public int asI32() {
        return requireNonNull(value);
    }

    @Override
    public @Nullable String asString() {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    public I16Wrapper setValue(Short value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", I16Wrapper.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .toString();
    }

}

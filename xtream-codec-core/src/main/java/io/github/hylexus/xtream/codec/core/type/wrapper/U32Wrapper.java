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

package io.github.hylexus.xtream.codec.core.type.wrapper;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;

import java.util.StringJoiner;

public class U32Wrapper implements DataWrapper<Long> {
    static final int MASK = 0xFF;

    @Preset.RustStyle.u32
    private Long value;

    public U32Wrapper() {
    }

    public U32Wrapper(Long value) {
        this.value = value;
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeInt(value.intValue());
    }

    @Override
    public int length() {
        return 4;
    }

    @Override
    public byte[] asBytes() {
        return new byte[]{
                (byte) ((value >>> 24) & MASK),
                (byte) ((value >>> 16) & MASK),
                (byte) ((value >>> 8) & MASK),
                (byte) (value & MASK)
        };
    }

    @Override
    public byte asI8() {
        return value.byteValue();
    }

    @Override
    public short asI16() {
        return value.shortValue();
    }

    @Override
    public int asI32() {
        return value.intValue();
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    public U32Wrapper setValue(Long value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", U32Wrapper.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .toString();
    }
}
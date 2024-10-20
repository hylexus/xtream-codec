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

public class U8Wrapper implements DataWrapper<Short> {

    @Preset.RustStyle.u8
    private Short value;

    public U8Wrapper() {
    }

    public U8Wrapper(Short value) {
        this.value = value;
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeByte(value);
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public byte[] asBytes() {
        return new byte[]{value.byteValue()};
    }

    @Override
    public byte asI8() {
        return value.byteValue();
    }

    @Override
    public short asI16() {
        return value;
    }

    @Override
    public int asI32() {
        return value;
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    public U8Wrapper setValue(Short value) {
        this.value = value;
        return this;
    }
}

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

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.StringJoiner;

public class BytesDataWrapper implements DataWrapper<byte[]> {
    static final int MASK = 0xFF;

    private final byte[] value;

    public BytesDataWrapper(byte[] value) {
        this.value = value;
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeBytes(value);
    }

    @Override
    public int length() {
        return value.length;
    }

    @Override
    public byte @Nullable [] asBytes() {
        return value;
    }

    @Override
    public byte asI8() {
        return value[0];
    }

    @Override
    public short asI16() {
        return (short) ((value[0] & 0xff) << 8 | (value[1] & 0xff));
    }

    @Override
    public int asI32() {
        return ((value[0] & MASK) << 24)
               |
               ((value[1] & MASK) << 16)
               |
               ((value[2] & MASK) << 8)
               |
               ((value[3] & MASK));
    }

    @Override
    public @Nullable String asString() {
        if (value == null) {
            return null;
        }
        return new String(value);
    }

    @Override
    public @Nullable String asString(Charset charset) {
        if (value == null) {
            return null;
        }
        return new String(value, charset);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BytesDataWrapper.class.getSimpleName() + "[", "]")
                .add("value=" + Arrays.toString(value))
                .toString();
    }

}

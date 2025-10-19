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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class StringWrapperUtf8 implements DataWrapper<String> {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Preset.RustStyle.str
    protected @Nullable String value;
    protected int length;

    public StringWrapperUtf8() {
    }

    public StringWrapperUtf8(String value) {
        this.value = value;
        this.length = value.getBytes(UTF_8).length;
    }

    @Override
    public void writeTo(ByteBuf output) {
        output.writeCharSequence(this.value, UTF_8);
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public byte @Nullable [] asBytes() {
        if (value == null) {
            return null;
        }
        return value.getBytes(XtreamConstants.CHARSET_UTF8);
    }

    @Override
    public byte asI8() {
        throw new UnsupportedOperationException();
    }

    @Override
    public short asI16() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int asI32() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable String asString() {
        return value;
    }

    public StringWrapperUtf8 setValue(String value) {
        this.value = value;
        this.length = value.getBytes(UTF_8).length;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StringWrapperUtf8.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .toString();
    }

}

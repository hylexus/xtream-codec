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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * @author hylexus
 */
@JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
public interface DataWrapper<T> {

    void writeTo(ByteBuf output);

    int length();

    byte @Nullable [] asBytes();

    byte asI8();

    default short asU8() {
        return (short) (this.asI8() & 0xFF);
    }

    default short asByte() {
        return asU8();
    }

    short asI16();

    default int asU16() {
        return this.asI16() & 0xFFFF;
    }

    default int asWord() {
        return asU16();
    }

    int asI32();

    default long asDword() {
        return asU32();
    }

    default long asU32() {
        return this.asI32() & 0xFFFFFFFFL;
    }

    default @Nullable String asBcd() {
        final byte[] bytes = this.asBytes();
        if (bytes == null) {
            return null;
        }
        return BcdOps.decodeBcd8421AsString(bytes, 0, this.length());
    }

    @Nullable String asString();

    default @Nullable String asString(Charset charset) {
        return asString();
    }

}

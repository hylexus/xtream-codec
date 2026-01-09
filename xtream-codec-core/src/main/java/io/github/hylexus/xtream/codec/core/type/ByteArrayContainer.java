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


import io.github.hylexus.xtream.codec.common.utils.BcdOps;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.impl.codec.ByteArrayContainerFieldCodec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Copied from jt-framework.
 *
 * @author hylexus
 * @see ByteArrayContainerFieldCodec
 */
public interface ByteArrayContainer extends BytesContainer {

    int MASK = 0xFF;

    static ByteArrayContainer ofBytes(byte[] bytes) {
        return () -> bytes;
    }

    static ByteArrayContainer ofI8(int value) {
        return () -> new byte[]{(byte) value};
    }

    static ByteArrayContainer ofU8(int value) {
        return ofI8(value);
    }

    static ByteArrayContainer ofU16(int value) {
        return ofI16(value);
    }

    static ByteArrayContainer ofI16(int value) {
        return () -> new byte[]{
                (byte) (value >>> 8),
                (byte) value
        };
    }

    static ByteArrayContainer ofI32(int value) {
        return () -> new byte[]{
                (byte) ((value >>> 24) & MASK),
                (byte) ((value >>> 16) & MASK),
                (byte) ((value >>> 8) & MASK),
                (byte) (value & MASK)
        };
    }

    static ByteArrayContainer ofU32(int value) {
        return ofI32(value);
    }

    static ByteArrayContainer ofByte(int value) {
        return ofU8(value);
    }

    static ByteArrayContainer ofWord(int value) {
        return ofU16(value);
    }

    static ByteArrayContainer ofDword(int i) {
        return ofU32(i);
    }

    static ByteArrayContainer ofBcd(String bcd) {
        return () -> BcdOps.encodeBcd8421AsBytes(bcd);
    }

    static ByteArrayContainer ofString(String string, Charset charset) {
        return () -> string.getBytes(charset);
    }

    static ByteArrayContainer ofString(String string) {
        return ofString(string, StandardCharsets.UTF_8);
    }

    static ByteArrayContainer ofStringGbk(String string) {
        return ofString(string, XtreamConstants.CHARSET_GBK);
    }

    byte[] payload();

    @Override
    default int length() {
        return this.payload().length;
    }

    @Override
    default byte[] asBytes() {
        return this.payload();
    }

    @Override
    default String asString(Charset charset) {
        return new String(this.payload(), charset);
    }

    @Override
    default byte asI8() {
        return this.payload()[0];
    }

    @Override
    default short asI16() {
        final byte[] value = this.payload();
        return (short) ((value[0] & 0xff) << 8 | (value[1] & 0xff));
    }

    @Override
    default int asI32() {
        final byte[] bytes = this.payload();
        return ((bytes[0] & MASK) << 24)
                |
                ((bytes[1] & MASK) << 16)
                |
                ((bytes[2] & MASK) << 8)
                |
                ((bytes[3] & MASK));
    }

    @Override
    default void release() {
        // do nothing
    }
}

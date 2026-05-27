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

package io.github.hylexus.xtream.codec.common.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author hylexus
 */
public interface XtreamByteReader {

    ByteBuf readable();

    static XtreamByteReader of(ByteBuf value) {
        return () -> value;
    }

    static <T> T doWithReader(byte[] bytes, Function<XtreamByteReader, T> fn) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes);
        try {
            return fn.apply(of(buf));
        } finally {
            XtreamBytes.releaseBuf(buf);
        }
    }

    static void doWithReader(byte[] bytes, Consumer<XtreamByteReader> fn) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes);
        try {
            fn.accept(of(buf));
        } finally {
            XtreamBytes.releaseBuf(buf);
        }
    }

    default String readBcd(int length) {
        return XtreamBytes.readBcd(readable(), length);
    }

    default XtreamByteReader readBcd(int length, Consumer<String> consumer) {
        consumer.accept(this.readBcd(length));
        return this;
    }

    default short readU8() {
        return XtreamBytes.readU8(readable());
    }

    default XtreamByteReader readU8(Consumer<Short> consumer) {
        consumer.accept(this.readU8());
        return this;
    }

    default int readU16() {
        return XtreamBytes.readU16(readable());
    }

    default XtreamByteReader readU16(Consumer<Integer> consumer) {
        consumer.accept(this.readU16());
        return this;
    }

    default long readU32() {
        return readable().readUnsignedInt();
    }

    default XtreamByteReader readU32(Consumer<Long> consumer) {
        consumer.accept(this.readU32());
        return this;
    }

    default long readI64() {
        return readable().readLong();
    }

    default XtreamByteReader readI64(Consumer<Long> consumer) {
        consumer.accept(this.readI64());
        return this;
    }

    default String readString(int length) {
        return readString(length, StandardCharsets.UTF_8);
    }

    default String readString(int length, Charset charset) {
        return (String) readable().readCharSequence(length, charset);
    }

    default XtreamByteReader readString(int length, Charset charset, Consumer<String> consumer) {
        final String string = readString(length, charset);
        consumer.accept(string);
        return this;
    }

    default XtreamByteReader readString(int length, Consumer<String> consumer) {
        final String string = this.readString(length);
        consumer.accept(string);
        return this;
    }

}

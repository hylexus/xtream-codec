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

package io.github.hylexus.xtream.codec.core.type;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.impl.codec.ByteArrayContainerFieldCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author hylexus
 * @apiNote 注意 {@link #release()} 方法
 * @see #release()
 * @see ByteArrayContainerFieldCodec
 */
public interface ByteBufContainer extends BytesContainer {

    ByteBufAllocator FACTORY = ByteBufAllocator.DEFAULT;

    static ByteBufContainer ofBytes(ByteBuf byteBuf) {
        return () -> byteBuf;
    }

    static ByteBufContainer ofBytes(byte[] bytes) {
        return () -> FACTORY.buffer(bytes.length).writeBytes(bytes);
    }

    static ByteBufContainer ofI8(int value) {
        return () -> FACTORY.buffer(1).writeByte(value);
    }

    static ByteBufContainer ofU8(int value) {
        return ofI8(value);
    }

    static ByteBufContainer ofI16(int value) {
        return () -> FACTORY.buffer(2).writeShort(value);
    }

    static ByteBufContainer ofU16(int value) {
        return ofI16(value);
    }

    static ByteBufContainer ofI32(int value) {
        return () -> FACTORY.buffer(4).writeInt(value);
    }

    static ByteBufContainer ofU32(int value) {
        return ofI32(value);
    }

    static ByteBufContainer ofBcd(String bcd) {
        return () -> XtreamBytes.writeBcd(FACTORY.buffer(bcd.length()), bcd);
    }

    static ByteBufContainer ofString(String string, Charset charset) {
        return () -> FACTORY.buffer().writeBytes(string.getBytes(charset));
    }

    static ByteBufContainer ofString(String string) {
        return ofString(string, StandardCharsets.UTF_8);
    }

    static ByteBufContainer ofStringGbk(String string) {
        return ofString(string, XtreamConstants.CHARSET_GBK);
    }

    ByteBuf payload();

    @Override
    default void writeTo(ByteBuf output) {
        output.writeBytes(payload());
    }

    @Override
    default int length() {
        return this.payload().readableBytes();
    }

    @Override
    default byte[] asBytes() {
        return XtreamBytes.getBytes(this.payload());
    }

    @Override
    default String asString(Charset charset) {
        return XtreamBytes.getString(this.payload(), this.length(), charset);
    }

    @Override
    default byte asI8() {
        return this.payload().getByte(0);
    }

    @Override
    default short asI16() {
        return this.payload().getShort(0);
    }

    @Override
    default int asI32() {
        return this.payload().getInt(0);
    }

    @Override
    default void release() {
        XtreamBytes.releaseBuf(this.payload());
    }
}

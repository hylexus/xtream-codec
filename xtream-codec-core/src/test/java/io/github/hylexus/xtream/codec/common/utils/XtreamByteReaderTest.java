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
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class XtreamByteReaderTest {

    @Test
    void shouldReadUnsignedNumbersAndStringsAndReleaseBuffer() {
        final byte[] bytes = XtreamBytes.decodeHex("ffffff ffffffff fffffffffffffffe 1234 e4b8ad 4142".replace(" ", ""));
        final AtomicReference<ByteBuf> allocated = new AtomicReference<>();
        final List<Object> values = XtreamByteReader.doWithReader(bytes, (Function<XtreamByteReader, List<Object>>) reader -> {
            allocated.set(reader.readable());
            return List.of(reader.readU8(), reader.readU16(), reader.readU32(), reader.readI64(),
                    reader.readBcd(2), reader.readString(3), reader.readString(2, StandardCharsets.US_ASCII));
        });
        assertEquals(List.of((short) 255, 65535, 4294967295L, -2L, "1234", "中", "AB"), values);
        assertEquals(0, allocated.get().refCnt());
    }

    @Test
    void shouldChainConsumerReadsAndReleaseBuffer() {
        final byte[] bytes = XtreamBytes.decodeHex("ffffff ffffffff fffffffffffffffe 1234 e4b8ad 4142".replace(" ", ""));
        final List<Object> values = new ArrayList<>();
        final AtomicReference<ByteBuf> allocated = new AtomicReference<>();
        XtreamByteReader.doWithReader(bytes, (Consumer<XtreamByteReader>) reader -> {
            allocated.set(reader.readable());
            assertSame(reader, reader.readU8(values::add).readU16(values::add).readU32(values::add)
                    .readI64(values::add).readBcd(2, values::add).readString(3, values::add)
                    .readString(2, StandardCharsets.US_ASCII, values::add));
            assertEquals(0, reader.readable().readableBytes());
        });
        assertEquals(List.of((short) 255, 65535, 4294967295L, -2L, "1234", "中", "AB"), values);
        assertEquals(0, allocated.get().refCnt());
    }

    @Test
    void shouldReleaseBufferWhenFunctionThrows() {
        final AtomicReference<ByteBuf> allocated = new AtomicReference<>();
        final IllegalStateException failure = new IllegalStateException("读取失败");
        assertSame(failure, assertThrows(IllegalStateException.class, () ->
                XtreamByteReader.doWithReader(new byte[]{1}, (Function<XtreamByteReader, Object>) reader -> {
                    allocated.set(reader.readable());
                    throw failure;
                })));
        assertEquals(0, allocated.get().refCnt());
    }

    @Test
    void shouldReleaseBufferWhenConsumerThrows() {
        final AtomicReference<ByteBuf> allocated = new AtomicReference<>();
        assertThrows(IndexOutOfBoundsException.class, () ->
                XtreamByteReader.doWithReader(new byte[]{1}, (Consumer<XtreamByteReader>) reader -> {
                    allocated.set(reader.readable());
                    reader.readI64();
                }));
        assertEquals(0, allocated.get().refCnt());
    }
}

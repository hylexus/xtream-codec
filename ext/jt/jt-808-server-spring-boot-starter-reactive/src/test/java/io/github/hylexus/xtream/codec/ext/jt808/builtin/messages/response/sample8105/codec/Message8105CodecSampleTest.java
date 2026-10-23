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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.codec;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8105;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.*;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 命令偏移与流式读取边界回归测试。
 *
 * @author Codex (AI)
 */
class Message8105CodecSampleTest extends BaseCodecTest {
    @Test
    void testEmptyFieldsAndReaderIndex() {
        final String hex = "01" + "3b".repeat(13);
        final BuiltinMessage8105 decoded = decodeBody(BuiltinMessage8105.class, hex);
        assertEquals(1, decoded.getCommandWord());
        assertEquals(13, decoded.getCommandValue().size());
        for (int i = 0; i < 13; i++) {
            assertEquals(i, decoded.getCommandValue().get(i).offset());
            assertInstanceOf(EmptyCommandValue.class, decoded.getCommandValue().get(i));
            assertNull(decoded.getCommandValue().get(i).value());
        }
    }

    @Test
    void testBinaryWordContainingSeparator() {
        final String hex = "01" + "3b".repeat(12) + "003b3b";
        final BuiltinMessage8105 message = new BuiltinMessage8105().setCommandWord((short) 1)
                .setCommandValue(List.of(new WordCommandValue(12, 59)));
        assertEquals(hex, encodeBody(message));
        final BuiltinMessage8105 decoded = decodeBody(BuiltinMessage8105.class, hex);
        assertEquals(13, decoded.getCommandValue().size());
        assertEquals(59, decoded.getCommandValue().getLast().value());
    }

    @Test
    void testSortedOffsetsWithGbk() {
        final BuiltinMessage8105 message = new BuiltinMessage8105().setCommandWord((short) 2)
                .setCommandValue(List.of(new StringCommandValue(1, "中文"), new ByteCommandValue(0, (short) 1)));
        final String hex = "02013bd6d0cec43b" + "3b".repeat(11);
        assertEquals(hex, encodeBody(message));
        final BuiltinMessage8105 decoded = decodeBody(BuiltinMessage8105.class, hex);
        assertEquals((short) 1, decoded.getCommandValue().getFirst().value());
        assertEquals("中文", decoded.getCommandValue().get(1).value());
        assertEquals(13, decoded.getCommandValue().size());
    }

    @Test
    void testDirectDecodeConsumesOnlyDeclaredLength() {
        final ByteBuf buffer = allocator.buffer();
        try {
            buffer.writeByte(0x55);
            buffer.writeBytes(HexFormat.of().parseHex("3b".repeat(13)));
            buffer.writeByte(0x5a);
            buffer.readByte();
            final List<CommandValue<Object>> values = new Message8105CodecSample().deserialize(null, null, buffer, 13);
            assertEquals(13, values.size());
            assertEquals(1, buffer.readableBytes());
            assertEquals(0x5a, buffer.readUnsignedByte());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    @Test
    void testDuplicateOffsetIsRejected() {
        final ByteBuf buffer = allocator.buffer(0, 128);
        try {
            final List<CommandValue<Object>> values = List.of(new EmptyCommandValue<>(1), new EmptyCommandValue<>(1));
            assertThrows(IllegalArgumentException.class,
                    () -> new Message8105CodecSample().serialize(null, null, buffer, values));
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}


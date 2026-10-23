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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 固定消息体的编解码回归测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage30316364Test extends BaseCodecTest {
    private static final String HEX = "b1a8beaf2e62696e000000000000000000000000000000000000000000000000000000000000000000000000000000000000f000000100000004007d7eff";

    @Test
    void testEncode() {
        final BuiltinMessage30316364 entity = new BuiltinMessage30316364()
                .setFileName("报警.bin").setDataOffset(0xf0000001L).setDataLength(4)
                .setData(new byte[]{0, 0x7d, 0x7e, (byte) 0xff});
        assertEquals(HEX, encodeBody(entity));
    }

    @Test
    void testDecode() {
        final BuiltinMessage30316364 entity = decodeBody(BuiltinMessage30316364.class, HEX);
        assertEquals("报警.bin" + "\0".repeat(42), entity.getFileName());
        assertEquals("报警.bin", entity.getFileName().trim());
        assertEquals(0xf0000001L, entity.getDataOffset());
        assertEquals(4, entity.getDataLength());
        assertArrayEquals(new byte[]{0, 0x7d, 0x7e, (byte) 0xff}, entity.getData());
    }

    @Test
    void testDataLengthLeavesFollowingBytesUnread() {
        final ByteBuf buffer = allocator.buffer();
        try {
            buffer.writeBytes(HexFormat.of().parseHex(HEX));
            buffer.writeByte(0x5a);
            final BuiltinMessage30316364 entity = entityCodec.decode(BuiltinMessage30316364.class, buffer);
            assertEquals(4, entity.getData().length);
            assertEquals(1, buffer.readableBytes());
            assertEquals(0x5a, buffer.readUnsignedByte());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}


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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8103Sample4;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 共享参数编解码器的类型、回退与空项测试。
 *
 * @author Codex (AI)
 */
class CustomParameterListFieldCodecTest extends BaseCodecTest {
    @Test
    void testKnownParameterTypes() {
        final BuiltinMessage8103Sample4 message = new BuiltinMessage8103Sample4().setParameterCount((short) 5)
                .setParameterItemList(List.of(
                        new ParameterItem(1, (short) 4, 0xffffffffL),
                        new ParameterItem(0x40, (short) 4, "中文"),
                        new ParameterItem(0x81, (short) 2, 65535),
                        new ParameterItem(0x84, (short) 1, (short) 255),
                        new ParameterItem(0x32, (short) 4, "00117d7e")));
        final String hex = "050000000104ffffffff0000004004d6d0cec40000008102ffff0000008401ff000000320400117d7e";
        assertEquals(hex, encodeBody(message));
        final BuiltinMessage8103Sample4 decoded = decodeBody(BuiltinMessage8103Sample4.class, hex);
        assertEquals(5, decoded.getParameterCount());
        final List<ParameterItem> items = decoded.getParameterItemList();
        assertEquals(5, items.size());
        final long[] ids = {1, 0x40, 0x81, 0x84, 0x32};
        final short[] lengths = {4, 4, 2, 1, 4};
        final Object[] values = {0xffffffffL, "中文", 65535, (short) 255, "00117d7e"};
        final ParameterItem.ParameterType[] types = {ParameterItem.ParameterType.DWORD, ParameterItem.ParameterType.STRING_GBK,
                ParameterItem.ParameterType.WORD, ParameterItem.ParameterType.BYTE, ParameterItem.ParameterType.STRING_HEX};
        for (int i = 0; i < ids.length; i++) {
            assertEquals(ids[i], items.get(i).getParameterId());
            assertEquals(lengths[i], items.get(i).getParameterLength());
            assertEquals(values[i], items.get(i).getParameterValue());
            assertEquals(types[i], items.get(i).getParameterType());
        }
    }

    @Test
    void testUnknownParameterFallbackPreservesFollowingItem() {
        final BuiltinMessage8103Sample4 decoded = decodeBody(BuiltinMessage8103Sample4.class, "02f000000103007d7e0000000104ffffffff");
        assertEquals(2, decoded.getParameterCount());
        assertEquals(2, decoded.getParameterItemList().size());
        final ParameterItem first = decoded.getParameterItemList().getFirst();
        assertEquals(0xf0000001L, first.getParameterId());
        assertEquals(3, first.getParameterLength());
        assertEquals("[fallback:hex]007d7e", first.getParameterValue());
        assertEquals(ParameterItem.ParameterType.STRING_HEX, first.getParameterType());
        final ParameterItem last = decoded.getParameterItemList().getLast();
        assertEquals(1, last.getParameterId());
        assertEquals(4, last.getParameterLength());
        assertEquals(0xffffffffL, last.getParameterValue());
        assertEquals(ParameterItem.ParameterType.DWORD, last.getParameterType());
    }

    @Test
    void testNullItemIsSkipped() {
        final BuiltinMessage8103Sample4 message = new BuiltinMessage8103Sample4().setParameterCount((short) 1)
                .setParameterItemList(Arrays.asList(null, new ParameterItem(1, (short) 4, 111L)));
        assertEquals("0100000001040000006f", encodeBody(message));
    }

    @Test
    void testUnknownParameterCannotBeEncoded() {
        final ByteBuf buffer = allocator.buffer();
        try {
            assertThrows(IllegalArgumentException.class, () -> new CustomParameterListFieldCodec()
                    .serialize(null, null, buffer, List.of(new ParameterItem(0xf0000001L, (short) 1, "ff"))));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}


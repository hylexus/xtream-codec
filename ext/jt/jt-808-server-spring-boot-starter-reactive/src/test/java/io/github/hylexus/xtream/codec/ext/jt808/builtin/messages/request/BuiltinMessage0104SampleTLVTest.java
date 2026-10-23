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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0104SampleTLVTest extends BaseCodecTest {

    @Test
    void test() {
        final List<TLV> parameterItems = List.of(
                new TLV(u32(0x0001L), LengthFieldType.u8, u32(1L)), // 0
                new TLV(u32(0x0040L), LengthFieldType.u8, gbkString("13900001111")), // 1
                new TLV(u32(0x0040L), LengthFieldType.u8, gbkString("13900002222")), // 2
                new TLV(u32(0x0040L), LengthFieldType.u8, gbkString("13900003333")), // 3
                new TLV(u32(0x0081L), LengthFieldType.u8, u16(62)), // 4
                new TLV(u32(0x0082L), LengthFieldType.u8, u16(103)), // 5
                new TLV(u32(0x0084L), LengthFieldType.u8, u8((short) 1)) // 6
        );
        final BuiltinMessage0104SampleTLV instance = new BuiltinMessage0104SampleTLV()
                .setFlowId(123)
                .setParameterCount((short) parameterItems.size())
                .setParameterItems(parameterItems);

        final String expectedHex = "7e0104405001000000000139123443290000007b07000000010400000001000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e00000082020067000000840101b67e";
        assertEquals(expectedHex, encode(instance, Jt808ProtocolVersion.VERSION_2019, terminalId2019));

        final BuiltinMessage0104SampleTLV actual = decodeAsEntity(BuiltinMessage0104SampleTLV.class, expectedHex);
        assertEquals(123, actual.getFlowId());
        assertEquals(7, actual.getParameterCount());
        final List<TLV> actualParameterItems = actual.getParameterItems();
        assertEquals(parameterItems.size(), actualParameterItems.size());
        final List<Integer> expectedLengths = List.of(4, 11, 11, 11, 2, 2, 1);
        for (int i = 0; i < parameterItems.size(); i++) {
            final TLV expectedItem = parameterItems.get(i);
            final TLV actualItem = actualParameterItems.get(i);
            assertEquals(expectedItem.tag().value(), actualItem.tag().value());
            assertEquals(LengthFieldType.u8, actualItem.length().type());
            assertEquals(expectedLengths.get(i), actualItem.length().value());
            assertEquals(((DataField) expectedItem.value()).value(), actualItem.value());
        }
    }

    @Test
    void testUnknownParameter() {
        final BuiltinMessage0104SampleTLV instance = new BuiltinMessage0104SampleTLV()
                .setFlowId(123)
                .setParameterCount((short) 1)
                .setParameterItems(List.of(new TLV(u32(0xf0000001L), LengthFieldType.u8, hexString("01027e7d"))));
        final String expectedHex = "7e0104400c01000000000139123443290000007b01f00000010401027d027d01b37e";
        assertEquals(expectedHex, encode(instance, Jt808ProtocolVersion.VERSION_2019, terminalId2019));

        final BuiltinMessage0104SampleTLV actual = decodeAsEntity(BuiltinMessage0104SampleTLV.class, expectedHex);
        assertEquals(123, actual.getFlowId());
        assertEquals(1, actual.getParameterCount());
        assertEquals(1, actual.getParameterItems().size());
        final TLV item = actual.getParameterItems().getFirst();
        assertEquals(0xf0000001L, item.tag().value());
        assertEquals(LengthFieldType.u8, item.length().type());
        assertEquals(4, item.length().value());
        assertEquals("01027e7d", item.value());
    }
}

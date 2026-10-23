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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8103Sample1Test extends BaseCodecTest {

    @Test
    void testEncode() {

        final BuiltinMessage8103Sample1 entity = new BuiltinMessage8103Sample1();
        final List<BuiltinMessage8103Sample1.ParameterItem> parameterItemList = new ArrayList<>();
        // 终端心跳发送间隔,单位为秒
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0001, ByteArrayContainer.ofU32(111)));
        // 多个相同 ID 的配置项(监控平台电话号码)
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900001111")));
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900002222")));
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("13900003333")));
        // 车辆所在省域 ID
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0081, ByteArrayContainer.ofU16(62)));
        // 车辆所在市域 ID
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0082, ByteArrayContainer.ofU16(103)));
        // 车牌颜色
        parameterItemList.add(new BuiltinMessage8103Sample1.ParameterItem(0x0084, ByteArrayContainer.ofU8((short) 1)));

        entity.setParameterCount((short) parameterItemList.size());
        entity.setParameterItemList(parameterItemList);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x8103);
        assertEquals("7e8103404e010000000001391234432900000700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013a7e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage8103Sample1 entity = decodeAsEntity(BuiltinMessage8103Sample1.class,
                "8103404e010000000001391234432900000700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013a");
        assertEquals(7, entity.getParameterCount());
        assertEquals(7, entity.getParameterItemList().size());
        final long[] ids = {0x0001, 0x0040, 0x0040, 0x0040, 0x0081, 0x0082, 0x0084};
        final int[] lengths = {4, 11, 11, 11, 2, 2, 1};
        final String[] values = {"0000006f", "3133393030303031313131", "3133393030303032323232",
                "3133393030303033333333", "003e", "0067", "01"};
        for (int i = 0; i < ids.length; i++) {
            final BuiltinMessage8103Sample1.ParameterItem item = entity.getParameterItemList().get(i);
            assertEquals(ids[i], item.getParameterId());
            assertEquals(lengths[i], item.getParameterLength());
            assertEquals(lengths[i], item.getParameterValue().length());
            assertEquals(values[i], HexFormat.of().formatHex(item.getParameterValue().asBytes()));
        }
    }

    @Test
    void testGbkAndEscapedParameterValues() {
        final BuiltinMessage8103Sample1 entity = new BuiltinMessage8103Sample1().setParameterCount((short) 2)
                .setParameterItemList(List.of(
                        new BuiltinMessage8103Sample1.ParameterItem(0xf0000001L, ByteArrayContainer.ofBytes(new byte[]{0x7d, 0x7e})),
                        new BuiltinMessage8103Sample1.ParameterItem(0x0040, ByteArrayContainer.ofStringGbk("区域"))
                ));
        final String hex = "7e810340110100000000013912344329000002f0000001027d017d020000004004c7f8d3f20e7e";
        assertEquals(hex, encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019));
        final BuiltinMessage8103Sample1 decoded = decodeAsEntity(BuiltinMessage8103Sample1.class, hex);
        assertEquals(2, decoded.getParameterCount());
        assertEquals(2, decoded.getParameterItemList().size());
        final BuiltinMessage8103Sample1.ParameterItem first = decoded.getParameterItemList().getFirst();
        assertEquals(0xf0000001L, first.getParameterId());
        assertEquals(2, first.getParameterLength());
        assertArrayEquals(new byte[]{0x7d, 0x7e}, first.getParameterValue().asBytes());
        final BuiltinMessage8103Sample1.ParameterItem second = decoded.getParameterItemList().getLast();
        assertEquals(0x0040, second.getParameterId());
        assertEquals(4, second.getParameterLength());
        assertEquals("c7f8d3f2", HexFormat.of().formatHex(second.getParameterValue().asBytes()));
    }

}

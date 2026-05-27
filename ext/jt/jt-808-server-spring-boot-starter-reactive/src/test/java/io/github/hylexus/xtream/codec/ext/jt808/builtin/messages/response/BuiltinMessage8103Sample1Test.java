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
import java.util.List;

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

}

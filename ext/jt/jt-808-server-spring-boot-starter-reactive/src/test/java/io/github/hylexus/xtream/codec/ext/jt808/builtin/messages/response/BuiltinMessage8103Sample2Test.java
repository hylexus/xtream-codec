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

import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperGbk;
import io.github.hylexus.xtream.codec.core.type.wrapper.U16Wrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.U32Wrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.U8Wrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8103Sample2Test extends BaseCodecTest {

    @Test
    void testEncode() {

        final BuiltinMessage8103Sample2 entity = new BuiltinMessage8103Sample2();
        final List<BuiltinMessage8103Sample2.ParameterItem> parameterItemList = new ArrayList<>();
        // 终端心跳发送间隔,单位为秒
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0001, new U32Wrapper(111L)));
        // 多个相同 ID 的配置项(监控平台电话号码)
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0040, new StringWrapperGbk("13900001111")));
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0040, new StringWrapperGbk("13900002222")));
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0040, new StringWrapperGbk("13900003333")));
        // 车辆所在省域 ID
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0081, new U16Wrapper(62)));
        // 车辆所在市域 ID
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0082, new U16Wrapper(103)));
        // 车牌颜色
        parameterItemList.add(new BuiltinMessage8103Sample2.ParameterItem(0x0084, new U8Wrapper((short) 1)));

        entity.setParameterCount((short) parameterItemList.size());
        entity.setParameterItemList(parameterItemList);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x8103);
        assertEquals("7e8103404e010000000001391234432900000700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013a7e", hex);
    }

}

/*
 * Copyright 2024 the original author or authors.
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

import io.github.hylexus.xtream.codec.common.utils.XtreamConstants;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamFieldMapDescriptor;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperGbk;
import io.github.hylexus.xtream.codec.core.type.wrapper.U16Wrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.U32Wrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8103Sample3Test extends BaseCodecTest {

    @Test
    void testEncode() {

        final BuiltinMessage8103Sample3 entity = new BuiltinMessage8103Sample3();
        final List<BuiltinMessage8103Sample3.ParameterItem> parameterItemList = new ArrayList<>();
        // 终端心跳发送间隔,单位为秒
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0001, new U32Wrapper(111L)));
        // 多个相同 ID 的配置项(监控平台电话号码)
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0040, new StringWrapperGbk("13900001111")));
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0040, new StringWrapperGbk("13900002222")));
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0040, new StringWrapperGbk("13900003333")));
        // 车辆所在省域 ID
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0081, new U16Wrapper(62)));
        // 车辆所在市域 ID
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0082, (short) 2, new U16Wrapper(103)));
        // 车牌颜色
        parameterItemList.add(new BuiltinMessage8103Sample3.ParameterItem(0x0084, ByteArrayContainer.ofU8((short) 1)));

        entity.setParameterCount((short) parameterItemList.size());
        entity.setParameterItemList(parameterItemList);

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x8103);
        assertEquals("7e8103404e010000000001391234432900000700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013a7e", hex);
    }

    @Test
    void testDecode() {
        final Temp8103MessageForDebug entity = decodeAsEntity(Temp8103MessageForDebug.class, "8103404e010000000001391234432900000700000001040000006f000000400b3133393030303031313131000000400b3133393030303032323232000000400b31333930303030333333330000008102003e000000820200670000008401013a");
        assertEquals(7, entity.getParameterCount());

        final Map<Long, Object> extraItems = entity.getExtraItems();

        assertEquals(5, extraItems.size());

        assertEquals(111L, extraItems.get(0x0001L));

        // 由于这里是个 Map, 所以 相同 ID 只会保留最后一个
        assertEquals("13900003333", extraItems.get(0x0040L));
        assertEquals(62, extraItems.get(0x0081L));

        // 0x0082L示例 1：解析为 ByteArrayContainer
        // ByteArrayContainer item0082 = (ByteArrayContainer) extraItems.get(0x0082L);
        // assertEquals(103, item0082.asU16());

        // 0x0082L示例 2：解析为 DataWrapper
        // DataWrapper item0082 = (DataWrapper) extraItems.get(0x0082L);
        // assertEquals(103, item0082.asU16());

        // 0x0082L示例 3：解析为 U16Wrapper
        U16Wrapper item0082 = (U16Wrapper) extraItems.get(0x0082L);
        assertEquals(103, item0082.asU16());

        // 0x0082L示例 4：解析为 Integer
        // assertEquals(103, extraItems.get(0x0082L));

        // 0x0084L 解析为 Short
        assertEquals((short) 1, extraItems.get(0x0084L));
    }

    @Getter
    @Setter
    @ToString
    public static class Temp8103MessageForDebug {

        @Preset.JtStyle.Byte
        private short parameterCount;

        @Preset.JtStyle.Map
        @XtreamFieldMapDescriptor(
                keyDescriptor = @XtreamFieldMapDescriptor.KeyDescriptor(type = XtreamFieldMapDescriptor.KeyType.u32),
                valueLengthFieldDescriptor = @XtreamFieldMapDescriptor.ValueLengthFieldDescriptor(length = 1),
                valueDecoderDescriptors = @XtreamFieldMapDescriptor.ValueDecoderDescriptors(
                        defaultValueDecoderDescriptor = @XtreamFieldMapDescriptor.ValueDecoderDescriptor(javaType = byte[].class),
                        valueDecoderDescriptors = {
                                @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0001, config = @XtreamField(length = 4, signedness = NumberSignedness.UNSIGNED), javaType = Long.class, desc = "终端心跳发送间隔,单位为秒"),
                                // 由于这里是个 Map, 所以多个相同 ID 的配置项会被覆盖(只剩下最后一个)
                                @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0040, javaType = String.class, config = @XtreamField(charset = XtreamConstants.CHARSET_NAME_GBK), desc = "监控平台电话号码"),
                                @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0081, config = @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED), javaType = Integer.class, desc = "车辆所在省域 ID"),
                                // 和 0x0081 一样，0x0082 可以解析为 Integer、ByteArrayContainer、ByteBufContainer、U16Wrapper、DataWrapper 等类型
                                // @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0082, config = @XtreamField, javaType = ByteArrayContainer.class, desc = "车辆所在市域 ID"),
                                // @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0082, javaType = DataWrapper.class, desc = "车辆所在市域 ID"),
                                @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0082, javaType = U16Wrapper.class, desc = "车辆所在市域 ID"),
                                // @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0082, config = @XtreamField(length = 2), javaType = Integer.class, desc = "车辆所在市域 ID"),
                                @XtreamFieldMapDescriptor.ValueCodecConfig(whenKeyIsU32 = 0x0084, config = @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED), javaType = Short.class, desc = "车牌颜色"),
                        }
                )
        )
        private Map<Long, Object> extraItems;
    }
}

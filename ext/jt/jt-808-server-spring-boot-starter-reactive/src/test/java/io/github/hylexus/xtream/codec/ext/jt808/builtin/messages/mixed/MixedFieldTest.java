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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.mixed;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriber;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.type.simple.DataField.U8;
import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixedFieldTest extends BaseCodecTest {

    @Test
    void test2() {
        final LocalDateTime now = LocalDateTime.of(2025, 11, 11, 22, 20, 58);
        final MixedEntity01 mixedEntity01 = new MixedEntity01()
                .setAlarmFlag(1L)
                .setStatus(2L)
                .setLatitude(3L)
                .setLongitude(4L)
                .setAltitude(5)
                .setSpeed(6)
                .setDirection(u16(7))
                .setTime(now);
        final String hex = encode(mixedEntity01, Jt808ProtocolVersion.VERSION_2013, terminalId2013, 0x0200);
        assertEquals("7e0200001c0139123443230000000000010000000200000003000000040005000600072511112220581f7e", hex);
    }

    @Test
    void test() {
        final Map<U8, DataField> extraItems = getExtraItems();
        List<DataField> dataFieldList = List.of(
                // 报警标志
                u32("报警标志", 11L),
                // 状态
                u32(21L),
                // 纬度
                u32(31L),
                // 经度
                u32(41L),
                // 高度
                u16(51),
                // 速度
                u16(61),
                // 方向
                u16(71),
                // 时间
                bcd8421String("251108223456"),
                // 位置附加项
                dict(U8.class, LengthFieldType.u8, extraItems)
        );

        final ByteBuf encode = responseEncoder.encode(dataFieldList, new Jt808MessageDescriber(0x0200, Jt808ProtocolVersion.VERSION_2013, terminalId2013));
        try {
            assertEquals("7e0200005501391234432300000000000b000000150000001f000000290033003d004725110822345601040000006f020200de0302014d0402115c050201020602029a1105010000029a120601000000de032a0200de2b04000000ea3001ff3101ff697e",
                    FormatUtils.toHexString(encode));
        } finally {
            encode.release();
            assertEquals(0, encode.refCnt());
        }

        final ByteBuf buffer = this.allocator.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            this.entityCodec.entityEncoder().encodeWithTracker(dataFieldList, buffer, tracker);
            assertEquals("0000000b000000150000001f000000290033003d004725110822345601040000006f020200de0302014d0402115c050201020602029a1105010000029a120601000000de032a0200de2b04000000ea3001ff3101ff",
                    FormatUtils.toHexString(buffer));
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    private static Map<U8, DataField> getExtraItems() {
        final Map<U8, DataField> extraItems = new LinkedHashMap<>();
        // 里程，DWORD，1/10km，对应车上里程表读数
        extraItems.put(u8("里程", (short) 0x01), dword(111L));
        // 油量，WORD，1/10L，对应车上油量表读数
        extraItems.put(u8((short) 0x02), word(222));
        // 行驶记录功能获取的速度，WORD，1/10km/h
        extraItems.put(u8((short) 0x03), u16(333));
        // 需要人工确认报警事件的 ID，WORD，从 1 始计数
        extraItems.put(u8((short) 0x04), u16(4444));
        // 胎压
        extraItems.put(u8((short) 0x05), byteSequence(new byte[]{1, 2}));
        // 车厢温度
        extraItems.put(u8((short) 0x06), i16((short) 666));
        // 长度1或5；超速报警附加信息见 表 28
        extraItems.put(
                u8((short) 0x11),
                struct(
                        u8((short) 1),
                        // todo #locationType != 0
                        u32(666L)
                )
        );
        // 扩展车辆信号状态位，定义见 表 31
        extraItems.put(u8((short) 0x12), struct(u8((short) 1), u32(222L), u8((short) 3)));
        // IO 状态位，表 32
        extraItems.put(u8((short) 0x2A), u16(222));
        // 模拟量，bit0-15,AD0,bit16-31,AD1
        extraItems.put(u8((short) 0x2B), i32(234));
        // 数据类型为 BYTE，无线通信网络信号强度
        extraItems.put(u8((short) 0x30), u8((short) 255));
        // 数据类型为 BYTE，GNSS定位卫星数
        extraItems.put(u8((short) 0x31), u8((short) 255));
        return extraItems;
    }
}

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MixedEntity02Test extends BaseCodecTest {

    private final LocalDateTime time = LocalDateTime.of(2025, 11, 15, 15, 43, 19);

    @Test
    void test() {
        final List<Object> dataList = new ArrayList<>();
        dataList.add(u32("报警标志", 111L));
        dataList.add(u32("状态", 222L));
        final Map<DataField.U8, DataField> values = new LinkedHashMap<>();
        // 里程，DWORD，1/10km，对应车上里程表读数
        values.put(u8((short) 0x01), u32(123L));
        // 油量，WORD，1/10L，对应车上油量表读数
        values.put(u8((short) 0x02), u16(88));
        values.put(u8((short) 0xe1), gbkString("测试"));
        final DataField.Dict<DataField.U8> extraItems = dict(
                DataField.U8.class,
                LengthFieldType.u8,
                values
        );
        final MixedEntity02 mixedEntity02 = new MixedEntity02()
                .setLatitude(31)
                .setLongitude(121)
                .setAltitude(1000)
                .setSpeed(80)
                .setDirection(u16(1))
                .setTime(time)
                .setExtraItems(extraItems)
                .setExtraItems2(tlv(u8((short) 0x01), LengthFieldType.u8, u32(123L)));
        dataList.add(mixedEntity02);

        final ByteBuf encodeBuffer = responseEncoder.encode(dataList, new Jt808MessageDescriber(0x0200, Jt808ProtocolVersion.VERSION_2013, terminalId2013));
        try {
            assertEquals("7e0200003201391234432300000000006f000000de0000001f0000007903e80050000125111515431901040000007b02020058e104b2e2cad401040000007bbe7e",
                    FormatUtils.toHexString(encodeBuffer));
        } finally {
            encodeBuffer.release();
            assertEquals(0, encodeBuffer.refCnt());
        }

        final ByteBuf buffer = allocator.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            entityCodec.encode(dataList, buffer, tracker);
            assertEquals("0000006f000000de0000001f0000007903e80050000125111515431901040000007b02020058e104b2e2cad401040000007b",
                    FormatUtils.toHexString(buffer));
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }
}

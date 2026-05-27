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

import io.github.hylexus.xtream.codec.common.utils.XtreamBitOperator;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0705Test extends BaseCodecTest {

    final LocalTime time1 = LocalTime.of(20, 23, 45, 112299999);

    @Test
    void testEncode() {
        final BuiltinMessage0705 entity = new BuiltinMessage0705();
        entity.setCount(2);
        entity.setReceiveTime(time1);
        entity.setItemList(List.of(
                        new BuiltinMessage0705.Item()
                                .setCanId(XtreamBitOperator.mutable().set(31).longValue())
                                .setCanData(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}),
                        new BuiltinMessage0705.Item()
                                .setCanId(XtreamBitOperator.mutable().set(29).longValue())
                                .setCanData(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x08})
                )
        );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e0705401f0100000000013912344329000000022023451122800000000102030405060708200000001122334455667708ff7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0705401f0100000000013912344329000000022023451122800000000102030405060708200000001122334455667708ff";
        final BuiltinMessage0705 entity = decodeAsEntity(BuiltinMessage0705.class, hex);
        assertEquals(2, entity.getCount());
        assertEquals("2023451122", DateTimeFormatter.ofPattern("HHmmssSSSS").format(entity.getReceiveTime()));

        final BuiltinMessage0705.Item first = entity.getItemList().getFirst();
        assertEquals(XtreamBitOperator.mutable().set(31).longValue(), first.getCanId());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, first.getCanData());

        final BuiltinMessage0705.Item second = entity.getItemList().getLast();
        assertEquals(XtreamBitOperator.mutable().set(29).longValue(), second.getCanId());
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x08}, second.getCanData());
    }
}

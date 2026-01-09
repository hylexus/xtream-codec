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

        codec(
                Jt808ProtocolVersion.VERSION_2019, terminalId2019,
                instance,
                (expected, actual, hexString) -> {
                    assertEquals(expected.getFlowId(), actual.getFlowId());
                    assertEquals(expected.getParameterCount(), actual.getParameterCount());
                    final List<TLV> expectedParameterItems = expected.getParameterItems();
                    final List<TLV> actualParameterItems = actual.getParameterItems();
                    assertEquals(expectedParameterItems.size(), actualParameterItems.size());
                    assertEquals(((DataField.U32) (expectedParameterItems.get(0).value())).value(), actualParameterItems.get(0).value());
                    assertEquals(((DataField.GbkString) (expectedParameterItems.get(1).value())).value(), actualParameterItems.get(1).value());
                    assertEquals(((DataField.GbkString) (expectedParameterItems.get(2).value())).value(), actualParameterItems.get(2).value());
                    assertEquals(((DataField.GbkString) (expectedParameterItems.get(3).value())).value(), actualParameterItems.get(3).value());
                    assertEquals(((DataField.U16) (expectedParameterItems.get(4).value())).value(), actualParameterItems.get(4).value());
                    assertEquals(((DataField.U16) (expectedParameterItems.get(5).value())).value(), actualParameterItems.get(5).value());
                    assertEquals(((DataField.U8) (expectedParameterItems.get(6).value())).value(), actualParameterItems.get(6).value());
                },
                true
        );
    }
}

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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.mixed;

import io.github.hylexus.xtream.codec.core.type.FieldLength;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.wrapper.U8Wrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0200;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;

class MixedEntity03Test extends BaseCodecTest {
    private final LocalDateTime time = LocalDateTime.of(2025, 11, 15, 15, 43, 19);

    @Test
    void test() {
        final MixedEntity03 sourceEntity = new MixedEntity03()
                .setAlarmFlag(1)
                .setStatus(2)
                .setLatitude(31)
                .setLongitude(121)
                .setAltitude(1000)
                .setSpeed(80)
                .setDirection(u16(1))
                .setTime(time)
                .setExtraItems2(u8((short) 0x01))
                .setExtraItems3(new U8Wrapper((short) 0x01))
                .setExtraItems1(TLV.of(u8((short) 0x01), FieldLength.LengthType.u8, u32(0x01L)))
                .setExtraItems(List.of(
                        TLV.of(u8((short) 0x01), FieldLength.LengthType.u8, u32(11L)),
                        TLV.of(u8((short) 0x02), FieldLength.LengthType.u8, u16(22)),
                        TLV.of(u8((short) 0x03), FieldLength.LengthType.u8, u16(33)),
                        TLV.of(u8((short) 0x04), FieldLength.LengthType.u8, u16(44)),
                        TLV.of(u8((short) 0x05), FieldLength.LengthType.u8, byteSequence(new byte[]{1, 2, 3, 4, 5})),
                        TLV.of(u8((short) 0x06), FieldLength.LengthType.u8, i16((short) 66)),
                        TLV.of(u8((short) 0x11), FieldLength.LengthType.u8, new BuiltinMessage0200.Item0x11((short) 11, 22L)),
                        TLV.of(u8((short) 0x12), FieldLength.LengthType.u8, new BuiltinMessage0200.Item0x12((short) 11, 22L, (short) 33)),
                        TLV.of(u8((short) 0x13), FieldLength.LengthType.u8, new BuiltinMessage0200.Item0x13(11L, 22, (short) 33)),
                        TLV.of(u8((short) 0x25), FieldLength.LengthType.u8, u32(0x25L)),
                        TLV.of(u8((short) 0x2A), FieldLength.LengthType.u8, u16(0x2A)),
                        TLV.of(u8((short) 0x2B), FieldLength.LengthType.u8, i32(0x2B)),
                        TLV.of(u8((short) 0x30), FieldLength.LengthType.u8, u8((short) 30)),
                        TLV.of(u8((short) 0x31), FieldLength.LengthType.u8, u8((short) 31))
                ));
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        System.out.println(hex);
    }
}

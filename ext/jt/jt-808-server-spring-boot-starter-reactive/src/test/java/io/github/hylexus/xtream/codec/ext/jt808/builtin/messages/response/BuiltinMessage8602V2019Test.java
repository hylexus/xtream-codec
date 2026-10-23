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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 2019 版消息的固定报文与条件字段回归测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage8602V2019Test extends BaseCodecTest {

    private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    private static final LocalDateTime END_TIME = START_TIME.plusHours(2);

    private static Stream<Arguments> packets() {
        return Stream.of(
                Arguments.of(0, "7e8602403b010000000001391234432900000102f0000001000001d907f2073d336c01d907f3073d336d0006c7f8d3f2bcd7000000de000001d907f4073d336e01d907f5073d336f000342c7f8ab7e"),
                Arguments.of(1, "7e86024047010000000001391234432900000102f0000001000101d907f2073d336c01d907f3073d336d2410261839562410262039560006c7f8d3f2bcd7000000de000001d907f4073d336e01d907f5073d336f000342c7f8ee7e"),
                Arguments.of(2, "7e86024040010000000001391234432900000102f0000001000201d907f2073d336c01d907f3073d336d006f0a00500006c7f8d3f2bcd7000000de000001d907f4073d336e01d907f5073d336f000342c7f8e77e"),
                Arguments.of(3, "7e8602404c010000000001391234432900000102f0000001000301d907f2073d336c01d907f3073d336d241026183956241026203956006f0a00500006c7f8d3f2bcd7000000de000001d907f4073d336e01d907f5073d336f000342c7f8d27e")
        );
    }

    @ParameterizedTest
    @MethodSource("packets")
    void testEncode(int props, String hex) {
        assertEquals(hex, encode(createMessage(props), Jt808ProtocolVersion.VERSION_2019, terminalId2019));
    }

    @ParameterizedTest
    @MethodSource("packets")
    void testDecode(int props, String hex) {
        final BuiltinMessage8602V2019 entity = decodeAsEntity(BuiltinMessage8602V2019.class, hex);
        assertEquals(1, entity.getType());
        assertEquals(2, entity.getAreaCount());
        assertEquals(2, entity.getAreaList().size());
        for (int i = 0; i < 2; i++) {
            final BuiltinMessage8602V2019.RectangularArea area = entity.getAreaList().get(i);
            final int areaProps = i == 0 ? props : 0;
            assertEquals(i == 0 ? 0xf0000001L : 222L, area.getAreaId());
            assertEquals(areaProps, area.getAreaProps());
            assertEquals(31000562L + 2 * i, area.getLeftTopLatitude());
            assertEquals(121451372L + 2 * i, area.getLeftTopLongitude());
            assertEquals(31000563L + 2 * i, area.getRightBottomLatitude());
            assertEquals(121451373L + 2 * i, area.getRightBottomLongitude());
            assertEquals((areaProps & 1) != 0 ? START_TIME : null, area.getStartTime());
            assertEquals((areaProps & 1) != 0 ? END_TIME : null, area.getEndTime());
            assertEquals((areaProps & 2) != 0 ? 111 : 0, area.getTopSpeed());
            assertEquals((areaProps & 2) != 0 ? 10 : 0, area.getDurationOfOverSpeed());
            assertEquals((areaProps & 2) != 0 ? 80 : 0, area.getTopSpeedAtNight());
            assertEquals(i == 0 ? 6 : 3, area.getAreaNameLength());
            assertEquals(i == 0 ? "区域甲" : "B区", area.getAreaName());
        }
    }

    @ParameterizedTest
    @MethodSource("packets")
    void testDecodeLeavesFollowingDataUnread(int props, String hex) {
        final Jt808Request request = decodeAsRequest(hex.substring(2, hex.length() - 2));
        final ByteBuf input = allocator.buffer();
        try {
            // 消息可嵌入 0x0608 应答，解码时必须保留后续数据。
            input.writeBytes(request.payload(), request.payload().readerIndex(), request.payload().readableBytes());
            input.writeShort(0xa55a);
            final BuiltinMessage8602V2019 decoded = entityCodec.decode(
                    Jt808ProtocolVersion.VERSION_2019.versionValue(), BuiltinMessage8602V2019.class, input);
            assertEquals(props, decoded.getAreaList().getFirst().getAreaProps());
            assertEquals(2, input.readableBytes());
            assertEquals(0xa55a, input.readUnsignedShort());
        } finally {
            input.release();
            request.release();
            assertEquals(0, input.refCnt());
            assertEquals(0, request.payload().refCnt());
        }
    }

    private BuiltinMessage8602V2019 createMessage(int props) {
        final List<BuiltinMessage8602V2019.RectangularArea> areas = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            areas.add(new BuiltinMessage8602V2019.RectangularArea()
                    .setAreaId(i == 0 ? 0xf0000001L : 222L)
                    .setAreaProps(i == 0 ? props : 0)
                    .setLeftTopLatitude(31000562L + 2 * i)
                    .setLeftTopLongitude(121451372L + 2 * i)
                    .setRightBottomLatitude(31000563L + 2 * i)
                    .setRightBottomLongitude(121451373L + 2 * i)
                    .setStartTime(START_TIME)
                    .setEndTime(END_TIME)
                    .setTopSpeed(111)
                    .setDurationOfOverSpeed((short) 10)
                    .setTopSpeedAtNight(80)
                    .setAreaNameLength(i == 0 ? 6 : 3)
                    .setAreaName(i == 0 ? "区域甲" : "B区"));
        }
        return new BuiltinMessage8602V2019().setType((short) 1).setAreaCount((short) 2).setAreaList(areas);
    }
}


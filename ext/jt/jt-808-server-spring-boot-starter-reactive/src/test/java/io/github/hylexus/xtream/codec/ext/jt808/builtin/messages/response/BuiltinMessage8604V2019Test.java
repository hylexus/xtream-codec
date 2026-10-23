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
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 2019 版消息的固定报文与条件字段回归测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage8604V2019Test extends BaseCodecTest {

    private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    private static final LocalDateTime END_TIME = START_TIME.plusHours(2);

    private static Stream<Arguments> packets() {
        return Stream.of(
                Arguments.of(0, "7e8604402001000000000139123443290000f00000010000000201d907f2073d336c01d907f3073d336d0006c7f8d3f2bcd7177e"),
                Arguments.of(1, "7e8604402c01000000000139123443290000f00000010001241026183956241026203956000201d907f2073d336c01d907f3073d336d0006c7f8d3f2bcd7227e"),
                Arguments.of(2, "7e8604402501000000000139123443290000f00000010002006f0a000201d907f2073d336c01d907f3073d336d00500006c7f8d3f2bcd7257e"),
                Arguments.of(3, "7e8604403101000000000139123443290000f00000010003241026183956241026203956006f0a000201d907f2073d336c01d907f3073d336d00500006c7f8d3f2bcd7087e")
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
        final BuiltinMessage8604V2019 entity = decodeAsEntity(BuiltinMessage8604V2019.class, hex);
        assertEquals(0xf0000001L, entity.getAreaId());
        assertEquals(props, entity.getAreaProps());
        assertEquals((props & 1) != 0 ? START_TIME : null, entity.getStartTime());
        assertEquals((props & 1) != 0 ? END_TIME : null, entity.getEndTime());
        assertEquals((props & 2) != 0 ? 111 : 0, entity.getTopSpeed());
        assertEquals((props & 2) != 0 ? 10 : 0, entity.getDurationOfOverSpeed());
        assertEquals((props & 2) != 0 ? 80 : 0, entity.getTopSpeedAtNight());
        assertEquals(2, entity.getPointCount());
        assertEquals(2, entity.getPointList().size());
        for (int i = 0; i < 2; i++) {
            assertEquals(31000562L + i, entity.getPointList().get(i).getLatitude());
            assertEquals(121451372L + i, entity.getPointList().get(i).getLongitude());
        }
        assertEquals(6, entity.getAreaNameLength());
        assertEquals("区域甲", entity.getAreaName());
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
            final BuiltinMessage8604V2019 decoded = entityCodec.decode(
                    Jt808ProtocolVersion.VERSION_2019.versionValue(), BuiltinMessage8604V2019.class, input);
            assertEquals(props, decoded.getAreaProps());
            assertEquals(2, input.readableBytes());
            assertEquals(0xa55a, input.readUnsignedShort());
        } finally {
            input.release();
            request.release();
            assertEquals(0, input.refCnt());
            assertEquals(0, request.payload().refCnt());
        }
    }

    private BuiltinMessage8604V2019 createMessage(int props) {
        return new BuiltinMessage8604V2019()
                .setAreaId(0xf0000001L)
                .setAreaProps(props)
                .setStartTime(START_TIME)
                .setEndTime(END_TIME)
                .setTopSpeed(111)
                .setDurationOfOverSpeed((short) 10)
                .setPointCount(2)
                .setPointList(List.of(
                        new BuiltinMessage8604V2019.Point(31000562L, 121451372L),
                        new BuiltinMessage8604V2019.Point(31000563L, 121451373L)
                ))
                .setTopSpeedAtNight(80)
                .setAreaNameLength(6)
                .setAreaName("区域甲");
    }
}


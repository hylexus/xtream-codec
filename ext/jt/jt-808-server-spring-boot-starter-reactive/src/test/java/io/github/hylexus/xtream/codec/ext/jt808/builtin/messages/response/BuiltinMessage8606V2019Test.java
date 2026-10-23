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
class BuiltinMessage8606V2019Test extends BaseCodecTest {

    private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 10, 26, 18, 39, 56);
    private static final LocalDateTime END_TIME = START_TIME.plusHours(2);

    private static Stream<Arguments> packets() {
        return Stream.of(
                Arguments.of(0, "7e86064040010000000001391234432900000000029a000000020000006f0000045701d907f2073d336c14000006c2b7cfdfbcd7000000700000045801d907f3073d336d1403270f0457006f0a000342c2b75e7e"),
                Arguments.of(1, "7e8606404c010000000001391234432900000000029a000124102618395624102620395600020000006f0000045701d907f2073d336c1401270f04570006c2b7cfdfbcd7000000700000045801d907f3073d336d1402006f0a000342c2b76b7e"),
                Arguments.of(2, "7e86064040010000000001391234432900000000029a000200020000006f0000045701d907f2073d336c1402006f0a0006c2b7cfdfbcd7000000700000045801d907f3073d336d1401270f0457000342c2b75c7e"),
                Arguments.of(3, "7e8606404c010000000001391234432900000000029a000324102618395624102620395600020000006f0000045701d907f2073d336c1403270f0457006f0a0006c2b7cfdfbcd7000000700000045801d907f3073d336d1400000342c2b7697e")
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
        final BuiltinMessage8606V2019 entity = decodeAsEntity(BuiltinMessage8606V2019.class, hex);
        assertEquals(666, entity.getRouteId());
        assertEquals(props, entity.getRouteProps());
        assertEquals((props & 1) != 0 ? START_TIME : null, entity.getStartTime());
        assertEquals((props & 1) != 0 ? END_TIME : null, entity.getEndTime());
        assertEquals(2, entity.getCount());
        assertEquals(2, entity.getItemList().size());
        for (int i = 0; i < 2; i++) {
            final BuiltinMessage8606V2019.Item item = entity.getItemList().get(i);
            final int itemProps = i == 0 ? props : 3 - props;
            assertEquals(111L + i, item.getId());
            assertEquals(1111L + i, item.getRouteId());
            assertEquals(31000562L + i, item.getLatitude());
            assertEquals(121451372L + i, item.getLongitude());
            assertEquals(20, item.getRouteWidth());
            assertEquals(itemProps, item.getRouteProps());
            assertEquals((itemProps & 1) != 0 ? 9999 : null, item.getLongDriveThreshold());
            assertEquals((itemProps & 1) != 0 ? 1111 : null, item.getShortDriveThreshold());
            assertEquals((itemProps & 2) != 0 ? 111 : null, item.getMaxSpeedLimit());
            assertEquals((itemProps & 2) != 0 ? (short) 10 : null, item.getSpeedingDuration());
            assertEquals(i == 0 ? 6 : 3, item.getAreaNameLength());
            assertEquals(i == 0 ? "路线甲" : "B路", item.getAreaName());
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
            final BuiltinMessage8606V2019 decoded = entityCodec.decode(
                    Jt808ProtocolVersion.VERSION_2019.versionValue(), BuiltinMessage8606V2019.class, input);
            assertEquals(props, decoded.getRouteProps());
            assertEquals(2, input.readableBytes());
            assertEquals(0xa55a, input.readUnsignedShort());
        } finally {
            input.release();
            request.release();
            assertEquals(0, input.refCnt());
            assertEquals(0, request.payload().refCnt());
        }
    }

    private BuiltinMessage8606V2019 createMessage(int props) {
        final List<BuiltinMessage8606V2019.Item> items = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            items.add(new BuiltinMessage8606V2019.Item()
                    .setId(111L + i)
                    .setRouteId(1111L + i)
                    .setLatitude(31000562L + i)
                    .setLongitude(121451372L + i)
                    .setRouteWidth((short) 20)
                    .setRouteProps((short) (i == 0 ? props : 3 - props))
                    .setLongDriveThreshold(9999)
                    .setShortDriveThreshold(1111)
                    .setMaxSpeedLimit(111)
                    .setSpeedingDuration((short) 10)
                    .setAreaNameLength(i == 0 ? 6 : 3)
                    .setAreaName(i == 0 ? "路线甲" : "B路"));
        }
        return new BuiltinMessage8606V2019().setRouteId(666).setRouteProps(props)
                .setStartTime(START_TIME).setEndTime(END_TIME).setCount(2).setItemList(items);
    }
}


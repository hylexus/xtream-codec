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

package io.github.hylexus.xtream.debug.codec.core.demo02;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.debug.codec.core.BaseEntityCodecTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RawStyleDebugEntity02FlattenTest extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final RawStyleDebugEntity02Flatten originalEntity = this.createEntity();
        final String hexString = this.encodeAsHexString(originalEntity);
        final RawStyleDebugEntity02Flatten entity = this.doDecode(RawStyleDebugEntity02Flatten.class, hexString);

        assertEquals(originalEntity.getMsgId(), entity.getMsgId());
        assertEquals(originalEntity.getMsgBodyProps(), entity.getMsgBodyProps());
        assertEquals(originalEntity.getProtocolVersion(), entity.getProtocolVersion());

        assertEquals(originalEntity.getTerminalId(), entity.getTerminalId());
        assertEquals(originalEntity.getMsgSerialNo(), entity.getMsgSerialNo());
        assertEquals(originalEntity.getSubPackageInfo(), entity.getSubPackageInfo());
        assertEquals(originalEntity.getAlarmFlag(), entity.getAlarmFlag());
        assertEquals(originalEntity.getLatitude(), entity.getLatitude());
        assertEquals(originalEntity.getLongitude(), entity.getLongitude());
        assertEquals(originalEntity.getAltitude(), entity.getAltitude());
        assertEquals(originalEntity.getSpeed(), entity.getSpeed());
        assertEquals(originalEntity.getDirection(), entity.getDirection());
        assertEquals(originalEntity.getTime(), entity.getTime());
        assertEquals(originalEntity.getCheckSum(), entity.getCheckSum());

        assertEquals(originalEntity.getExtraItems().size(), entity.getExtraItems().size());
        for (int i = 0; i < originalEntity.getExtraItems().size(); i++) {
            final RawStyleDebugEntity02Flatten.ExtraItem original = originalEntity.getExtraItems().get(i);
            final RawStyleDebugEntity02Flatten.ExtraItem decoded = entity.getExtraItems().get(i);
            assertEquals(original.getId(), decoded.getId());
            assertEquals(original.getContentLength(), decoded.getContentLength());
            assertArrayEquals(original.getContent(), decoded.getContent());
        }
    }

    RawStyleDebugEntity02Flatten createEntity() {
        final RawStyleDebugEntity02Flatten entity = new RawStyleDebugEntity02Flatten();
        this.initBasicProperties(entity);
        entity.setExtraItems(List.of(
                new RawStyleDebugEntity02Flatten.ExtraItem((short) 0x01, (short) 4, ByteArrayContainer.ofDword(6666).asBytes()),
                new RawStyleDebugEntity02Flatten.ExtraItem((short) 0x02, (short) 2, ByteArrayContainer.ofI16(88).asBytes()),
                new RawStyleDebugEntity02Flatten.ExtraItem((short) 0x03, (short) 2, ByteArrayContainer.ofI16(89).asBytes())
        ));
        this.initMsgBodyProps(entity, entity.getExtraItems());
        return entity;
    }

    private void initMsgBodyProps(RawStyleDebugEntity02Flatten entity, List<RawStyleDebugEntity02Flatten.ExtraItem> extraItemList) {

        // 消息体长度
        int msgBodyLength = 0;
        // 报警标志  DWORD(4)
        msgBodyLength += 4;
        // 状态  DWORD(4)
        msgBodyLength += 4;
        // 纬度  DWORD(4)
        msgBodyLength += 4;
        // 经度  DWORD(4)
        msgBodyLength += 4;
        // 高程  WORD(2)
        msgBodyLength += 2;
        // 速度  WORD(2)
        msgBodyLength += 2;
        // 方向  WORD(2)
        msgBodyLength += 2;
        // 时间  BCD[6] yyMMddHHmmss
        msgBodyLength += 6;

        // 附加项长度
        // +1: 附加信息ID 的长度
        // +1: 附加信息长度 的长度
        // +it.getContent().length: 附加消息内容 的长度
        msgBodyLength += (extraItemList.stream().mapToInt(it -> 1 + 1 + it.getContent().length).sum());

        final int props = generateMsgBodyPropsForJt808(msgBodyLength, 0, false, 0);
        entity.setMsgBodyProps(props);
    }

    private void initBasicProperties(RawStyleDebugEntity02Flatten entity) {
        entity.setMsgId(0x0200);
        entity.setProtocolVersion((byte) 1);
        entity.setTerminalId("00000000013911112222");
        entity.setMsgSerialNo(99);
        entity.setSubPackageInfo(null);

        entity.setAlarmFlag(88);

        entity.setStatus(111);

        entity.setLatitude(31234567);

        entity.setLongitude(121987654);
        entity.setSpeed(666);
        entity.setAltitude(8989);
        entity.setDirection(90);
        entity.setTime("240322222633");

        entity.setCheckSum((byte) 91);

    }

    static int generateMsgBodyPropsForJt808(int msgBodySize, int encryptionType, boolean isSubPackage, int reversedBit15) {
        // [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
        int props = (msgBodySize & 0x3FF)
                    // [10-12] 0001,1100,0000,0000(1C00)(加密类型)
                    | ((encryptionType << 10) & 0x1C00)
                    // [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
                    | (((isSubPackage ? 1 : 0) << 13) & 0x2000)
                    // [14_ ]  0100,0000,0000,0000(4000)(保留位)
                    // 1: V2019 固定为 1
                    | ((1 << 14) & 0x4000)
                    // [15_ ]  1000,0000,0000,0000(8000)(保留位)
                    | ((reversedBit15 << 15) & 0x8000);
        return props & 0xFFFF;
    }
}

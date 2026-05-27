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

import static io.github.hylexus.xtream.debug.codec.core.demo02.JtStyleDebugEntity02FlattenTest.generateMsgBodyPropsForJt808;
import static org.junit.jupiter.api.Assertions.*;

class RustStyleDebugEntity02NestedTest extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final RustStyleDebugEntity02Nested originalEntity = this.createEntity();
        final String hexString = this.encodeAsHexString(originalEntity);
        final RustStyleDebugEntity02Nested entity = this.doDecode(RustStyleDebugEntity02Nested.class, hexString);
        assertEquals(originalEntity.getHeader().getMsgId(), entity.getHeader().getMsgId());
        assertEquals(originalEntity.getHeader().getMsgBodyProps(), entity.getHeader().getMsgBodyProps());
        assertEquals(originalEntity.getHeader().getProtocolVersion(), entity.getHeader().getProtocolVersion());
        assertEquals(originalEntity.getHeader().getTerminalId(), entity.getHeader().getTerminalId());
        assertEquals(originalEntity.getHeader().getMsgSerialNo(), entity.getHeader().getMsgSerialNo());
        assertEquals(originalEntity.getHeader().getSubPackageInfo(), entity.getHeader().getSubPackageInfo());
        assertEquals(originalEntity.getHeader().msgBodyLength(), entity.getHeader().msgBodyLength());

        assertEquals(originalEntity.getBody().getAlarmFlag(), entity.getBody().getAlarmFlag());
        assertEquals(originalEntity.getBody().getStatus(), entity.getBody().getStatus());
        assertEquals(originalEntity.getBody().getLatitude(), entity.getBody().getLatitude());
        assertEquals(originalEntity.getBody().getLongitude(), entity.getBody().getLongitude());
        assertEquals(originalEntity.getBody().getAltitude(), entity.getBody().getAltitude());
        assertEquals(originalEntity.getBody().getSpeed(), entity.getBody().getSpeed());
        assertEquals(originalEntity.getBody().getDirection(), entity.getBody().getDirection());
        assertEquals(originalEntity.getBody().getTime(), entity.getBody().getTime());

        assertEquals(originalEntity.getBody().getExtraItems().size(), entity.getBody().getExtraItems().size());
        for (int i = 0; i < originalEntity.getBody().getExtraItems().size(); i++) {
            final RustStyleDebugEntity02Nested.ExtraItem originalItem = originalEntity.getBody().getExtraItems().get(i);
            final RustStyleDebugEntity02Nested.ExtraItem item = entity.getBody().getExtraItems().get(i);
            assertEquals(originalItem.getId(), item.getId());
            assertEquals(originalItem.getContentLength(), item.getContentLength());
            assertArrayEquals(originalItem.getContent(), item.getContent());
        }

        assertEquals(originalEntity.getCheckSum(), entity.getCheckSum());
    }

    RustStyleDebugEntity02Nested createEntity() {
        final RustStyleDebugEntity02Nested entity = new RustStyleDebugEntity02Nested();
        final RustStyleDebugEntity02Nested.Header header = new RustStyleDebugEntity02Nested.Header();
        final RustStyleDebugEntity02Nested.Body body = new RustStyleDebugEntity02Nested.Body();
        entity.setHeader(header);
        entity.setBody(body);
        int bodyLength = this.initBody(body);
        body.setExtraItems(List.of(
                new RustStyleDebugEntity02Nested.ExtraItem((short) 0x01, (short) 4, ByteArrayContainer.ofDword(6666).asBytes()),
                new RustStyleDebugEntity02Nested.ExtraItem((short) 0x02, (short) 2, ByteArrayContainer.ofWord(88).asBytes()),
                new RustStyleDebugEntity02Nested.ExtraItem((short) 0x03, (short) 2, ByteArrayContainer.ofWord(89).asBytes())
        ));
        this.initHeader(header);

        bodyLength += body.getExtraItems().stream().mapToInt(it -> 1 + 1 + it.getContent().length).sum();
        header.setMsgBodyProps(generateMsgBodyPropsForJt808(bodyLength, 0, false, 0));
        entity.setCheckSum((byte) 111);

        return entity;
    }

    int initBody(RustStyleDebugEntity02Nested.Body body) {
        int msgBodyLength = 0;

        // 报警标志  DWORD(4)
        body.setAlarmFlag(88);
        msgBodyLength += 4;

        // 状态  DWORD(4)
        body.setStatus(111);
        msgBodyLength += 4;

        // 纬度  DWORD(4)
        body.setLatitude(31234567);
        msgBodyLength += 4;

        // 经度  DWORD(4)
        body.setLongitude(121987654);
        msgBodyLength += 4;

        // 高程  WORD(2)
        body.setSpeed(666);
        msgBodyLength += 2;

        // 速度  WORD(2)
        body.setAltitude(8989);
        msgBodyLength += 2;

        // 方向  WORD(2)
        body.setDirection(90);
        msgBodyLength += 2;

        // 时间  BCD[6] yyMMddHHmmss
        body.setTime("240322222633");
        msgBodyLength += 6;

        return msgBodyLength;
    }

    void initHeader(RustStyleDebugEntity02Nested.Header header) {
        header.setMsgId(0x0200);
        header.setProtocolVersion((byte) 1);
        header.setTerminalId("00000000013911112222");
        header.setMsgSerialNo(99);
        header.setSubPackageInfo(null);
    }

}

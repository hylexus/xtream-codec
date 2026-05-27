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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage9212Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage9212 entity = new BuiltinMessage9212()
                .setFileName("00_64_6401_0_lrveywwdibs9pe6onrbz6hvb2sfvp91b.jpg")
                .setFileType((short) 0x00)
                .setUploadResult((short) 0x01)
                .setPackageCountToReTransmit((short) 2)
                .setRetransmitItemList(List.of(
                        new BuiltinMessage9212.RetransmitItem()
                                .setDataOffset(1)
                                .setDataLength(20),
                        new BuiltinMessage9212.RetransmitItem()
                                .setDataOffset(2)
                                .setDataLength(30)
                ));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x9212);
        assertEquals("7e92124045010000000001391234432900003130305f36345f363430315f305f6c72766579777764696273397065366f6e72627a3668766232736676703931622e6a70670001020000000100000014000000020000001eb17e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage9212 entity = decodeAsEntity(BuiltinMessage9212.class, "92124045010000000001391234432900003130305f36345f363430315f305f6c72766579777764696273397065366f6e72627a3668766232736676703931622e6a70670001020000000100000014000000020000001eb1");
        assertEquals("00_64_6401_0_lrveywwdibs9pe6onrbz6hvb2sfvp91b.jpg", entity.getFileName());
        assertEquals(0x00, entity.getFileType());
        assertEquals(0x01, entity.getUploadResult());
        assertEquals(2, entity.getPackageCountToReTransmit());
        assertEquals(2, entity.getRetransmitItemList().size());
        assertEquals(1, entity.getRetransmitItemList().get(0).getDataOffset());
        assertEquals(20, entity.getRetransmitItemList().get(0).getDataLength());
        assertEquals(2, entity.getRetransmitItemList().get(1).getDataOffset());
        assertEquals(30, entity.getRetransmitItemList().get(1).getDataLength());
    }
}

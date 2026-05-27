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
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hylexus
 */
class BuiltinMessage1210Test extends BaseCodecTest {

    @Test
    void testDecode() {
        final BuiltinMessage1210 entity = decodeAsEntity(BuiltinMessage1210.class, "1210406f0100000000013912344329000031323334353637313233343536372409252055320101006c72766579777764696273397065366f6e72627a36687662327366767039316200013130305f36345f363430315f305f6c72766579777764696273397065366f6e72627a3668766232736676703931622e6a706700003fec96");
        assertEquals("1234567", entity.getTerminalId());

        final AlarmIdentifier identifier = entity.getAlarmIdentifier();
        assertEquals("1234567", identifier.getTerminalId());
        assertEquals(LocalDateTime.parse("240925205532", JtProtocolConstant.DEFAULT_DATE_TIME_FORMATTER), identifier.getTime());
        assertEquals((short) 1, identifier.getSequence());
        assertEquals((short) 1, identifier.getAttachmentCount());
        assertEquals((short) 0, identifier.getReserved());

        assertEquals("lrveywwdibs9pe6onrbz6hvb2sfvp91b", entity.getAlarmNo());
        assertEquals(0, entity.getMessageType());
        assertEquals(1, entity.getAttachmentCount());
        assertEquals(1, entity.getAttachmentItemList().size());

        final BuiltinMessage1210.AttachmentItem attachmentItem = entity.getAttachmentItemList().getFirst();
        assertEquals("00_64_6401_0_lrveywwdibs9pe6onrbz6hvb2sfvp91b.jpg", attachmentItem.getFileName());
        assertEquals(16364, attachmentItem.getFileSize());
    }

    @Test
    void testEncode() {
        final BuiltinMessage1210 entity = new BuiltinMessage1210()
                .setTerminalId("1234567")
                .setAlarmIdentifier(new AlarmIdentifier()
                        .setTerminalId("1234567")
                        .setTime(LocalDateTime.parse("240925205532", JtProtocolConstant.DEFAULT_DATE_TIME_FORMATTER))
                        .setSequence((short) 1)
                        .setAttachmentCount((short) 1)
                        .setReserved((short) 0)
                )
                .setAlarmNo("lrveywwdibs9pe6onrbz6hvb2sfvp91b")
                .setMessageType((short) 0)
                .setAttachmentCount((short) 1)
                .setAttachmentItemList(
                        List.of(
                                new BuiltinMessage1210.AttachmentItem()
                                        .setFileName("00_64_6401_0_lrveywwdibs9pe6onrbz6hvb2sfvp91b.jpg")
                                        .setFileSize(16364)
                        )
                );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x1210);
        assertEquals("7e1210406f0100000000013912344329000031323334353637313233343536372409252055320101006c72766579777764696273397065366f6e72627a36687662327366767039316200013130305f36345f363430315f305f6c72766579777764696273397065366f6e72627a3668766232736676703931622e6a706700003fec967e", hex);
    }
}

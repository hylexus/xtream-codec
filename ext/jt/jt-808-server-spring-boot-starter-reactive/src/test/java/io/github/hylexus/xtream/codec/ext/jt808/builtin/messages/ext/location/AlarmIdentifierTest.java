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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 报警标识固定长度及无符号字段回归测试。
 *
 * @author Codex (AI)
 */
class AlarmIdentifierTest extends BaseCodecTest {
    @Test
    void testEncode() {
        final AlarmIdentifier entity = new AlarmIdentifier().setTerminalId("ABC1234")
                .setTime(LocalDateTime.of(2025, 2, 21, 21, 33, 32))
                .setSequence((short) 255).setAttachmentCount((short) 2).setReserved((short) 0);
        assertEquals("41424331323334250221213332ff0200", encodeBody(entity));
    }

    @Test
    void testDecode() {
        final AlarmIdentifier entity = decodeBody(AlarmIdentifier.class, "41424331323334250221213332ff0200");
        assertEquals("ABC1234", entity.getTerminalId());
        assertEquals(LocalDateTime.of(2025, 2, 21, 21, 33, 32), entity.getTime());
        assertEquals(255, entity.getSequence());
        assertEquals(2, entity.getAttachmentCount());
        assertEquals(0, entity.getReserved());
    }
}


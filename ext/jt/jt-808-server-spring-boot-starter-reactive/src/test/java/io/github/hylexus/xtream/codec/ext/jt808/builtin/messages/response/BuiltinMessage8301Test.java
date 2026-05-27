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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8301Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8301 entity = new BuiltinMessage8301();
        entity.setEventType((short) 1);
        entity.setEventCount((short) 2);
        entity.setEventItemList(List.of(
                new BuiltinMessage8301.EventItem((short) 1, "haha"),
                new BuiltinMessage8301.EventItem((short) 2, "heihei")
        ));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e83010010013912344323000001020104686168610206686569686569ee7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "83010010013912344323000001020104686168610206686569686569ee";
        final BuiltinMessage8301 entity = decodeAsEntity(BuiltinMessage8301.class, hex);
        assertEquals(1, entity.getEventType());
        assertEquals(2, entity.getEventCount());
        assertEquals("haha", entity.getEventItemList().getFirst().getEventContent());
        assertEquals("heihei", entity.getEventItemList().getLast().getEventContent());
    }

}

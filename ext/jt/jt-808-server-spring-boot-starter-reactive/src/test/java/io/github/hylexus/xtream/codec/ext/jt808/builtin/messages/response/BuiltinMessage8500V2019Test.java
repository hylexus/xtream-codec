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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8500V2019Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8500V2019 entity = new BuiltinMessage8500V2019();
        entity.setParams(Map.of(
                0x0001, 1
        ));
        entity.setParamCount(entity.getParams().size());

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e85004005010000000001391234432900000001000101b47e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "85004005010000000001391234432900000001000101b4";
        final BuiltinMessage8500V2019 entity = decodeAsEntity(BuiltinMessage8500V2019.class, hex);
        assertEquals(1, entity.getParams().size());
        assertEquals((short) 1, entity.getParams().get(0x0001));
    }
}

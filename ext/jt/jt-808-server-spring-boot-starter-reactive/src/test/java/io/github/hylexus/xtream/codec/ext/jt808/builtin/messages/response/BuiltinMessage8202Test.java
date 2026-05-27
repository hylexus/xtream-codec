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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8202Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8202 entity = new BuiltinMessage8202();
        entity.setTimeDurationInSeconds(30);
        entity.setTraceValidityDurationInSeconds(1000);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8202400601000000000139123443290000001e000003e8467e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8202400601000000000139123443290000001e000003e846";
        final BuiltinMessage8202 entity = decodeAsEntity(BuiltinMessage8202.class, hex);
        assertEquals(30, entity.getTimeDurationInSeconds());
        assertEquals(1000, entity.getTraceValidityDurationInSeconds());
    }

    @Test
    void testEncode1() {
        final BuiltinMessage8202 entity = new BuiltinMessage8202();
        entity.setTimeDurationInSeconds(0);
        entity.setTraceValidityDurationInSeconds(1000);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e82024002010000000001391234432900000000b77e", hex);
    }

    @Test
    void testDecode1() {
        final String hex = "82024002010000000001391234432900000000b7";
        final BuiltinMessage8202 entity = decodeAsEntity(BuiltinMessage8202.class, hex);
        assertEquals(0, entity.getTimeDurationInSeconds());
        assertEquals(0, entity.getTraceValidityDurationInSeconds());
    }

}

/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0001Test extends BaseCodecTest {

    @Test
    void testV2011() {
        codecTestV2011(0x0001, createSource(), (expected, actual, hexString) -> {
            assertEquals(Jt808ProtocolVersion.VERSION_2013, actual.header().version());
            doCompare(expected, actual);
        });
    }

    @Test
    void testV2013() {
        codecTestV2013(0x0001, createSource(), (expected, actual, hexString) -> {
            assertEquals(Jt808ProtocolVersion.VERSION_2013, actual.header().version());
            doCompare(expected, actual);
        });
    }

    @Test
    void testV2019() {
        codecTestV2019(0x0001, createSource(), (expected, actual, hexString) -> {
            assertEquals(Jt808ProtocolVersion.VERSION_2019, actual.header().version());
            doCompare(expected, actual);
        });
    }

    private void doCompare(BuiltinMessage0001 expected, Jt808MessageForTest<BuiltinMessage0001> actual) {
        final BuiltinMessage0001 body = actual.body();
        assertEquals(expected.getServerFlowId(), body.getServerFlowId());
        assertEquals(expected.getServerMessageId(), body.getServerMessageId());
        assertEquals(expected.getResult(), body.getResult());
    }

    private static BuiltinMessage0001 createSource() {
        return new BuiltinMessage0001()
                .setServerFlowId(123)
                .setServerMessageId(0x0200)
                .setResult((short) 1);
    }

}

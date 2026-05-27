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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.type.wrapper.WordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0005Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage0005 entity = new BuiltinMessage0005()
                .setOriginalMessageFlowId(111)
                .setPackageCount(3)
                .setPackageIdList(List.of(
                                new WordWrapper(2),
                                new WordWrapper(3),
                                new WordWrapper(4)
                        )
                );

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0005);
        assertEquals("7e0005400a01000000000139123443290000006f0003000200030004537e", hex);
    }

    @Test
    void testDecode() {
        final BuiltinMessage0005 entity = decodeAsEntity(BuiltinMessage0005.class, "0005400a01000000000139123443290000006f000300020003000453");

        assertEquals(111, entity.getOriginalMessageFlowId());
        assertEquals(3, entity.getPackageCount());
        assertEquals(3, entity.getPackageIdList().size());
        assertEquals(2, entity.getPackageIdList().get(0).asU16());
        assertEquals(3, entity.getPackageIdList().get(1).asU16());
        assertEquals(4, entity.getPackageIdList().get(2).asU16());
    }

}

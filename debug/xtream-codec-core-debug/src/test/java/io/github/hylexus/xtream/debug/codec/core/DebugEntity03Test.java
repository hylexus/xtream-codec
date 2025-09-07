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

package io.github.hylexus.xtream.debug.codec.core;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.debug.codec.core.demo03.RustStyleDebugEntity03ForDecode;
import io.github.hylexus.xtream.debug.codec.core.demo03.RustStyleDebugEntity03ForEncode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DebugEntity03Test extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        this.entityCodec = EntityCodec.DEFAULT;
    }

    @Test
    void test() {
        final RustStyleDebugEntity03ForEncode entity = new RustStyleDebugEntity03ForEncode();
        entity.setMsgId(123);

        final Map<Integer, Object> attr = new LinkedHashMap<>();
        attr.put(1, "abcd");
        attr.put(2, "哈哈 123");
        attr.put(3, 111);
        attr.put(4, "...。。。");
        entity.setAttr(attr);

        // System.out.println(entity);
        final String hexString = encodeAsHexString(entity);
        // System.out.println(hexString);

        final RustStyleDebugEntity03ForDecode instance = doDecode(RustStyleDebugEntity03ForDecode.class, hexString);

        assertEquals(entity.getMsgId(), instance.getMsgId());
        assertEquals(attr.get(1), instance.getAttr().get(1));
        assertEquals(attr.get(2), instance.getAttr().get(2));
        assertEquals(attr.get(3), instance.getAttr().get(3));
        assertEquals(attr.get(4), instance.getAttr().get(4));
        // System.out.println(instance);
    }
}

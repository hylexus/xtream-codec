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

package io.github.hylexus.xtream.codec.core.impl.codec;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapFieldCodecTest extends BaseFieldCodecTest {

    @Test
    void testV0() {
        final MapFieldEntity sourceEntity = createSourceEntityV0();

        this.codec(0, sourceEntity, (instance, assertion) -> {
            System.out.println(instance);
            System.out.println(assertion);
            final Map<Object, Object> instanceMap = instance.getMap();
            final Map<Object, Object> assertionMap = assertion.getMap();
            assertFalse(instanceMap.isEmpty());
            assertEquals(instanceMap.size(), assertionMap.size());
            assertEquals(instanceMap.get(11), assertionMap.get(11));
            assertEquals(instanceMap.get(12), assertionMap.get(12));
            assertEquals(instanceMap.get(13), assertionMap.get(13));
            assertEquals(instanceMap.get(14), assertionMap.get(14));
            assertNotEquals(instanceMap.get(5), assertionMap.get(5));
            assertEquals("010203", assertionMap.get(5));
            assertEquals(instanceMap.get(61), assertionMap.get(61));
            assertEquals(instanceMap.get(62), assertionMap.get(62));
            assertEquals(instanceMap.get(63), assertionMap.get(63));
        });

    }

    @Test
    void testV1() {
        final MapFieldEntity sourceEntity = createSourceEntityV1();

        this.codec(1, sourceEntity, (instance, assertion) -> {
            System.out.println(instance);
            System.out.println(assertion);
            final Map<Object, Object> instanceMap = instance.getMap();
            final Map<Object, Object> assertionMap = assertion.getMap();
            assertFalse(instanceMap.isEmpty());
            assertEquals(instanceMap.size(), assertionMap.size());
            assertEquals(instanceMap.get(11L), assertionMap.get(11L));
            assertEquals(instanceMap.get(12L), assertionMap.get(12L));
            assertEquals(instanceMap.get(13L), assertionMap.get(13L));
            assertEquals(instanceMap.get(14L), assertionMap.get(14L));
            assertNotEquals(instanceMap.get(5L), assertionMap.get(5L));
            assertEquals("010203", assertionMap.get(5L));
            assertEquals(instanceMap.get(61L), assertionMap.get(61L));
            assertEquals(instanceMap.get(62L), assertionMap.get(62L));
            assertEquals(instanceMap.get(63L), assertionMap.get(63L));
        });
    }

    @Test
    void testV2() {
        final MapFieldEntity sourceEntity = createSourceEntityV2();

        this.codec(2, sourceEntity, (instance, assertion) -> {
            System.out.println(instance);
            System.out.println(assertion);
            final Map<Object, Object> instanceMap = instance.getMap();
            final Map<Object, Object> assertionMap = assertion.getMap();
            assertFalse(instanceMap.isEmpty());
            assertEquals(instanceMap.size(), assertionMap.size());
            assertEquals(instanceMap.get("配置项11"), assertionMap.get("配置项11"));
            assertEquals(instanceMap.get("配置项12"), assertionMap.get("配置项12"));
            assertEquals(instanceMap.get("配置项13"), assertionMap.get("配置项13"));
            assertEquals(instanceMap.get("配置项14"), assertionMap.get("配置项14"));
            assertNotEquals(instanceMap.get("配置项05"), assertionMap.get("配置项05"));
            assertEquals("010203", assertionMap.get("配置项05"));
            assertEquals(instanceMap.get("配置项61"), assertionMap.get("配置项61"));
            assertEquals(instanceMap.get("配置项62"), assertionMap.get("配置项62"));
            assertEquals(instanceMap.get("配置项63"), assertionMap.get("配置项63"));
        });
    }

    private static MapFieldEntity createSourceEntityV0() {
        final LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        map.put(5, new byte[]{1, 2, 3});
        map.put(11, (byte) 127);
        map.put(12, (1 << 16) - 1);
        map.put(13, (1L << 32) - 1);
        map.put(14, 1L);
        map.put(61, "张三");
        map.put(62, "李四");
        map.put(63, "王五");
        map.put(64, "赵六");
        map.put(71, new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));
        map.put(81, new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));

        return new MapFieldEntity()
                .setMessageId(255)
                .setMap(map);
    }

    private static MapFieldEntity createSourceEntityV2() {
        final LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        map.put("配置项05", new byte[]{1, 2, 3});
        map.put("配置项11", (byte) 127);
        map.put("配置项12", (1 << 16) - 1);
        map.put("配置项13", (1L << 32) - 1);
        map.put("配置项14", 1L);
        map.put("配置项61", "张三");
        map.put("配置项62", "李四");
        map.put("配置项63", "王五");
        map.put("配置项64", "赵六");
        map.put("配置项71", new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));
        map.put("配置项81", new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));

        return new MapFieldEntity()
                .setMessageId(255)
                .setMap(map);
    }

    private static MapFieldEntity createSourceEntityV1() {
        final LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        map.put(11L, (byte) 127);
        // 65535(U16)
        map.put(12L, (int) Math.pow(2, 16) - 1);
        map.put(13L, (byte) -128);
        map.put(14L, 1L);
        map.put(5L, new byte[]{1, 2, 3});
        map.put(61L, "张三");
        map.put(62L, "李四");
        map.put(63L, "王五");
        map.put(64L, "赵六");
        map.put(71L, new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));
        map.put(81L, new MapFieldEntity.NestedEntity01().setId(123).setName("哈哈哈").setDate(new Date()));

        return new MapFieldEntity()
                .setMessageId(255)
                .setMap(map);
    }
}

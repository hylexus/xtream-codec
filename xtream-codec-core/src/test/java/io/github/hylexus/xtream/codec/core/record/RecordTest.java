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

package io.github.hylexus.xtream.codec.core.record;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecordTest extends BaseEntityCodecTest {

    public record Entity1(
            @Preset.RustStyle.str(version = XtreamField.ALL_VERSION, prependLengthFieldType = PrependLengthFieldType.u8, charset = "UTF-8")
            @Preset.RustStyle.str(version = 1, prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
            String name,
            @Preset.RustStyle.u8 Short level,
            // @Preset.RustStyle.u32(version = 1, condition = "level == 2")
            @Preset.RustStyle.u32(version = 1, conditions = @Expression(spel = "level == 2", mvel = "self.level == 2", aviator = "self.level == 2"))
            // @Preset.RustStyle.u32(version = 2, condition = "#level == 2")
            @Preset.RustStyle.u32(version = 2, conditions = @Expression(spel = "#level == 2", mvel = "self.level == 2", aviator = "self.level == 2"))
            // @Preset.RustStyle.u32(version = 3, condition = "#self.level == 2")
            @Preset.RustStyle.u32(version = 3, conditions = @Expression(spel = "#self.level == 2", mvel = "self.level == 2", aviator = "self.level == 2"))
            Long age) {

        // for Aviator
        @SuppressWarnings("unused")
        public Short getLevel() {
            return level;
        }
    }

    @Test
    void testEntity1() {
        doCodecTest(
                XtreamField.ALL_VERSION,
                new Entity1("张三李四王五", (short) 1, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.age());
                },
                false
        );

        doCodecTest(
                1,
                new Entity1("张三李四王五", (short) 1, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.age());
                },
                false
        );

        doCodecTest(
                1,
                new Entity1("张三李四王五", (short) 2, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertEquals(sourceEntity.age(), decodedEntity.age());
                },
                false
        );

        doCodecTest(
                2,
                new Entity1("张三李四王五", (short) 2, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertEquals(sourceEntity.age(), decodedEntity.age());
                },
                false
        );

        doCodecTest(
                3,
                new Entity1("张三李四王五", (short) 2, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertEquals(sourceEntity.age(), decodedEntity.age());
                },
                false
        );

        doCodecTest(
                1111,
                new Entity1("张三李四王五", (short) 2, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.age());
                },
                false
        );

        doCodecTest(
                3,
                new Entity1("张三李四王五", (short) 1, 1000L),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.name())));
                    doCompareEntity1(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.age());
                },
                false
        );
    }

    public record Entity2(
            @Preset.JtStyle.Str(desc = "姓名", prependLengthFieldType = PrependLengthFieldType.u8) String name,
            Integer x,
            @Preset.JtStyle.Word(desc = "年龄") int age,
            @Preset.JtStyle.TransientRc(version = 1, nulls = XtreamField.Nulls.AS_NULL)
            @Preset.JtStyle.TransientRc(version = 2, nulls = XtreamField.Nulls.AS_EMPTY)
            String address,
            @Preset.RustStyle.transient_rc(version = 1, nulls = XtreamField.Nulls.AS_EMPTY)
            @Preset.RustStyle.transient_rc(version = 2, nulls = XtreamField.Nulls.AS_NULL)
            List<String> add,
            @Preset.JtStyle.Object NestedRecord1 nestedRecord1,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String desc) {
        public Entity2(String name, Integer x, int age, String address) {
            this(name, x, age, address, List.of("111"), new NestedRecord1("张三", 100, new NestedRecord2("张三1", 1100, "desc2"), "desc1"), "...");
        }
    }

    public record NestedRecord1(
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String name1,
            @Preset.JtStyle.Word int age1,
            @Preset.JtStyle.Object NestedRecord2 nestedRecord2,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String desc1) {
    }

    public record NestedRecord2(
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String name2,
            @Preset.JtStyle.Word int age2,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String des2) {
    }

    @Test
    void testEntity2() {
        doCodecTest(
                XtreamField.ALL_VERSION,
                new Entity2("张三李四王五", 0, 18, "t"),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    doCompareEntity2(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.x());
                },
                false
        );

        doCodecTest(
                1,
                new Entity2("张三李四王五", 0, 18, "t"),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    doCompareEntity2(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.x());
                    assertNull(decodedEntity.address());
                    assertNotNull(decodedEntity.add());
                    assertTrue(decodedEntity.add().isEmpty());
                },
                false
        );
        doCodecTest(
                2,
                new Entity2("张三李四王五", 0, 18, "t"),
                (sourceEntity, encodedHexString, decodedEntity) -> {
                    doCompareEntity2(sourceEntity, decodedEntity);
                    assertNull(decodedEntity.x());
                    assertNotNull(decodedEntity.address());
                    assertTrue(decodedEntity.address().isEmpty());
                    assertNull(decodedEntity.add());
                },
                false
        );
    }

    private void doCompareEntity1(Entity1 sourceEntity, Entity1 decodedEntity) {
        assertEquals(sourceEntity.name(), decodedEntity.name());
        assertEquals(sourceEntity.level(), decodedEntity.level());
    }

    private void doCompareEntity2(Entity2 sourceEntity, Entity2 decodedEntity) {
        assertEquals(sourceEntity.name(), decodedEntity.name());
        assertEquals(sourceEntity.age(), decodedEntity.age());
        assertEquals(sourceEntity.nestedRecord1(), decodedEntity.nestedRecord1());
        assertEquals(sourceEntity.desc(), decodedEntity.desc());
    }

}

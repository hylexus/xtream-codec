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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class I8FieldCodecTest extends BaseFieldCodecTest {

    @Test
    void test() {
        codec(
                XtreamField.ALL_VERSION,
                new I8FiledTestEntity()
                        .setB1((byte) -127)
                        .setB2((byte) -128)
                        .setS1((short) -127)
                        .setS2((short) -128)
                        .setI1(-127)
                        .setI2(-128)
                        .setL1(-127)
                        .setL2(-128L),
                (s, t) -> {
                    assertNotSame(s, t);
                    assertEquals(s.b1, t.b1);
                    assertEquals(s.b2, t.b2);
                    assertEquals(s.s1, t.s1);
                    assertEquals(s.s2, t.s2);
                    assertEquals(s.i1, t.i1);
                    assertEquals(s.i2, t.i2);
                    assertEquals(s.l1, t.l1);
                    assertEquals(s.l2, t.l2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "-1", "-2", "100", "-100", "127", "-128"})
    void testBoundaryValues(int boundary) {
        codec(
                XtreamField.ALL_VERSION,
                new I8FiledTestEntity()
                        .setB1((byte) boundary)
                        .setB2((byte) boundary)
                        .setS1((short) boundary)
                        .setS2((short) boundary)
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    assertNotSame(s, t);
                    assertEquals(s.b1, t.b1);
                    assertEquals(s.b2, t.b2);
                    assertEquals(s.s1, t.s1);
                    assertEquals(s.s2, t.s2);
                    assertEquals(s.i1, t.i1);
                    assertEquals(s.i2, t.i2);
                    assertEquals(s.l1, t.l1);
                    assertEquals(s.l2, t.l2);
                }
        );
    }

    @Test
    void testOutOfRangePositive() {
        final I8FiledTestEntity entity = new I8FiledTestEntity()
                .setOutOfRangeValue(128) // i8: [-128, 127] 128 溢出为 -128
                .setB2((byte) 0)
                .setS2((short) 0)
                .setI2(0)
                .setL2(0L);

        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> assertEquals(-128, t.outOfRangeValue)
        );
    }

    @Test
    void testOutOfRangeNegative() {
        final I8FiledTestEntity entity = new I8FiledTestEntity()
                .setOutOfRangeValue(-129)
                .setB2((byte) 0)
                .setS2((short) 0)
                .setI2(0)
                .setL2(0L);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> assertEquals(127, t.outOfRangeValue)
        );
    }

    @Data
    @Accessors(chain = true)
    public static class I8FiledTestEntity {
        @Preset.RustStyle.i8
        private byte b1;

        @Preset.RustStyle.i8
        private Byte b2;

        @Preset.RustStyle.i8
        private short s1;

        @Preset.RustStyle.i8
        private Short s2;

        @Preset.RustStyle.i8
        private int i1;

        @Preset.RustStyle.i8
        private Integer i2;

        @Preset.RustStyle.i8
        private long l1;

        @Preset.RustStyle.i8
        private Long l2;

        @Preset.RustStyle.i8
        private int outOfRangeValue;
    }
}

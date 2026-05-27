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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class I16FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({"-32768", "-100", "-1", "0", "1", "100", "32767"})
    void testBigEndianBoundaryValues(int boundary) {
        codec(
                XtreamField.ALL_VERSION,
                new I16BeFiledTestEntity()
                        .setS1((short) boundary)
                        .setS2((short) boundary)
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.s1, t.s1);
                    Assertions.assertEquals(s.s2, t.s2);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({"-32768", "-100", "-1", "0", "1", "100", "32767"})
    void testLittleEndianBoundaryValues(int boundary) {
        codec(XtreamField.ALL_VERSION, new I16LeFiledTestEntity()
                        .setS1((short) boundary)
                        .setS2((short) boundary)
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.s1, t.s1);
                    Assertions.assertEquals(s.s2, t.s2);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Test
    void testBigEndianOutOfRangePositive() {
        final I16BeFiledTestEntity entity = new I16BeFiledTestEntity()
                .setS1((short) 0)
                .setS2((short) 0)
                .setI1(0)
                .setI2(0)
                .setL1(0)
                .setL2(0L)
                .setOutOfRangeValue(32768);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(-32768, t.outOfRangeValue)
        );
    }

    @Test
    void testBigEndianOutOfRangeNegative() {
        final I16BeFiledTestEntity entity = new I16BeFiledTestEntity()
                .setS1((short) 0)
                .setS2((short) 0)
                .setI1(0)
                .setI2(0)
                .setL1(0)
                .setL2(0L)
                .setOutOfRangeValue(-32769);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(32767, t.outOfRangeValue)
        );
    }

    @Data
    @Accessors(chain = true)
    public static class I16BeFiledTestEntity {
        @Preset.RustStyle.i16
        private short s1;
        @Preset.RustStyle.i16
        private Short s2;
        @Preset.RustStyle.i16
        private int i1;
        @Preset.RustStyle.i16
        private Integer i2;
        @Preset.RustStyle.i16
        private long l1;
        @Preset.RustStyle.i16
        private Long l2;
        @Preset.RustStyle.i16
        private int outOfRangeValue;
    }

    @Data
    @Accessors(chain = true)
    public static class I16LeFiledTestEntity {
        @Preset.RustStyle.i16_le
        private short s1;
        @Preset.RustStyle.i16_le
        private Short s2;
        @Preset.RustStyle.i16_le
        private int i1;
        @Preset.RustStyle.i16_le
        private Integer i2;
        @Preset.RustStyle.i16_le
        private long l1;
        @Preset.RustStyle.i16_le
        private Long l2;
        @Preset.RustStyle.i16_le
        private int outOfRangeValue;
    }
}

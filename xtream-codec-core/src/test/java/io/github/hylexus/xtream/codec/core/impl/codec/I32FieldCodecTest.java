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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class I32FieldCodecTest extends BaseFieldCodecTest {

    @ParameterizedTest
    @CsvSource({
            "-2147483648",
            "-100",
            "-1",
            "0",
            "1",
            "100",
            "2147483647"
    })
    void testBigEndianBoundaryValues(int boundary) {
        codec(XtreamField.ALL_VERSION, new I32BeFiledTestEntity()
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-2147483648",
            "-100",
            "-1",
            "0",
            "1",
            "100",
            "2147483647"
    })
    void testLittleEndianBoundaryValues(int boundary) {
        codec(XtreamField.ALL_VERSION, new I32LeFiledTestEntity()
                        .setI1(boundary)
                        .setI2(boundary)
                        .setL1(boundary)
                        .setL2((long) boundary),
                (s, t) -> {
                    Assertions.assertNotSame(s, t);
                    Assertions.assertEquals(s.i1, t.i1);
                    Assertions.assertEquals(s.i2, t.i2);
                    Assertions.assertEquals(s.l1, t.l1);
                    Assertions.assertEquals(s.l2, t.l2);
                }
        );
    }

    @Test
    void testBigEndianOutOfRangePositive() {
        final I32BeFiledTestEntity entity = new I32BeFiledTestEntity()
                .setI1(0)
                .setI2(0)
                .setL1(0L)
                .setL2(0L)
                .setOutOfRangeValue(2147483648L);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(-2147483648L, t.outOfRangeValue)
        );
    }

    @Test
    void testBigEndianOutOfRangeNegative() {
        final I32BeFiledTestEntity entity = new I32BeFiledTestEntity()
                .setI1(0)
                .setI2(0)
                .setL1(0L)
                .setL2(0L)
                .setOutOfRangeValue(-2147483649L);
        codec(XtreamField.ALL_VERSION, entity,
                (s, t) -> Assertions.assertEquals(2147483647L, t.outOfRangeValue)
        );
    }

    @Data
    @Accessors(chain = true)
    public static class I32BeFiledTestEntity {
        @Preset.RustStyle.i32
        private int i1;
        @Preset.RustStyle.i32
        private Integer i2;
        @Preset.RustStyle.i32
        private long l1;
        @Preset.RustStyle.i32
        private Long l2;
        @Preset.RustStyle.i32
        private long outOfRangeValue;
    }

    @Data
    @Accessors(chain = true)
    public static class I32LeFiledTestEntity {
        @Preset.RustStyle.i32_le
        private int i1;
        @Preset.RustStyle.i32_le
        private Integer i2;
        @Preset.RustStyle.i32_le
        private long l1;
        @Preset.RustStyle.i32_le
        private Long l2;
        @Preset.RustStyle.i32_le
        private long outOfRangeValue;
    }
}
